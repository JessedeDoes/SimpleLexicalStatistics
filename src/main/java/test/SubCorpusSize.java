package test;

import java.io.File;

import blacklabapi.BlacklabFunctions;
import nl.inl.blacklab.search.Searcher;
import wordlist.WordListMaker;

public class SubCorpusSize
{
	public static void main(String[] args)
	{
		try
		{
			Searcher searcher = Searcher.open(new File(args[0]));
			String filter = args[1];
			int[] td =BlacklabFunctions.getSubcorpusSize(searcher, filter);
			if (td[0] > 0)
			{
				System.out.println("filter " +  filter + " tokens: "+ td[0] + " docs: " +  td[1]);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
