import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


public class FileReader {

	private Map<Integer, Node> nodes;
	private Set<Integer> goals;
	private int start;

	public void ReadFile(String filename){
		// initialise fields
		this.nodes = new HashMap<Integer, Node>();
		this.goals = new HashSet<Integer>();
		this.start = 0;

		try {
			System.out.println("Reading file: " + filename);
			Scanner scan = new Scanner(new File(filename));

			while(scan.hasNextInt()){
				int id1 = scan.nextInt();
				int id2 = scan.nextInt();

				Node n1 = null;
				if((n1 = nodes.get(id1)) == null){
					n1 = new Node(id1);
					nodes.put(id1, n1);
				}

				Node n2 = null;
				if((n2 = nodes.get(id2)) == null){
					n2 = new Node(id2);
					nodes.put(id2, n2);
				}

				// connect the nodes
				n1.AddChild(n2);
				System.out.println(String.format("Added %d -> %d", id1, id2));
			}

			while(scan.hasNext()){
				String token = scan.next().trim();
				int id = scan.nextInt();
				if(token.equals("S")){
					if(this.start == 0){
						this.start = id;
						System.out.println(String.format("Start -> %d", id));
					} else {
						throw new IllegalArgumentException("Invalid file: more than one start node!");
					}
				}
				else if(token.equals("G")){
					goals.add(id);
					System.out.println(String.format("Goal -> %d", id));
				}
			}
			System.out.println("done \n");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public Map<Integer, Node> getNodes() {
		return nodes;
	}

	public Set<Integer> getGoals() {
		return goals;
	}

	public int getStart() {
		return start;
	}


	public static void GenerateGraph(int numNodes, int numEdges){

		HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();

		// create the nodes
		for(int i = 1; i <= numNodes; i++){
			nodes.put(i, new Node(i));
		}

		// add in random connections
		Random r = new Random();
		for(int i = 1; i <= numEdges; i++){

			// get two random nodes
			int id1;
			int id2;
			do{
				id1 = r.nextInt(numNodes) + 1;
				id2 = r.nextInt(numNodes) + 1;
			} while (id1 == id2);
			Node node1 = nodes.get(id1);
			Node node2 = nodes.get(id2);

			// connect them
			node1.AddChild(node2);
			node2.AddChild(node1);
		}

		// print the graph to sysout
		for(Node n: nodes.values()){
			for(Node c: n.GetChildren()){
				System.out.println(n.GetId() + " " + c.GetId());
			}
		}
	}


	public static void main(String[] args){
		FileReader.GenerateGraph(50, 50);
	}
}
