import java.io.*;
import java.util.*;

//interface to implement state pattern
public interface Message
{
	public void getMessage(String s, PrintStream ps, UUID u);
}
