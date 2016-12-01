package blacklabapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.inl.blacklab.forwardindex.ForwardIndex;
import nl.inl.blacklab.forwardindex.Terms;
import nl.inl.blacklab.search.Searcher;
import nl.inl.util.LuceneUtil;

import org.apache.lucene.document.Document;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.spans.Spans;

import util.Counter;

/**
 * Rather a messy class, which does things which are also handled by Blacklab's grouping mechanism on a lower level for the sake of efficiency 
 * 
 * <p>
 * The purpose is twofold: 
 * <ul>
 * <li>to collect <i>all</i> words in documents with certain metadata criteria as efficiently as possible 
 * (which is why the multithreaded subclass is there)
 * <li>To do so for words also satisfying a certain content query
 * </ul>
 */
public class TokenVisitor
{
	Searcher searcher;

	public int nTokens = 0;
	public int nDocs = 0;

	List<ForwardIndex> forwardIndexes = new ArrayList<ForwardIndex>();
	List<Terms> termsList = new ArrayList<Terms>();
	List<String> propertyNames = new ArrayList<String>();

	public Map<String,String> tokenProperties = new HashMap<String,String>();
	public String tokenAsString;

	boolean verbose = false;
	boolean async= true;

	int nThreads = Runtime.getRuntime().availableProcessors()-1;

	Map<String,Counter<String>> counterMap = new HashMap<String,Counter<String>>();

	public PropertyFilter propertyFilter = null;

	public TokenVisitor()
	{

	}


	private TokenVisitor(Searcher searcher)
	{
		this.searcher = searcher;
	}

	public TokenVisitor(Searcher searcher, List<String> properties)
	{
		this.searcher = searcher;
		for (String s: properties)
		{ 
			ForwardIndex fi = searcher.getForwardIndex("contents%" + s);
			forwardIndexes.add(fi);
			termsList.add(fi.getTerms());
			propertyNames.add(s);
		}
	}

	public TokenVisitor(Searcher searcher, String propertyNames)
	{
		this.searcher = searcher;
		for (String s: propertyNames.split(","))
		{ 
			ForwardIndex fi = searcher.getForwardIndex("contents%" + s);
			forwardIndexes.add(fi);
			termsList.add(fi.getTerms());
			this.propertyNames.add(s);
		}
	}

	public void enumerateQueryResults(String corpusQuery, TokenHandler tokenHandler)
	{
		try
		{
			Spans spans = BlacklabFunctions.filteredSearchSpans(searcher, corpusQuery, "");
			this.enumerateSpans(spans, tokenHandler);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void enumerateQueryResults(String corpusQuery, String metadataFilter, TokenHandler tokenHandler)
	{
		try
		{
			Spans spans = BlacklabFunctions.filteredSearchSpans(searcher, corpusQuery, metadataFilter);
			this.enumerateSpans(spans, tokenHandler);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void enumerateSpans(Spans spans, TokenHandler tokenHandler)
	{
		try 
		{
			int did,  tid;
			while ((did = spans.nextDoc()) != Spans.NO_MORE_DOCS)
			{
				while ((tid = spans.nextStartPosition()) != Spans.NO_MORE_POSITIONS)
				{
					int doc = spans.docID();
					int start = spans.startPosition();
					int end = spans.endPosition();

					String [] propertyValues = new String[propertyNames.size()];
					for (int i=0; i < propertyNames.size(); i++)
					{
						ForwardIndex fi = this.forwardIndexes.get(i);
						Terms t = this.termsList.get(i);
						String p = this.propertyNames.get(i);
						int fiid = fi.luceneDocIdToFiid(doc);

						String v =  retrieveTokenContentAt(fi, t,  fiid, start, end);
						propertyValues[i] = v;
					}
					if (tokenHandler != null)
						tokenHandler.handleToken(propertyNames, propertyValues);
				}
			}
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public int[] getNumberOfDocumentsAndTokensForFilterOld(String filterQueryString)
	{
		int nDocs = 0;
		int nTokens = 0;
		int[] r = new int[2];
		try
		{
			Query filterQuery = BlacklabFunctions.parseLuceneQuery(filterQueryString, "");

			Scorer s = searcher.findDocScores(filterQuery);
			IndexSearcher indexSearcher = searcher.getIndexSearcher();
			int doc;
			if (s == null)
			{
				System.err.println("!!!!!NULL scorer for " + filterQuery);
				return r;
			}
			while ((doc = s.nextDoc()) != s.NO_MORE_DOCS)
			{
				int maxL=0;
				for (int i=0; i < propertyNames.size(); i++)
				{
					ForwardIndex fi = this.forwardIndexes.get(i);
					Terms t = this.termsList.get(i);
					String p = this.propertyNames.get(i);
					int fiid = fi.luceneDocIdToFiid(doc);
					int l = fi.getDocLength(fiid);
					if (l > maxL)
					{
						maxL = l;
					}
				}
				nTokens += maxL;
				nDocs ++;
			}	
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		r[0] = nTokens;
		r[1] = nDocs;
		return r;
	}

	public int[] getNumberOfDocumentsAndTokensForFilter(String filterQueryString)
	{
		int nDocs = 0;
		int nTokens = 0;
		int[] r = new int[2];
		try
		{
			Query filterQuery = BlacklabFunctions.parseLuceneQuery(filterQueryString, "");

			Scorer s = searcher.findDocScores(filterQuery);
			IndexSearcher indexSearcher = searcher.getIndexSearcher();
			List<Integer> allDocs = collectDocuments(indexSearcher, filterQuery); // maak hier nog een bitset van ofzo...
			for (int doc: allDocs)
			{
				int maxL=0;
				for (int i=0; i < propertyNames.size(); i++)
				{
					ForwardIndex fi = this.forwardIndexes.get(i);
					Terms t = this.termsList.get(i);
					String p = this.propertyNames.get(i);
					int fiid = fi.luceneDocIdToFiid(doc);
					int l = fi.getDocLength(fiid);
					if (l > maxL)
					{
						maxL = l;
					}
				}
				nTokens += maxL;
				nDocs ++;
			}	
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		r[0] = nTokens;
		r[1] = nDocs;
		return r;
	}
	public void loopOverAllTokensinDocumentsSatisfyingMetadataFilter(String filterQueryString)
	{
		loopOverAllTokensinDocumentsSatisfyingMetadataFilter(filterQueryString, null, null);
	}


	public final void loopOverAllTokensinDocumentsSatisfyingMetadataFilter(String filterQueryString, TokenHandler tokenHandler, 
			DocumentContentHandler documentHandler)
	{
		nDocs = 0;
		if (filterQueryString == null || filterQueryString.trim().length() == 0)
		{
			this.enumerateAllTokensInCorpus(tokenHandler, documentHandler);
			return;
		}

		try
		{
			Query filterQuery = BlacklabFunctions.parseLuceneQuery(filterQueryString, "");

			List<Integer> matches = collectDocuments(searcher.getIndexSearcher(), filterQuery);
			//Scorer s = searcher.findDocScores(filterQuery);
			//int doc;
			for (int doc: matches)
			{
				Document d = searcher.document(doc);
				//System.err.println(d.get("idno"));
				if (verbose)
				{
					String fileName = ("#### " + doc +  " " + d.get("titleLevel1") + "/" + d.get("titleLevel2") + " " + d.get("witnessYear_from"));
					System.err.println(fileName);
				}

				handleDocument(doc, tokenHandler, documentHandler);

				synchronized(this)
				{
					nDocs++;
				}
			}
			System.err.println("docs found: " + nDocs);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		// System.err.println("docs found: " + foundDocs + " tokens found: " + nTokens);
		// mag pas NA de shutdown....
	}

	protected void handleDocument(int doc, TokenHandler tokenHandler, 
			DocumentContentHandler documentHandler)
	{
		loopOverTokensInDocument(doc, tokenHandler, documentHandler);
	}

	protected void handleDocument(int doc)
	{
		handleDocument(doc, null, null);
	}

	private void enumerateAllTokensInCorpus(TokenHandler th, DocumentContentHandler dch)
	{			
		ForwardIndex fi = this.forwardIndexes.get(0);
		for (int i=0; i < fi.getNumDocs(); i++) 
		{
			nTokens += loopOverTokensInDocument(i,th, dch); 
			// TODO: dit gebeurt zo nooit multithreaded!!! 
		}
	}

	public void handleToken(String token)
	{

	}

	/*
	 * Speedup: multitask this one (implemented in MultiThreadedTokenVisitor)
	 */

	protected int loopOverTokensInDocument(int luceneDocumentNumber, TokenHandler tokenHandler, DocumentContentHandler documentContentHandler) 
	{
		Document d = searcher.document(luceneDocumentNumber);
		String docId = d.get("idno");
		String[] propertyValues = new String[propertyNames.size()];
		List<List<String>> propertyValuesPerProperty = new ArrayList<List<String>>();


		int maxL=0;
		for (int i=0; i < propertyNames.size(); i++)
		{
			ForwardIndex fi = this.forwardIndexes.get(i);
			Terms t = this.termsList.get(i);
			String p = this.propertyNames.get(i);
			int fiid = fi.luceneDocIdToFiid(luceneDocumentNumber);
			int l = fi.getDocLength(fiid);
			if (l > maxL)
			{
				maxL = l;
			}
			propertyValuesPerProperty.add(retrievePropertyValuesForAllTokensInDocument(fi, t, p, fiid, 0, l));
		}

		synchronized (this)
		{
			nTokens += maxL;
			nDocs++;
		}


		if (documentContentHandler != null)
		{
			if (documentContentHandler.handleDocumentContent(docId, propertyNames, propertyValuesPerProperty))
				return maxL;
		}

		boolean doSomethingWithOutput = true;

		if (doSomethingWithOutput) 
		{
			for (int i=0; i < maxL; i++)
			{
				//tokenProperties.clear();
				StringBuilder sb = new StringBuilder();
				String tokenAsString="";


				for (int j=0; j < propertyNames.size(); j++) // dit moet niet zo.
				{
					List<String> v = propertyValuesPerProperty.get(j);
					String name = propertyNames.get(j);

					String value = v.get(i);
					propertyValues[j] = value;

				}

				tokenAsString = sb.toString();
				//System.err.println(Thread.currentThread() + " " + tokenAsString);
				if (tokenHandler != null)
					tokenHandler.handleToken(propertyNames, propertyValues);

				//nTokens++;
			}
		}
		//System.err.println("tokens handled");
		return maxL;
	}

	/**
	 * Alleen mogelijk als hele document opgehaald wordt voor slechts een property.
	 * Anders werkt ie niet.
	 * 
	 * @param fi
	 * @param terms
	 * @param propertyName
	 * @param fiid
	 * @param s
	 * @param l
	 */

	private List<String>  retrievePropertyValuesForAllTokensInDocument(ForwardIndex fi, Terms terms, String propertyName, int fiid, int s, int l) 
	{

		int[] start = {s};
		int[] end = {l};
		List<String> values = new ArrayList<String>();

		List<int[]> allWordTokensInDocument = fi.retrievePartsInt(fiid, start, end);
		int[] tids = allWordTokensInDocument.get(0);

		for (int j=0; j < tids.length; j++)
		{	
			values.add(terms.get(tids[j]));

		}
		return values;
	}


	/**
	 * 
	 * @param fi
	 * @param terms
	 * @param propertyName
	 * @param fiid
	 * @param s
	 * @param l
	 * 
	 * // translation to internal token id:
		// does this depend on the particular FI? or are token id's for lemma, pos and word the same??

		// problem: what happens with holes ? for instance lemma not specified, or pos not specified
		// result range may be smaller than input range
	 */
	protected String retrieveTokenContentAt(ForwardIndex fi, Terms terms, int fiid, int s, int l) 
	{
		int[] start = {s};
		int[] end = {l};


		List<int[]> allWordTokensInDocument = fi.retrievePartsInt(fiid, start, end);
		int[] tids = allWordTokensInDocument.get(0);

		String value = "";
		for (int j=0; j < tids.length; j++)
		{	
			if (value.length() > 0)
				value += " ";
			value += terms.get(tids[j]);
			//nTokens++;
		}
		return value;
		// this.tokenProperties.put(propertyName, value);
	}

	/**
	 * This should be in the multithreaded subclass
	 * @return
	 */
	public Counter<String> getEncompassingCounter()
	{
		Counter<String> total = new Counter<String>();
		for (Counter<String> c: counterMap.values())
		{
			System.err.println("partial map size: " + c.size());
			for (Map.Entry<String,Integer> entry : c.entrySet()) 
			{
				String key = entry.getKey();
				Integer value = entry.getValue();
				total.increment(key,value);
			}
		}
		return total;
	}

	void printTokenFrequencyList()
	{
		Counter<String> total = getEncompassingCounter();
		int k=0;
		for (String s: total.keyList())
		{
			System.out.println(s + "\t" + total.get(s));
			k++;
		}
	}

	/*
	 * Slowish...
	 */
	public List<Integer> collectDocuments(IndexSearcher searcher, Query query)
	{
		final List<Integer>x = new ArrayList<Integer>();
		try
		{
			searcher.search(query, new Collector() 
			{
				private int docBase;

				// ignore scorer
				public void setScorer(Scorer scorer) {

				}

				// accept docs out of order (for a BitSet it doesn't matter)
				public boolean acceptsDocsOutOfOrder() {
					return true;
				}

				public void collect(int doc) 
				{
					x.add(doc + docBase);
				}

				public void setNextReader(LeafReaderContext context) 
				{
					this.docBase = context.docBase;
				}

				@Override
				public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException 
				{
					final int docBase = context.docBase;
					return new LeafCollector() 
					{

						// ignore scorer
						public void setScorer(Scorer scorer) throws IOException {
						}

						public void collect(int doc) throws IOException 
						{
							x.add(docBase + doc);
						}

					};
				}

				@Override
				public boolean needsScores() {
					// TODO Auto-generated method stub
					return false;
				}
			});
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return x;
	};
}