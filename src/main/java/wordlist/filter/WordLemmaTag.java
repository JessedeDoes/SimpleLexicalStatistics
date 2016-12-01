package wordlist.filter;

import java.util.List;

import wordlist.TokenFilter;

public class WordLemmaTag implements TokenFilter
{
	String posPattern  = "NOU-C";
	static String[] props = {"word","lemma", "pos"};
	boolean allowAll = false;
	
	public WordLemmaTag(String posPattern)
	{
		this.posPattern = posPattern;
		if (posPattern.equals("-") || posPattern.equals("*"))
			allowAll = true;
	}
	
	@Override
	public String filterToken(List<String> propertyNames,
			String[] propertyValues)
	{
		String w ="";
		String lemma="";
		String pos = "";
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
			if (p.equals("lemma"))
				lemma= v;
			if (p.equals("pos"))
				pos = v;
		}
		String r = w + "\t"  + lemma + "\t"  + pos;
		return r;
	}
	
	public String[] needsTokenProperties()
	{
		return props;
	}
}
