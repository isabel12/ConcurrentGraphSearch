import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GraphSearchThreadPool extends Search {

	private static String filename = "CyclicGraph50.txt";

	private ExecutorService pool;
	
	private TaskTracker tasks = new TaskTracker();
	private class TaskTracker{
		private List<Future<?>> futures = new LinkedList<Future<?>>();
		private int size = 0;
		private long waitTime = 0;  // this would be higher if each task took longer to process
		
		/**
		 * Adds one future to the list safely.
		 */
		public synchronized void AddTask(Future<?> future){
			futures.add(future);
			size++;
		}
		
		/**
		 * Blocks the calling thread until we are confident all tasks are complete.
		 */
		public void Wait(){		
			// go through the whole list waiting for each task as we go
			int index = 0;
			while (index < size){
				Future<?> future = futures.get(index);
				try {
					// wait for the current task
					future.get();
					
					// increment index
					index++;
					
					// if we have caught up, wait a bit
					if(index == size){
						Thread.sleep(waitTime);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public GraphSearchThreadPool(int numThreads) {
		this(filename, numThreads);
	}

	public GraphSearchThreadPool(String filename, int numThreads) {
		super(filename);
		pool = Executors.newFixedThreadPool(numThreads);
	}

	public void Search(){
		startTime = System.currentTimeMillis();
		// create a new thread
		Runnable task = new GraphSearchTask("", nodes.get(this.start), new HashSet<Integer>());
		
		// start
		tasks.AddTask(pool.submit(task));
		
		// wait for all to finish
		tasks.Wait();
		
		stopTime = System.currentTimeMillis();
		pool.shutdown();
		
		// print results
		PrintResults();
	}


	private class GraphSearchTask implements Runnable{
		
		Node node;
		String pathToNode;
		int nodeId;
		Set<Integer> visited;

		public GraphSearchTask(String pathToParent, Node n, Set<Integer> visited){
			this.nodeId = n.GetId();
			this.pathToNode = pathToParent + nodeId + " ";  // adds itself to the path to the parent.
			this.node = n;
			this.visited = new HashSet<Integer>(visited);
		}

		@Override
		public void run() {
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

			// make a new task for each child search
			for(Node child: node.GetChildren()){
				tasks.AddTask(pool.submit(new GraphSearchTask(pathToNode, child, visited)));
			}	
		}

	}


	public static void main(String[] args){
		new GraphSearchThreadPool(6).Search();
	}

}
