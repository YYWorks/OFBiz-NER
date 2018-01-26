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
<#assign tokenizedResults = request.getAttribute("tokenizedResults")!>
<#assign addWordsResults = request.getAttribute("addWordsResults")!>
<div class="container-fluid bkg-ner">
  <div>
    <p class="text-center padding-top-20 padding-bottom-5">
      <button class="btn btn-ner-primary-outline btn-sm m-xl padding-right-20 padding-left-20" type="button" onclick="javascript: document.location='/ner/control/main';">${uiLabelMap.NerNLP}</button>
<#if security.hasPermission("NERMANAGE_VIEW", session) || security.hasPermission("NERMANAGE_ADMIN", session)>
      <button class="btn btn-ner-primary-outline btn-sm m-xl" type="button" onclick="javascript: document.location='/ner/control/addMLWords';">${uiLabelMap.NerAddMLWords}</button>
</#if>
    </p>
  </div>
<#if security.hasPermission("NERMANAGE_VIEW", session) || security.hasPermission("NERMANAGE_ADMIN", session)>
  <div class="ner-words-box" id="logindev">
      <form class="form col-lg-5 col-lg-offset-0 col-sm-5 col-sm-offset-0 col-xs-2 col-xs-offset-0" method="post" action="<@ofbizUrl>addToDictionary</@ofbizUrl>" name="dictionaryform">
      	<div class="f-ti hidden-xs">${uiLabelMap.SourceWords}</div>
      	<textarea id="words" name="words" rows="18" cols="60" maxlength="5000">${request.getAttribute("words")!}</textarea>
      </form>
      <div id="nerrobot" class="col-lg-2 col-lg-offset-0 col-sm-2 col-sm-offset-0 col-xs-1 col-xs-offset-0 padding-top-100">
          <button class="margin-left-30" type="button" onclick="document.forms['dictionaryform'].submit();">
              <img src="/ner/images/addbutton.png" alt="${uiLabelMap.AddWordsToDictionary}">
          </button>
      </div>
      <div class="col-lg-5 col-lg-offset-0 col-sm-5 col-sm-offset-0 col-xs-2 col-xs-offset-0 padding-top-15">
        <#if addWordsResults?exists && addWordsResults?has_content && addWordsResults.size() gt 0>
          <textarea id="addWordsResults" name="addWordsResults" rows="18" cols="60"><#list addWordsResults as result>${result + "\n"}</#list></textarea>
        <#else>
          &nbsp;
        </#if>
      </div>
  </div>
  <div class="ner-sentences-box" id="logindev">
      <form class="form col-lg-5 col-lg-offset-0 col-sm-5 col-sm-offset-0 col-xs-2 col-xs-offset-0" method="post" action="<@ofbizUrl>tokenizeSentences</@ofbizUrl>" name="sourceform">
      	<div class="f-ti hidden-xs">${uiLabelMap.SourceSentences}</div>
      	<textarea id="sentences" name="sentences" rows="18" cols="60" maxlength="5000">${request.getAttribute("sentences")!}</textarea>
      </form>
      <div id="nerrobot" class="col-lg-2 col-lg-offset-0 col-sm-2 col-sm-offset-0 col-xs-1 col-xs-offset-0 padding-top-100">
          <button class="margin-left-30" type="button" onclick="document.forms['sourceform'].submit();">
              <img src="/ner/images/tokenizebutton.png" alt="">
          </button>
      </div>
      <div class="col-lg-5 col-lg-offset-0 col-sm-5 col-sm-offset-0 col-xs-2 col-xs-offset-0">
      	<div class="f-ti hidden-xs">
          ${uiLabelMap.TokenizedResults}
        </div>
      	<textarea id="tokenizedResults" name="tokenizedResults" rows="18" cols="60"><#if tokenizedResults?exists && tokenizedResults?has_content && tokenizedResults.size() gt 0><#list tokenizedResults as result>${result + "\n"}</#list></#if></textarea>
      </div>
  </div>
<#else>
  <div class="ner-box" id="logindev">
  <#if !userLogin?exists>
    ${uiLabelMap.PleaseLoginFirst}
  <#else>
    ${uiLabelMap.MustHasNERPermission}
  </#if>
  </div>
</#if>
</div>
<script language="javascript">
$(document).ready(function(){
    $("textarea#sentences").attr("placeholder", "${StringUtil.wrapString(uiLabelMap.SourceSentencesSample)}");
    $("textarea#sentences").on("keydown keyup blur cut paste", function() {
        var length = $("textarea#sentences").val().length;
        if (length > 5000) {
            return false;
        }
        $("div#counter").html(length + "/5000");
    });
})
</script>
