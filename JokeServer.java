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
import java.util.*;

class Worker extends Thread
{
	Socket sock;
	UUID uuid = null;

	Worker(Socket sock)
	{
		this.sock = sock; //take constructor's parameter and set to field
	}

	//run thread
	public void run()
	{
		PrintStream out = null;
		BufferedReader in = null;
		String clientName = null;
		try {
			out = new PrintStream(sock.getOutputStream()); //create stream to send data to client
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));  //create stream to read data from client
			
			//if server control variable is true 
			if (JokeServer.controlSwitch != true)
			{
				System.out.println("Listener is now shutting down as per client request."); //print this on the server
				//out.println("Server is now shutting down. Good day!"); // send this data to client
			}
			else 
			{
				try { 
					clientName = in.readLine(); //get the client name that was sent by the client
					uuid = registerUUID(sock, in, out); //register uuid send from client in hash table
					String command, tmpUUID; 
					
					command = in.readLine(); //get the command that was sent by the client

					//if JokeClient sends "yes"
				       	if (command.indexOf("yes") > -1)
					{
						System.out.println("Printing Message for  " + clientName); //print on server terminal

						//get a message (joke, proverb, maintenance) and send to client
						JokeServer.message.getMessage(clientName, out, uuid);
						//save state to disk
						writeStateToDisk();

						//debug info
						if(command.indexOf("print") > -1)
							printtable();
					}
					else if (command.indexOf("quit") > -1)
						out.println("shutting down client ...");
					else
						out.println("Please input valid command");
				}
				catch (IOException x) {
					System.out.println("Server read error");
					x.printStackTrace();
				}

			}
			sock.close(); //close connection with client
		}
		catch (IOException ioe) { System.out.println(ioe); } 
		catch (NullPointerException npe) {}
	}

	static void writeStateToDisk()
	{
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try
		{
			JokeServer.tableState = new File("server_state/message_state.o"); //create file for hash table object
			fos = new FileOutputStream(JokeServer.tableState);
			out = new ObjectOutputStream(fos);
			out.writeObject(JokeServer.statusTable); //write hash table object to disk
			out.close();
		}
		catch(IOException ioe) { ioe.printStackTrace(); }
	}

	static void printtable()
	{
		System.out.println(JokeServer.statusTable.toString());
	}
	
	
	//register uuid with hash table
	UUID registerUUID(Socket sock, BufferedReader in, PrintStream out)
	{
		UUID id = null;
		String idStatus = null;
		try{
			idStatus = (in.readLine()); //grab uuid from client. Either "notSet" or actual UUID
		}
		catch (Exception e) { e.printStackTrace(); System.out.println("failed to read in uuid from client"); }

		if(idStatus.equals("notset"))
		{
			//if client does not have a UUID create one and put it in the hashtable
			UUID randID = UUID.randomUUID();
			JokeServer.statusTable.put(randID, 0);
			String sID = randID.toString();

			try{
				out.println(sID); //send client UUID
			}
			catch(Exception e) { System.out.println("Error registering UUID"); }

			id = randID;
		}
		//otherwise client already has UUID. So use theirs
		else
			id = UUID.fromString(idStatus);

		return id;
	}


}

class JokeServer
{
	public static boolean controlSwitch = true; //control variable on whether to run server or not
	public static Hashtable<UUID, Integer> statusTable; //keep track of joke/proverb state with UUID as keys
	public static Message message;
	public static File tableState; //file to read in hashtable from disk
	
	public static void main(String args[]) throws IOException
	{
		int q_len = 6; //number of requests for OS to queue
		int port = 8080; //port that server will be listening on

		FileInputStream fis = null;
		ObjectInputStream in = null;

		try
		{
			tableState = new File("server_state/message_state.o");
			//if there is already a file then server must of terminated abnormally.
			//Therefore grab state from disk
			if(tableState.exists())
			{
				fis = new FileInputStream(tableState);
				in = new ObjectInputStream(fis);
				statusTable = (Hashtable<UUID, Integer>) in.readObject(); //read hash table into memory
				in.close();
			}
			//otherwise create a new hashtable and use it
			else
			{
				statusTable = new Hashtable<UUID, Integer>();
				System.out.println("file not found ex ");
			}

		}
		catch(IOException ex) { ex.printStackTrace(); }
		catch(ClassNotFoundException cnf) { cnf.printStackTrace(); }

		Socket sock;
		message = MessageFactory.getMode("joke-mode"); //default mode is "joke-mode"

		ServerSocket servsock = new ServerSocket(port, q_len); //create a server socket to wait for requests to come over network on port 8080

		System.out.println("Raj Arasu's Inet Server statring up, listening at port " + port + "\n"); //indicate server is starting

		//create AdminServer thread to listen for admin commands.
		//This is done so there can be two blocking commands
		AdminServer adminServer = new AdminServer();
		Thread as = new Thread(adminServer);
		as.start();
		
		//while server is allowed to run
		while(controlSwitch)
		{
			sock = servsock.accept(); //block until a conncection request comes and accept it
			new Worker(sock).start(); //spawn Worker thread with the current socket
		}
		//delete hashtable state
		tableState.delete();
		System.out.println("Server is now down.");
	}
	
			
}

