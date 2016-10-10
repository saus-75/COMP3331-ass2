import java.io.*;
import java.net.*;
import java.util.*;

public class Lrs {
	private static final int UPDATE_INTERVAL = 1000; //ms
	private static final int HEARTBEAT_INTERVAL = 500; //ms
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
		
		//config file to ArrayList
		ArrayList<String> config = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null){
			config.add(line);
		}
		br.close();
		
		//Neighbour data init
		int neighbourCount = 0;
		String[] configS = new String[config.size()-1];
		
		//Data sorting
		int i = 0;
		int j = 0;
		while (i < config.size()){
			if (i == 0){
				neighbourCount = Integer.parseInt(config.get(i));
			} else {
				configS[j] = config.get(i);
				j++;
			}
			i++;
		}
		
		
		//initialization
		DatagramSocket node = new DatagramSocket(port);
		
		while (true){
			
		}
			
	}
	
	//used to split up config
	public static String[] dataSplitter(String data){
		String[] splitted = data.split(" ");
		
		return splitted;
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
