package test;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.StringUtils;
import nl.inl.blacklab.search.Searcher;
import blacklabapi.DocumentContentHandler;
import blacklabapi.TokenVisitor;

/**
 * Dumps plain text of selected / all tweets <br>
 * We try to dump only unique tweets - better for distributional, &  topic modeling, etc
 * @author does
 *
 */

public class DumpTweets implements DocumentContentHandler
{
	String[] properties = {"word", "punct"};
	Searcher searcher;
	int  nDuplicates =0;
	int nTweets = 0;
	
	Set<String> seenThisTweetBefore = new HashSet<String>();
	
	public DumpTweets(Searcher searcher)
	{
		this.searcher = searcher;
	}
	
	public DumpTweets(String dir)
	{
		try
		{
			this.searcher = Searcher.open(new File(dir));
			System.err.println("Searcher opened from " + dir);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void dump(String metadataQuery)
	{
		TokenVisitor tv = new  TokenVisitor(searcher, Arrays.asList(properties));
		tv.loopOverAllTokensinDocumentsSatisfyingMetadataFilter(metadataQuery, null, this);
	}
	
	@Override
	public boolean handleDocumentContent(String idno, List<String> propertyNames,
			List<List<String>> propertyValuesPerProperty)
	{
		List<String> words=null, punct=null;
		for (int i=0; i <  propertyNames.size(); i++)
		{
			String p = propertyNames.get(i);
			if (p.equals("word"))
				words = propertyValuesPerProperty.get(i);
			else if (p.equals("punct"))
				punct = propertyValuesPerProperty.get(i);
		}
		String md5 = twitter.Utilities.getTweetMD5(words, punct);
		if (!this.checkSeenBefore(md5))
			System.out.println(idno + "\t" + StringUtils.join(propertyValuesPerProperty.get(0),  " "));
		nTweets++;
		return true;
	}
	
	synchronized boolean checkSeenBefore(String md5)
	{
		if (this.seenThisTweetBefore.contains(md5))
		{
			nDuplicates++;
			if (nDuplicates % 1000 == 0)
				System.err.println("nDuplicates: " + nDuplicates  + " nTweets: " + nTweets);
			return true;
		}
		this.seenThisTweetBefore.add(md5);
		return false;
	}
	
	public static void main(String[] args)
	{
		DumpTweets dd = new DumpTweets(args[0]);
		dd.dump(args[1]);
	}
}
