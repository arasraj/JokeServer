import java.net.*;
import java.io.*;
import java.util.*;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocketFactory;

class AdminWorker extends Thread
{
	Socket sock;

	AdminWorker(Socket sock)
	{
		this.sock = sock; //takes socket created from AdminServer
	}

	public void run()
	{
		PrintStream out = null;
		BufferedReader in = null;

		try
		{
			out = new PrintStream(sock.getOutputStream()); //create stream to send data out of socket
			in = new BufferedReader(new InputStreamReader(sock.getInputStream())); //create stream to read data in from sock

			String mode = in.readLine(); //reads in first line sent from AdminClient

			//if AdminClient sends "shutdown", turn controlSwitch flag in JokeServer to false
			//else change the mode the the Jokeclient receives
			if(mode.indexOf("shutdown") < 0)
			{
				out.println("Setting mode to " + mode);
				setMode(mode); //takes a string based on AdminClient input and changes mode
			}
			else
			{
				JokeServer.controlSwitch = false;
				out.println("Server shutting down... ");
			}

			sock.close(); //close the socket
		}
		catch (IOException ioe) { System.out.println(ioe); }
	}

	//sets static var in JokeServer to mode requested by AdminClient.
	//This is done by getting a new Message from the static class MessageFactory.
	//A Message is an interface which can be a JokeMessage, ProverbMessage, or 
	//MaintenanceMessage.
	void setMode(String mode)
	{
		JokeServer.message = MessageFactory.getMode(mode);
	}
}


//Thread started from JokeServer.
//This is done so modes can be changed and 
//JokeClients and recieve jokes simultaneously
public class AdminServer implements Runnable
{
	public void run()
	{
		//System.setProperty("javax.net.debug", "all");
		System.out.println("Admin thread started ... ");
		
		//set these properties so they do not have to be included as command line
		//args.
		System.setProperty("javax.net.ssl.trustStore", "serverKeyStore");
		System.setProperty("javax.net.ssl.trustStorePassword", "csc435");

		int q_len = 6;
		int port = 1565;

		try
		{
			//create ssl server socket factory from which you create a server socket
			SSLServerSocketFactory sslSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			SSLServerSocket sslServerSock = (SSLServerSocket) sslSocketFactory.createServerSocket(port, q_len);
			
			//neccessary since I am using a self-signed cert.
			sslServerSock.setEnabledCipherSuites(sslSocketFactory.getSupportedCipherSuites());
			SSLSocket sslSocket;

			//wait from AdminClient connection.
			//when there is a connection spawn new worker thread
			while(JokeServer.controlSwitch)
			{
				sslSocket = (SSLSocket) sslServerSock.accept();
				new AdminWorker(sslSocket).start();
			}
		}
		catch (IOException ioe) { System.out.println(ioe); }
	}
}
