package blacklabapi;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.inl.blacklab.queryParser.corpusql.CorpusQueryLanguageParser;
import nl.inl.blacklab.search.Hits;
import nl.inl.blacklab.search.Searcher;
import nl.inl.blacklab.search.TextPattern;
import nl.inl.util.LuceneUtil;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
// import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.Version;

import util.Counter;

public class BlacklabFunctions 
{


	public static void removeLimits()
	{
		Hits.setDefaultMaxHitsToCount(Integer.MAX_VALUE);
		Hits.setDefaultMaxHitsToRetrieve(Integer.MAX_VALUE);
	}

	public static SpanQuery getQuery(Searcher searcher, String corpusQlQuery, String filterQueryString) throws ParseException, nl.inl.blacklab.queryParser.corpusql.ParseException
	{
		SpanQuery combined;
		TextPattern pattern = CorpusQueryLanguageParser.parse(corpusQlQuery);

		if (filterQueryString != null && filterQueryString.length() > 0)
		{
                     // new StandardAnalyzer(Version.LUCENE_CURRENT)
			Query filterQuery = parseLuceneQuery(filterQueryString, "");
			QueryWrapperFilter filter = new QueryWrapperFilter(filterQuery);
			combined = searcher.createSpanQuery(pattern, filter);
		} else
		{
			combined = searcher.createSpanQuery(pattern);
		}
		return combined;
	}

	public static Query parseLuceneQuery(String s, String m)
	{
		Analyzer a = new StandardAnalyzer();
		try {
			return LuceneUtil.parseLuceneQuery(s, a, m);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Hits filteredSearch(Searcher searcher, String corpusQlQuery, String filterQueryString) 
			throws ParseException, nl.inl.blacklab.queryParser.corpusql.ParseException 
	{
		SpanQuery combined;
		TextPattern pattern = CorpusQueryLanguageParser.parse(corpusQlQuery);

		if (filterQueryString != null && filterQueryString.length() > 0)
		{
			Query filterQuery = parseLuceneQuery(filterQueryString, "");
			QueryWrapperFilter filter = new QueryWrapperFilter(filterQuery);
			combined = searcher.createSpanQuery(pattern, filter);
		} else
		{
			combined = searcher.createSpanQuery(pattern);
		}

		// Execute the TextPattern

		Hits hits = searcher.find(combined); // dit is niet optimaal...
		return hits;
	}

	public static Spans getSpans(SpanQuery q, Searcher s)
	{


		IndexReader reader = s.getIndexReader();
		//System.err.println("Reader: " + reader + " docs: " + reader.numDocs());
		Spans spans = null;
		try
		{
			//this is not the best way of doing this, but it works for the example.  See http://www.slideshare.net/lucenerevolution/is-your-index-reader-really-atomic-or-maybe-slow for higher performance approaches
			LeafReader wrapper = SlowCompositeReaderWrapper.wrap(reader);
			Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
			spans = q.getSpans(wrapper.getContext(), new Bits.MatchAllBits(reader.numDocs()), termContexts);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return spans;
	}


	public static Spans filteredSearchSpans(Searcher searcher, String corpusQlQuery, String filterQueryString) throws ParseException, nl.inl.blacklab.queryParser.corpusql.ParseException 
	{
		SpanQuery combined;
		TextPattern pattern = CorpusQueryLanguageParser.parse(corpusQlQuery);

		if (filterQueryString != null && filterQueryString.length() > 0)
		{
			Query filterQuery = parseLuceneQuery(filterQueryString, "");
			QueryWrapperFilter filter = new QueryWrapperFilter(filterQuery);
			combined = searcher.createSpanQuery(pattern, filter);
		} else
		{
			combined = searcher.createSpanQuery(pattern);
		}

		// Execute the TextPattern


		//System.err.println("spanquery " + combined);
		Spans spans;
		try 
		{
			SpanQuery rewritten = (SpanQuery) combined.rewrite(searcher.getIndexReader());
			//Set<Term> terms = new HashSet<Term>();
			//rewritten.extractTerms(terms);
			System.err.println("rewritten query: " + rewritten);

			Spans s = getSpans(rewritten, searcher);
			//spans = rewritten.getSpans(searcher.getIndexReader());
			return s;
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	static Map<String, int[]> subcorpusSizeCache = new HashMap<String, int[]>();
	
	public static int[] getSubcorpusSize(Searcher searcher, String metadataFilter)
	{
		String[] properties  = {"word"};
		String key = null;
		try
		{
			key = searcher.getIndexDirectory().getCanonicalPath()  +  "###"  + metadataFilter;
			int[] cached = subcorpusSizeCache.get(key);
			if (cached != null)
				return cached;
		} catch (Exception e)
		{
			
		}
		final List<String> propList = Arrays.asList(properties);
		MultiThreadedTokenVisitor th  = new MultiThreadedTokenVisitor(searcher,propList);
		int[] r = th.getNumberOfDocumentsAndTokensForFilter(metadataFilter);
		subcorpusSizeCache.put(key, r);
		return r;
	}

	public static String singleWordQuery(String property, String value) // or actually, a phrase
	{
		String q="";
		for (String v: value.split("\\s+"))
		{
			String valueEscaped  = v;
			if (value.contains("-"))
			{
				valueEscaped = v.replaceAll("-" , "\\-");
				
				System.err.println("escaped: " + valueEscaped);
			}
			q += "[" + property + "=\"" + valueEscaped + "\"]"; 
		}
		return q;
	}

	public static Counter<String> listLookup(Searcher searcher, String[] wordQueries, String filter)
	{
		return listLookup(searcher, wordQueries, filter, "word");
	}
	
	public static Counter<String> listLookup(Searcher searcher, String[] wordQueries, String filter, String property)
	{
		IndexSearcher isearcher = new IndexSearcher(searcher.getIndexReader());
		Counter<String> corpusFrequencies = new Counter<String>();
		for (String q: wordQueries)
		{
			if (q   == null ||  q.trim().length() == 0)
				continue;
			SpanQuery spanQuery = null;
			try
			{
				spanQuery = getQuery(searcher, singleWordQuery(property, q.trim()), filter);
				SpanQuery rewritten = (SpanQuery) spanQuery.rewrite(searcher.getIndexReader());
				spanQuery = rewritten;
				Spans s = getSpans(spanQuery, searcher);
				//s
				int k=0;
				//s.
				//System.err.println(s);
				if (s== null)
					continue;
				while (s.nextDoc() != Spans.NO_MORE_DOCS) 
				{ 
					while (s.nextStartPosition() != Spans.NO_MORE_POSITIONS)
					k++;
				}
				
				corpusFrequencies.increment(q,k);
			} catch (Exception e)
			{
				System.err.println("some problem with " + spanQuery);
				e.printStackTrace();
			}
		}
		return corpusFrequencies ;
	}

	static Counter<String> docFreqs = new Counter<String>();

	public static synchronized int getDocumentFrequency(Searcher searcher,  String property,  String w)
	{
		String fullIndexName = "contents%" + property  + "@i";
		Term t = new Term(fullIndexName,  w.toLowerCase());
		if (docFreqs.get(w) > 0)
			return docFreqs.get(w);
		try
		{
			int d = searcher.getIndexReader().docFreq(t);
			docFreqs.put(w, d);
			return d;
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
}
