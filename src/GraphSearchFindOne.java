import java.util.HashSet;
import java.util.Set;

public class GraphSearchFindOne extends Search {

	private static String filename = "CyclicGraph2.txt";

	// a monitor to stop multiple answers being added.
	private AnswerMonitor answerMonitor = new AnswerMonitor();
	private class AnswerMonitor{
		private boolean found;

		public boolean Found(){
			return found;
		}

		public synchronized void RecordAnswer(Node n, String pathToNode){
			if(!found){
				n.AddAnswer(pathToNode);
				this.found = true;
			}
		}
	}

	public GraphSearchFindOne() {
		super(filename);
	}

	public GraphSearchFindOne(String filename) {
		super(filename);
	}

	/**
	 * Initialises a search from the start node, and prints results.
	 */
	public void Search(){
		startTime = System.currentTimeMillis();
		// create a new thread
		Thread thread = new SearchThread("", nodes.get(this.start), new HashSet<Integer>());
		// start
		thread.start();
		// wait to finish
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		stopTime = System.currentTimeMillis();

		PrintResults();
	}

	public class SearchThread extends Thread{
		Node node;
		String pathToNode;
		int nodeId;
		Set<Integer> visited;

		public SearchThread(String pathToParent, Node n, Set<Integer> visited){
			this.nodeId = n.GetId();
			this.pathToNode = pathToParent + nodeId + " ";
			this.node = n;
			this.visited = new HashSet<Integer>(visited);
		}

		@Override
		public void run() {
			SearchRec();
		}

		public void SearchRec(){
			// check if the answer has been found
			if(answerMonitor.Found()){
				return;
			}

			// check for a cycle
			if(visited.contains(nodeId)){
				node.AddCycle(pathToNode);
				return;
			}

			// visit
			visited.add(nodeId);

			// see if we are at a goal
			if(goals.contains(nodeId)){
				answerMonitor.RecordAnswer(node, pathToNode);
				return;
			}

			// spawn a new thread for each child search
			Set<Thread> childThreads = new HashSet<Thread>();
			for(Node child: node.GetChildren()){
				Thread childThread = new SearchThread(pathToNode, child, visited);
				childThreads.add(childThread);
				childThread.start();
			}

			// wait for all children to finish
			try {
				for(Thread childThread: childThreads){
					if(childThread.isAlive()){
						childThread.join();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args){
		new GraphSearchFindOne().Search();
	}

}
