
public class Lrs {
	public static void main(String[] args) throws Exception{
		if (args.length != 3){
			System.out.println("Required arguements: [Node_ID] [Node_Port] [config.txt]\n");
			return;
		}
		char ID = args[0].charAt(0);
		int port = Integer.parseInt(args[1]);
		String file = args[2];
		
		System.out.println(ID + " " + port + " " + file + " ");
	}
}
