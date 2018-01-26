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
package edu.stanford.nlp.ie.crf;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifierEvaluator;
import edu.stanford.nlp.ie.crf.CRFDatum;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.optimization.DiffFunction;
import edu.stanford.nlp.optimization.Evaluator;
import edu.stanford.nlp.optimization.Function;
import edu.stanford.nlp.optimization.HasEvaluators;
import edu.stanford.nlp.optimization.HybridMinimizer;
import edu.stanford.nlp.optimization.InefficientSGDMinimizer;
import edu.stanford.nlp.optimization.MemoryEvaluator;
import edu.stanford.nlp.optimization.OFBizQNMinimizer;
import edu.stanford.nlp.optimization.Minimizer;
import edu.stanford.nlp.optimization.QNMinimizer;
import edu.stanford.nlp.optimization.ResultStoringMonitor;
import edu.stanford.nlp.optimization.SGDMinimizer;
import edu.stanford.nlp.optimization.SGDToQNMinimizer;
import edu.stanford.nlp.optimization.SGDWithAdaGradAndFOBOS;
import edu.stanford.nlp.optimization.SMDMinimizer;
import edu.stanford.nlp.optimization.ScaledSGDMinimizer;
import edu.stanford.nlp.sequences.*;
import edu.stanford.nlp.util.*;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

import org.apache.ofbiz.htmlreport.InterfaceReport;

/**
 * OFBiz CRF classifier which can output messages to html report during training.
 * 
 */
public class OFBizCRFClassifier<IN extends CoreMap> extends CRFClassifier<IN> {
	
	public static final String module = OFBizCRFClassifier.class.getName();

    public static final String resource = "NerUiLabels";
    
    private InterfaceReport report;
    
	protected OFBizCRFClassifier() {
        super();
    }

	public OFBizCRFClassifier(Properties props) {
        super(props);
    }

	public OFBizCRFClassifier(SeqClassifierFlags flags) {
        super(flags);
    }

	@SuppressWarnings("unchecked")
    public void train(File file, InterfaceReport report) {
		Collection<List<IN>> objectBankWrapper = makeObjectBankFromFile(file.getAbsolutePath());
		this.report = report;
		
		Timing timer = new Timing();
		timer.start();

		Collection<List<IN>> docs = new ArrayList<List<IN>>();
		for (List<IN> doc : objectBankWrapper) {
			docs.add(doc);
		}

		if (flags.numOfSlices > 0) {
			System.err.println(
					"Taking " + flags.numOfSlices + " out of " + flags.totalDataSlice + " slices of data for training");
			List<List<IN>> docsToShuffle = new ArrayList<List<IN>>();
			for (List<IN> doc : docs) {
				docsToShuffle.add(doc);
			}
			Collections.shuffle(docsToShuffle, random);
			int cutOff = (int) (docsToShuffle.size() / (flags.totalDataSlice + 0.0) * flags.numOfSlices);
			docs = docsToShuffle.subList(0, cutOff);
		}

		Collection<List<IN>> totalDocs = loadAuxiliaryData(docs, defaultReaderAndWriter());

		makeAnswerArraysAndTagIndex(totalDocs);

		long elapsedMs = timer.stop();
		System.err
				.println("Time to convert docs to feature indices: " + Timing.toSecondsString(elapsedMs) + " seconds");

		if (flags.serializeClassIndexTo != null) {
			timer.start();
			serializeClassIndex(flags.serializeClassIndexTo);
			elapsedMs = timer.stop();
			System.err.println("Time to export class index : " + Timing.toSecondsString(elapsedMs) + " seconds");
		}

		if (flags.exportFeatures != null) {
			dumpFeatures(docs);
		}

		for (int i = 0; i <= flags.numTimesPruneFeatures; i++) {
			timer.start();
			Triple<int[][][][], int[][], double[][][][]> dataAndLabelsAndFeatureVals = documentsToDataAndLabels(docs);
			elapsedMs = timer.stop();
			System.err
					.println("Time to convert docs to data/labels: " + Timing.toSecondsString(elapsedMs) + " seconds");

			Evaluator[] evaluators = null;
			if (flags.evaluateIters > 0 || flags.terminateOnEvalImprovement) {
				List<Evaluator> evaluatorList = new ArrayList<Evaluator>();
				if (flags.useMemoryEvaluator)
					evaluatorList.add(new MemoryEvaluator());
				if (flags.evaluateTrain) {
					CRFClassifierEvaluator<IN> crfEvaluator = new CRFClassifierEvaluator<IN>("Train set", this);
					List<Triple<int[][][], int[], double[][][]>> trainDataAndLabels = new ArrayList<Triple<int[][][], int[], double[][][]>>();
					int[][][][] data = dataAndLabelsAndFeatureVals.first();
					int[][] labels = dataAndLabelsAndFeatureVals.second();
					double[][][][] featureVal = dataAndLabelsAndFeatureVals.third();
					for (int j = 0; j < data.length; j++) {
						Triple<int[][][], int[], double[][][]> p = new Triple<int[][][], int[], double[][][]>(data[j],
								labels[j], featureVal[j]);
						trainDataAndLabels.add(p);
					}
					crfEvaluator.setTestData(docs, trainDataAndLabels);
					if (flags.evalCmd.length() > 0)
						crfEvaluator.setEvalCmd(flags.evalCmd);
					evaluatorList.add(crfEvaluator);
				}
				if (flags.testFile != null) {
					CRFClassifierEvaluator<IN> crfEvaluator = new CRFClassifierEvaluator<IN>(
							"Test set (" + flags.testFile + ")", this);
					ObjectBank<List<IN>> testObjBank = makeObjectBankFromFile(flags.testFile, defaultReaderAndWriter());
					List<List<IN>> testDocs = new ArrayList<List<IN>>();
					for (List<IN> doc : testObjBank) {
						testDocs.add(doc);
					}
					List<Triple<int[][][], int[], double[][][]>> testDataAndLabels = documentsToDataAndLabelsList(
							testDocs);
					crfEvaluator.setTestData(testDocs, testDataAndLabels);
					if (flags.evalCmd.length() > 0)
						crfEvaluator.setEvalCmd(flags.evalCmd);
					evaluatorList.add(crfEvaluator);
				}
				if (flags.testFiles != null) {
					String[] testFiles = flags.testFiles.split(",");
					for (String testFile : testFiles) {
						CRFClassifierEvaluator<IN> crfEvaluator = new CRFClassifierEvaluator<IN>(
								"Test set (" + testFile + ")", this);
						ObjectBank<List<IN>> testObjBank = makeObjectBankFromFile(testFile, defaultReaderAndWriter());
						List<Triple<int[][][], int[], double[][][]>> testDataAndLabels = documentsToDataAndLabelsList(
								testObjBank);
						crfEvaluator.setTestData(testObjBank, testDataAndLabels);
						if (flags.evalCmd.length() > 0)
							crfEvaluator.setEvalCmd(flags.evalCmd);
						evaluatorList.add(crfEvaluator);
					}
				}
				evaluators = new Evaluator[evaluatorList.size()];
				evaluatorList.toArray(evaluators);
			}

			if (flags.numTimesPruneFeatures == i) {
				docs = null; // hopefully saves memory
			}
			// save feature index to disk and read in later
			File featIndexFile = null;

			// CRFLogConditionalObjectiveFunction.featureIndex = featureIndex;
			// int numFeatures = featureIndex.size();
			if (flags.saveFeatureIndexToDisk) {
				try {
					System.err.println("Writing feature index to temporary file.");
					featIndexFile = IOUtils.writeObjectToTempFile(featureIndex, "featIndex" + i + ".tmp");
					// featureIndex = null;
				} catch (IOException e) {
					throw new RuntimeException("Could not open temporary feature index file for writing.");
				}
			}

			// first index is the number of the document
			// second index is position in the document also the index of the
			// clique/factor table
			// third index is the number of elements in the clique/window thase
			// features are for (starting with last element)
			// fourth index is position of the feature in the array that holds
			// them
			// element in data[i][j][k][m] is the index of the mth feature
			// occurring
			// in position k of the jth clique of the ith document
			int[][][][] data = dataAndLabelsAndFeatureVals.first();
			// first index is the number of the document
			// second index is the position in the document
			// element in labels[i][j] is the index of the correct label (if it
			// exists) at position j in document i
			int[][] labels = dataAndLabelsAndFeatureVals.second();
			double[][][][] featureVals = dataAndLabelsAndFeatureVals.third();

			if (flags.loadProcessedData != null) {
				List<List<CRFDatum<Collection<String>, String>>> processedData = loadProcessedData(
						flags.loadProcessedData);
				if (processedData != null) {
					// enlarge the data and labels array
					int[][][][] allData = new int[data.length + processedData.size()][][][];
					double[][][][] allFeatureVals = new double[featureVals.length + processedData.size()][][][];
					int[][] allLabels = new int[labels.length + processedData.size()][];
					System.arraycopy(data, 0, allData, 0, data.length);
					System.arraycopy(labels, 0, allLabels, 0, labels.length);
					System.arraycopy(featureVals, 0, allFeatureVals, 0, featureVals.length);
					// add to the data and labels array
					addProcessedData(processedData, allData, allLabels, allFeatureVals, data.length);
					data = allData;
					labels = allLabels;
					featureVals = allFeatureVals;
				}
			}

			double[] oneDimWeights = trainWeights(data, labels, evaluators, i, featureVals);
			if (oneDimWeights != null) {
				this.weights = to2D(oneDimWeights, labelIndices, map);
			}

			// if (flags.useFloat) {
			// oneDimWeights = trainWeightsUsingFloatCRF(data, labels,
			// evaluators, i, featureVals);
			// } else if (flags.numLopExpert > 1) {
			// oneDimWeights = trainWeightsUsingLopCRF(data, labels, evaluators,
			// i, featureVals);
			// } else {
			// oneDimWeights = trainWeightsUsingDoubleCRF(data, labels,
			// evaluators, i, featureVals);
			// }

			// save feature index to disk and read in later
			if (flags.saveFeatureIndexToDisk) {
				try {
					System.err.println("Reading temporary feature index file.");
					featureIndex = (Index<String>) IOUtils.readObjectFromFile(featIndexFile);
				} catch (Exception e) {
					throw new RuntimeException("Could not open temporary feature index file for reading.");
				}
			}

			if (i != flags.numTimesPruneFeatures) {
				dropFeaturesBelowThreshold(flags.featureDiffThresh);
				System.err.println(
						"Removing features with weight below " + flags.featureDiffThresh + " and retraining...");
			}
		}
	}

	/**
	 * Loads a CRF classifier from a filepath, and returns it.
	 *
	 * @param file
	 *            File to load classifier from
	 * @return The CRF classifier
	 *
	 * @throws IOException
	 *             If there are problems accessing the input stream
	 * @throws ClassCastException
	 *             If there are problems interpreting the serialized data
	 * @throws ClassNotFoundException
	 *             If there are problems interpreting the serialized data
	 */
	public static <INN extends CoreMap> OFBizCRFClassifier<INN> getClassifier(File file)
			throws IOException, ClassCastException, ClassNotFoundException {
		OFBizCRFClassifier<INN> crf = new OFBizCRFClassifier<INN>();
		crf.loadClassifier(file);
		return crf;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
    public Minimizer<DiffFunction> getMinimizer(int featurePruneIteration, Evaluator[] evaluators) {
		Minimizer<DiffFunction> minimizer = null;
		OFBizQNMinimizer qnMinimizer = null;

		if (flags.useQN || flags.useSGDtoQN) {
			// share code for creation of QNMinimizer
			int qnMem;
			if (featurePruneIteration == 0) {
				qnMem = flags.QNsize;
			} else {
				qnMem = flags.QNsize2;
			}

			if (flags.interimOutputFreq != 0) {
				Function monitor = new ResultStoringMonitor(flags.interimOutputFreq, flags.serializeTo);
				qnMinimizer = new OFBizQNMinimizer(monitor, qnMem, flags.useRobustQN);
			} else {
				qnMinimizer = new OFBizQNMinimizer(qnMem, flags.useRobustQN);
			}

			qnMinimizer.setReport(report);
			qnMinimizer.terminateOnMaxItr(flags.maxQNItr);
			qnMinimizer.terminateOnEvalImprovement(flags.terminateOnEvalImprovement);
			qnMinimizer.setTerminateOnEvalImprovementNumOfEpoch(flags.terminateOnEvalImprovementNumOfEpoch);
			qnMinimizer.suppressTestPrompt(flags.suppressTestDebug);
			if (flags.useOWLQN) {
				qnMinimizer.useOWLQN(flags.useOWLQN, flags.priorLambda);
			}
		}

		if (flags.useQN) {
			minimizer = qnMinimizer;
		} else if (flags.useInPlaceSGD) {
			SGDMinimizer<DiffFunction> sgdMinimizer = new SGDMinimizer<DiffFunction>(flags.sigma, flags.SGDPasses,
					flags.tuneSampleSize, flags.stochasticBatchSize);
			if (flags.useSGDtoQN) {
				minimizer = new HybridMinimizer(sgdMinimizer, qnMinimizer, flags.SGDPasses);
			} else {
				minimizer = sgdMinimizer;
			}
		} else if (flags.useAdaGradFOBOS) {
			double lambda = 0.5 / (flags.sigma * flags.sigma);
			minimizer = new SGDWithAdaGradAndFOBOS<DiffFunction>(flags.initRate, lambda, flags.SGDPasses,
					flags.stochasticBatchSize, flags.priorType, flags.priorAlpha, flags.useAdaDelta, flags.useAdaDiff,
					flags.adaGradEps, flags.adaDeltaRho);
			((SGDWithAdaGradAndFOBOS) minimizer).terminateOnEvalImprovement(flags.terminateOnEvalImprovement);
			((SGDWithAdaGradAndFOBOS) minimizer).terminateOnAvgImprovement(flags.terminateOnAvgImprovement,
					flags.tolerance);
			((SGDWithAdaGradAndFOBOS) minimizer)
					.setTerminateOnEvalImprovementNumOfEpoch(flags.terminateOnEvalImprovementNumOfEpoch);
			((SGDWithAdaGradAndFOBOS) minimizer).suppressTestPrompt(flags.suppressTestDebug);
		} else if (flags.useSGDtoQN) {
			minimizer = new SGDToQNMinimizer(flags.initialGain, flags.stochasticBatchSize, flags.SGDPasses,
					flags.QNPasses, flags.SGD2QNhessSamples, flags.QNsize, flags.outputIterationsToFile);
		} else if (flags.useSMD) {
			minimizer = new SMDMinimizer<DiffFunction>(flags.initialGain, flags.stochasticBatchSize,
					flags.stochasticMethod, flags.SGDPasses);
		} else if (flags.useSGD) {
			minimizer = new InefficientSGDMinimizer<DiffFunction>(flags.initialGain, flags.stochasticBatchSize);
		} else if (flags.useScaledSGD) {
			minimizer = new ScaledSGDMinimizer(flags.initialGain, flags.stochasticBatchSize, flags.SGDPasses,
					flags.scaledSGDMethod);
		} else if (flags.l1reg > 0.0) {
			minimizer = ReflectionLoading.loadByReflection("edu.stanford.nlp.optimization.OWLQNMinimizer", flags.l1reg);
		} else {
			throw new RuntimeException("No minimizer assigned!");
		}

		if (minimizer instanceof HasEvaluators) {
			if (minimizer instanceof QNMinimizer) {
				((QNMinimizer) minimizer).setEvaluators(flags.evaluateIters, flags.startEvaluateIters, evaluators);
			} else
				((HasEvaluators) minimizer).setEvaluators(flags.evaluateIters, evaluators);
		}

		return minimizer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serializeClassifier(String serializePath) {
		report.print("Serializing classifier to file system ... ");

		ObjectOutputStream oos = null;
		try {
			oos = IOUtils.writeStreamFromString(serializePath);
			serializeClassifier(oos);
			report.println("done", InterfaceReport.FORMAT_OK);

		} catch (Exception e) {
			throw new RuntimeIOException("Failed to save classifier", e);
		} finally {
			IOUtils.closeIgnoringExceptions(oos);
		}
	}
}
