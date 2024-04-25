package com.mttk.lowcode.backend.web;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.core.env.Environment;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mttk.lowcode.backend.web.util.AbstractPersistentWithAuthController;
import com.mttk.lowcode.backend.web.util.MongoUtil;
import com.mttk.lowcode.backend.web.util.StringUtil;
import com.mttk.lowcode.backend.web.util.auth.AccountUtil;
import com.mttk.lowcode.backend.web.util.auth.EnviromentUtil;
import com.mttk.lowcode.backend.web.util.auth.PasswordEncoder;
import com.mttk.lowcode.backend.web.util.auth.SecurityContext;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/account")
public class AccountController extends AbstractPersistentWithAuthController {
	@Autowired
	protected MongoTemplate template;
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	private CacheManager cacheManager;
	@Autowired
	private Environment environment;
	@Override
	protected String getColName() {
		return "account";
	}

	@PostMapping(value = "/login")
	public ResponseEntity<Document> login(@RequestBody Document body, HttpServletRequest request) {
		String username = body.getString("username");
		String password = body.getString("password");
		//
		return loginInternal(username, password);

	}

	@PostMapping(value = "/logout")
	public ResponseEntity<Document> logout(HttpServletRequest request) {
		String token = request.getHeader("X-Token");
		if (!StringUtil.isEmpty(token)) {
			AccountUtil.getCache(cacheManager).evict(token);
		}
		return ResponseEntity.ok(new Document("result", true));
	}

	@GetMapping(value = "/info")
	public ResponseEntity<Document> info(HttpServletRequest request) {
		//
		String token = request.getHeader("X-Token");
		return infoInternal(token);
	}

	// List all the registered authorities
	@GetMapping(value = "/authorities")
	public ResponseEntity<Document> authorities() {
		return ResponseEntity.ok(new Document("list", template.findAll(Document.class, "authority")));
	}

	@Override
	public ResponseEntity<Document> save(@RequestBody Document body) throws Exception {
		if(StringUtil.isEmpty(MongoUtil.getId(body))) {
			//Add
			//Encrypt password
			body.append("password", encryptPassword(body.getString("password")));			
		}else {
			//Edit
			Document accountExist=template.findById(MongoUtil.getId(body), Document.class, getColName());
			if(accountExist!=null){
				body.append("password", accountExist.getString("password"));
			}
		}
		//
		return super.save(body);
	}
	@PostMapping(value = "/changePassword")
	//Change login user password
	//body {password:'',passwordOld:''}
	//if account is not existed,return error {error:true,code:201} 
	//if old password is not matched,return error {error:true,code:202} 
	public ResponseEntity<Document> changePassword(@RequestBody Document body) throws Exception{
	Document loginInfo=SecurityContext.getCurrentContext().getAuthentication();
	if(loginInfo==null) {
		 return ResponseEntity.ok(new Document("error",true).append("code", 201)); 
	}
	
	if(EnviromentUtil.getSuppressAdminPassword(environment) && "admin".equals(loginInfo.getString("username"))) {
		return ResponseEntity.ok(new Document("error",true).append("code", 250)); 
	}
	Document account=template.findById(MongoUtil.getId(loginInfo), Document.class, getColName());
	if(account==null) {
		return ResponseEntity.ok(new Document("error",true).append("code", 201)); 
	}
	//
	if(!passwordEncoder.matches(body.getString("passwordOld"), account.getString("password"))) {
		//Old password is incorrect
		return ResponseEntity.ok(new Document("error",true).append("code", 202));
	}
	//
	account.append("password", encryptPassword(body.getString("password")));
	//
	return super.save(account);
}

	
	//body  {id:'',password:'',passwordOld:''}
	//if account is not existed,return error {error:true,code:201} 
	//if old password is not matched,return error {error:true,code:202} 
	//Success change returns the result of save
//	public ResponseEntity<Document> changePassword(@RequestBody Document body) throws Exception{
//		String id=MongoUtil.getId(body);
//
//		//load should return successful result since data auth is checked before
//		Document account=template.findById(id, Document.class, getColName());
//		if(account==null) {
//			return ResponseEntity.ok(new Document("error",true).append("code", 201)); 
//		}
//		//
//		if(!passwordEncoder.matches(body.getString("passwordOld"), account.getString("password"))) {
//			//Old password is incorrect
//			return ResponseEntity.ok(new Document("error",true).append("code", 202));
//		}
//		//
//		account.append("password", encryptPassword(body.getString("password")));
//		//
//		return super.save(account);
//	}
	
	@PostMapping(value = "/resetPassword")
	//body  {id:'',password:'',passwordOld:''}
	//if account is not existed,return error {error:true,code:201} 
	//Success change returns the result of save
	public ResponseEntity<Document> resetPassword(@RequestBody Document body) throws Exception{
		String id=MongoUtil.getId(body);

		//load should return successful result since data auth is checked before
		Document account=template.findById(id, Document.class, getColName());
		if(account==null) {
			return ResponseEntity.ok(new Document("error",true).append("code", 201)); 
		}
		//
		account.append("password", encryptPassword(body.getString("password")));
		//
		return super.save(account);
	}
	// **********************************
	// * private methods
	// **********************************
	public ResponseEntity<Document> loginInternal(String username, String password) {
		Assert.isTrue(StringUtil.notEmpty(username), "username is empty");
		Assert.isTrue(StringUtil.notEmpty(password), "password is empty");
		// Try to load account from database
		Document account = template.findOne(new Query(Criteria.where("username").is(username)), Document.class,
				"account");
		if (account == null) {
			return buildLoginFail();
		}
		if(!account.getBoolean("active", false)) {
			return buildLoginFail();
		}
		if (!verifyPassword(account, password)) {
			return buildLoginFail();
		}

		// 生成Token并保存到Session
		String token = StringUtil.getUUID();
		// Remove password field since it is confidential
		account.remove("password");
		// Find all the authorities
		Document authorities = findAuthoritiesByAccount(account);
		account.append("authorities", authorities);
//		System.out.println("####"+account.toJson());
		AccountUtil.getCache(cacheManager).put(token, account.toJson());

		// 返回token到前端
		Document response = new Document();
		response.append("success", true);
		response.append("token", token);
		response.append("authorities", authorities);
		return ResponseEntity.ok(response);
	}

	public ResponseEntity<Document> infoInternal(String token) {

		if (StringUtil.isEmpty(token)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		ValueWrapper valueWrapper = AccountUtil.getCache(cacheManager).get(token);
		if (valueWrapper == null || StringUtil.isEmpty(valueWrapper.get())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		//
		Document account = Document.parse(valueWrapper.get().toString());
		//
		return ResponseEntity.ok(account);
	}

	// Login fail response
	private ResponseEntity<Document> buildLoginFail() {
		return ResponseEntity.ok(new Document("success", false));
	}

	// Encrypt password
	private String encryptPassword(CharSequence password) {
		return passwordEncoder.encode(password);
	}

	// Verify password
	private boolean verifyPassword(Document account, String password) {
		return passwordEncoder.matches(password, account.getString("password"));
	}

	// Authority structure: {"module" : "jdbcConnection","operations" : [ "access",
	// "add", "edit", "del", "auth", "all","all_read"] }
	private Document findAuthoritiesByAccount(Document account) {
		Document authorities = new Document();
		for (String roleId : account.getList("roles", String.class, new ArrayList<>(0))) {
			Document role = template.findById(roleId, Document.class, "accountRole");
			if (role == null) {
				continue;
			}
			//
			mergetAuthorities(authorities, role.get("authorities", Document.class));
		}
		//
		return authorities;
	}

	private void mergetAuthorities(Document authorities, Document authoritiesToMerge) {
		for (String module : authoritiesToMerge.keySet()) {
			List<String> operationsMerge = authoritiesToMerge.getList(module, String.class, new ArrayList<>());
			List<String> operations = authorities.getList(module, String.class);
			if (operations == null) {
				// This module is NOT set before, use the operations under this authority
				// directly
				authorities.append(module, operationsMerge);
			} else {
				// Merge
				mergeSingle(operations, operationsMerge);
			}
		}
	}

	// Merge all operations in toMerge to authority
	private void mergeSingle(List<String> operations, List<String> operationsMerge) {

		for (String operation : operationsMerge) {
			if (!operations.contains(operation)) {
				operations.add(operation);
			}
		}
	}
}
