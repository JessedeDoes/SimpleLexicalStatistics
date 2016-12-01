package test;

import java.io.File;

import blacklabapi.BlacklabFunctions;
import nl.inl.blacklab.search.Searcher;
import wordlist.WordListMaker;

public class TokensAndDocsPerYear
{
	public static void main(String[] args)
	{
		try
		{
			Searcher searcher = Searcher.open(new File(args[0]));
			int totalDocs = 0;
			int totalTokens = 0;
			for (int i=1800; i < 2015; i++)
			{
				String filter = "witnessYear_from:" + i;
				int[] td =BlacklabFunctions.getSubcorpusSize(searcher, filter);
				if (td[0] > 0)
				{
					System.out.println("year: " +  i + " tokens: "+ td[0] + " docs: " +  td[1]);
					totalDocs += td[1];
					totalTokens += td[0];
				}
			}
			System.out.println("total  tokens: "+ totalTokens + " docs: " +  totalDocs);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
