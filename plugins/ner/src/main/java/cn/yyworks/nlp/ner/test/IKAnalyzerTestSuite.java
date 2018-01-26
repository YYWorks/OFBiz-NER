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
package cn.yyworks.nlp.ner.test;

import java.io.StringReader;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.ofbiz.base.util.Debug;
import org.wltea.analyzer.lucene.IKTokenizer;

import junit.framework.TestCase;

public class IKAnalyzerTestSuite extends TestCase {
	
    public static final String module = IKAnalyzerTestSuite.class.getName();
    
	public void testIKAnalyzerParser() throws Exception {
		String str = "KAILAS KG12165 暗橙红M 女款三合一冲锋衣";
		StringReader reader = new StringReader(str);
		IKTokenizer tokenizer = new IKTokenizer(reader, true);
		tokenizer.reset();
        CharTermAttribute term = tokenizer.getAttribute(CharTermAttribute.class); 
        Debug.logInfo(str, module);
        String output = "";
        while(tokenizer.incrementToken()) {
        	output += term.toString() + ", ";
        }
        Debug.logInfo(output, module);
        tokenizer.close();
	}

	public void testIKAnalyzerDictionarySingleton() throws Exception {
		String str = "KAILAS KG12165 暗橙红M 女款三合一冲锋衣";
		StringReader reader = new StringReader(str);
		IKTokenizer tokenizer = new IKTokenizer(reader, true);
		tokenizer.reset();
        CharTermAttribute term = tokenizer.getAttribute(CharTermAttribute.class);
        Debug.logInfo(str, module);
        String output = "";
        while(tokenizer.incrementToken()) {
        	output += term.toString() + ", ";
        }
        Debug.logInfo(output, module);
        tokenizer.close();
	}
}