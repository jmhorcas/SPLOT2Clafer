import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.regex.Pattern;

import constraints.BooleanVariable;
import constraints.PropositionalFormula;
import fm.FeatureGroup;
import fm.FeatureModel;
import fm.FeatureTreeNode;
import fm.RootNode;
import fm.SolitaireFeature;
import fm.XMLFeatureModel;

public class SPLOT2Clafer {

	public static void main(String args[]) throws FileNotFoundException {
		if (args.length != 1) {
			System.err.println("Not valid arguments.\r\nUsage: java -jar SPLOT2Clafer <<splot-feature-model.xml>>");
		}
		String filename = args[0];
		String filenameOutput = filename.substring(0, filename.indexOf('.'));
		
		// Creating a File object that represents the disk file. 
        PrintStream o = new PrintStream(new File(filenameOutput + ".txt")); 
  
        // Assign o to output stream 
        System.setOut(o); 
  
        new SPLOT2Clafer().parse(filename);
	} 
	
	public void parse(String filename) {
		try {
			String featureModelFile = filename;
			
			/* Creates the Feature Model Object
			 * ********************************
			 * - Constant USE_VARIABLE_NAME_AS_ID indicates that if an ID has not been defined for a feature node
			 *   in the XML file the feature name should be used as the ID. 
			 * - Constant SET_ID_AUTOMATICALLY can be used to let the system create an unique ID for feature nodes 
			 *   without an ID specification
			 *   Note: if an ID is specified for a feature node in the XML file it will always prevail
			 */			
			FeatureModel featureModel = new XMLFeatureModel(featureModelFile, XMLFeatureModel.USE_VARIABLE_NAME_AS_ID);
			
			// Load the XML file and creates the feature model
			featureModel.loadModel();
			
			// A feature model object contains a feature tree and a set of contraints			
			// Let's traverse the feature tree first. We start at the root feature in depth first search.
			traverseDFS(featureModel.getRoot(), 0);
			
			System.out.println("\r\n");
			
			// Now, let's traverse the extra constraints as a CNF formula
			traverseConstraints(featureModel);
			
			System.out.println("\r\n\r\ninstance: " + cleanName(featureModel.getName()) + "\r\n");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
		
	private void printTabs(int tab) {
		for( int j = 0 ; j < tab ; j++ ) {
			System.out.print("\t");
		}
	}
	
	private String cleanName(String str) {
		str = str.toLowerCase();
		if (! Pattern.compile("^[A-Za-z]").matcher(str).find()) {
			str = "F" + str;
		}
		return str.replace("_", "").replace(" ", "").replace("-", "");	
	}
	
	public void traverseDFS(FeatureTreeNode node, int tab) {
		printTabs(tab);
		// Root Feature
		if ( node instanceof RootNode ) {
			System.out.print("abstract ");
		}
		// Solitaire Feature
		else if ( node instanceof SolitaireFeature ) {
			// Optional Feature
			if ( ((SolitaireFeature)node).isOptional()) {
				System.out.print("0..1 Opt" + cleanName(node.getName()) + "\r\n");
				tab += 1;
				printTabs(tab);
			// Mandatory Feature
			}
		}
		// Feature Group
		else if ( node instanceof FeatureGroup ) {
			int minCardinality = ((FeatureGroup)node).getMin();
			int maxCardinality = ((FeatureGroup)node).getMax();
			maxCardinality = maxCardinality == -1 ? node.getChildCount() : maxCardinality;
			System.out.print(minCardinality + ".." + maxCardinality + " "); 
		}
		System.out.print( cleanName(node.getName()) + "\r\n");
		for( int i = 0 ; i < node.getChildCount() ; i++ ) {
			traverseDFS((FeatureTreeNode )node.getChildAt(i), tab+1);
		}
	}
	
	public void traverseConstraints(FeatureModel featureModel) {
		for( PropositionalFormula formula : featureModel.getConstraints() ) {
			String f = "[" + formula.getFormula() + "]";
			f = f.replaceAll("~", "not ");
			f = f.replaceAll(" or ", " || ");
			for (BooleanVariable v : formula.getVariables()) {
				f = f.replaceAll(v.getID(), cleanName(v.getID()));
			}
			System.out.println(f);
		}
	}
	
}
