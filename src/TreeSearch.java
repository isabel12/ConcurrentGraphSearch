import java.util.LinkedList;
import java.util.List;


public class TreeSearch extends Search {

	private static String filename = "tree1.txt";
	
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

	public TreeSearch() {
		super(filename);
	}

	public TreeSearch(String filename){
		super(filename);
	}

	public void Search(){
		startTime = System.currentTimeMillis();
		// create a new thread
		Thread thread = new TreeSearchThread("", nodes.get(this.start));
		// start
		thread.start();
		
		threads.Add(thread);
		
		// wait to finish
		threads.Wait();

		stopTime = System.currentTimeMillis();

		PrintResults();
	}

	public class TreeSearchThread extends Thread{
		Node node;
		String pathToNode;
		int nodeId;

		public TreeSearchThread(String pathToParent, Node n){
			this.nodeId = n.GetId();
			this.pathToNode = pathToParent + nodeId + " ";  // adds itself to the path to the parent.
			this.node = n;
		}

		@Override
		public void run() {
			SearchRec();
		}

		public void SearchRec(){

			// base case
			if(goals.contains(nodeId)){
				node.AddAnswer(pathToNode);
			}

			// spawn a new thread for each child search
			for(Node child: node.GetChildren()){
				Thread childThread = new TreeSearchThread(pathToNode, child);
				childThread.start();
				threads.Add(childThread);
			}
		}
	}


	public static void main(String[] args){
		new TreeSearch().Search();
	}

}
