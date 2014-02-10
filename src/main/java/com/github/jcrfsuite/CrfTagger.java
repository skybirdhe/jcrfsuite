package com.github.jcrfsuite;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.github.jcrfsuite.util.CrfSuiteLoader;
import com.github.jcrfsuite.util.Pair;

import third_party.org.chokkan.crfsuite.Attribute;
import third_party.org.chokkan.crfsuite.Item;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;
import third_party.org.chokkan.crfsuite.Tagger;

public class CrfTagger {
	
	static {
		try {
			CrfSuiteLoader.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Tagger tagger = new Tagger();
	
	public static void loadModel(String modelFile) {
		tagger.open(modelFile);
	}
	
	protected static List<ItemSequence> loadTaggingInstances(String fileName) 
			throws Exception 
	{
		List<ItemSequence> xseqs = new ArrayList<ItemSequence>();
		ItemSequence xseq = new ItemSequence();
		
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.length() > 0) {
				String[] fields = line.split("\t");
				Item item = new Item();
				for (int i = 1; i < fields.length; i++) { // field 0 is a label
					item.add(new Attribute(fields[i]));
				}
				xseq.add(item);
			} else { // end of sequence
				xseqs.add(xseq);
				xseq = new ItemSequence();
			}
		}
		br.close();
		return xseqs;
	}
	
	/**
	 * Tags a item sequence
	 */
	public static List<Pair<String, Double>> tag(ItemSequence xseq) {
		
		List<Pair<String, Double>> predicted = 
				new ArrayList<Pair<String, Double>>();
		
		tagger.set(xseq);
		StringList labels = tagger.viterbi();
		for (int i = 0; i < labels.size(); i++) {
			String label = labels.get(i);
			predicted.add(new Pair<String, Double>(
					label, tagger.marginal(label, i)));
		}
		
		return predicted;
	}
	
	/**
	 * Tag text stored in file
	 */
	public static List<List<Pair<String, Double>>> tag(String fileName) throws Exception {
		
		List<List<Pair<String, Double>>> taggedSentences = 
				new ArrayList<List<Pair<String, Double>>>();
		
		for (ItemSequence xseq: loadTaggingInstances(fileName)) {
			taggedSentences.add(tag(xseq));
		}
		
		return taggedSentences;
	}
}