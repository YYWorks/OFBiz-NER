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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.ofbiz.base.util.Debug;

import junit.framework.TestCase;

/**
 * this is for Lucene 6.1.0 test
 */
public class LuceneAnalyzerTestSuite extends TestCase {
	
    public static final String module = LuceneAnalyzerTestSuite.class.getName();

	public void testChineseTokenizer() throws Exception {
		String str = "KAILAS KG12165 暗橙红M 女款三合一冲锋衣";
		StringReader reader = new StringReader(str);
//		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);
		StandardAnalyzer analyzer = new StandardAnalyzer();
		TokenStream ts = analyzer.tokenStream("", reader);
        CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
        Debug.logInfo(str, module);
        String output = "";
        while(ts.incrementToken()) {
        	output += term.toString() + ", ";
        }
        Debug.logInfo(output, module);
        ts.close();
        analyzer.close();
	}

//	public void testHMMChineseTokenizer() throws Exception {
//		String str = "KAILAS KG12165 暗橙红M 女款三合一冲锋衣";
//		StringReader reader = new StringReader(str);
//		HMMChineseTokenizer tokenizer = new HMMChineseTokenizer();
//		tokenizer.setReader(reader);
//		tokenizer.reset();
//        CharTermAttribute term = tokenizer.getAttribute(CharTermAttribute.class); 
//        Debug.logInfo(str, module);
//        String output = "";
//        while(tokenizer.incrementToken()) {
//        	output += term.toString() + ", ";
//        }
//        Debug.logInfo(output, module);
//        tokenizer.close();
//	}

//	public void testSmartChineseAnalyzer() throws Exception {
//		String str = "KAILAS KG12165 暗橙红M 女款三合一冲锋衣";
//		SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
//		analyzer.createComponents(CharTermAttribute.class.toString());
//		TokenStream ts = analyzer.tokenStream("", str);
//		ts.reset();
//        CharTermAttribute term = ts.getAttribute(CharTermAttribute.class); 
//        Debug.logInfo(str, module);
//        String output = "";
//        while(ts.incrementToken()) {
//        	output += term.toString() + ", ";
//        }
//        Debug.logInfo(output, module);
//        analyzer.close();
//	}

}