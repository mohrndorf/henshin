package org.eclipse.emf.henshin.examples.mutualexclusion;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.InterpreterFactory;
import org.eclipse.emf.henshin.interpreter.RuleApplication;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.TransformationSystem;
import org.eclipse.emf.henshin.model.TransformationUnit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

public class STSBenchmark {

	/**
	 * Relative path to the example files.
	 */
	public static final String PATH = "src/org/eclipse/emf/henshin/examples/mutualexclusion";

	final static int GRAPH_SIZE_MIN = 1000;
	
	final static int GRAPH_SIZE_MAX = 10000;
	
	final static int STEP = 1000;

	final static int ITERATIONS = 3;
	
	public static void run(String path) {

		System.out.println("**************** Short Transformation Sequence ***************");
		System.out.println("Memory allocated:" +Runtime.getRuntime().maxMemory()/1024/1024 + "M");
		System.out.println("GRAPH_SIZE_MIN:" + GRAPH_SIZE_MIN);
		System.out.println("GRAPH_SIZE_MAX:" + GRAPH_SIZE_MAX);
		System.out.println("STEP:" + STEP);
		System.out.println("ITERATIONS:" + ITERATIONS + " , first iteration is not included into evaluation");
		System.out.println("**************************************************************");

		// Create a resource set with a base directory:
		HenshinResourceSet resourceSet = new HenshinResourceSet(path);

		// Load the transformation system:
		TransformationSystem trasys = resourceSet.getTransformationSystem("mutualexclusion.henshin");
		EObject container = resourceSet.getObject("initialgraph.xmi");

		// Load the rules:
		Rule newRule = trasys.findRuleByName("newRule");
		TransformationUnit stsUnit = trasys.findUnitByName("sts");
		TransformationUnit loopStsUnit = trasys.findUnitByName("loopSts");

		
		//Perform benchmark for several graph sizes
		for(int graphSize = GRAPH_SIZE_MIN; graphSize <=GRAPH_SIZE_MAX; graphSize +=STEP){

			long sum = 0;
			//Repeat execution for obtaining mean value
			for (int j = 0; j <ITERATIONS; j++) {
	
				EObject container2 = EcoreUtil.copy(container);
				
				// Initialize the Henshin interpreter:
				EGraph graph = InterpreterFactory.INSTANCE.createEGraph();
				graph.addTree(container2);
				Engine engine = InterpreterFactory.INSTANCE.createEngine();
				
				RuleApplication ruleAppl = InterpreterFactory.INSTANCE.createRuleApplication(engine);
				ruleAppl.setEGraph(graph);
				UnitApplication unitAppl = InterpreterFactory.INSTANCE.createUnitApplication(engine);
				unitAppl.setEGraph(graph);
				
				// get Starting Time
				long startTime = System.currentTimeMillis();
	
				// create initial graph
				ruleAppl.setRule(newRule);
				for (int i = 0; i < graphSize - 2; i++) {
					ruleAppl.execute(null);
				}

				// main transformation sequence
				unitAppl.setUnit(stsUnit);
				unitAppl.execute(null);

				unitAppl.setUnit(loopStsUnit);
				for (int i = 0; i < graphSize; i++) {
					unitAppl.execute(null);
				}
				
				//get finish time
				long finishTime = System.currentTimeMillis();
				
				if(j!=0){ //don't include first ITERATION; emf classes are loaded
					sum = sum + finishTime - startTime;
					//System.out.println("execution time ms: " + (finishTime - startTime));
					}
				
				//if (j == ITERATIONS -1)// persist the resulting graph on the last iteration
				//BenchmarkHelper.persistGraph(BENCHMARK_CASE, resourceSet, container2);
			}
			System.out.println(" Graph size: "+graphSize +"  average time:" + (sum/(ITERATIONS-1)));
		} //FOR several STEPS
	}
	
	public static void main(String[] args) {
		run(PATH);
	}
	
}