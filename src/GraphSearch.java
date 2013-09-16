import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GraphSearch extends Search {

	private static String filename = "CyclicGraph50.txt";
	
	// this monitor keeps track of all threads initiated, so that the main thread can tell when they are all done.
	private ThreadMonitor threads = new ThreadMonitor();
	private class ThreadMonitor{
		private List<Thread> threads = new LinkedList<Thread>();
		private int size = 0;
		private long waitTime = 0; // if each task takes time, we might increase this to be certain index == size means we are done.
		
		/**
		 * Adds one thread to the list safely.
		 */
		public synchronized void Add(Thread thread){
			threads.add(thread);
			size++;
		}
		
		/**
		 * Calling this method blocks the calling thread until we are certain all threads are complete.
		 */
		public void Wait(){
			int index = 0;
			while (index < size){
				Thread t = threads.get(index);
				try {
					t.join();
					index++;
					if(index == size){
						Thread.sleep(waitTime); // if we have caught up, give size a chance to increase
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}
		}
	}
	
	
	public GraphSearch() {
		super(filename);
	}

	public GraphSearch(String filename) {
		super(filename);
	}

	public void Search(){
		startTime = System.currentTimeMillis();
		// create a new thread
		Thread thread = new GraphSearchThread("", nodes.get(this.start), new HashSet<Integer>());
		
		// start
		thread.start();
		
		// add it to the list of threads
		threads.Add(thread);

		// wait for all to finish
		threads.Wait();
		
		stopTime = System.currentTimeMillis();

		// print results
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
			for(Node child: node.GetChildren()){
				Thread childThread = new GraphSearchThread(pathToNode, child, visited);
				threads.Add(childThread);
				childThread.start();
			}

		}
	}


	public static void main(String[] args){
		new GraphSearch().Search();
	}

}
