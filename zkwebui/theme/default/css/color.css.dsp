<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ page contentType="text/css;charset=UTF-8" %>

<%-- Words --%>
<c:set var="Color01" value="#403E39"/>
<%-- Menu Background --%>
<c:set var="Color02" value="#EEEDEB"/>
<%-- Group Background --%>
<c:set var="Color03" value="#F3F3F1"/>
<%-- Tab Border --%>
<c:set var="Color04" value="#ABA799"/>
<%-- Input Border --%>
<c:set var="Color05" value="#FFFFFF"/>
<%-- Selection --%>
<c:set var="Color06" value="#D1CFCA"/>
<%-- Zebra Color --%>
<c:set var="Color07" value="#F9F9F8"/>
<%-- Group Bottom Line --%>
<c:set var="Color08" value="#CDCDCD"/>
<%-- Hover+Select--%>
<c:set var="Color09" value="#B2AEA6"/>
<%-- Hover --%>
<c:set var="Color10" value="#D6D6D6"/>

<c:set var="ColorWhite" value="#FFFFFF"/>
<c:set var="ColorBGTree" value="#F7F7F7"/>
<c:set var="ColorSeld" value="#0068c5"/>
<c:set var="ColorGray" value="#FFFFFF"/>
<c:set var="ColorLightGray" value="#F0F0F0"/>
<c:set var="ColorBorder" value="#999999"/>
<c:set var="aColor" value="#debe09"/>

<%-- OpenUp Ltda. Inicio --%>
<%-- Window Font --%>
<c:set var="WindowFont" value="'Open Sans', sans-serif, Verdana, Arial, Helvetica;"/>
<%-- OpenUp Ltda. Fin --%>


@import url('https://fonts.googleapis.com/css?family=Open+Sans');

.desktop-header-font {
    font-family: 'Open Sans', sans-serif, Verdana, Arial, Helvetica;
	font-size: 10px;
	color: #FFFFFF;
}


<%-- Start e-Evolution --%>
.z-toolbar-button {
 color: #282828 !important;
}

.z-listbox {
  color: #3282828 !important;
}
<%-- End e-Evolution --%>

<%-- OpenUp Ltda. Inicio --%>
*, .z-label, .z-checkbox {
  font-family: ${WindowFont};
  
}
.mandatory-decorator-text {
  color: #ff0000 !important;
}

.mandatory-field, z-textbox{
   min-inline-size: -webkit-fill-available;

}

.normal-field, z-textbox{
 min-inline-size: -webkit-fill-available;

}

.z-combobox-inp, .z-combobox-inp, .z-textbox, .z-decimalbox, .z-checkbox-cnt {
  font-family: ${WindowFont};
}
.z-combobox-disd * {
  font-family: ${WindowFont};
  color: #686868 !important;
  
}


.z-comboitem {
    display: block;
    padding: 3px 2px;
    position: relative;
    text-shadow: 0 0px #ffffff21;
}   

.z-comboitem-selected:hover .z-comboitem-text {
    color: #fefefe;
}


.z-comboitem-selected {
    background: -webkit-linear-gradient(top, #1f9bde 0%, #1f9bde 100%);
    background: linear-gradient(to bottom, #1f9bde 0%, #1f9bde 100%);
}

.z-comboitem:hover {
    background: -webkit-linear-gradient(top, #1f9bde 0%, #1f9bde 100%); 
    background: linear-gradient(to bottom, #1f9bde 0%, #1f9bde 100%);
    color: #fefefe;
    
}
.z-comboitem-selected .z-comboitem-text {
    color: #fefefe;
}
.z-button{
 
    background: -webkit-linear-gradient(top, #336b9b  0%, #336b9b  100%);
    color: #fefefe;
    
     }
     
.z-button:hover {
    border-color: #0a3a5e !important;
    background: -webkit-linear-gradient(top, #0a3a5e 0%, #0a3a5e 100%)!important;
    background: linear-gradient(to bottom, #0a3a5e 0%, #0a3a5e 100%)!important;
}

.z-button:focus {
    color: #000;
    border-color: #0a3a5e;
    background: -webkit-linear-gradient(top, #0a3a5e 0%, #0a3a5e 100%);
    background: linear-gradient(to bottom, #0a3a5e 0%, #0a3a5e 100%);
    -webkit-box-shadow: inset 0 0 2px #0a3a5e;
    box-shadow: inset 0 0 2px #0a3a5e;
}





     
.ad-button .ad-editorbox-button .z-button{
       
       width: 66px;
 
} 


.form-button{
 
    
	}

 .z-listitem.z-listitem-selected .z-listcell
{
     border-color: #1f9bde;
    background: -webkit-linear-gradient(top, #1f9bde 0%, #1f9bde 100%);
    background: linear-gradient(to bottom, #1f9bde 0%, #1f9bde 100%);
    background-clip: padding-box;
    position: relative;
}
.z-listitem:hover>.z-listcell {
    background: -webkit-linear-gradient(top, #1f9bde 0%, #1f9bde 100%);
    background: linear-gradient(to bottom, #1f9bde 0%, #1f9bde 100%);
    background-clip: padding-box;
    position: relative;
    
}

input[type="checkbox"] {

    box-shadow: 0 1px 2px rgba(0,0,0,0.05), inset 0px -15px 10px -12px rgba(0,0,0,0.05);
	padding: 9px;
    border: 1px solid #f0f0f0 !important;
	display: inline-block;
	position: relative;
	   width: 20px; 
     height: 20px;
 
}

.ad-textbox ad-weditor
{
   

}


}
.ad-textbox, z-textbox
{

 

}
.ad-wlogin-layout-center{

    background-color: #003674 !important;
        font-size: 13px;
}
.ad-label{

    color: #333 !important;
}

.ad-panel, z-div{

 display: flex;
 width: 100%;

 
}
.z-image{



}
.input    
{
         width: -webkit-fill-available;     
     
}
.readonly-field, z-combobox

{
     width: 272px;
}  
.ad-loginpanel-header-logo, z-image{
   
  
}
.ad-weditor, ad-wstringeditor { 
     
     min-inline-size: -webkit-fill-available; 
       
}
.ad-textbox, ad-weditor{
    
      
}
 
.ad-weditor, image z-image{
 
    height: 50px;
  
    }
 
.ad-combobox, ad-weditor{

     min-inline-size: auto;
     
} 
    
.ad-editorbox-input {

  width: 100%;
}


.ad-editorbox-cell-input    
{
        width: 100% !important;

}



.ad-weditor{

   
 }






.ad-headerpanel-logo, z-image{
     text-align: -webkit-right;
     position: fixed;

}
.ad-rolepanel-header-logo, z-image{
    padding: 15px;

}
.ad-numberbox-button{
   
       padding:3px !important;     
       border-radius:5px !important;
       color: #fefefe !important;
       background: -webkit-linear-gradient(top, #346c9b 0%, #346c9b 100%)!important;
        width: 25px;
       
}

.ad-numberbox-button:hover{

     background: -webkit-linear-gradient(top, #0a3a5e 0%, #0a3a5e 100%)!important;
}
   
.ad-button{

    background: -webkit-linear-gradient(top, #346c9b 0%, #346c9b 100%)!important;
    color: #FEFEFE;   
    height: 28px !important;
    border-radius: 3px !important;
        
       
}

.ad-button:hover{
     
     background: -webkit-linear-gradient(top, #0a3a5e 0%, #0a3a5e 100%)!important;

}   
.user-panel-username{
            color: #FEFEFE !important;
            margin-right: 25px;

}





.z-button:active {
    border-color: #336b9b;
    background: -webkit-linear-gradient(top, #336b9b 0%, #336b9b 100%);
    background: linear-gradient(to bottom, #336b9b 0%, #336b9b 100%);

}
.action-button
{
     WIDTH: 99%;
}
.desktop-header-font {
  color: #FFFFFF !important;
}

.link .z-toolbarbutton-content{

     display: inline-block;
    height:24px;
    border: 0px solid #003764;
    color:#fefefe !important;
    padding-right: 10px;
;

}

.link .z-toolbarbutton-content:hover{
    background: -webkit-linear-gradient(top, #003674 0%, #003674 100%);
    background: linear-gradient(to bottom, #003674 0%, #003674 100%);
    border: 0px solid #003764;
   
  

}

.z-toolbarbutton {
    display: inline-block;
    height: 24px;
    border: 0px solid #003764;
    color:#333;
    padding-right: 10px;
}

.z-toolbarbutton-content {
    font-family: "Helvetica Neue",Helvetica,Arial,sans-serif;
    font-size: 11px;
    font-weight: normal;
    font-style: normal;
    color: #000;
    padding: 2px;
    line-height: 14px;
    vertical-align: baseline;
    position: relative;
    text-shadow: 0 0px #ffffff;
    white-space: nowrap;
    text-decoration: underline;
    text-align: right !important;
}

.z-toolbarbutton:hover {
    border-color: #0000;
    background: -webkit-linear-gradient(top, #0000 0%, #0000 100%);
    background: linear-gradient(to bottom, #0000 0%, #0000 100%);
}

.z-tab:hover{
    background: -webkit-linear-gradient(top, #0000 0%, #fefefe 100%);
    background: linear-gradient(to bottom, #fefefe 0%, #fefefe 100%);
}

.z-tab-selected{
           border-color: #2196f3 !important;
           background: -webkit-linear-gradient(top, #2196f3 0%, #2196f3 100%)!importnt;
           background: linear-gradient(to bottom, #2196f3 0%, #2196f3 100%)!important;
           color: #fefefe !important;
}

.z-tab-selected:hover {
    border-color: #2196f3;
    background: -webkit-linear-gradient(top, #2196f3 0%, #2196f3 100%);
    background: linear-gradient(to bottom, #2196f3 0%, #2196f3 100%);
    color: #fefefe !important;
 }

.z-tab-selected .z-tab-text{

color: #FFF!important;

}


.z-tabs-content{
   width: 100%;
    border-collapse: separate;
    border-spacing: 0px;
    border-bottom: 0px solid #cfcfcf;
    margin: 0px;
    padding-left: 9px;
    padding-top: 0px;
    list-style-image: none;
    list-style-position: outside;
    list-style-type: none;
    zoom: 1;
    background-color: #fefefe;
    clear: both;
    background: -webkit-linear-gradient(top, #e7e7e7 0%, #e7e7e7 100%);
    background: linear-gradient(to bottom, #e7e7e7 0%, #e7e7e7 100%);
}

.z-tab-icon {
     
    background-repeat: no-repeat;
    cursor: pointer;
    display: block;
    width: 12px;
    height: 22px;
    position: absolute;
    right: 1px;
    top: 1px;
    z-index: 15;
}



.z-label {
    line-height: normal;
    color: #333;
    font-size: 11px;
    font-family: 'Open Sans', sans-serif, Verdana, Arial, Helvetica;
    
 }   
 .z-label, z-checkbox {
 text-align: -webkit-center !important;
}

.z-window {
    -webkit-border-radius: 4px;
    border-radius: 4px;
    padding: 4px;
    background: #2196F3;
}

.z-window-content {
    border: 1px solid #cfcfcf;
    margin: 0;
    padding: 4px;
    background: #fefefe;
    overflow: hidden;
    zoom: 1;
}

.z-window-icon {
    font-size: 11px;
    color: #ffffff;
    display: block;
    width: 21px;
    height: 20px;
    border: 1px solid #2196f3;
    -webkit-border-radius: 4px;
    border-radius: 15px;
    margin: auto 1px;
    line-height: 20px;
    background: -webkit-linear-gradient(top, #919191 0%, #919191 100%);
    background: linear-gradient(to bottom, #919191 0%, #919191 100%);
    text-align: center;
    overflow: hidden;
    cursor: pointer;
    float: right;
}

.z-window-icon:hover {
    color: #607D8B;
    border-color: #6aacd3;
    background: 0;
    filter: progid:DXImageTransform.Microsoft.gradient(enabled=false);
    background: #e7e7e7;
}

.z-window-header {
    font-family: "Helvetica Neue",Helvetica,Arial,sans-serif;
    font-size: 12px;
    font-weight: normal;
    font-style: normal;
    color: #eeeeee;
    padding: 3px 0 5px 0;
    line-height: 24px;
    overflow: hidden;
    zoom: 1;
    cursor: default;
}

.z-window-noborder>.z-window-content {
    border: 0;
    margin: 0px 0px;
    border-radius: 5px;
   

}


.z-textbox
, .z-combobox-inp
, .z-decimalbox
, .z-datebox-inp {
  background-color: #ffffff !important;
  border: 1px solid #bbbbbb !important;
  font-size: 11px;
  font-weight: normal;
  font-style: normal;
  min-height: 23px;
  min-width: 99 %;
  
}
}
.z-textbox-readonly
, .z-combobox-inp-readonly
, .z-decimalbox-readonly
, .z-datebox-inp-readonly
, .z-combobox-text-disd {
  background-image: url("../images/zul/misc/text-bg8-dis.gif") !important;
  background-repeat: repeat !important;
  color: #282828 !important;
}

.z-panel-header {
  color: #ffffff !important;
}
.adtab-grid-panel .z-row:nth-child(even) * {
  background-color: #f0f0f0 !important;
}
.adtab-grid-panel .z-row:nth-child(odd) * {
  background-color: #ffffff !important;
}

<%-- Skyblue underline tabs - Start --%>
.z-tabs, z-tabs-scroll {
  padding-bottom: 0px !important;
}
.z-tabpanels {
 
    border-top: solid 1px #1f9bde !important;
    overflow: auto;
    width: 100%;
    height: 100%;
    visibility: inherit;
  
  
   
}
<%-- Skyblue underline tabs - End --%>

.z-tree-row-over *, .z-tree-row-over-seld *, .z-tree-row-seld * {
  color: #ffffff !important;
  background-color: #1F9BDE !important;
}

<%-- Logo position --%>
.desktop-header-right {
  margin-top: 0px;
  padding-left: 15px;
}

.z-combo-item-text {
  font-size: 12px !important;
}
.z-combo-item-inner {
  font-size: 10px !important;
  font-style: italic;
}

.z-combobox-input{
    font-family: Arial,Sans-serif;
    font-size: 12px;
    font-weight: normal;
    font-style: normal;
    color: #333;
    height: 24px;
    border: 1px solid #a9a9a9;
    border-right: 0;
    -webkit-border-radius: 3px 0 0 3px;
    border-radius: 3px 0 0 3px;
    margin: 0;
    padding: 4px 5px;
    line-height: 14px;
    background: #fefefe;
      
}

.z-column:not(.z-column-over) {
  background-color: #EBEBEB !important;
}




.z-list-item-seld *, .z-list-item-over * {
}

.adwindow-navbtn-uns
, .adwindow-navbtn-sel
, .adwindow-navbtn-dis {
  padding-top: 8px;
  padding-bottom: 8px;
  padding-right: 20px;
}

.z-north-splt, .z-south-splt {
  background: #003764;
  height: 1px;
}
.z-border-layout {
  background-color: #003764;
}


.z-treerow.z-treerow-selected .z-treecell-content {
  
    color: #ffffff !important;
    background-color: #2196f3 !important;
    
}

.z-treerow:hover>.z-treecell {
    
    background: -webkit-linear-gradient(top, #2196f3 0%, #2196f3 100%);
    background: linear-gradient(to bottom, #2196f3 0%, #2196f3 100%);
    background-clip: padding-box;
        position: relative;
}

.z-treerow:hover .z-treecell-content {
    color: #fefefe;
}

.z-treerow.z-treerow-selected .z-treecell:hover {
    
    background: -webkit-linear-gradient(top, #008fd3 0%, #008fd3 100%);
    background: linear-gradient(to bottom, #008fd3 0%, #2196f3 100%);
    background-clip: padding-box;
    position: relative;
}


<%-- OpenUp Ltda. Fin --%>


<%-- Begin ERPCyA --%>
.z-toolbar-body span {
	font-size: 11px !important;
}
<%-- ERPCyA end --%>

tr.z-vbox{
	background-color:${ColorGray};
}

.z-drag-over {
	background: ${Color09};
}

div.z-drop-ghost {
	border:none;
}
.z-loading {
	background-color:${Color04};
	border:none;
}
div.z-progressmeter {
	border:none;
}
.z-loading-indicator {
	border:none;
	color:${Color01};
}
.z-datebox-pp .z-datebox-calyear {
	background-color:${Color07};
}
.z-calendar-calmon td.z-calendar-seld, .z-calendar-calday td.z-calendar-seld,
.z-datebox-calmon td.z-datebox-seld, .z-datebox-calday td.z-datebox-seld {
	background-color:${ColorSeld};
	border:none;
}
.z-textbox, .z-decimalbox, .z-intbox, .z-longbox, .z-doublebox {
	border-color:${ColorBorder};
}
.z-combobox-inp ,.z-spinner-inp,.z-datebox-inp,.z-timebox-inp ,.z-bandbox-inp {
	border-color:${ColorBorder};
}
.z-combobox .z-combobox-img,
.z-spinner .z-spinner-img,
.z-datebox .z-datebox-img,
.z-timebox .z-timebox-img ,
.z-bandbox .z-bandbox-img {
	border:none;
}
.z-textbox-focus, .z-textbox-focus input,
.z-decimalbox-focus, .z-decimalbox-focus input,
.z-intbox-focus, .z-intbox-focus input,
.z-longbox-focus, .z-longbox-focus input,
.z-doublebox-focus, .z-doublebox-focus input {

}
.z-combobox-focus .z-combobox-inp,
.z-spinner-focus .z-spinner-inp,
.z-datebox-focus .z-datebox-inp,
.z-timebox-focus .z-timebox-inp ,
.z-bandbox-focus .z-bandbox-inp  {

}
.z-combobox-focus .z-combobox-img,
.z-spinner-focus .z-spinner-img,
.z-datebox-focus .z-datebox-img,
.z-timebox-focus .z-timebox-img ,
.z-bandbox-focus .z-bandbox-img {
	border:none;
}
.z-combobox-input:focus
{
   -webkit-box-shadow: inset 1px 1px 1px #2196f3;
    box-shadow: inset 1px 1px 1px #6aacd3;
    background: #fefefe;
    border: 1px solid #2196f3;
}
.z-combobox-pp ,.z-bandbox-pp {
	border:none;
}

.z-combobox-pp .z-combo-item-over {
	background-color: ${ColorSeld};
}

.z-combobox-pp .z-combo-item-seld .z-combo-item-text .z-combo-item-inner {
	color:${ColorWhite};
}
.z-combobox-pp .z-combo-item-over-seld .z-combo-item-text{
	color:${ColorWhite};
}
.z-combobox-pp .z-combo-item-over .z-combo-item-text{
	color:${ColorWhite};
}


.z-groupbox-tl,
.z-groupbox-hl {
	border:none;
}
.z-groupbox-cnt {
	border:none;
}
.z-window-embedded-cnt, .z-window-popup-cnt {
	border:none;
}
.z-window-embedded-tl, .z-window-embedded-tl-noborder {
	border:none;
}
.z-window-popup-cm, .z-window-modal-cm, .z-window-highlighted-cm, .z-window-overlapped-cm {
	border:none;
}
.z-window-resize-proxy {
	border:none;
	background-color: ${Color06};
}
.z-window-move-ghost {
	background-color : ${Color06};
}
.z-window-move-ghost dl,
.z-window-resize-faker {
	background-color : ${Color06};
	border:none;
}
.z-window-popup-tl {
	border:none;
}
div.z-tree, div.z-dottree, div.z-filetree, div.z-vfiletree {
	border:none;
}

div.z-tree-body{
	background-color : ${ColorBGTree};
	border:none;
	overflow: hidden;
}


tr.z-tree-row td.z-tree-row-focus tr.z-tree-row-text {
	color : ${ColorWhite};
}

tr.z-tree-row-seld tr.z-tree-row-text {
	color : ${ColorWhite};
}

tr.z-tree-row-over tr.z-tree-row-text{
	color : ${ColorWhite};
}

tr.z-tree-row-over-seld tr.z-tree-row-text{
	color : ${ColorWhite};
}


.z-tab-seld .z-tab-text {
	color : ${ColorWhite};
}

div.z-tree-header th.z-tree-col, div.z-tree-header th.z-auxheader,
div.z-dottree-header th, div.z-filetree-header th, div.z-vfiletree-header th {
	border:none;
}
tr.z-listbox-odd {
	background-color : ${Color07};
}

tr.z-list-item-focus, div.z-listcell-cnt-text{
	color: ${ColorWhite};
}

tr.z-list-item-seld, div.z-listcell-cnt-text{
	color: ${ColorWhite};
}
tr.z-list-item-over,  div.z-listcell-cnt-text{
	color: ${ColorWhite};
}

tr.z-list-item-over-seld,  div.z-listcell-cnt-text{
	color:${ColorWhite};
}

div.z-listbox-header th.z-list-header, div.z-listbox-header th.z-auxheader {
	border:none;
}
div.z-listbox {
	   background-color : ${Color06};
	 border: none;
}
div.z-listbox-footer {
	background-color : ${Color06};!important;
   
	border:none;
}
tr.cells td {
	border-bottom-color:${Color04};
	border:none;
}
div.z-grid {
	background-color:#f7f7f7;
	border:none;
	-moz-box-shadow: 1px 1px 4px gray;
	-webkit-box-shadow: 1px 1px 4px gray;
	box-shadow: 1px 1px 4px #F0F0F0;
	box-shadow: 1px 1px 4px #ffffff !important;
	border-left: 3px solid rgb(0, 155, 222);
	 width: 99.1% !important;
}
.z-grid-body {
   color:#FEFEFE;
   overflow: auto !important;
}

.z-grid-body table {
    border-spacing: 0;
    table-layout: inherit !important;
   overflow: hidden !important;
}

.z-grid-body .z-cell {

   color: #000;
  
}

div.z-grid-body {
	-webkit-box-shadow: 0 0 5px #f7f7f7;
    box-shadow: 0 0 5px #fff;
    background-color: #FFFFFF;
    border: none;
        height: -webkit-fill-available !important;
        
    
}

.z-tabbox-left .z-tabs-content {
      border-right: 1px solid #cfcfcf;
    height: 429px!important;
    max-block-size: -webkit-fill-available;
    overflow: auto !important;
}



.z-tabbox-scroll .z-tabbox-icon {
    display: none;
}

tr.z-grid-odd td.z-row-inner, tr.z-grid-odd {
	background-color : ${ColorLightGray};
}

tr.z-grid-odd .z-cell{
	background-color : #fefefe;
}


tr.z-row td.z-row-inner{
	background-color : ${ColorGray};
	margin-left:4px;
	margin-right:4px;
	<%-- border-bottom: 1px solid ${ColorBGTree}; --%>
	border-left: 0;
	<%-- border-right: 1px solid ${ColorBGTree}; --%>
}

div.z-row-cnt{
	margin-left:4px;
	margin-right:20px;
}

.z-row-inner{

 
}
    
   
.z-row td:first-child {
    border-left: none;
   
    
}   

<%--
tr.z-rows{
	background-color : ${ColorGray};
}
--%>

div.z-grid-header th.z-column, div.z-grid-header th.z-auxheader {
	border:none;
}
td.z-list-group-inner div.z-list-cell-cnt {
	color : ${Color01};
}
td.z-list-group-inner {
border-top-color: ${Color04};
	border-bottom-color:${Color08};
	border:none;
}
td.z-list-group-foot-inner div.z-list-cell-cnt {
	color : ${Color01};
}
tr.z-list-group {
	background-color: ${Color03};
}
tr.z-group {
	background-color: ${Color03};
}
td.z-group-inner {
	border-top-color: ${Color04};
	border-bottom-color:${Color08};
	border:none;
}
.z-group-inner .z-group-cnt span, .z-group-inner .z-group-cnt {
	color : ${Color01};
}
.z-group-foot-inner .z-group-foot-cnt span, .z-group-foot-inner .z-group-foot-cnt {
	color : ${Color01};
}
.z-paging {
	border:none;
}
.z-paging-inp {
	border:none;
}
.z-paging-os .z-paging-os-cnt {
	background-color: ${Color09};
	border:none;
	color: ${Color01};
}
.z-paging-os .z-paging-os-seld:hover {
	color: white;
}

.z-paging-info, .z-paging-text{
    color:#FFFFFF;
}

.z-west-header, .z-center-header, .z-east-header, .z-north-header, .z-south-header {
	color : ${Color01};
}
.z-east-colpsd, .z-west-colpsd{
	background-color : ${Color07};
	border: 1px solid ${ColorBGTree};
}

.z-north, .z-south, .z-west, .z-center, .z-east {
    background: #fefefe;
    overflow: inherit;
}
.z-south{
  
           
}


.z-center{
    border: none;    
}

.z-borderlayout {
    height: 100%;
    border: 0;
    background: #f7f7f7;
}


.z-north{
  
}
.z-north-body{
    background-color: #fefefe;
}

.z-north-splitter{
         background: linear-gradient(to bottom, #e7e7e7 0%, #e7e7e7 100%)!important;
}

.z-west-splitter-button
{
    color: #f7f7f7 !important;
    display: inline-block !important;
    border: 1px solid #f7f7f7 !important;

}


.z-west-splitter
{
    width: 0px;
   height: 0px

}

.z-west-body{
    line-height: 14px;    
    overflow: inherit !important;  
}
.z-west-header, .z-center-header, .z-east-header, .z-north-header, .z-south-header {
	 border:none;
	 background-color: #003764
	
    
}

.z-west, .z-center, .z-east, .z-north, .z-south {
	border:none;
	 
}	
desktop-header-right {

    background-color: transparent;
    border: none;
    left: 112px;
    top: 0px;
    width: 1249px;
    height: 50px;


}

.z-hbox, .z-vbox {
    border-spacing: 0;
    height: 100% !important;
    
 } 
.z-hbox td{

   
  visibility: inherit;
  width: 100%;
   
  } 

 
.z-toolbar-content z-toolbar-start{

 background-color: #FEFEFE;
}

.z-toolbar-start{
     
     background-color: #f7f7f7;
}

.z-toolbar-panel .z-toolbar-content span {
    font-size: 12px;
    color: #333;
}
.z-menubar-hor, .z-menubar-ver {
	border:none;
	background-color: ${Color02};
}


.z-menu-popup-cnt .z-menu-over a.z-menu-item-cnt,
.z-menu-popup-cnt .z-menu-item-over a.z-menu-item-cnt {
	border:none;
	color:${ColorWhite};
}


.z-menu-popup {
	border:none;
}
.z-menu-body-over .z-menu-inner-m .z-menu-btn,
.z-menu-body-seld .z-menu-inner-m .z-menu-btn,
.z-menu-item-body-over .z-menu-item-inner-m .z-menu-btn{
	color : ${ColorSeld};
}
.z-tabs-scroll {
	background-color : ${Color07};
	border:none;
}
.z-tabs .z-tabs-space {
	background-color : ${Color07};
	border:none;
}
.z-tabs .z-tabs-cnt {
	border:none;
}



.z-tabpanel{
	border:none;
}
.z-tabbox-ver .z-tabpanels-ver {
	border:none;
}
.z-tabs-ver-scroll {
	border:none;
}
.z-tabs-ver .z-tabs-ver-cnt {
	border:none;
}
.z-tabs-ver-space {
	background-color : ${Color07};
	border:none;
}


.z-tab-text {
    cursor: default;
    font-weight: bold; 
    font-style: normal;
    font-family: Verdana,Tahoma,Arial,Helvetica,sans-serif;
    font-size: 11px;
    white-space: normal;
    padding: 3px 9px 0px;
}

.z-tab-text,
 .z-tab-hl:hover .z-tab-text {
	color : #333;	
	
};
}

   
 

   
      
}

         

 .z-row:hover>.z-row-inner{

    background: -webkit-linear-gradient(top, #fefefe 0%, #fefefe 100%);
    background: linear-gradient(to bottom, #fefefe 0%, #fefefe 100%);
    background-clip: padding-box;
    position: relative;
}





.z-tab-seld .z-tab-text {
	color : ${ColorWhite};
}
.z-tab .z-tab-body:hover .z-tab-text {
	color : ${Color01};
}
.z-tab-ver .z-tab-ver-text {
	color : ${Color01};
}
.z-tab-ver-seld .z-tab-ver-text {
	color : ${ColorWhite};
}
.z-tabs-left-scroll , .z-tabs-right-scroll,
.z-tabs-ver-down-scroll, .z-tabs-ver-up-scroll {
	border:none;
}
.z-tabbox-accordion .z-tabpanel-accordion {
	border:none;
}
.z-tabbox-accordion-lite .z-tabpanel-accordion-lite {
	border:none;
}
.z-tab-accordion-lite-header {
	border:none;
}
.z-tabpanels-accordion-lite {
	border:none;
}

.z-combobox-button{
    border: 1px solid #9E9E9E  !important;

}


.z-combobox-button:hover{
    background: -webkit-linear-gradient(top, #e7e7e7 0%, #e7e7e7 100%);
    background: linear-gradient(to bottom, #e7e7e7 0%, #e7e7e7 100%);

}

.z-combobox-button:focus-within{
    border-color: #e7e7e7 !important;
    background: -webkit-linear-gradient(top, #e7e7e7 0%, #e7e7e7 100%) !important;
    background: linear-gradient(to bottom, #e7e7e7 0%, #e7e7e7 100%) !important;
    -webkit-box-shadow: inset 1px 1px 1px #e7e7e7 !important;
    box-shadow: inset 1px 1px 1px #e7e7e7 !important;
}

.z-treecell-content{
    font-family: Verdana,Tahoma,Arial,Helvetica,sans-serif;
    font-size: 11px;
    font-weight: normal;
    font-style: normal;
    color: #333;
}

.z-tree-icon {
    font-size: 14px;
    color: #2196f3;
    text-align: center;
    cursor: pointer;
}
.z-toolbar {

	vertical-align: middle;
	border:none;
	height:38px;
}

.toolbar-button, z-toolbarbutton {
    background: #f7f7f7 !important;
    border: none !important;
    border: 1px solid #f7f7f7 !important;
    color: #f7f7f7 !important;
    border-style: inherit !important;
 }
 
    
.z-toolbar a, .z-toolbar a:visited {
	
	 background: #f7f7f7;
    border: none;
    color: #333;
    
    
}

.z-panel-body {

    overflow: hidden !important;

}


toolbar-button disableFilter z-toolbarbutton
{
        

}
.z-panel-children{
	background-color:${ColorBGTree};
	border-color:${ColorBorder};
}


.z-panel-hl,
.z-panel-tl {
	border:none;
}
.z-panel-hm .z-panel-header,

.z-panel-header {
	color : #FFFFFF;
	border:none;
}

.z-panelchildren {
    border: none;
    background: #f7f7f7;
    position: relative;
    overflow: auto;
    zoom: 1;
}

.z-anchorlayout, .z-anchorlayout-body, .z-anchorchildren {
    overflow: hidden;
    background-color: white;
}


.z-panel-children {
	border:1px;
	border-color:${ColorBorder};
}
.z-panel-cm .z-panel-children,
.z-panel-cl .z-panel-children,
.z-panel-children-noheader {
	border:1px;
	border-color:${ColorBorder};
}
.z-panel-cm {
	background-color :${Color03};
}
.z-panel-tl .z-panel-header {
	color : ${Color01};
}
.z-panel-fm {
	background-color:${Color07} ;
}

.z-panel-move-ghost {
	background-color: ${Color06};
}
.z-panel-move-ghost ul {
	border:none;
	background-color: ${Color06};
}
.z-panel-move-block {
	border:none;
}
.z-panel-top-noborder .z-toolbar,
.z-panel-top.z-panel-noheader .z-toolbar,
.z-panel-noborder .z-panel-header-noborder,
.z-panel-noborder .z-panel-tbar-noborder .z-toolbar,
.z-panel-noborder .z-panel-header.z-panel-header-noborder,
.z-panel-noborder .z-panel-top-noborder .z-toolbar,
.z-panel-noborder .z-panel-top.z-panel-top-noborder .z-toolbar,
.z-panel-noborder .z-panel-btm.z-panel-btm-noborder .z-toolbar,
.z-panel-noborder .z-panel-btm.z-panel-fbar-noborder .z-toolbar,
.z-panel-noborder .z-panel-btm.z-panel-btm-noborder .z-toolbar {
	border:none;
}
.z-panel-tbar .z-toolbar ,
.z-panel-bbar .z-toolbar,
.z-panel-tbar .z-toolbar,
.z-panel-body .z-panel-top .z-toolbar,
.z-panel-body .z-panel-btm .z-toolbar,
.z-panel-cl .z-panel-top .z-toolbar {
	border:none;
}
.z-popup .z-popup-cm {
	background-color:${Color03} ;
    }
    
.z-popup{
   background: linear-gradient(to bottom, #f3f3f1 0%, #f3f3f1 100%)!important;
    
}
/*additional */


.z-menu-separator-inner {
	background-color:${Color08};
}
div.z-listbox-pgi-b, div.z-tree-pgi-b, div.z-grid-pgi-b  {
	border:none;
}




<%-- calendar.css.dsp --%>
.z-calendar-calyear, .z-datebox-calyear {
	background-color: ${Color02};
	border:none;
}
.z-calendar-calday, .z-datebox-calday {
	border:none;
}
.z-calendar-calmon td, .z-calendar-calday td, .z-calendar-calday td a, .z-calendar-calday td a:visited,
.z-datebox-calmon td, .z-datebox-calday td, .z-datebox-calday td a, .z-datebox-calday td a:visited {
	color: ${Color01};
}
.z-calendar-calmon td.z-calendar-seld, .z-calendar-calday td.z-calendar-seld,
.z-datebox-calmon td.z-datebox-seld, .z-datebox-calday td.z-datebox-seld {
	background-color: ${ColorSeld};
	border:none;
	color:#FFF !important;
}
.z-calendar-over, .z-datebox-over {
	background-color: ${ColorSeld};
}
.z-datebox-calmon td.z-datebox-over-seld,
.z-datebox-calday td.z-datebox-over-seld{
	background-color: ${ColorSeld};
}
.z-calendar-caldow td, .z-datebox-caldow td {
	color: ${Color01};
	background-color: ${Color02};
}
.z-datebox-readonly {
    background-image: url(../images/zul/misc/text-bg8-dis.gif) !important;
    border-color: #bbbbbb;
    background-color: #ECEAE4 !important;
}
.z-datebox-readonly + span {
opacity: 0.5;
}
