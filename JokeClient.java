/*--------------------------------------------------------
 *  
 * 1. Raj Arasu - 01/24/10:
 *
 * 2. >javac JokeServer.java
 *    >javac JokeClient.java
 *    >javac AdminClient.java
 *    >javac AdminServer.java
 *    >javac Message.java
 *    >javac JokeMessage.java
 *    >javac ProverbMessage.java
 *    >javac MaintenanceMessage.java
 *    >javac MessageFactory.java
 *
 * 3. Instructions to run this program:
 * 
 * In separate shell windows:
 *
 * > java JokeServer
 * > java JokeClient
 * > java AdminClient
 * 
 * All acceptable commands are displayed on the various consoles.
 * 
 * This runs across machines, in which case you have to pass the IP address of
 * the server to the clients. For exmaple, if the server is running at
 * 140.192.1.22 then you would type:
 *
 * > java JokeClient 140.192.1.22
 *
 * 4. Java version used: 
 *
 *    build 1.6.0.0
 *
 *
 *  5. Notes:
 *   
 *  Everything seems to work nicely. However failure tolerance can be buggy. It is assumed
 *  that if either the JokeClient or JokeServer terminate without using the appropriate 
 *  commands, they were exited abnormally.
 *
 *----------------------------------------------------------*/
 
import java.io.*; 
import java.net.*; 
import java.util.UUID;

public class JokeClient
{

	public static void main(String args[])
	{
		String serverName;
		String name = null; //name of client
		String command; 
		UUID uuid = null; //unique id that assosiates server NOTE: this is no longer needed after
				//addition of emailID

		String emailID = null; //id so multiple users can use different client on same machine

		if (args.length < 1)
			serverName = "localhost"; //if no arguments given default to 127.0.0.1
		else
			serverName = args[0];  //grab first command line arg 

		System.out.println("Raj Arasu's Joke Client.\n"); //indicate the program is running
		System.out.println("Using server: " + serverName + ", Port: 8080"); 

		FileInputStream fis = null;
		ObjectInputStream in = null;
		BufferedReader txtIn = null;

		//input stream from Standard In
		BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));

		//files for saving state to disk.	
		File diskUUID = null;
		File diskName = null;

		PrintStream logWriter = null;
		try
		{
			System.out.print("Please enter your email address: ");
			emailID = systemIn.readLine(); //read email id from user input	

			//create files for saving state to disk
			diskUUID = new File("client_state/" + emailID + "_client_uuid.o");
			diskName = new File("client_state/" + emailID + "_client_name.txt");

			//if both files exits read them in to memory from disk
			if(diskUUID.exists() && diskName.exists())
			{
				fis = new FileInputStream(diskUUID);
				in = new ObjectInputStream(fis);
				uuid = (UUID) in.readObject(); //read UUID object to memory
				in.close();
				fis = new FileInputStream(diskName);
				txtIn = new BufferedReader(new InputStreamReader(fis)); 
				name = txtIn.readLine(); //read user name to memory
				txtIn.close();
			}
			//otherwise assume there was no failure in client side
			else
			{
				System.out.print("\nEnter your name: ");
				name = systemIn.readLine(); //ask user for name
			}
			
		}
		catch(IOException ex) { ex.printStackTrace(); }
		catch(ClassNotFoundException cnf) { cnf.printStackTrace(); }

		PrintStream out = null;
		BufferedReader fromServer = null;
		Socket sock;
		try {

			//while the user does not type "quit"
			do
			{
				System.out.print("Print jokes/proverbs? (yes/quit): ");
				command = systemIn.readLine(); //get command from user input

				//create new connection with JokeServer
				sock = new Socket(serverName, 8080);

				//get i/o streams to and from server
				out = new PrintStream(sock.getOutputStream());
				fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				
				out.println(name); //send client name to JokeServer

				//register uuid with server
				uuid = registerUUID(uuid, serverName, fromServer, out);
				//write current state to disk
				writeStateToDisk(uuid, name, emailID);

				out.println(command); //send command that user input to server to get a joke or quit
				System.out.println("");

				//if user input if not "quit" go and ask the server to get a message
				if (command.indexOf("quit") < 0)
					getJokes(name, serverName, fromServer, out, sock, logWriter);
			}
			while (command.indexOf("quit") < 0);

			//delete state on disk since we are exiting normally
			diskUUID.delete();
			diskName.delete();

		}
		catch (IOException ioe) { 
			//ioe.printStackTrace(); 
		}  
			
	}

	
	//register uuid with JokeServer
	static UUID registerUUID(UUID id, String serverName, BufferedReader fromServer, PrintStream toServer)
	{

		UUID tmpUUID;

		if(id == null)
		{
			//if JokeClient has received a UUID sent "notset" to server
			toServer.println("notset");

			try{
				//get uuid from server
				String sID = fromServer.readLine();
				id = UUID.fromString(sID);
			}
			catch (Exception e) { System.out.println("Error registering client"); }
		}
		//otherwise send JokeServer uuid it has already been given
		else
			toServer.println(id.toString());
		
		return id;
	}		

	static void writeStateToDisk(UUID uuid, String name, String email)
	{
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		PrintStream pout = null;
		try
		{
			//save all state to disk: 1. client name, 2. uuid
			fos = new FileOutputStream("client_state/" + email + "_client_uuid.o");
			out = new ObjectOutputStream(fos);
			out.writeObject(uuid);
			out.close();
			pout = new PrintStream(new FileOutputStream("client_state/" + email + "_client_name.txt"));
			pout.println(name);
			pout.close();
		}
		catch(IOException ioe) { ioe.printStackTrace(); }
	}

	//get output from JokeServer in from of Joke, proverb, or maintenance 
	static void getJokes(String name, String serverName, BufferedReader fromServer, PrintStream toServer, Socket sock, PrintStream log)
	{
		String textFromServer;

		try
		{
			//while JokeServer is sending data
			while ((textFromServer = fromServer.readLine()) != null)
			{
				//if an empty string is returned from the server do not print it
				if (textFromServer != null) 
				{
					System.out.println(textFromServer); //print data from server
				}
			}
			
			sock.close(); //close connection with server
		}
		catch (IOException ioe) {
			System.out.print("Please enter quit: ");
			//ioe.printStackTrace();
		}
	}

}
