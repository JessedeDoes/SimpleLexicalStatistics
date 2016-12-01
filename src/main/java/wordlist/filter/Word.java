package wordlist.filter;

import java.util.List;

import wordlist.TokenFilter;

public class Word implements TokenFilter
{
	String posPattern  = "NOU-C";
	static String[] props = {"word"};
	boolean allowAll = false;
	
	public Word(String posPattern)
	{
		this.posPattern = posPattern;
		if (posPattern.equals("-") || posPattern.equals("*"))
			allowAll = true;
	}
	
	public Word()
	{
		allowAll = true;
	}
	@Override
	public String filterToken(List<String> propertyNames,
			String[] propertyValues)
	{
		String w ="?";
		for (int i=0; i < propertyNames.size(); i++)
		{
			String p = propertyNames.get(i);
			String v = propertyValues[i];
			
			if (!allowAll && p.equals("pos")   &&! v.startsWith(posPattern))// nouns only
			{
				return null;
			}
		
			if (p.equals("word"))
				w = v;
		}
		return w;
	}
	
	public String[] needsTokenProperties()
	{
		return props;
	}
}
