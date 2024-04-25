import{d as B,r as k,D as U,z as j,a as s,b as h,c as M,f as a,g as u,p as v,j as g,F as O,v as w,i as D,k as N,q as S,s as T,S as q,T as $,U as z,W as A,Z as F}from"./index-168435c7.js";const R={class:"dialog-footer"},J=B({__name:"DataEditorDialog",setup(I,{expose:V}){function b(d,o){i.value=d,y=o,n.value=!0}const n=k(!1),i=k({});let y;const r=k(),f=async d=>{d&&await d.validate(o=>{o&&(n.value=!1,y&&y(i.value))})};return V({show:b}),(d,o)=>{const C=s("el-input"),_=s("el-form-item"),e=s("el-option"),l=s("el-select"),t=s("el-form"),m=s("el-button"),E=s("el-dialog");return h(),w(E,{modelValue:n.value,"onUpdate:modelValue":o[5]||(o[5]=p=>n.value=p),title:d.$t("_.builtIn.plugin.dataEditor.dialogTitle"),width:"30%","close-on-click-modal":!1,"close-on-press-escape":!1},{footer:u(()=>[D("span",R,[a(m,{onClick:o[3]||(o[3]=p=>n.value=!1)},{default:u(()=>[v(g(d.$t("_._.cancel")),1)]),_:1}),a(m,{type:"primary",onClick:o[4]||(o[4]=p=>f(r.value))},{default:u(()=>[v(g(d.$t("_._.save")),1)]),_:1})])]),default:u(()=>[a(t,{ref_key:"dataEditorFormRef",ref:r,model:i.value,"label-width":"120px"},{default:u(()=>[a(_,{label:d.$t("_._.key"),prop:"key",required:""},{default:u(()=>[a(C,{modelValue:i.value.key,"onUpdate:modelValue":o[0]||(o[0]=p=>i.value.key=p)},null,8,["modelValue"])]),_:1},8,["label"]),a(_,{label:d.$t("_._.description"),prop:"description"},{default:u(()=>[a(C,{modelValue:i.value.description,"onUpdate:modelValue":o[1]||(o[1]=p=>i.value.description=p)},null,8,["modelValue"])]),_:1},8,["label"]),a(_,{label:d.$t("_.builtIn.plugin.dataEditor.type"),prop:"type",required:""},{default:u(()=>[a(l,{modelValue:i.value.type,"onUpdate:modelValue":o[2]||(o[2]=p=>i.value.type=p)},{default:u(()=>[a(e,{label:"String",value:"String"}),a(e,{label:"Number",value:"Number"}),a(e,{label:"Boolean",value:"Boolean"}),a(e,{label:"Object",value:"Object"}),a(e,{label:"Array",value:"Array"})]),_:1},8,["modelValue"])]),_:1},8,["label"])]),_:1},8,["model"])]),_:1},8,["modelValue","title"])}}}),P={class:"dialog-footer"},G=B({__name:"DataValueDialog",setup(I,{expose:V}){const b=U("context"),n=k(),i=k(!0),y=k(!1);let r,f=0;function d(l,t){r=l,f=t,n.value=C(l),o.value=!0}const o=k(!1);function C(l){let t;return f==0?t=l.initValue:(t=b.dataManager.get(r.key),t=A(t)?t.value:t),t!=null?l.type=="String"?t:JSON.stringify(t,null,2):l.type=="Object"?"{}":l.type=="Array"?"[]":l.type=="String"?"":l.type=="Number"?"0":l.type=="Boolean"?"true":""}function _(){return r.type=="Object"||r.type=="Array"?JSON.parse(n.value):r.type=="String"?n.value:r.type=="Number"?parseFloat(n.value):r.type=="Boolean"?n.value=="true":""}function e(){let l;try{l=_()}catch(t){F({title:"Error",message:t.message,type:"error"});return}f==0?(r.initValue=l,i.value&&b.dataManager.set(r.key,l)):(b.dataManager.set(r.key,l),y.value&&(r.initValue=l)),o.value=!1}return V({show:d}),(l,t)=>{const m=s("b-ace-editor"),E=s("el-switch"),p=s("el-button"),x=s("el-dialog");return h(),w(x,{modelValue:o.value,"onUpdate:modelValue":t[4]||(t[4]=c=>o.value=c),title:l.$t("_.builtIn.plugin.dataEditor.dataValueTitle")},{footer:u(()=>[D("span",P,[N(f)==0?(h(),w(E,{key:0,style:{"margin-right":"20px"},modelValue:i.value,"onUpdate:modelValue":t[1]||(t[1]=c=>i.value=c),size:"large","inactive-text":l.$t("_.builtIn.plugin.dataEditor.resetCurrent")},null,8,["modelValue","inactive-text"])):S("",!0),N(f)==1?(h(),w(E,{key:1,style:{"margin-right":"20px"},modelValue:y.value,"onUpdate:modelValue":t[2]||(t[2]=c=>y.value=c),size:"large","inactive-text":l.$t("_.builtIn.plugin.dataEditor.resetInit")},null,8,["modelValue","inactive-text"])):S("",!0),a(p,{onClick:t[3]||(t[3]=c=>o.value=!1)},{default:u(()=>[v(g(l.$t("_._.cancel")),1)]),_:1}),a(p,{type:"primary",onClick:e},{default:u(()=>[v(g(l.$t("_._.save")),1)]),_:1})])]),default:u(()=>[a(m,{modelValue:n.value,"onUpdate:modelValue":t[0]||(t[0]=c=>n.value=c),lang:"json",width:"100%",height:"50vh",readonly:!1,"font-size":14},null,8,["modelValue"])]),_:1},8,["modelValue","title"])}}}),Z=B({__name:"index",setup(I){const V=k(),b=k();let n=-1;const i=U("context");function y(){n=-1,V.value.show({},f)}function r(e){n=e.$index;const l=T(e.row);V.value.show(l,f)}const f=e=>{if(n<0){for(const l of _.value)if(l.key==e.key){q.error($("_.builtIn.plugin.dataEditor.errorExist",[e.key]));return}_.value.push(e)}else _.value[n]=e},d=e=>{z.confirm($("_.builtIn.plugin.dataEditor.deletePrompt"),$("_._.warning"),{confirmButtonText:$("_._.yes"),cancelButtonText:$("_._.no"),type:"warning"}).then(()=>{_.value.splice(e.$index,1),i.dataManager.clear(e.row.key)})},o=e=>{b.value.show(e.row,0)},C=e=>{b.value.show(e.row,1)},_=j(()=>i.codeManager.getCode().data);return(e,l)=>{const t=s("el-table-column"),m=s("el-button"),E=s("el-button-group"),p=s("el-table");return h(),M(O,null,[a(p,{data:_.value,border:""},{empty:u(()=>[a(m,{type:"primary",onClick:l[1]||(l[1]=x=>y())},{default:u(()=>[v(g(e.$t("_._.add")),1)]),_:1})]),default:u(()=>[a(t,{prop:"key",label:e.$t("_._.key")},null,8,["label"]),a(t,{prop:"description",label:e.$t("_._.description")},null,8,["label"]),a(t,{prop:"type",label:e.$t("_.builtIn.plugin.dataEditor.type")},null,8,["label"]),a(t,{fixed:"right",label:e.$t("_._.operation"),width:"420px"},{default:u(x=>[a(E,null,{default:u(()=>[a(m,{type:"primary",onClick:l[0]||(l[0]=c=>y())},{default:u(()=>[v(g(e.$t("_._.add")),1)]),_:1}),a(m,{type:"success",onClick:c=>r(x)},{default:u(()=>[v(g(e.$t("_._.edit")),1)]),_:2},1032,["onClick"]),a(m,{type:"danger",onClick:c=>d(x)},{default:u(()=>[v(g(e.$t("_._.del")),1)]),_:2},1032,["onClick"]),a(m,{type:"primary",onClick:c=>o(x)},{default:u(()=>[v(g(e.$t("_.builtIn.plugin.dataEditor.initValue")),1)]),_:2},1032,["onClick"]),a(m,{type:"success",onClick:c=>C(x)},{default:u(()=>[v(g(e.$t("_.builtIn.plugin.dataEditor.currentValue")),1)]),_:2},1032,["onClick"])]),_:2},1024)]),_:1},8,["label"])]),_:1},8,["data"]),a(J,{ref_key:"dataEditorDialogRef",ref:V},null,512),a(G,{ref_key:"dataValueDialogRef",ref:b},null,512)],64)}}});export{Z as default};