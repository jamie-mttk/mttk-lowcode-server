import{u as r,K as s}from"./index-1207c5cc.js";import{c as p,e as u,b as i,f as l}from"./uiUtil-e8f4b7d4.js";function c(a,t){const n={sys:{component:"el-pagination",modelValueName:"currentPage",modelValue:r(t),modelValuePath:a["_key-current-page"]||"page"},props:{},events:{"update:current-page":function(e,o){!a._sync_to_data||!t||(s(t)?t.value[a["_key-current-page"]||"page"]=o:t[a["_key-current-page"]||"page"]=o)},"update:page-size":function(e,o){!a._sync_to_data||!t||!a["_key-page-size"]||(s(t)?t.value[a["_key-page-size"]||"page"]=o:t[a["_key-page-size"]||"page"]=o)}}};for(const e of Object.keys(a))if(a[e]){if(e=="page-sizes"){n.props[e]=JSON.parse(a[e]);continue}else{if(e=="_key-current-page")continue;if(e=="_sync_to_data")continue;if(e=="_key-total"){n.props.total=r(t)[a[e]];continue}else if(e=="_key-page-count"){n.props["page-count"]=r(t)[a[e]];continue}else if(e=="_key-page-size"){n.props["page-size"]=r(t)[a[e]];continue}}n.props[e]=a[e]}return n.props.total==null&&(n.props.total=0),n}const _={key:"_pagination",name:"Pagination",description:"Basic pagination",icon:"mdiBookOpenPageVariantOutline",sequence:5,transform:c,editor:[p("layout"),u("pager-count"),i("small"),i("background"),p("page-sizes"),p("prev-text"),l("prev-icon"),p("next-text"),l("next-icon"),i("disabled"),i("hide-on-single-page"),i("_sync_to_data","Synchronize currrent page and page size to data"),p("_key-current-page","Field name of current page in model value"),p("_key-total","Field name of total elements in model value"),p("_key-page-count","Field name of page count in model value"),p("_key-page-size","Field name of page size in model value")],dataConfig:{type:"Object"},initConfig:{props:{layout:"prev, pager, next, jumper, ->, total","pager-count":7,small:!1,background:!1,"page-sizes":"[10, 20, 30, 40, 50, 100]",disabled:!1,"hide-on-single-page":!1,_sync_to_data:!0,"_key-current-page":"page","_key-total":"total","_key-page-size":"size"}},initStyles:{margin:"6px 0"}};export{_ as default};