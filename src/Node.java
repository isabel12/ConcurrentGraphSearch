import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node{
	private int id;
	private Set<Node> children;
	private List<String> answers;
	private String cycles;	

	public Node(int id){
		this.id = id;
		this.children = new HashSet<Node>();
		this.answers = new ArrayList<String>();
		this.cycles = "";
	}

	public void AddChild(Node c){
		children.add(c);
	}

	public Set<Node> GetChildren(){
		return children;
	}

	public int GetId(){
		return id;
	}

	public void AddAnswer(String answer){
		synchronized (answers){
			this.answers.add(answer);
		}
	}	
	
	/**
	 * This method will be passed the path to the visited node,
	 * and will record the cycle if it hasn't been recorded already.
	 * 
	 * Eg.  "1 3 4 6 3" given to node 3 will add "(4,6,3)*" to the cycles string.
	 * 
	 * @param pathToNode
	 * @param nodeId
	 */
	public void AddCycle(String pathToNode){
		// Find the cycle in the path, and format it.
		// e.g.  "1 3 4 6 3" -> "(4,6,3)*"
		int startIndex = pathToNode.indexOf(id + "");
		String cycle = String.format("(%s)*", pathToNode.substring(startIndex + 1).trim());	
		cycle = cycle.replace(" ", ",");

		synchronized(cycles){		
			// If the cycle hasn't already been found by another thread, add it to the cycles starting from nodeId.
			if(!cycles.contains(cycle)){
				this.cycles += cycle;	
			}
		}
	}
	
	public List<String> GetAnswers(){
		return new ArrayList<String>(answers);  // No need to synchronize this as it is only accessed after running the search.
	}
	
	public String GetCycles(){
		return cycles;  // No need to synchronize this as it is only accessed after running the search.
	}
}