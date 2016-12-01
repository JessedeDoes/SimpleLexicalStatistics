package twitter;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import util.StringUtils;


public class Utilities
{
	public static String getPlainTextMD5(String s)
	{
	
		try 
		{
			byte[] bytesOfMessage = s.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(bytesOfMessage);
			StringBuffer hexString = new StringBuffer();
			
			for (int i=0;i<thedigest.length;i++) 
			{
				hexString.append(Integer.toHexString(0xFF & thedigest[i]));
			}

			return hexString.toString();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		return "oompf";
	}
	
	public static String getTweetMD5(List<String> words, List<String> punct)
	{
		List<String> includedWords = new ArrayList<String>();
		for (int i=0; i < words.size(); i++)
		{
			String w = words.get(i).toLowerCase();
			String p = punct.get(i);
			if (p.contains("#") || p.contains("@") || w.contains("http"))
			{
				
			} else
			{
				includedWords.add(w);
			}
		}
		String s = StringUtils.join(includedWords, " ");
		return getPlainTextMD5(s);
	}
}
