
public class Dijkstra {
	
	public int[] dijkstra (Graph G, int start){
		float[] dist = new float[G.size()];
		int[] path = new int[G.size()];
		boolean[] visited = new boolean [G.size()];
		
		for (int v = 0; v < dist.length; v++){
			dist[v] = Integer.MAX_VALUE;
		}
		
		dist[start] = 0;
		
		for (int vextex = 0; vextex < dist.length; vextex++){
			int next = minVertex (dist, visited);
			visited[next] = true;
			
			int[] n = G.neighbours(next);
			for (int neigh = 0; neigh < n.length; neigh++){
				int node = n[neigh];
				float td = dist[next] + G.getWeight(next, node);
				
				if (dist[node] > td){
					dist[node] = td;
					path[node] = next;
				}
			}
		}
		
		return path;
	}
	
	private int minVertex (float[] dist, boolean[] visited){
		float x = Integer.MAX_VALUE;
		int y = -1;
		
		for (int i = 0; i < dist.length; i++){
			if (visited[i] == false && dist[i] < x){
				y = i;
				x = dist[i];
			}
		}
		return y;
	}
	
	public void PrintPath (Graph G, int[] path){
		int start = 0;

		for (int i = 1; i < G.size(); i++){
			float TD = 0;
			String labels = G.getLabel(start);
			for (int j = 0; path[j] != i; j++){
				TD += G.getWeight(start, path[j]);
				labels += G.getLabel(path[j]);
				System.out.println("least-cost path to node " + G.getLabel(path[i]) +
						": " + labels + " and the cost is " + TD);
			}
		}
	}
}
