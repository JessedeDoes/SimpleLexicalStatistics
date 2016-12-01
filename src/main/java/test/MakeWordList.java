package test;

import wordlist.TokenFilter;
import wordlist.WordListMaker;

/**
 * Arguments: &lt;index dir> &lt;token filterClass> &lt;argument of token filter constructor> &lt;metadataFilter>
 * 
 * @author does
 *
 */

public class MakeWordList
{
	public static void main(String[] args)
	{
		try
		{
			WordListMaker wlm = new WordListMaker(args[0]);
			Class  filterClass = Class.forName("wordlist.filter." + args[1]);
			wlm.setTokenFilter((TokenFilter) filterClass.getDeclaredConstructor(String.class).newInstance(args[2]));
			wlm.makeAndPrintWordList(args[3]);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
