import java.util.*;
import java.io.*;


//see JokesMessage.java comments
public class ProverbMessage implements Message
{
	String[] proverbs = new String[5];

	public ProverbMessage()
	{
		initProverbs();
	}
		
	public void getMessage(String clientName, PrintStream out, UUID uuid)
	{
		printProverb(clientName, out, uuid);

	}

	public void initProverbs()
	{
		String proverb;
		try
		{
			BufferedReader in = new BufferedReader(new FileReader("proverb.txt"));
			for (int i=0; (proverb = in.readLine()) != null; i++)
				proverbs[i] = proverb;
		}
		catch (IOException ioe) { System.out.println(ioe); }
	}

	public void printProverb(String name, PrintStream out, UUID uuid)
	{
		int proverbIndex = getRandomProverb(uuid);
		String proverb = proverbs[proverbIndex];
			proverb = proverb.replaceAll("#name", name);
		try { 
			out.println(proverb); 
		}
		catch (Exception e) { System.out.println("Error printing proverb");}
		setProverbStatus(proverbIndex, uuid);
	}

	public int getRandomProverb(UUID uuid)
	{
		Integer status = JokeServer.statusTable.get(uuid);
		if((status & 0x3E0) == 0x3E0)
		{
			status = status & 0x1F;
			JokeServer.statusTable.put(uuid, status);
			status = 0;
		}

		System.out.println("status ----- " + status);
		ArrayList<Integer> activeProverbs = new ArrayList<Integer>();
		status = status & 0x3E0;
		status = status >> 5;
		int tmpStatus;

		for (int i=0; i<5; i++)
		{
			tmpStatus = status & 0x1;
			if(tmpStatus == 0)
				activeProverbs.add(Integer.valueOf(i));
			status = status >> 1;
		}

		Random generator = new Random();
		int randomIndex = generator.nextInt(activeProverbs.size());
		System.out.println("size --- " + activeProverbs.size());

		return activeProverbs.get(randomIndex);
	}

	public void setProverbStatus(int index, UUID uuid)
	{
		Integer status = JokeServer.statusTable.get(uuid);
		status = (0x1 << (index + 5)) | status;
		JokeServer.statusTable.put(uuid, status);
	}
}
