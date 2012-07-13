package org.eclipse.emf.henshin.examples.ecore2genmodel;

import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.ChangeImpl;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.interpreter.util.InterpreterUtil;
import org.eclipse.emf.henshin.model.TransformationSystem;
import org.eclipse.emf.henshin.model.TransformationUnit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

/**
 * This implementation of an Ecore to Genmodel transformation by 
 * <a href="http://www.eclipse.org/modeling/emft/henshin/">Henshin</a> 
 * was created in the context of the 
 * <a href="http://is.ieis.tue.nl/staff/pvgorp/events/TTC2010/">Transformation Tool
 * Contest 2010</a> organized as satellite workshop to the 
 * <a href="http://malaga2010.lcc.uma.es/">TOOLS 2010</a> conference.<br>
 * Authors are (in alphabetical order):
 * <ul>
 * <li>Enrico Biermann</li>
 * <li>Claudia Ermel</li>
 * <li>Stefan Jurack</li>
 * <li>Christian Krause</li>
 * </ul>
 * 
 * <i>Remark:</i> As proof of concept only, in the following source (*.ecore) and
 * target (*.genmodel) model files are hard-coded. An adaptation to a full-fledged 
 * plug-in providing a context menu entry for Ecore files is straightforward.
 * 
 */
public class Ecore2GenmodelTrafo {

	// Base directory relative to the plug-in root:
	public static final String PATH = "src/org/eclipse/emf/henshin/examples/ecore2genmodel";

	/**
	 * Example transformation that translates an Ecore model for flow charts to a GenModel. 
	 */
	public static void runEcore2GenmodelExample(String path, boolean save) {
		
		System.out.println("Generating GenModel for flowchartdsl.ecore...");
		
		// Create a resource set:
		HenshinResourceSet resourceSet = new HenshinResourceSet(path);
		
		// Register Genmodel package (everything else is automatically registered):
		resourceSet.registerXMIResourceFactories("genmodel");
		GenModelPackage.eINSTANCE.getName();
		
		// Load the transformation system:
		TransformationSystem system = resourceSet.getTransformationSystem("Ecore2Genmodel.henshin");
		
		// Load Ecore files:
		EPackage mappingModel = (EPackage) resourceSet.getObject("ecore2gen.ecore");
		EPackage ecoreModel = (EPackage) resourceSet.getObject("flowchartdsl.ecore");

		// Create the object graph:
		EGraph graph = new EGraphImpl(ecoreModel);
		
		// Prepare the interpreter engine:
		Engine engine = new EngineImpl();
		ChangeImpl.PRINT_WARNINGS = false; // we can ignore the warnings
		UnitApplication unitApp = new UnitApplicationImpl(engine);

		// Generate genmodel from ecore model (without annotations).
		unitApp.setEGraph(graph);
		unitApp.setUnit(system.getTransformationUnit("translateGenModel"));
		
		// File name and plug-in name cannot be reliably deduced by the model elements, thus need to be set:
		unitApp.setParameterValue("modelFileName", "flowchartdsl.ecore");
		unitApp.setParameterValue("pluginName", ecoreModel.getName());
		
		// Execute the transformation unit:
		InterpreterUtil.executeOrDie(unitApp, null);
		
		// Get the generated Genmodel:
		GenModel genModel = (GenModel) unitApp.getResultParameterValue("genModel");
		
		graph.addTree(system);
		graph.addTree(GenModelPackage.eINSTANCE);
		graph.addTree(mappingModel);

		// Process annotations and generate related Henshin rules:
		unitApp.setUnit(system.getTransformationUnit("prepareCustomizationUnit"));
		InterpreterUtil.executeOrDie(unitApp, null);

		// Apply generated rules to transfer annotations to the genmodel.
		TransformationUnit customizationUnit = (TransformationUnit) unitApp.getResultParameterValue("seqUnit");
		unitApp.setUnit(customizationUnit);
		InterpreterUtil.executeOrDie(unitApp, null);

		System.out.println("Successfully generated GenModel.");
		
		if (save) {
			resourceSet.saveObject(genModel, "flowchartdsl-generated.genmodel");
			System.out.println("Saved the result to flowchartdsl-generated.genmodel");
		}

	}

	public static void main(String[] args) {
		runEcore2GenmodelExample(PATH, false);
	}
	
}