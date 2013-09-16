import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Collections;

/**
 * This class is the base class for all the different search classes.  It holds the nodes, start, goals,
 * and is responsible for interpreting and formatting the results from the nodes after the sub-class has performed the
 * search, and printing out the results.
 *
 * The search will be initialised from the sub-class.
 *
 * However, the main method for the project is contained in this class.
 * @author Izzi
 *
 */
public class Search {

	protected Map<Integer, Node> nodes;
	protected Set<Integer> goals;
	protected int start;

	protected HashMap<Integer, String> foundCycles;

	protected long startTime;
	protected long stopTime;
	
	private static boolean printCycles = false;

	/**
	 * Constructor :)
	 * @param filename
	 */
	protected Search(String filename){
		FileReader reader = new FileReader();
		reader.ReadFile(filename);
		this.nodes = reader.getNodes();
		this.goals = reader.getGoals();
		this.start = reader.getStart();
	}

	protected void PrintResults(){
		System.out.println(String.format("Time taken: %dms", (int)(stopTime-startTime)));

		// get all answers
		List<String> answers = new ArrayList<String>();
		for(Node n: nodes.values()){
			for(String answer: n.GetAnswers()){
				answers.add(answer);
			}
		}

		System.out.println("NumAnswers: " + answers.size());
		
		if(printCycles){
			// add in the cycles.
			//--------------------------------------
	
			// for each answer
			for(int i = 0; i < answers.size(); i++){
				String initialAnswer = answers.get(i);
				Scanner scan = new Scanner(initialAnswer);
	
				Set<String> containedCycles = new HashSet<String>(); // this is to help keep track of if it is already contained or not.
				String actualAnswer = initialAnswer;
	
				// for each nodeid in the answer
				while(scan.hasNextInt()){
					// get the cycles starting from that node
					Node nextNode = nodes.get(scan.nextInt());
					String cycleString = nextNode.GetCycles();
					if(cycleString != ""){
	
						// add the cycles in if they aren't already contained (this means if (4,6,3)* is already contained, we won't add (3,4,6)*)
						if(!CycleUsed(cycleString, containedCycles)){
	
							// insert it into the answer
							int nodeIndex = actualAnswer.indexOf(nextNode.GetId() + " ");  // it will always be at most once with a space after it.
							if(nodeIndex > -1){
								String firstHalf = actualAnswer.substring(0, nodeIndex);
								String secondHalf = actualAnswer.substring(nodeIndex + 2);
								actualAnswer = firstHalf + nextNode.GetId() + " " + cycleString + " " + secondHalf;
							}
	
							// record that we have used it
							containedCycles.add(cycleString);
						}
					}
				}
	
				// add in the new answer
				answers.remove(i);
				answers.add(i, actualAnswer);
			}
		}

		// print complete answers
		//---------------------------------
		for(String answer: answers){
			System.out.println(answer);
		}
	}


	/**
	 * This checks to see that there are no permutations of 'cycle1' contained in the 'containedCycles' set.
	 *
	 * Eg.  This method would count all the following as equal:
	 *
	 * 		(4,6,3)*, (6,3,4)*, (4,3,6)*
	 *
	 * Currently it just checks that the set is equal, rather than also checking the order.  So this won't work for graphs with symmetric cycles.
	 *
	 * @param cycle1
	 * @param containedCycles
	 * @return
	 */
	private static boolean CycleUsed(String cycle1, Set<String> containedCycles){

		String[] cycle1Array = cycle1.split("[()*,]");
		List<String> cycle1List = new ArrayList<String>();
		for(int i = 0; i < cycle1Array.length; i++){
			cycle1List.add(cycle1Array[i]);
		}
		Collections.sort(cycle1List);

		for(String cycle2: containedCycles){

			String[] cycle2Array = cycle2.split("[()*,]");

			if(cycle1Array.length != cycle2Array.length){
				return false;
			}

			List<String> cycle2List = new ArrayList<String>();
			for(int i = 0; i < cycle2Array.length; i++){
				cycle2List.add(cycle2Array[i]);
			}
			Collections.sort(cycle2List);

			for(int i = 0; i < cycle1List.size(); i++){
				if(!cycle1List.get(i).equals(cycle2List.get(i))){
					continue;
				}
			}
			return true;
		}

		return false;
	}

	/**
	 * Entry point into the project
	 * @param args
	 */
	public static void main(String[] args){

		String searchType = "graphFindOne";
		String filename = null;
		int numThreads = 0;
		printCycles = false;
		
		// extract arguments
		for(String arg: args){
			if(arg.startsWith("searchType=")){
				searchType = arg.replaceAll("searchType=", "");
			}
			
			else if(arg.startsWith("filename=")){
				filename = arg.replaceAll("filename=", "");
			}
			
			else if (arg.startsWith("numThreads=")){
				numThreads = Integer.parseInt(arg.replaceAll("numThreads=", ""));
			}
			
			else if (arg.startsWith("printCycles=")){
				printCycles = Boolean.parseBoolean(arg.replace("printCycles=", ""));
			}
		}

		if(searchType.equals("tree")){
			System.out.println("Performing find all tree search");
			if(filename != null){
				new TreeSearch(filename).Search();
			}else {
				new TreeSearch().Search();
			}
		}

		else if (searchType.equals("graph")){
			if(numThreads == 0){			
				System.out.println("Performing find all graph search with unlimited threads");
				if(filename != null){
					new GraphSearch(filename).Search();
				}else {
					new GraphSearch().Search();
				}
			} else {
				System.out.println("Performing find all graph search with " + " threads.");
				if(filename != null){
					new GraphSearchThreadPool(filename, numThreads).Search();
				}else {
					new GraphSearchThreadPool(numThreads).Search();
				}
			}
		}

		else if (searchType.equals("graphFindOne")) {
			System.out.println("Performing find one graph search");
			if(filename != null){
				new GraphSearchFindOne(filename).Search();
			}else {
				new GraphSearchFindOne().Search();
			}
		}

	}
}
