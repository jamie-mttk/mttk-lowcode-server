import{d as C,D as b,_ as k,z as y,T as d,a as r,b as c,v as s,g as n,f as l,p as a,j as u,k as I,q as w}from"./index-168435c7.js";const M=C({__name:"buttonSys",setup(x){const e=b("context");function p(){i(!1),e.mitt.emit("action",{type:"return"})}function m(){i(!0)}k(()=>{i(!1)},120*1e3);function i(t){(t||e.codeManager.dirty.value)&&(e.mitt.emit("action",{type:"save",code:e.codeManager.getCode()}),e.codeManager.dirty.value=!1)}const v=y(()=>e.mode.value=="edit"?d("_.builtIn.plugin.top.preview"):d("_.builtIn.plugin.top.edit"));function f(){e.mode.value=="edit"?e.mode.value="view":e.mode.value="edit"}function _(){e.mode.value="view",e.mitt.emit("previewFullSCreen")}return(t,S)=>{const o=r("el-button"),g=r("el-button-group");return c(),s(g,null,{default:n(()=>[l(o,{onClick:p},{default:n(()=>[a(u(t.$t("_.builtIn.plugin.top.return")),1)]),_:1}),l(o,{onClick:m},{default:n(()=>[a(u(t.$t("_.builtIn.plugin.top.save")),1)]),_:1}),l(o,{onClick:f},{default:n(()=>[a(u(v.value),1)]),_:1}),I(e).mode.value=="edit"?(c(),s(o,{key:0,onClick:_},{default:n(()=>[a(u(t.$t("_.builtIn.plugin.top.previewFull")),1)]),_:1})):w("",!0)]),_:1})}}});export{M as default};