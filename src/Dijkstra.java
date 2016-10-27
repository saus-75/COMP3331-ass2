import java.util.ArrayList;
import java.util.Collections;

public class Dijkstra {
	
	public static int[] dijkstra (Graph G, int start){
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
	
	private static int minVertex (float[] dist, boolean[] visited){
		float x = Integer.MAX_VALUE;
		int y = 0;
		
		for (int i = 0; i < dist.length; i++){
			if (visited[i] == false && dist[i] < x){
				y = i;
				x = dist[i];
			}
		}
		return y;
	}
	
	public static void PrintPath (Graph G, int[] path){
		int start = 0;
		float TD = 0;
		for (int i = 1; i < G.size();i++){
			ArrayList<String> fpaths  = new ArrayList<String>();
			int x = i;
			while (x != start){
				float temp = G.getWeight(x, path[x]);
				TD += temp;
				fpaths.add(G.getLabel(x));
				x = path[x];
			}
			fpaths.add(G.getLabel(0));
			Collections.reverse(fpaths);
			String finalP = fpaths.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(", ", "");
			System.out.println("least-cost path to node " + G.getLabel(path[i]) +
			": " + finalP + " and the cost is " + TD);
		}

	}
}
