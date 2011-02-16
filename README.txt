1. General

Simply put, the Joke Server application allows multiple clients to connect to a server and ask it for jokes and/or proverbs depending on the mode the server is in. Each particular client receives their own joke/proverb chosen at random.  It is possible to change the server mode via and admin channel. There is no GUI to this application. So, all the fun is achieved through the command line.  It demonstrates and solves some of the common problems associated with distributed systems using a simple client, server model.  The highlights of this applicatoin include:

-Can run simultaneously with different clients on different machines
-Can return at 5 jokes and 5 proverbs
-Inserts the client's name into jokes
-Returns jokes randomly
-Allows for an Admin to connect to an admin server securely via SSL.
-Allows Admin to switch between 3 modes: Joke, Proverb and Maintenance modes
-Does not repeat and jokes or proverbs until all have been seen by a particular client
-Fault Tolerant(buggy): writes state of client to disk in the case of failure
-Uses a 10 bits to keep state of jokes/proverbs seen by client
-Makes use of non-blocking calls
-Multi-threaded server

2. List of files:

JokeServer.java
JokeClient.java
AdminClient.java
AdminServer.java
Message.java
JokeMessage.java
ProverbMessage.java
MaintenanceMessage.java
MessageFactory.java
 
3. Instructions to run this program:
  
  In separate shell windows:
 
  > java JokeServer
  > java JokeClient
  > java AdminClient
  
  All acceptable commands are displayed on the various consoles.
  
  This runs across machines, in which case you have to pass the IP address of
  the server to the clients. For exmaple, if the server is running at
  140.192.1.22 then you would type:
 
  > java JokeClient 140.192.1.22
 
4. Java version used:  build 1.6.0.0
 
5. Notes:
    
   Everything seems to work nicely. However failure tolerance can be buggy. It is assumed
   that if either the JokeClient or JokeServer terminate without using the appropriate 
   commands, they were exited abnormally. Client and Server states are dumped into  client_state
   and server_state folders respectively. 
