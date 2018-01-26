/*******************************************************************************
 * Licensed to YYWorks Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package cn.yyworks.nlp.ner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.FileUtil;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.lucene.IKTokenizer;

public class NerServices {
    
	public static final String module = NerServices.class.getName();
    
	public static final String resource = "NerUiLabels";
	
    public static Map<String, Object> nerParseContent(DispatchContext dctx, Map<String, ?> context) {
        String content = (String) context.get("content");
		StringReader reader = new StringReader(content);
		IKTokenizer tokenizer = new IKTokenizer(reader, true);
        Map<String, Object> results = ServiceUtil.returnSuccess();
		try {
			tokenizer.reset();
	        CharTermAttribute term = tokenizer.getAttribute(CharTermAttribute.class);
	        String output = "";
	        while(tokenizer.incrementToken()) {
	        	output += term.toString() + " ";
	        }
	        
	        OFBizCRFClassifierContainer mcc = OFBizCRFClassifierContainer.getInstance();
			try {
		        mcc.reset();
			} catch (IOException e) {
				// do nothing
			}
	        results.putAll(mcc.classifyFirstSentence(output));
		} catch (IOException e1) {
			return ServiceUtil.returnError(e1.getMessage());
		} finally {
	        try {
				tokenizer.close();
			} catch (IOException e) {
				// do nothing
			}
	        reader.close();
		}
        return results;
    }

    public static Map<String, Object> nerParseSentences(DispatchContext dctx, Map<String, ?> context) {
    	Locale locale = (Locale) context.get("locale");
        String content = (String) context.get("sentences");
        content = content.replaceAll("\\r", "");
        String[] sentences = content.split("\\n");
        List<String> parseResults = new ArrayList<String>();
        Map<String, Object> results = ServiceUtil.returnSuccess();
        OFBizCRFClassifierContainer mcc = OFBizCRFClassifierContainer.getInstance();
		try {
	        mcc.reset();
		} catch (IOException e) {
			// do nothing
		}
		String outputHeader = UtilProperties.getMessage(resource, "BrandColumn", locale) + "," +
				UtilProperties.getMessage(resource, "StyleNumberColumn", locale) + "," +
				UtilProperties.getMessage(resource, "ProductNameColumn", locale) + "," +
				UtilProperties.getMessage(resource, "ColorColumn", locale) + "," +
				UtilProperties.getMessage(resource, "SizeColumn", locale) + "," +
				UtilProperties.getMessage(resource, "TypeColumn", locale) + "," +
				UtilProperties.getMessage(resource, "OtherColumn", locale);
		parseResults.add(outputHeader);

    	Pattern p = Pattern.compile(".*\\d+.*");
		for (String sentence : sentences) {
    		StringReader reader = new StringReader(sentence);
    		IKTokenizer tokenizer = new IKTokenizer(reader, true);
    		try {
    			tokenizer.reset();
    	        CharTermAttribute term = tokenizer.getAttribute(CharTermAttribute.class);
    	        String output = "";
    	        while(tokenizer.incrementToken()) {
    	        	String termString  = term.toString();
    	        	Matcher m = p.matcher(termString);
    	        	if ((termString.indexOf("-") != -1 || termString.indexOf("_") != -1) && m.matches()) {
    	        		String[] words = termString.split("\\-|\\_");
    	        		for (int i = 0; i < words.length; i++) {
        	        		output += words[i] + " ";
        	        	}
    	        	}
    	        	output += termString + " ";
    	        }
    	        String sentenceResult = mcc.classifyFirstSentenceAsString(output);
    	        parseResults.add(sentenceResult);
    		} catch (IOException e1) {
    			return ServiceUtil.returnError(e1.getMessage());
    		} finally {
    	        try {
    				tokenizer.close();
    			} catch (IOException e) {
    				// do nothing
    			}
    	        reader.close();
    		}
        }
		results.put("sentences", content);
		results.put("parseResults", parseResults);
        return results;
    }

    public static Map<String, Object> nerTokenizeSentences(DispatchContext dctx, Map<String, ?> context) {
        String content = (String) context.get("sentences");
        content = content.replaceAll("\\r", "");
        String[] sentences = content.split("\\n");
        List<String> tokenizedResults = new ArrayList<String>();
        Map<String, Object> results = ServiceUtil.returnSuccess();

    	Pattern p = Pattern.compile(".*\\d+.*");
		for (String sentence : sentences) {
    		StringReader reader = new StringReader(sentence);
    		IKTokenizer tokenizer = new IKTokenizer(reader, true);
    		try {
    			tokenizer.reset();
    	        CharTermAttribute term = tokenizer.getAttribute(CharTermAttribute.class);
    	        String output = "";
    	        while(tokenizer.incrementToken()) {
    	        	String termString  = term.toString();
    	        	Matcher m = p.matcher(termString);
    	        	if ((termString.indexOf("-") != -1 || termString.indexOf("_") != -1) && m.matches()) {
    	        		String[] words = termString.split("\\-|\\_");
    	        		for (int i = 0; i < words.length; i++) {
        	        		output += words[i] + " ";
        	        	}
    	        	}
    	        	output += termString + " ";
    	        }
    	        tokenizedResults.add(output);
    		} catch (IOException e1) {
    			return ServiceUtil.returnError(e1.getMessage());
    		} finally {
    	        try {
    				tokenizer.close();
    			} catch (IOException e) {
    				// do nothing
    			}
    	        reader.close();
    		}
        }
		results.put("sentences", content);
		results.put("tokenizedResults", tokenizedResults);
        return results;
    }

    public static Map<String, Object> nerAddWordsToDictionary(DispatchContext dctx, Map<String, ?> context) {
    	Locale locale = (Locale) context.get("locale");
        String content = (String) context.get("words");
        content = content.replaceAll("\\r", "");
        String[] sentences = content.split("\\n");
        List<String> addWordsResults = new ArrayList<String>();
        Map<String, Object> results = ServiceUtil.returnSuccess();
        Set<String> wordsToDict = new HashSet<String>();
        // 1. construct words list
		for (String sentence : sentences) {
    		String[] words = sentence.split(" |,|，");
    		for (int i = 0; i < words.length; i++) {
        		String word = words[i];
        		if (UtilValidate.isNotEmpty(word) && UtilValidate.isNotEmpty(word.trim())) {
        			wordsToDict.add(word.trim());
        		}
        	}
        }
		addWordsResults.add(UtilProperties.getMessage(resource, "AddingWordsToDictionary", new Object[] { String.valueOf(wordsToDict.size()) }, locale));
        
        // 2. append the words to the 1st user dictionary
        Configuration config = DefaultConfig.getInstance();
        Dictionary dictionary = null;
        try {
        	dictionary = Dictionary.getSingleton();
        } catch (IllegalStateException e) {
        	dictionary = Dictionary.initial(config);
        }
		List<String> extDictFiles  = config.getExtDictionarys();
		if (extDictFiles != null) {
			for (String extDictName : extDictFiles) {
				// only try files in file system as they are writable
				File file = FileUtil.getFile(extDictName);
				if (file == null) {
					continue;
				}
				try (FileWriter fw = new FileWriter(file, true);
					    BufferedWriter bw = new BufferedWriter(fw);
					    PrintWriter out = new PrintWriter(bw)) {
					for (String word : wordsToDict) {
						out.println(word);
					}
					out.flush();
					bw.flush();
					fw.flush();
					out.close();
					bw.close();
					fw.close();
					addWordsResults.add(UtilProperties.getMessage(resource, "WordsAddedToDictionary", new Object[] { extDictName }, locale));
					break;
				} catch (IOException e) {
				    //exception handling left as an exercise for the reader
					Debug.logError(e, module);
					addWordsResults.add(UtilProperties.getMessage(resource, "FailedAddingWordsToDictionary", new Object[] { extDictName }, locale));
				}
			}
		}		
        
        // 3. call addWords
        dictionary.addWords(wordsToDict);
		addWordsResults.add(UtilProperties.getMessage(resource, "WordsAddedAndWorked", locale));
        
		results.put("addWordsResults", addWordsResults);
        return results;
    }
}
