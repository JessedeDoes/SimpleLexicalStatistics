package test;

import wordlist.SubcorpusComparison;
import wordlist.SubcorpusComparison.PairOfFrequencies;

public class TestSubcorpusComparison
{
	public static void main(String[] args)
	{
		SubcorpusComparison flc = new SubcorpusComparison();
		flc.setStrict(true);
		for (PairOfFrequencies pof: flc.compareSubcorpora(args[0], args[1], args[2]))
		{
			System.out.println(pof);
		}
	}
}
