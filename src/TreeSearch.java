import java.util.HashSet;
import java.util.Set;

public class TreeSearch extends Search {

	private static String filename = "Tree1.txt";

	public TreeSearch() {
		super(filename);
	}

	public void Search(){	
		startTime = System.currentTimeMillis();	
		// create a new thread
		Thread thread = new TreeSearchThread("", nodes.get(this.start));	
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
			Set<Thread> childThreads = new HashSet<Thread>();
			for(Node child: node.GetChildren()){
				Thread childThread = new TreeSearchThread(pathToNode, child);
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
		new TreeSearch().Search();
	}

}
