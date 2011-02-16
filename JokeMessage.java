import java.util.*;
import java.io.*;


public class JokeMessage implements Message
{
	String[] jokes = new String[5];

	public JokeMessage()
	{
		//read jokes in from file
		initJokes();
	}
		
	//interface method to get message
	public void getMessage(String clientName, PrintStream out, UUID uuid)
	{
		printJoke(clientName, out, uuid);

	}

	//read in jokes from jokes.txt and store them in an array
	public void initJokes()
	{
		String joke;
		try
		{
			BufferedReader in = new BufferedReader(new FileReader("jokes.txt"));
			for (int i=0; (joke = in.readLine()) != null; i++)
				jokes[i] = joke;
		}
		catch (IOException ioe) { System.out.println(ioe); }
	}

	//print out jokes to client
	public void printJoke(String name, PrintStream out, UUID uuid)
	{
		int jokeIndex = getRandomJoke(uuid); //get random index into joke array
		String joke = jokes[jokeIndex];
		//perform name subs
		joke = joke.replaceAll("#name", name);
		try { 
			out.println(joke); //send joke out to client
		}
		catch (Exception e) { System.out.println("Error printing joke");}
		setJokeStatus(jokeIndex, uuid); //tell hashtable this joke as been used
	}

	public int getRandomJoke(UUID uuid)
	{
		Integer status = JokeServer.statusTable.get(uuid); //grab integer state using uuid as key
		ArrayList<Integer> activeJokes = new ArrayList<Integer>(); // create list to store jokes that have not been used.

		//if first five bits are all 1's then all jokes have been given.
		//So reset to O's
		if((status & 0x1F) == 0x1F)
		{
			status = status & 0x3E0;
			JokeServer.statusTable.put(uuid, status);
			status = 0;
		}

		//grab first five bits
		status = status & 0x1F;
		int tmpStatus;

		//loop through the five bits and if they are 0 add them to jokes list
		for (int i=0; i<5; i++)
		{
			tmpStatus = status & 0x1;
			if(tmpStatus == 0)
				activeJokes.add(Integer.valueOf(i));
			status = status >> 1;
		}

		//get random number to index into jokes array based on activeJokes size
		Random generator = new Random();
		int randomIndex = generator.nextInt(activeJokes.size());

		System.out.println("size === " + activeJokes.size());

		return activeJokes.get(randomIndex);
	}

	//set jokes status in hashtable
	public void setJokeStatus(int index, UUID uuid)
	{
		Integer status = JokeServer.statusTable.get(uuid);
		status = (0x1 << index) | status;
		JokeServer.statusTable.put(uuid, status);
	}
}
