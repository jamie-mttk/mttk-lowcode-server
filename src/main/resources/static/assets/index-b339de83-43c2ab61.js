import{d as l,D as s,z as d,a as u,b as r,v as c}from"./index-5e7b76fe.js";const f=l({__name:"index",setup(i){const o=s("context"),e=d(()=>JSON.stringify(o.codeManager.getCode(),null,2));return(m,n)=>{const t=u("b-ace-editor");return r(),c(t,{modelValue:e.value,"onUpdate:modelValue":n[0]||(n[0]=a=>e.value=a),lang:"json",width:"100%",height:"80vh","font-size":14},null,8,["modelValue"])}}});export{f as default};