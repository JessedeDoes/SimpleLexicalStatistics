package test;

import util.Counter;
import wordlist.TokenFilter;
import wordlist.WordListMaker;

public class MakeWordListWithCorpusQuery
{
	public static void main(String[] args)
	{
		try
		{
			WordListMaker wlm = new WordListMaker(args[0]);
			wordlist.filter.Word w = new wordlist.filter.Word();
			wlm.setTokenFilter(w);
			Counter<String> c = wlm.makeWordList(args[1] /* corpus */ , args[2] /* metadata */);
			WordListMaker.printTokenFrequencyList(c);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
