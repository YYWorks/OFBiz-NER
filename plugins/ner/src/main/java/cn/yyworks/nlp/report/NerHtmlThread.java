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
package cn.yyworks.nlp.report;

import cn.yyworks.nlp.ner.OFBizCRFClassifierContainer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ofbiz.htmlreport.AbstractReportThread;
import org.apache.ofbiz.htmlreport.InterfaceReport;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.FileUtil;
import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.service.LocalDispatcher;

/**
 * Thread for running Ner training html report.
 * 
 */
public class NerHtmlThread extends AbstractReportThread {
	
	public static final String module = NerHtmlThread.class.getName();

	public static final String NER_TRAINING = "ner_training";
	
	public static final String CONFIRM = "confirm_action";
	
	public static final String[] messageLabels = new String[] {"FORMAT_DEFAULT", "FORMAT_WARNING", "FORMAT_HEADLINE", "FORMAT_NOTE", "FORMAT_OK", "FORMAT_ERROR", "FORMAT_THROWABLE"};
	
	public static final List<String> messages = Collections.unmodifiableList(Arrays.asList(messageLabels));
	
	private LocalDispatcher dispatcher;
	
	private Delegator delegator;
	
    public static final String resource = "NerUiLabels";
    
    private String words;
    
    private Locale locale;
    
    private OFBizCRFClassifierContainer classifierContainer;
    
	/**
     * Constructor, creates a new html thread.
	 * 
	 * @param request
	 * @param response
	 * @param name
	 */
    public NerHtmlThread(HttpServletRequest request, HttpServletResponse response, String name) {

        super(request, response, name);
        initHtmlReport(request, response);
		dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		setDelegator(dispatcher.getDelegator());
		words = request.getParameter("words");
		locale = UtilHttp.getLocale(request);
    }

    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        try {
            if (getName().startsWith(NER_TRAINING)) {
    			getReport().println();
            	getReport().println(UtilProperties.getMessage(resource, "StartAddingWordsToML", getLocale()), InterfaceReport.FORMAT_HEADLINE);
            	storeWordsToTrainDataFile();
            	getReport().println(UtilProperties.getMessage(resource, "StartMLTraining", getLocale()), InterfaceReport.FORMAT_HEADLINE);
            	trainMLModel();
            	getReport().println(UtilProperties.getMessage(resource, "MLTrainingCompleted", locale), InterfaceReport.FORMAT_HEADLINE);
    			try {
    				classifierContainer.reset();
    				getReport().println(UtilProperties.getMessage(resource, "NewClassifierWorked", locale), InterfaceReport.FORMAT_HEADLINE);
    			} catch (IOException e) {
    				Debug.logError(e, module);
    				getReport().println(UtilProperties.getMessage(resource, "NewClassifierFailedToWork", locale), InterfaceReport.FORMAT_ERROR);
    			}
        	} else {
            	getReport().println(getName(), InterfaceReport.FORMAT_ERROR);
            	Debug.logError(getName(), module);
            }
        } catch (Exception e) {
        	getReport().println(e);
            if (Debug.errorOn()) {
                Debug.log(e);
            }
        }
    }

	private void trainMLModel() {
		classifierContainer = OFBizCRFClassifierContainer.getInstance();
		classifierContainer.train(getReport());
	}

	private void storeWordsToTrainDataFile() {
		File file = FileUtil.getFile(OFBizCRFClassifierContainer.TrainDataFile);
		if (file == null) {
			getReport().println(UtilProperties.getMessage(resource, "CannotFindTrainDataFile", new Object[] { OFBizCRFClassifierContainer.TrainDataFile }, locale), InterfaceReport.FORMAT_ERROR);
			return;
		}
		try (FileWriter fw = new FileWriter(file, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw)) {
			out.println();
			out.println(words);
			out.flush();
			bw.flush();
			fw.flush();
			out.close();
			bw.close();
			fw.close();
			getReport().println(UtilProperties.getMessage(resource, "WordsAddedToTrainData", new Object[] { OFBizCRFClassifierContainer.TrainDataFile }, locale));
		} catch (IOException e) {
		    //exception handling left as an exercise for the reader
			Debug.logError(e, module);
			getReport().println(UtilProperties.getMessage(resource, "FailedAddingWordsToTrainData", new Object[] { OFBizCRFClassifierContainer.TrainDataFile }, locale), InterfaceReport.FORMAT_ERROR);
		}
	}

	public Delegator getDelegator() {
		return delegator;
	}

	public void setDelegator(Delegator delegator) {
		this.delegator = delegator;
	}
}
