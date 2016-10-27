import java.io.*;
import java.net.*;
import java.util.*;

//This is will send out all packets received from surrounding neighbours to all surrounding neighbours
class LSPUpdate extends TimerTask{
	public DatagramSocket mainSock;
	public ArrayList<byte[]> rePacks;
	public int numOfNeighbours;
	public int[] neighbour_ports;
	public InetAddress IP;
	
	public LSPUpdate(DatagramSocket mainSock, ArrayList<byte[]> rePacks, int numOfNeighbours, int[] neighbour_ports) throws Exception{
		this.mainSock = mainSock;
		this.rePacks = rePacks;
		this.numOfNeighbours = numOfNeighbours;
		this.neighbour_ports = neighbour_ports;
		InetAddress IP = InetAddress.getLocalHost();
	}
	public void run(){
		for (int i = 0; i < numOfNeighbours; i++){
			for (int j = 0; j < rePacks.size(); j++){
				DatagramPacket mainLSP = new DatagramPacket(rePacks.get(j), rePacks.get(j).length, IP, neighbour_ports[i]);
				try {
					mainSock.send(mainLSP);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

class RouteUpdate extends TimerTask{
	public ArrayList<String> packets;
	Dijkstra route = new Dijkstra();
	
	public RouteUpdate (ArrayList<String> packets){
		this.packets = packets;
	}
	
	public void run(){
		Graph G = new Graph(packets.size());
		labelAll(G, packets);
		connectLabels(G, packets);
		int[] pathways = route.dijkstra(G, 0);
		route.PrintPath(G, pathways);
	}
	
	public void labelAll (Graph G, ArrayList<String> packets){
		for (int i = 0; i < packets.size(); i++){
			// string will be in the form of A 2000, 3, B 1.3 1000, C 2 2001, D 3 3000
			String[] nodes = packets.get(i).split(", ");
			// A 2000 will split [A] [2000]
			G.NameNode(nodes[0].split(" ")[0], i);
		}
	}
	
	public void connectLabels (Graph G, ArrayList<String> packets){
		for (int i = 0; i< packets.size(); i++){
			String[] nodes = packets.get(i).split(", ");
			for (int j = 2; j < nodes.length; j++){
				G.createEdge(G.getVertex(nodes[0].split(" ")[0]), 
						G.getVertex(nodes[j].split(" ")[0]), Float.parseFloat(nodes[j].split(" ")[1]));
			}
		}
	}
}

public class Lrs{
	private static final int UPDATE_INTERVAL = 1000; //ms
	//private static final int HEARTBEAT_INTERVAL = 500; //ms
	private static final int ROUTE_UPDATE_INTERVAL = 30000; //ms
	
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
		
		for (int i = 1; i < config.size(); i++){
			String[] temp = config.get(i).split(" ");
			neighbour_ports[i] = Integer.parseInt(temp[2]);
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
		Packets.add(Arrays.toString(mainPacket).replaceAll("\\[", "").replaceAll("\\]",""));
		
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
		
		//Start the timer bois
		Timer time1 = new Timer();
		Timer time2 = new Timer();
		time1.scheduleAtFixedRate(new LSPUpdate(mainSock, rePacks, numOfNeighbours, neighbour_ports), 0, UPDATE_INTERVAL);
		time2.scheduleAtFixedRate(new RouteUpdate(Packets), 0, ROUTE_UPDATE_INTERVAL);
		
		long saveTime = System.currentTimeMillis();
		while (true){
			int j = 0;
			//Just a system to clear the arraylist so it doesnt get too big
			if ((System.currentTimeMillis() - saveTime) > 2500){
				rePacks = null;
				saveTime = System.currentTimeMillis();
			}
			//Receiving the initial packets from neighbours
			while (j < numOfNeighbours){
				DatagramPacket nbPack = new DatagramPacket(new byte[1024], 1024);
				rcvdSock.receive(nbPack);
				byte[] nbPackB = nbPack.getData();
				String[] nbPackS = ByteAToStringA(nbPackB);
				rePacks.add(nbPackB);
				if (Packets.contains(Arrays.toString(nbPackS)) == false){
					Packets.add(Arrays.toString(nbPackS).replaceAll("\\[", "").replaceAll("\\]",""));
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
