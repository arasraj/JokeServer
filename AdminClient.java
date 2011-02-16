import java.io.*;
import java.net.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class AdminClient
{
	public static void main(String args[])
	{
		//set system properties so they do not have to be included as command line args.
		System.setProperty("javax.net.ssl.keyStore", "serverKeyStore");
		System.setProperty("javax.net.ssl.keyStorePassword", "csc435");

		String serverName;

		//if program run with no args set serverName to localhost
		//otherwise set it to the arg
		if(args.length < 1)
			serverName = "localhost";
		else
			serverName = args[0];

		System.out.println("Starting Admin Client ...");
		
		PrintStream out = null;
		BufferedReader in = null;
		
		//create input stream from Standard In.
		BufferedReader sysIn = new BufferedReader(new InputStreamReader(System.in));

		try
		{
			String mode = null;
			SSLSocketFactory sslSocketFactory = null;	
			SSLSocket sslSocket;

			//needed to make shutdown run more smoothly
			boolean shutdown = false;

			do
			{
				//if the input from the keyboard is not "shutdown" then print instructions
				//to change mode, ect...
				if(!shutdown)
				{
					System.out.println("\nEnter mode (joke-mode, proverb-mode, maintenance-mode");
					System.out.println("Use quit to close admin client or shutdown to close server)");
					System.out.println(" ....... \n");
					System.out.print("Mode: ");
				}
				else
				{
					System.out.print("Please enter shutdown command again to completely close server: ");
					mode = sysIn.readLine();
					System.out.print("Enter quit to shutdown admin client: ");
				}

				mode = sysIn.readLine(); //read command from user
				
				//create SSL to use with AdminServer
				sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				sslSocket = (SSLSocket) sslSocketFactory.createSocket(serverName, 1565);
				sslSocket.setEnabledCipherSuites(sslSocketFactory.getSupportedCipherSuites());

				out = new PrintStream(sslSocket.getOutputStream()); //create output stream over ssl
				in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream())); // create input stream over ssl

				out.println(mode); //send the mode input by user to AdminServer

				System.out.println("sending to admin ...");
				String response = in.readLine(); //get the response from the AdminServer and print it out
				System.out.println(response);

				//if user types in "shutdown" on next iteration of loop print
				//instructions on how to do so
				if(mode.indexOf("shutdown") > -1)
				{
					shutdown = true;
				}
				
			}
			while(mode.indexOf("quit") < 0); //loop while user does not type "quit"
		}
		catch (IOException ioe) { System.out.println(ioe); } 
	}	
}
