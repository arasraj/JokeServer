//returns a Message based on AdminClient mode input
public class MessageFactory
{
	private MessageFactory() {}

	private static Message joke = null;
	private static Message proverb = null;
	private static Message maintenance = null;

	public static Message getMode(String mode)
	{
		Message sendMode;

		if(mode.equals("joke-mode"))
			sendMode =  getJokeMessage();
		else if (mode.equals("proverb-mode"))
			sendMode =  getProverbMessage();
		else
			sendMode = getMaintenanceMessage();
		return sendMode;
	}

	//if a JokeMessage does not already exist create one otherwise
	//use existing one
	private static Message getJokeMessage()
	{
		if(joke == null)
		{
			joke = new JokeMessage();
		}		
		return joke;
	}

	private static Message getProverbMessage()
	{
		if(proverb == null)
		{
			proverb = new ProverbMessage();
		}
		return proverb;
	}

	private static Message getMaintenanceMessage()
	{
		if(maintenance == null)
		{
			maintenance = new MaintenanceMessage();
		}
		return maintenance;
	}
}
