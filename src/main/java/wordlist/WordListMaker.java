package wordlist;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;

import util.Counter;
import util.StopWatch;
import blacklabapi.BlacklabFunctions;
import blacklabapi.MultiThreadedTokenVisitor;
import blacklabapi.PropertyFilter;
import blacklabapi.TokenHandler;
import blacklabapi.TokenVisitor;
//import nl.inl.blacklab.queryParser.lucene.LuceneQueryParser;
import nl.inl.blacklab.search.*;
//import nl.inl.blacklab.search.grouping.Group;
import nl.inl.blacklab.search.grouping.HitProperty;
import nl.inl.blacklab.search.grouping.HitPropertyMultiple;
import nl.inl.blacklab.search.grouping.RandomAccessGroup;
//import nl.inl.blacklab.search.lucene.SpanQueryPositionFilter.Filter;
import nl.inl.util.XmlUtil;


/**
 * Extracts word lists for a subcorpus defined by a filter
 * Still a bit messy.
 * <p>
 * To use it in a practical way, you override the filterToken method, which is a function of the basic token properties<br>
 * <ol>
 * <li>Initialize a WordListMaker object from an index directory or an already created Searcher object
 * <li>Call makeWordList (subcorpusDefiningFilter, array of relevant token properties)
 * </ol>
 * @author does
 *
 */
public class WordListMaker implements TokenFilter
{
	String indexDir = "n:/taalbank/werkfolder_redactie/jesse/workspace/blacklab/parole-index";
	public Searcher searcher = null;
	TokenFilter tokenFilter = this;
	PropertyFilter propertyFilter = null;
	
	public void setTokenFilter(TokenFilter t)
	{
		this.tokenFilter= t;
	}
	
	public WordListMaker(String indexdirectoryName) 
	{
		if (indexdirectoryName != null)
			indexDir = indexdirectoryName;
		try {
			openIndex();
			System.err.printf("Index %s opened...\n", indexdirectoryName);
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public WordListMaker(Searcher s)
	{
		searcher = s;
	}

	public void makeAndPrintWordList(String filter)
	{
		try 
		{
			printTokenFrequencyList(makeWordList(filter));
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		} finally 
		{
			System.err.println("closing....");
			//searcher.close();
		}
	}


	class MyTask extends MultiThreadedTokenVisitor.DocumentTask
	{
		public MyTask(MultiThreadedTokenVisitor multiThreadedTokenVisitor, int did)
		{
			multiThreadedTokenVisitor.super(did);
		};	
		
		public void handleToken(List<String> propertyNames, String[] propertyValues)
		{
			String z="";
			for (int i=0; i < propertyNames.size(); i++)
			{
			   z += propertyValues[i] + ((i < propertyNames.size()-1)?",":"");
			}
			partialCounter.increment(z);
		}
	}
	
	/**
	 * Default token filter: common lowercase nouns of sufficient length
	 * problem if tagging is a mess, especially for keywords extraction
	 */
	
	public String filterToken(List<String> propertyNames, String[] propertyValues)
	{
		String w="";
		
		for (int i=0; i < propertyNames.size(); i++)
		{
			String p = propertyNames.get(i);
			String v = propertyValues[i];
			
			if (p.equals("pos")   &&! v.contains("NOU-C")) // nouns only
			{
				return null;
			}
			if (p.equals("word")   && !
					(v.matches("^[a-z]+$") && (v.length() >= 5
					&& v.matches(".*[aeiou].*"))))
			{
				return null;
			}
			if (p.equals("lemma")) w = v;
		}
		return w.toLowerCase();
	}
	
	/**
	 * Implementation with corpus query is different, no multithreading
	 * @param corpusQuery
	 * @param metadataFilter
	 * @return
	 */
	
	public Counter<String> makeWordList(String corpusQuery, String metadataFilter)
	{
		final List<String> propList = Arrays.asList(tokenFilter.needsTokenProperties());
		final Counter<String> counter = new  Counter<String>();
		TokenVisitor tv = new TokenVisitor(searcher, propList);
		System.err.println("make list with tokens: " + corpusQuery  + " and docs: " + metadataFilter);
		TokenHandler th = new TokenHandler ()
		{
			@Override
			public void handleToken(List<String> propertyNames,String[] propertyValues)
			{
				String v =  tokenFilter.filterToken(propertyNames, propertyValues);
				if (v != null)
					counter.increment(v);
		   }
		};
		 tv.enumerateQueryResults(corpusQuery, metadataFilter, th);
		 return counter;
	}
	
	public  Counter<String> makeWordList(String metadataFilter) 
	{
		final List<String> propList = Arrays.asList(tokenFilter.needsTokenProperties());
		
		MultiThreadedTokenVisitor th  = new MultiThreadedTokenVisitor(searcher, propList)
		{
			@Override
			public String filterToken(List<String> propertyNames, String[] propertyValues)
			{
				return tokenFilter.filterToken(propertyNames, propertyValues);
			}
		};
	
		StopWatch s = new StopWatch();
		s.start();
		th.loopOverAllTokensinDocumentsSatisfyingMetadataFilter(metadataFilter); // dit deugt niet meer....., hij komt niet meer bij de handledocument methode....
		s.stop();
		return th.getEncompassingCounter();
	}

	protected void openIndex() throws CorruptIndexException, IOException {
		File f = new File(indexDir);
		
		if (!f.isDirectory())
		{
			System.err.println("gaat niet werken " + indexDir);
			return;
		}
		
		Hits.setDefaultMaxHitsToCount(Integer.MAX_VALUE);
		Hits.setDefaultMaxHitsToRetrieve(Integer.MAX_VALUE);
		searcher = Searcher.open(new File(indexDir));
		
		System.err.println("searcher opened from " + indexDir);
	}

	static public void printTokenFrequencyList(Counter<String> stringCounter)
	{
		for (String s: stringCounter.keyList())
		{
			System.out.println(s + "\t" + stringCounter.get(s));
		}
	}

	static String[] props = {"word", "lemma", "pos"}; 
		// AHA -- this is the catch....
	@Override
	public String[] needsTokenProperties()
	{
		// TODO Auto-generated method stub
		return props;
	}
}
