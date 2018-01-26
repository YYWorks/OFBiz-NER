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
<#assign parseResults = request.getAttribute("parseResults")!>
<#assign addWordsResults = request.getAttribute("addWordsResults")!>
<div class="container-fluid bkg-ner">
  <div>
    <p class="text-center padding-top-20 padding-bottom-5">
      <button class="btn btn-ner-primary-outline btn-sm m-xl padding-right-20 padding-left-20" type="button" onclick="javascript: document.location='/ner/control/main';">${uiLabelMap.NerNLP}</button>
<#if security.hasPermission("NERMANAGE_VIEW", session) || security.hasPermission("NERMANAGE_ADMIN", session)>
      <button class="btn btn-ner-primary-outline btn-sm m-xl" type="button" onclick="javascript: document.location='/ner/control/addDictWords';">${uiLabelMap.NerAddMLWords}</button>
</#if>
    </p>
  </div>
<#if security.hasPermission("NERMANAGE_VIEW", session) || security.hasPermission("NERMANAGE_ADMIN", session)>
  <div class="ner-words-box" id="logindev">
      <form class="form col-lg-5 col-lg-offset-0 col-sm-5 col-sm-offset-0 col-xs-2 col-xs-offset-0" method="post" action="<@ofbizUrl>addAndTrain</@ofbizUrl>" name="dictionaryform">
      	<div class="f-ti hidden-xs">${uiLabelMap.TrainingWords}</div>
      	<textarea id="words" name="words" rows="18" cols="60" maxlength="5000">${request.getAttribute("words")!}</textarea>
      </form>
      <div id="nerrobot" class="col-lg-2 col-lg-offset-0 col-sm-2 col-sm-offset-0 col-xs-1 col-xs-offset-0 padding-top-100">
          <button class="margin-left-30" type="button" onclick="document.forms['dictionaryform'].submit();">
              <img src="/ner/images/trainingbutton.png" alt="${uiLabelMap.MLAddAndTraining}">
          </button>
      </div>
      <div class="col-lg-5 col-lg-offset-0 col-sm-5 col-sm-offset-0 col-xs-2 col-xs-offset-0">
    <#assign report = Static["cn.yyworks.nlp.report.NerHtmlReport"].getReport(request, response)>
    ${report.prepareDisplayReport(request, response, "ner_training", "/ner/control/addAndTrain")}
    <#include "component://ner/template/report.ftl">
      </div>
  </div>
  <div class="ner-sentences-box" id="logindev">
      <form class="form col-lg-5 col-lg-offset-0 col-sm-5 col-sm-offset-0 col-xs-2 col-xs-offset-0" method="post" action="<@ofbizUrl>parseSentencesML</@ofbizUrl>" name="sourceform">
      	<div class="f-ti hidden-xs">${uiLabelMap.SourceSentences}</div>
      	<textarea id="sentences" name="sentences" rows="18" cols="60" maxlength="5000">${request.getAttribute("sentences")!}</textarea>
      </form>
      <div id="nerrobot" class="col-lg-2 col-lg-offset-0 col-sm-2 col-sm-offset-0 col-xs-1 col-xs-offset-0 padding-top-100">
          <button class="margin-left-30" type="button" onclick="document.forms['sourceform'].submit();">
              <img src="/ner/images/analyzebutton.png" alt="">
          </button>
      </div>
      <div class="col-lg-5 col-lg-offset-0 col-sm-5 col-sm-offset-0 col-xs-2 col-xs-offset-0">
      	<div class="f-ti hidden-xs">
          ${uiLabelMap.ParsedResults}
        </div>
      	<textarea id="parseResults" name="parseResults" rows="18" cols="60"><#if parseResults?exists && parseResults?has_content && parseResults.size() gt 0><#list parseResults as result>${result + "\n"}</#list></#if></textarea>
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
