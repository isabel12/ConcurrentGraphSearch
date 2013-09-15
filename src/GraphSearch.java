import java.util.HashSet;
import java.util.Set;

public class GraphSearch extends Search {

	private static String filename = "CyclicGraph1.txt";

	public GraphSearch() {
		super(filename);
	}

	public void Search(){	
		startTime = System.currentTimeMillis();	
		// create a new thread
		Thread thread = new GraphSearchThread("", nodes.get(this.start), new HashSet<Integer>());	
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


	public class GraphSearchThread extends Thread{
		Node node;
		String pathToNode;
		int nodeId;
		Set<Integer> visited;

		public GraphSearchThread(String pathToParent, Node n, Set<Integer> visited){
			this.nodeId = n.GetId();
			this.pathToNode = pathToParent + nodeId + " ";  // adds itself to the path to the parent.
			this.node = n;
			this.visited = new HashSet<Integer>(visited);
		}
		
		@Override
		public void run() {
			SearchRec();
		}

		public void SearchRec(){	
			// check if it has been visited
			if(visited.contains(nodeId)){
				node.AddCycle(pathToNode);
				return;
			}

			// visit
			visited.add(nodeId);

			// base case
			if(goals.contains(nodeId)){
				node.AddAnswer(pathToNode);
			}

			// spawn a new thread for each child search
			Set<Thread> childThreads = new HashSet<Thread>();
			for(Node child: node.GetChildren()){
				Thread childThread = new GraphSearchThread(pathToNode, child, visited);
				childThreads.add(childThread);
				childThread.start();
			}	

			// wait for all children to finish - this is so that we know when the results are ready, because it ensures that while the first thread is alive, the search is still in progress.
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
		new GraphSearch().Search();
	}

}
