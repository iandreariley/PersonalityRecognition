package io.personalityrecognition;

import io.personalityrecognition.util.DataShaper;
import io.personalityrecognition.util.PCADataWriter;
import io.personalityrecognition.util.PersonalityData;
import io.personalityrecognition.util.PersonalityDataReader;
import io.personalityrecognition.util.PersonalityDataWriter;
import Jama.Matrix;

import com.mkobos.pca_transform.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.RBFNetwork;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.PerceptronLearning;
import org.neuroph.nnet.learning.RBFLearning;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;

import static io.personalityrecognition.TestRunner.*;

public class PersonalityRecognition {
	
	private static final String[] TRAITS = new String[] { "Extraverion", "Neuroticism", "Openness", "Agreeableness", "Conscientiousness" };
	private static final int TRAIT_COUNT = 5;
	private static final double TRAINING_RATIO = .8;
	private static final String TRAINING_FILE = "essay_train.csv";
	private static final String TEST_FILE = "pca_data_test.csv";
	private static List<String> WORDS;

	public static void main(String args[]) {
		try {
			NeuralNetwork nn = NeuralNetwork.createFromFile("facebook_pca_RBF.nnet");
			TestRunner tr = new TestRunner(nn, TEST_FILE);
			printResults(tr.runPCATest());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static RBFNetwork newRBFNetwork(int ins, int hidden, int outs, int iterations) {
		RBFNetwork nn = new RBFNetwork(ins, hidden, outs);
		RBFLearning learningRule = (RBFLearning) nn.getLearningRule();
		learningRule.setLearningRate(.2);
		learningRule.setMaxIterations(iterations);
		nn.setLearningRule(learningRule);
		return nn;
	}
	
	private static Perceptron newPerceptronNetwork(int ins, int outs, int iterations) {
		Perceptron nn = new Perceptron(ins, outs);
		PerceptronLearning learningRule = (PerceptronLearning) nn.getLearningRule();
		learningRule.setLearningRate(.2);
		learningRule.setMaxIterations(iterations);
		nn.setLearningRule(learningRule);
		return nn;
	}
	
	private static MultiLayerPerceptron newMultiLayerPerceptronNetwork(int ins, int hidden,  int outs, int iterations) {
		MultiLayerPerceptron nn = new MultiLayerPerceptron(ins, hidden, outs);
		BackPropagation learningRule = (BackPropagation) nn.getLearningRule();
		learningRule.setLearningRate(.2);
		learningRule.setMaxIterations(iterations);
		nn.setLearningRule(learningRule);
		return nn;
	} 
	
	private static Map<String, Map<String, Double>> trainAndTestNetwork(NeuralNetwork nn, String outputFile) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		Trainer.train(nn, TRAINING_FILE, WORDS, outputFile);
		return new TestRunner(nn, TEST_FILE).runWordFrequencyTest(WORDS);
		
	}
	
	private static void printResults(Map<String, Map<String, Double>> results) {
		for(String trait : results.keySet()) {
			Map<String, Double> traitResults = results.get(trait);
			System.out.println(trait + ": ");
			for(String result : traitResults.keySet()) {
				System.out.println(result + ": " + traitResults.get(result));
			}
		}
	}
	
	private static void getWordOrder(String filename) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		HashMap<String, PersonalityData> map = PersonalityDataReader.readPersonalityData(filename);
		List<Map.Entry<String, PersonalityData>> list = new LinkedList<>(map.entrySet());
		PersonalityData firstRow = list.get(0).getValue();
		WORDS = new LinkedList<>(firstRow.getWordFrequencies().keySet());
		Collections.sort(WORDS);
	}
}
