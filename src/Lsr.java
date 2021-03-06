import java.io.*;
import java.net.*;
import java.util.*;

//This is will send out all packets received from surrounding neighbours to all surrounding neighbours
class LSPUpdate extends TimerTask{
	public DatagramSocket mainSock;
	public ArrayList<byte[]> rePacks;
	public int numOfNeighbours;
	public int[] neighbour_ports;
	public byte[] OgConfig;
	public InetAddress IP = InetAddress.getLocalHost();
	
	public LSPUpdate(DatagramSocket mainSock, ArrayList<byte[]> rePacks, int numOfNeighbours, int[] neighbour_ports, byte[] OgConfig) throws Exception{
		this.mainSock = mainSock;
		this.rePacks = rePacks;
		this.numOfNeighbours = numOfNeighbours;
		this.neighbour_ports = neighbour_ports;
		this.OgConfig = OgConfig;
	}
	public void run(){
		int k = 0;
		for (int i = 0; i < numOfNeighbours; i++){
			for (int j = 0; j < rePacks.size(); j++){
				try {
					DatagramPacket mainLSP = new DatagramPacket(rePacks.get(j), rePacks.get(j).length, IP, neighbour_ports[i]);
					mainSock.send(mainLSP);
					if (k == 10){
						DatagramPacket OG = new DatagramPacket(OgConfig, OgConfig.length, IP, neighbour_ports[i]);
						mainSock.send(OG);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				k++;
			}
		}
	}
	
	// Converters //
	public static byte[] StringAToByteA (String[] source) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(source);
		oos.flush();
		oos.close();
		byte[] converted = baos.toByteArray();
		return converted;
	}
	
	public static String[] ByteAToStringA (byte[] source) throws IOException, ClassNotFoundException{
		ByteArrayInputStream bais = new ByteArrayInputStream(source);
		ObjectInputStream ois = new ObjectInputStream(bais);
		String[] converted = (String[]) ois.readObject();
		ois.close();
		return converted;
	}
}

//Creates the graph and does the dij alg things
class RouteUpdate extends TimerTask{
	public ArrayList<String> packets;
	
	public RouteUpdate (ArrayList<String> packets){
		this.packets = packets;
	}
	
	public void run(){
		Graph G = new Graph(packets.size());
		labelAll(G, packets);
		connectLabels(G, packets);
		int[] pathways = Dijkstra.dijkstra(G, 0);
		Dijkstra.PrintPath(G, pathways);
	}
    public void labelAll (Graph G, ArrayList<String> packets){
    	for (int i = 0; i<G.size(); i++){
    		String temp = packets.get(i).replaceAll("\\[", "").replaceAll("\\]", "");
    		String first = temp.split(", ")[0].split(" ")[0];
    		G.NameNode(first, i);
    	}
    }
    
    public void connectLabels (Graph G, ArrayList<String> packets){
    	for (int i = 0; i < G.size(); i++){
    		String temp = packets.get(i).replaceAll("\\[", "").replaceAll("\\]", "");
    		String[] splits = temp.split(", ");
    		String first = splits[0].split(" ")[0];
    		
    		for (int j = 2; j < splits.length; j++){
    			String destNodes = splits[j].split(" ")[0];
    			float weight = Float.parseFloat(splits[j].split(" ")[1]);
    			System.out.println(first);
    			System.out.println(destNodes);
    			G.createEdge(G.getVertex(first), G.getVertex(destNodes),weight);
    		}
    	}
    }
}

public class Lrs{
	private static final int UPDATE_INTERVAL = 1000; //ms
	//private static final int HEARTBEAT_INTERVAL = 500; //ms
	private static final int ROUTE_UPDATE_INTERVAL = 30000; //ms
	//Start the timer bois
	static Timer time = new Timer();
	
	public static void main(String[] args) throws Exception{
		if (args.length != 3){
			System.out.println("Required arguements: [Node_ID] [Node_Port] [config.txt]\n");
			return;
		}
		//args
		char ID = args[0].charAt(0);
		int port = Integer.parseInt(args[1]);
		String file = args[2];
		InetAddress IP = InetAddress.getLocalHost();
		
		//reading config files
		ArrayList<String> config = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null){
			config.add(line);
		}
		br.close();
		
		//getting current neighbours
		int numOfNeighbours = Integer.parseInt(config.get(0));
		int[] neighbour_ports = new int[numOfNeighbours]; 
		int x =0;
		for (int i = 1; i < config.size(); i++){
			String[] temp = config.get(i).split(" ");
			neighbour_ports[x] = Integer.parseInt(temp[2]);
			x++;
		}
		
		//add its own ID and port into the packet
		config.add(0,  ID + " " + port);
		//The LSP we are sending first
		String[] mainPacket = new String[config.size()];
		for (int i = 0; i < config.size(); i++){
			mainPacket[i] = config.get(i);
		}
		
		//ArrayList to keep track of all received packets and to create our graph
		ArrayList<String> Packets = new ArrayList<String>();
		Packets.add(Arrays.toString(mainPacket));
		
		//ArrayList to keep track of whatever packets received
		ArrayList<byte[]> rePacks = new ArrayList<byte[]>();
		
		/*---------------------------------End of main LSP creation-----------------------------------*/
		//change to byte[]
		byte[] main = StringAToByteA(mainPacket);
		
		//Kickstart LSR
		DatagramSocket mainSock = new DatagramSocket();
		DatagramSocket rcvdSock = new DatagramSocket(port);
		
		for (int i = 0; i < numOfNeighbours; i++){
			DatagramPacket mainLSP = new DatagramPacket(main, main.length, IP, neighbour_ports[i]);
			mainSock.send(mainLSP);
		}
		
		time.scheduleAtFixedRate(new LSPUpdate(mainSock, rePacks, numOfNeighbours, neighbour_ports, main), UPDATE_INTERVAL, UPDATE_INTERVAL);
		time.scheduleAtFixedRate(new RouteUpdate(Packets), ROUTE_UPDATE_INTERVAL, ROUTE_UPDATE_INTERVAL);
		
		while (true){
			int j = 0;

			if (rePacks.size() > 10){
				for (int i = 0; i < (rePacks.size())/2; i++){
					rePacks.remove(i);
				}
			}
			
			//Receiving the initial packets from neighbours
			while (j < numOfNeighbours){
				DatagramPacket nbPack = new DatagramPacket(new byte[1024], 1024);
				rcvdSock.receive(nbPack);
				rcvdSock.disconnect();
				byte[] nbPackB = nbPack.getData();
				String[] nbPackS = ByteAToStringA(nbPackB);
				rePacks.add(nbPackB);
				if (Packets.contains(Arrays.toString(nbPackS)) == false){
					Packets.add(Arrays.toString(nbPackS));
				}
				j++;
			}
		}
	}

	// Converters //
	public static byte[] StringAToByteA (String[] source) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(source);
		oos.flush();
		oos.close();
		byte[] converted = baos.toByteArray();
		return converted;
	}
	
	public static String[] ByteAToStringA (byte[] source) throws IOException, ClassNotFoundException{
		ByteArrayInputStream bais = new ByteArrayInputStream(source);
		ObjectInputStream ois = new ObjectInputStream(bais);
		String[] converted = (String[]) ois.readObject();
		ois.close();
		return converted;
	}
}
