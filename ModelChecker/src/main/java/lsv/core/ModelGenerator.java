package lsv.core;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.RecognitionException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lsv.grammar.CTL;
import lsv.grammar.Formula;
import lsv.model.Model;
import lsv.model.State;
import lsv.model.Transition;
import lsv.util.Helper;

/**
 * The ModelGenerator is a class for generating the model. It allows the user to 
 * upload the model (i.e defined  a JSON file) into the application.
 * The ModelGenerator object provides the graph implementation and
 * the JSON object of the model input by the user.
 * An example of the JSON file for the model is shown in /data/model.json.
 * Each model has transitions and states. From both transitions and
 * states we generate a graph (i.e represented using an adjacent Matrix)
 * */
public class ModelGenerator {
	
	private Map<String, ArrayList<Transition>> graph;

	private Transition [] transitions;
	private State [] states;
	
	private Formula constraint;
	
	private boolean kripke;
	
	public Map<String, ArrayList<Transition>> getGraph() {
		return this.graph;
	}


	public State[] getStates() {
		return this.states;
	}

	public Transition [] getTransitions() {
		return this.transitions;
	}
	
	public boolean isKripke() {
		return this.kripke;
	}

	public Formula getConstraint() {
		return this.constraint;
	}
	
    /** 
     * ModelGenerator constructor.
     * Generate the model graph (Using adjacent list) and the model object
     * from the input file given (e.g. /data/model.json)
     * @param filePath	filePath of the JSON file
     * @throws FileNotFoundException 
     */
	public ModelGenerator(String filePath) throws FileNotFoundException {
		Gson gson = new Gson();
		
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		Model model = gson.fromJson(br, Model.class);
		
		//Initialize
		this.states = model.getStates();
		this.transitions = model.getTransitions();
		this.graph = new HashMap<String,  ArrayList<Transition>>();
		
		createGraph(model, this);				
		checkKripke(this);	
        addConstraint(filePath, this);      	
	}


	private static void addConstraint(String filePath, ModelGenerator mg) throws FileNotFoundException {
		JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(new FileReader(filePath));
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        jsonObject = jsonObject.getAsJsonObject("constraint");
        if(jsonObject != null) {
	        CTL ctl = CTL.createCTL(jsonObject);
			try {
				mg.constraint = SimpleParser.parseCTL(ctl.getFormula());
				Formula.addActions(ctl, mg.constraint);
	
			} catch (RecognitionException e) {
				System.out.println("Unable to generate CTL formula:");
				e.printStackTrace();
			}
        }
	}

	private static void createGraph(Model model, ModelGenerator mg) {
		for(int i = 0; i < model.getTransitions().length; i++) {
			Transition curr = model.getTransitions()[i];
			if(mg.graph.containsKey(curr.getSource())) {
				mg.graph.get(curr.getSource()).add(curr);
			} else {
				mg.graph.put(curr.getSource(), new ArrayList<Transition>());
				mg.graph.get(curr.getSource()).add(curr);
			}			
		}
	}

	private static void checkKripke(ModelGenerator mg) {
		mg.kripke = true;
		String [] sourceTransition = new String[mg.transitions.length];
		String [] states = new String[mg.states.length];
		for(int i = 0; i < sourceTransition.length; i++) {
			sourceTransition[i] = mg.transitions[i].getSource();
		}
		for(int i = 0; i < states.length; i++) {
			states[i] = mg.states[i].getName();
		}
		mg.kripke = Helper.containAnd(sourceTransition, states);
	}
	
}
