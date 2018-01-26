<#--
Licensed to the YYWorks Inc. under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<#assign report = Static["cn.yyworks.nlp.report.NerHtmlReport"].getInstance(request, response)>
<#assign resourceParam = "resource"
         borderStyle = "1px"
         borderSimpleStyle = "1px">
<#if "true" == report.isMultiOperation(request)?string("true", "false")>
    <#assign resourceParam = null>
</#if>
<#assign action = report.getParamAction(request)>
<#-- start of switch statement -->
<#switch action>
    <#case "reportupdate">
<#-- ACTION: get report update -->
<script language='JavaScript'>
<!--

function a(message) {
    parent.append(message);
}

function aH(message) {
}

function aW(message) {
    parent.appendWarning(message);
}

function aE(message) {
    parent.appendError(message);
}

function aN(message) {
    parent.appendNote(message);
}

function aO(message) {
    parent.appendOk(message);
}

function aT(message) {
    parent.appendThrowable(message);
}

function aB() {
    parent.appendBr();
}

var active = null;

function init() {
    if (active != null) {
        clearTimeout(active);
    }
    <#assign alive = report.isAlive(request)?string("true", "false")>
  var alive=${alive};
    
  parent.flushArray();

${StringUtil.wrapString(report.getReportUpdate())}
    
  parent.update();
    if (alive) {
        active = setTimeout("reload('reportupdate');", 2000);
  } else {
       var hasNext = "${report.getParamThreadHasNext(request)}";
       if ("true" == hasNext) {
               if (!${report.hasError()?string("true", "false")} || parent.isContinueChecked()) {
                   // all actions ok or continue checked, continue automatically
                   continueReport();
               } else {
                   // wait for user interaction
                   setTimeout('parent.stop();', 10);
               }// end hasError
       } else {
                 setTimeout('parent.stop();', 10);
       }// end hasNext
  }// end alive
}

function reload(actionParam) {
    <#assign resName = report.getResourceList(request).get(0)>
    var resName = "${resName}";
    if (resName != "") {
        resName = "&resource=" + encodeURIComponent(resName);
    }
    <#assign dialogUri = report.getDialogRealUri(request)
             thread = report.getParamThread(request)
             threadhasnext = report.getParamThreadHasNext(request)>
    location.href="${StringUtil.wrapString(dialogUri)}?action=" + actionParam + "&thread=${thread}&threadhasnext=${threadhasnext}" + resName;
}



function continueReport() {
    parent.hasError = false;
    parent.lastError = "";    
    setTimeout("reload('reportend');", 2000);
    if (parent.document.main.threadhasnext) {
        parent.document.main.threadhasnext.value = "false";
    }
}

// -->
</script>

    ${StringUtil.wrapString(report.bodyStart("empty", "style=\"background-color:ThreeDFace;\" onLoad=\"init();\""))}
    ${StringUtil.wrapString(report.bodyEnd())}

        <#break>
    <#-- ACTION: report begin -->
    <#case "reportbegin">
    <#default>
        ${report.setParamAction("reportend")}

<script type="text/javascript" language="JavaScript">
<!--

// saves the HTML of the extended report format, 
// built from the server-side generated JavaScripts
var htmlText = "";

// boolean flag whether this report is still running
var isRunning = false;

// boolean flag whether this report received the output of a warning/error message
var hasError = false;

// saves the last received headline in the report output
var lastHeadline = "";

// saves the last received warning/error message in the report output
var lastError = "";

// array to save the formats of the last received messages
var reportOutputFormats = new Array();

// array to save the last received messages
var reportOutputMessages = new Array();

// format flags for the HTML formatting of the messages
var FORMAT_DEFAULT = 0;
var FORMAT_WARNING = 1;
var FORMAT_HEADLINE = 2;
var FORMAT_NOTE = 3;
var FORMAT_OK = 4;
var FORMAT_NEWLINE = 5;
var FORMAT_THROWABLE = 6;
var FORMAT_ERROR = 7;

function append(message) {
    reportOutputFormats.push(FORMAT_DEFAULT);
    reportOutputMessages.push(message);
}

function appendHead(message) {
}

function appendWarning(message) {
    reportOutputFormats.push(FORMAT_WARNING);
    reportOutputMessages.push(message);
}

function appendError(message) {
    reportOutputFormats.push(FORMAT_ERROR);
    reportOutputMessages.push(message);
    hasError = true;
    lastError = message;
}

function appendNote(message) {
    reportOutputFormats.push(FORMAT_NOTE);
    reportOutputMessages.push(message);
}

function appendOk(message) {
    reportOutputFormats.push(FORMAT_OK);
    reportOutputMessages.push(message);
}

function appendThrowable(message) {
    reportOutputFormats.push(FORMAT_THROWABLE);
    reportOutputMessages.push(message);
}

function appendBr() {
    reportOutputFormats.push(FORMAT_NEWLINE);
    reportOutputMessages.push("");
}

var cssStyle =
    "<style type='text/css'>\n" +
    "body       { box-sizing: border-box; -moz-box-sizing: border-box; padding: 2px; margin: 0; color: #000000; background-color:#ffffff; font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 11px; }\n" +
    "div.main   { box-sizing: border-box; -moz-box-sizing: border-box; color: #000000; white-space: nowrap; }\n" +
    "span.head  { color: #000099; font-weight: bold; }\n" +
    "span.note  { color: #666666; }\n" +
    "span.ok    { color: #009900; }\n" +
    "span.warn  { color: #990000; padding-left: 40px; }\n" + 
    "span.err   { color: #990000; font-weight: bold; padding-left: 40px; }\n" +   
    "span.throw { color: #990000; font-weight: bold; }\n" +
    "span.link1 { color: #666666; }\n" +
    "span.link2 { color: #666666; padding-left: 40px; }\n" +    
    "span.link2 { color: #990000; }\n" +    
    "</style>\n";

var pageStartSimple =
    cssStyle +
    "<div style='vertical-align:middle; height: 100%;'>\n"+
    "<table border='0' style='vertical-align:middle; height: 100%;'>\n" + 
    "<tr><td width='40' align='center' valign='middle'><img name='report_img' src='/pricat/images/wait.gif' width='32' height='32' alt=''></td>\n" + 
    "<td valign='middle'>";
    
var pageEndSimple = 
    "</td></tr>\n" +
    "</table></div>\n";    

var pageStartExtended =
    cssStyle +
    "<div class='main'>\n";
    
var pageEndExtended = 
    "</div>\n";                                

function start() {
    isRunning = true;
}

function stop() {
    isRunning = false;
    if (document.main.threadhasnext) {
        document.main.threadhasnext.value = "false";
    }
    updateReport();    
}

// flush the arrays with the report formats and messages
function flushArray() {    
    reportOutputFormats = new Array();
    reportOutputMessages = new Array();    
}

// updates the report, builds the HTML string from the JavaScript input
function update() {

    var size = 512000; 

    // resize the HTML string
    if (htmlText.length > size) {
        htmlText = htmlText.substring(htmlText.length - size, htmlText.length);
        var pos = htmlText.indexOf("\n"); 
        if (pos > 0) {
            // cut output at the first linebreak to have a "nice" start
            htmlText = htmlText.substring(pos, htmlText.length);      
        }
    }        
    
    // append the HTML of the extended report format to the HTML string
    htmlText += getContentExtended();
        
    // write the HTML output to the iframe
    updateReport();
}

// writes the HTML output to the iframe
// this function gets also invoked when the report output format is toggled
function updateReport() {

    pageBody = pageStartExtended + htmlText + pageEndExtended;
    document.getElementById("report").style.border = "${borderStyle}";
    report.document.open();    
    report.document.write(pageBody);
    report.document.close();
    
    setTimeout('doScroll();', 1);
}

// builds the HTML string from the JavaScript input
function getContentExtended() {
    var htmlStr = "";
    var i = 0;
    for (i=0;i<reportOutputFormats.length && i<reportOutputMessages.length;i++) {
        switch (reportOutputFormats[i]) {
            case FORMAT_WARNING :
                htmlStr += "<span class='warn'>";
                htmlStr += reportOutputMessages[i];
                htmlStr += "</span>";
                break;
            case FORMAT_ERROR :
                htmlStr += "<span class='err'>";
                htmlStr += reportOutputMessages[i];
                htmlStr += "</span>";
                break;
            case FORMAT_HEADLINE :
                break;
            case FORMAT_NOTE :
                htmlStr += "<span class='note'>";
                htmlStr += reportOutputMessages[i];
                htmlStr += "</span>";            
                break;
            case FORMAT_OK :
                htmlStr += "<span class='ok'>";
                htmlStr += reportOutputMessages[i];
                htmlStr += "</span>";            
                break;    
            case FORMAT_NEWLINE :
                htmlStr += "\n";
                break;    
            case FORMAT_THROWABLE :
                htmlStr += "<span class='throw'>";
                htmlStr += reportOutputMessages[i];
                htmlStr += "</span>";            
                break;
            case FORMAT_DEFAULT :            
            default :
                htmlStr += "<span>";
                htmlStr += reportOutputMessages[i];            
                htmlStr += "</span>";                            
        }
    }
    
    return htmlStr;
}

function doScroll() {
    var pos = 1000000;
    report.window.scrollTo(0, pos);
}

function isContinueChecked() {
    if (document.main.continuereport && document.main.continuereport.checked == true) {
        return true;
    } else {
        return false;
    }
}

function submitActionRefresh(para1, para2, para3) {
<#if report.getParamRefreshWorkplace()?has_content && "true" == report.getParamRefreshWorkplace()>
    <#-- workplace must be refresehd (reloaded) -->
    top.location.href = "${StringUtil.wrapString(report.getDialogRealUri(request))}";
<#else> 
    <#-- no workplace refresh required -->
    return submitAction(para1, para2, para3);
</#if>
}

//-->
</script>

    ${StringUtil.wrapString(report.bodyStart("", "onLoad=\"start();\""))}
    ${StringUtil.wrapString(report.dialogStart())}

<form name="main" action="${StringUtil.wrapString(report.getDialogRealUri(request))}" method="post" class="nomargin" onsubmit="return submitActionRefresh('ok', null, 'main');">

    ${StringUtil.wrapString(report.dialogContentStart(report.getParamTitle(request)))}
    ${StringUtil.wrapString(report.paramsAsHidden(request))}
    <input type="hidden" name="action" value>
    <input type="hidden" name="sequenceNum" value="">
    ${StringUtil.wrapString(report.reportIntroductionText())}

<table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%">
<tr>
    <td><iframe name="report" id="report" src="about:blank" frameborder="0" style="width:99.8%; height:350px; padding: 0; margin: 0; border: ${borderStyle};"></iframe></td>
</tr>
</table>

    ${StringUtil.wrapString(report.reportConclusionText())}
    ${StringUtil.wrapString(report.dialogContentEnd())}

<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr><td>
  <#assign resources = "">
  <#if report.getResourceListAsParam(request)?has_content>
    <#assign resources = report.getResourceListAsParam(request)>
  </#if>
    <iframe src="${StringUtil.wrapString(report.getDialogRealUri(request))}?action=reportupdate&thread=${report.getParamThread(request)}&threadhasnext=${report.getParamThreadHasNext(request)}<#if resourceParam?has_content>${"&" + resourceParam + "=" + resources}<#else>${""}</#if>" name="updateWin" style="width:20px; height:20px; margin: 0px;" marginwidth="0" 
            marginheight="0" frameborder="0" framespacing="0" scrolling="no" class='hide'></iframe>
  </td></tr>
</table>

</form>

    ${StringUtil.wrapString(report.dialogEnd())}

</#switch>
<#-- //////////////////// end of switch statement --> 
