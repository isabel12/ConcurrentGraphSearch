import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class performs a concurrent graph search to find any path between Start and Goal(s).
 */
public class GraphSearchFindOne extends Search {

	private static String filename = "CyclicGraph50.txt";

	// a monitor to stop multiple answers being added.
	private AnswerMonitor answerMonitor = new AnswerMonitor();
	private class AnswerMonitor {
		private boolean found;

		
		public boolean Found() { // doesn't need to be synchronized as readonly
			return found;
		}

		public synchronized void RecordAnswer(Node n, String pathToNode) {
			if (!found) {
				n.AddAnswer(pathToNode);
				this.found = true;
			}
		}
	}
	
	// this monitor keeps track of all threads initiated, so that the main thread can tell when they are all done.
	private ThreadTracker threads = new ThreadTracker();
	private class ThreadTracker{
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
		public void Wait(){  // doesn't need to be synchronized as readonly
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

	public GraphSearchFindOne() {
		super(filename);
	}

	public GraphSearchFindOne(String filename) {
		super(filename);
	}

	/**
	 * Initialises a search from the start node, and prints results.
	 */
	public void Search() {
		startTime = System.currentTimeMillis();
		// create a new thread
		Thread thread = new SearchThread("", nodes.get(this.start),
				new HashSet<Integer>());
		// start
		thread.start();
		threads.Add(thread);
		
		// wait to finish
		threads.Wait();
		stopTime = System.currentTimeMillis();

		PrintResults();
	}

	private class SearchThread extends Thread {
		Node node;
		String pathToNode;
		int nodeId;
		Set<Integer> visited;

		public SearchThread(String pathToParent, Node n, Set<Integer> visited) {
			this.nodeId = n.GetId();
			this.pathToNode = pathToParent + nodeId + " ";
			this.node = n;
			this.visited = new HashSet<Integer>(visited);
		}

		@Override
		public void run() {
			// check if the answer has been found
			if (answerMonitor.Found()) {
				return;
			}

			// check for a cycle
			if (visited.contains(nodeId)) {
				node.AddCycle(pathToNode);
				return;
			}

			// visit
			visited.add(nodeId);

			// see if we are at a goal
			if (goals.contains(nodeId)) {
				answerMonitor.RecordAnswer(node, pathToNode);
				return;
			}

			// spawn a new thread for each child search
			for (Node child : node.GetChildren()) {
				Thread childThread = new SearchThread(pathToNode, child,
						visited);
				threads.Add(childThread);
				childThread.start();
			}
		}
	}

	public static void main(String[] args) {
		new GraphSearchFindOne().Search();
	}

}
