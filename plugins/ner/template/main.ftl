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
<div class="container-fluid bkg-ner">
  <div>
    <p class="text-center padding-top-20 padding-bottom-5">
      <button class="btn btn-ner-primary-outline btn-sm m-xl padding-right-20 padding-left-20" type="button" onclick="javascript: document.location='/ner/control/addDictWords';">${uiLabelMap.NerAddDictWords}</button>
      <button class="btn btn-ner-primary-outline btn-sm m-xl" type="button" onclick="javascript: document.location='/ner/control/addMLWords';">${uiLabelMap.NerAddMLWords}</button>
    </p>
  </div>

  <div class="ner-box" id="logindev">
      <form class="form col-lg-5 col-lg-offset-0 col-sm-5 col-sm-offset-0 col-xs-2 col-xs-offset-0" method="post" action="<@ofbizUrl>parseSentences</@ofbizUrl>" name="sourceform">
      	<div class="f-ti hidden-xs">${uiLabelMap.SourceSentences}</div>
      	<textarea id="sentences" name="sentences" rows="34" cols="60" maxlength="5000">${request.getAttribute("sentences")!}</textarea>
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
      	<textarea id="parsedResults" name="parsedResults" rows="34" cols="60"><#if parseResults?exists && parseResults?has_content && parseResults.size() gt 1><#list parseResults as result>${result + "\n"}</#list></#if></textarea>
      </div>
      <div class="col-lg-6 col-lg-offset-0 col-sm-6 col-sm-offset-0 col-xs-2 col-xs-offset-0">
        <div id="counter" class="text-left">0/5000</div>
        <div class="dictionaryrobot">
          <img class="padding-left-30" src="/ner/images/dictionaryrobot.png" alt="">
        </div>
      </div>
      <div class="col-lg-6 col-lg-offset-0 col-sm-6 col-sm-offset-0 col-xs-2 col-xs-offset-0">
        <div class="resultrobot">
          <image class="padding-left-30" src="/ner/images/resultrobot.png">
        </div>
      </div>
  </div>
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
