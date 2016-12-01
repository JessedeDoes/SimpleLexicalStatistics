package test;
import java.io.*;

public class RemoveIllegalUnicode
{
	public static boolean CheckIllegalCharacters(String s)
	{
		for (int i=0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (c == 133)
			{
				System.err.println("oink!");
				return false;
			}
			if (Character.getType(s.charAt(i)) == Character.OTHER_SYMBOL)
			{
				//System.err.printf("dangerous character in %s\n", s);
				//return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args)
	{
		InputStreamReader is = null;
		BufferedReader in = null;
		try 
		{ 
			is = new InputStreamReader(new FileInputStream(args[0]), "UTF-8");
			in = new BufferedReader(is);
			String line=null;
			while ((line = in.readLine()) != null)
			if (CheckIllegalCharacters(line))
			{
				System.out.println(line);
			}  else
			{
				System.err.println("llegal characters in " + line);
			}
		} catch (Exception e) { e.printStackTrace();};
	}
}
