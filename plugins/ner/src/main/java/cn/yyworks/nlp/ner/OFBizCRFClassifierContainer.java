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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.FileUtil;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.htmlreport.InterfaceReport;

import edu.stanford.nlp.ie.crf.OFBizCRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * OFBiz CRF classifier Container.
 */
public class OFBizCRFClassifierContainer {

    public static final String module = OFBizCRFClassifierContainer.class.getName();

	/** classifier singleton */
	private static OFBizCRFClassifierContainer singletonClassifierContainer;
	
	/** austen.prop */
	public static final String AustenProperties = "plugins/ner/config/austen.prop";
	
	/** train data file */
	public static final String TrainDataFile = "runtime/ner/data/traindata.tsv";
	
	/** CRFClassifier */
	@SuppressWarnings("rawtypes")
	private OFBizCRFClassifier classifier = null;
	
	@SuppressWarnings("rawtypes")
	private OFBizCRFClassifier classifierPending = null;
	
    public static final String serializedClassifier = "runtime/ner/model/ner-model.ser.gz";
    
    public static final Map<String, String> AnnotationTypes = UtilMisc.toMap("B", "brand", "C", "color", "N", "stylenumber", "P", "productname", "S", "size", "G", "types", "O", "other");
    
    public static final Set<String> AnnotationTypeKeys = AnnotationTypes.keySet();

	private OFBizCRFClassifierContainer(){
		Debug.logInfo("Loading CRF classifier container ...", module);
		loadCRFClassifierContainer();
	}
	
	/**
	 * Get instance.
	 */
	public static OFBizCRFClassifierContainer getInstance(){
		if(singletonClassifierContainer == null){
			synchronized(OFBizCRFClassifierContainer.class){
				if(singletonClassifierContainer == null){
					singletonClassifierContainer = new OFBizCRFClassifierContainer();
					return singletonClassifierContainer;
				}
			}
		}
		return singletonClassifierContainer;
	}
	
	/**
	 * Load CRFClassifier Container.
	 */
	public void loadCRFClassifierContainer(){
		File file = FileUtil.getFile(serializedClassifier);
		try {
			classifierPending = OFBizCRFClassifier.getClassifier(file);
		} catch (ClassCastException e) {
			Debug.logError(e, module);
		} catch (ClassNotFoundException e) {
			Debug.logError(e, module);
		} catch (IOException e) {
			Debug.logError(e, module);
		}
	}
	
	public synchronized void reset() throws IOException {
		if (UtilValidate.isEmpty(classifierPending)) {
			throw new IOException("The pending classifier is empty. Please run loadCRFClassifier method first.");
		}
		classifier = classifierPending;
		classifierPending = null;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> classifyFirstSentence(String content) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<List<CoreLabel>> sentences = classifier.classify(content);
        for (List<CoreLabel> sentence : sentences) {
            for (CoreLabel word : sentence) {
            	String value = word.word();
            	if(!value.equals("-") && !value.equals("_")){
            	    String annotation = word.get(CoreAnnotations.AnswerAnnotation.class);
            	    String annotationType = "other";
            	    if (AnnotationTypeKeys.contains(annotation) && !annotation.equals("O")) {
            	    	annotationType = AnnotationTypes.get(annotation);
            	    }
            	    if (result.containsKey(annotationType)) {
            	    	Object oldValue = result.get(annotationType);
        	    		if (oldValue instanceof String) {
                	    	if (!value.equalsIgnoreCase((String) oldValue)) {
                	    		result.put(annotationType, UtilMisc.toList(oldValue, tidyString(annotationType, value)));
                	    	}
            	    	} else if (oldValue instanceof List) {
            	    		boolean foundValue = false;
            	    		for (String oldValueItem : (List<String>) oldValue) {
                    	    	if (value.equalsIgnoreCase((String) oldValueItem)) {
                    	    		foundValue = true;
                    	    		break;
                    	    	}
            	    		}
            	    		if (!foundValue) {
                	    		((List<String>) oldValue).add(tidyString(annotationType, value));
                	    		result.put(annotationType, oldValue);
            	    		}
            	    	}
            	    } else {
            	    	result.put(annotationType, tidyString(annotationType, value));
            	    }
            	}
            }
            // only parse the 1st sentence
            break;
        }
		return result;
	}

	/**
	 * Classify a string and output the result in the order:
	 * brand, stylenumber, productname, color, size, types, other
	 * 
	 * @param content
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String classifyFirstSentenceAsString(String content) {
		Map<String, Object> result = classifyFirstSentence(content);
		String output = "";
		// format brand(s)
		if (result.containsKey("brand")) {
			Object brand = result.get("brand");
			if (brand instanceof String) {
				output += (String) brand;
			} else if (brand instanceof List) {
				output += "{";
				for (String oneBrand : (List<String>) brand) {
					output += oneBrand + "|";
				}
				if (output.endsWith("|")) {
					output = output.substring(0, output.length() - 1);
				}
				output += "}";
			}
		}
		output += ",";
		
		// format stylenumber(s)
		if (result.containsKey("stylenumber")) {
			Object stylenumber = result.get("stylenumber");
			if (stylenumber instanceof String) {
				output += (String) stylenumber;
			} else if (stylenumber instanceof List) {
				output += "{";
				for (String oneNumber : (List<String>) stylenumber) {
					output += oneNumber + "|";
				}
				if (output.endsWith("|")) {
					output = output.substring(0, output.length() - 1);
				}
				output += "}";
			}
		}
		output += ",";
		
		// format productname(s)
		if (result.containsKey("productname")) {
			Object productname = result.get("productname");
			if (productname instanceof String) {
				output += (String) productname;
			} else if (productname instanceof List) {
				output += "{";
				for (String oneProduct : (List<String>) productname) {
					output += oneProduct + "|";
				}
				if (output.endsWith("|")) {
					output = output.substring(0, output.length() - 1);
				}
				output += "}";
			}
		}
		output += ",";

		// format color(s)
		if (result.containsKey("color")) {
			Object color = result.get("color");
			if (color instanceof String) {
				output += (String) color;
			} else if (color instanceof List) {
				output += "{";
				for (String oneColor : (List<String>) color) {
					output += oneColor + "|";
				}
				if (output.endsWith("|")) {
					output = output.substring(0, output.length() - 1);
				}
				output += "}";
			}
		}
		output += ",";
		
		// format size(s)
		if (result.containsKey("size")) {
			Object size = result.get("size");
			if (size instanceof String) {
				output += (String) size;
			} else if (size instanceof List) {
				output += "{";
				for (String oneSize : (List<String>) size) {
					output += oneSize + "|";
				}
				if (output.endsWith("|")) {
					output = output.substring(0, output.length() - 1);
				}
				output += "}";
			}
		}
		output += ",";
		
		// format types
		if (result.containsKey("types")) {
			Object types = result.get("types");
			if (types instanceof String) {
				output += (String) types;
			} else if (types instanceof List) {
				for (String oneType : (List<String>) types) {
					output += oneType + " ";
				}
				if (output.endsWith(" ")) {
					output = output.substring(0, output.length() - 1);
				}
			}
		}
		output += ",";
		
		// format other
		if (result.containsKey("other")) {
			Object other = result.get("other");
			if (other instanceof String) {
				output += (String) other;
			} else if (other instanceof List) {
				for (String oneOther : (List<String>) other) {
					output += oneOther + " ";
				}
				if (output.endsWith(" ")) {
					output = output.substring(0, output.length() - 1);
				}
			}
		}
		
		return output;
	}

	private String tidyString(String annotationType, String value) {
		if ("stylenumber".equals(annotationType) || "types".equals(annotationType) || "size".equals(annotationType)) {
			value = value.toUpperCase();
		} else {
			value = Character.toUpperCase(value.charAt(0)) + value.substring(1);
		}
		return value;
	}

	@SuppressWarnings("rawtypes")
	public synchronized void train(InterfaceReport report) {
		Properties props = UtilProperties.getProperties(AustenProperties);
		classifierPending = new OFBizCRFClassifier(props);
		File trainDataFile = FileUtil.getFile(TrainDataFile);
		classifierPending.train(trainDataFile, report);
        File serializedClassifierFile = FileUtil.getFile(serializedClassifier);
        classifierPending.serializeClassifier(serializedClassifierFile.getAbsolutePath());
	}
}
