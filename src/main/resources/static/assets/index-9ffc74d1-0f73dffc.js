import{l,p as d,m}from"./transformUtil-999da02b-e777606b.js";import{M as p}from"./tooltipUtil-4635a4f7-2c64bc23.js";import{b as f,c as b,d as g,a as u}from"./index-1ede4d6c-b9d7dc37.js";import{ad as t,aT as c}from"./index-5e7b76fe.js";import"./biTool-e603ec91-3f60f9c4.js";import"./index-3458eb1c-625f9c79.js";import"./chartActionUtil-201ccd97-6331c097.js";import"./_plugin-vue_export-helper-dad06003-f875bd67.js";const v=[{key:"dimension",min:1},{key:"metric",min:1}];function x({config:e,data:k,context:w,key:h,contextWrap:q,fullConfig:r}){var a,o;const s=r.config.model,n={...d({config:e}),tooltip:{trigger:"axis",formatter:function(i){return p(i,r)}},xAxis:{type:e.reverse?"value":"category",name:e.reverse?(a=m(s,"metric",0))==null?void 0:a.label:(o=m(s,"dimension",0))==null?void 0:o.label},yAxis:{type:e.reverse?"category":"value"},series:[]};for(const i of r?.config.model.metric||[])n.series.push({type:"bar",id:i.id,name:i.label||i.column,stack:e.stack?"x":""});return n}const y=l(x,v,void 0),T={key:"_bi_bar",name:t.t("bi.widgets.bar.name"),description:"",icon:"mdiChartBar",sequence:13,transform:y,editor:{model:f({type:"bar"},["dimension","metric","drilling",...b]),basic:{init:{statck:!1,"axis-setting":"bl"},ui:[...g(),c.createSwitch("stack",t.t("bi.widgets.bar.stack")),c.createSwitch("reverse",t.t("bi.widgets.bar.reverse"))]},...u()}};export{T as default};