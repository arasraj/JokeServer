import java.io.*;
import java.util.*;	

public class MaintenanceMessage implements Message
{
	public void getMessage(String clientName, PrintStream out, UUID uuid)
	{
		out.println("Sorry " + clientName + " we are in maintenance mode");
	}
}
