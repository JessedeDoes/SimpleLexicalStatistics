package test;

import java.io.File;

import util.Counter;

import nl.inl.blacklab.search.Searcher;
import blacklabapi.BlacklabFunctions;

public class ListLookup
{
	public static void main(String[] args)
	{
		try
		{
			Searcher searcher = Searcher.open(new File(args[0]));
			String property = args[1];
			Counter<String>  c = Counter.readFromFile(args[2]);
			String[] A = new String[c.size()];
			String[] B = c.keyList().toArray(A);
			Counter<String> results = BlacklabFunctions.listLookup(searcher, B, null, property);
			for (String s:  results.keySet())
			{
				System.out.println(s + "\t" +  results.get(s));
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
