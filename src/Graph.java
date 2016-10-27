
public class Graph {
	private float[][] adjM;
	private String[] nodes;
	
	public Graph(int size){
		adjM = new float[size][size];
		nodes = new String[size];
	}
	
	//name one of the vertex
	public void NameNode(String node, int vertex){
		nodes[vertex] = node;
	}
	
	//Creates an edge between two vertex
	public void createEdge(int x, int y, float weight){
		adjM[x][y] = weight;
	}
	
	//get the vertex through using the string
	public int getVertex(String node){
		int i = 0;
		int v = -1;
		while (i < node.length()){
			if (nodes[i].equals(node)){
				v = i;
				break;
			} else {
				i++;
			}
		}
		return v;
	}
	
	//Get the label of the vertex
	public String getLabel(int vertex){
		String label = nodes[vertex];
		return label;
	}
	
	//get the weight of an edge
	public float getWeight (int x, int y){
		return adjM[x][y];
	}
	
	//the amount of nodes
	public int size(){
		return nodes.length;
	}
	
	//Check if the two vertex forms an edge
	public boolean isEdge(int x, int y){
		if (adjM[x][y] > 0){
			return true;
		} else {
			return false;
		}
	}
	
	
	//Search the vertex for its neighbours and return it in an array
	public int[] neighbours(int x){
		int count = 0;
		int i = 0;
		
		while (i < size()){
			if (adjM[x][i] > 0){
				count++;
			}
			i++;
		}
		int[] neigh = new int[count];
		i = 0;
		count = 0;
		while (i < size()){
			if (adjM[x][i] > 0){
				neigh[count] = i;
				count++;
			}
			i++;
		}
		return neigh;
	}
}
