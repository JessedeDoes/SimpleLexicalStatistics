package stats.colloc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import stats.colloc.score.Salience;
import stats.colloc.score.TScore;
import util.Counter;
import nl.inl.blacklab.search.Searcher;
import blacklabapi.MultiThreadedTokenVisitor;
import blacklabapi.MultiThreadedTokenVisitor.DocumentTask;

/**
 * Class collocation score (w):
 * f = #docs: w with an element of L
 * f1 = #docs containing w
 * f2 = #docs containing an element of L
 * N = #docs
 * @author does
 * 
 * We need to keep score of all this information.....
 * document frequencies for each word
 */

public class ClassCollocation
{
	static String theAttribute = "word";
	int minimumFrequency=2;
	CollocationScore score = new Salience();
	Set<String> seenThisTweetBefore = new HashSet<String>();
	int nDuplicates=0;

	/*
	 * This is slow... especially the combination of synchronized and the hashing is bad
	 * @param words
	 * @param punct
	 * @return
	 */
	synchronized boolean checkSeenBefore(String md5)
	{
		if (this.seenThisTweetBefore.contains(md5))
		{
			nDuplicates++;
			if (nDuplicates % 1000 == 0)
				System.err.println("Seen before: " + nDuplicates);
			return true;
		}
		this.seenThisTweetBefore.add(md5);

		return false;
	}

	static class Counts
	{
		Counter<String> documentFrequencies = new	Counter<String>() ;
		int classDocumentFrequency = 0;
		Counter<String> cooccurrenceFrequencies = new 	Counter<String>() ;
	}

	Set<String> classOfWords = new HashSet<String>();
	List<Collocation> collocations = new ArrayList<Collocation>();

	public void readWordClass(String fileName)
	{
		InputStreamReader is = null;
		BufferedReader in = null;
		try 
		{ 
			is = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
			in = new BufferedReader(is);
			String line=null;
			while ((line=in.readLine()) != null)
			{
				classOfWords.add(line);
			}
		} catch (Exception e) { e.printStackTrace();};
	}

	class MyTokenVisitor extends MultiThreadedTokenVisitor
	{
		Counts globalCounts = new Counts();
		Map<String, Counts> countsMap = new HashMap<String, Counts>();

		public void merge()
		{
			for (Counts c: countsMap.values())
			{
				globalCounts.classDocumentFrequency += c.classDocumentFrequency;
				for (String w:c. documentFrequencies.keySet())
				{
					globalCounts.documentFrequencies.increment(w, c.documentFrequencies.get(w));
					globalCounts.cooccurrenceFrequencies.increment(w, c.cooccurrenceFrequencies.get(w));
				}
			}

			for (String w: globalCounts.cooccurrenceFrequencies.keySet())
			{
				int f = globalCounts.cooccurrenceFrequencies.get(w);

				if ((!classOfWords.contains(w)) && f >= minimumFrequency && w.matches("^\\p{Ll}+$"))
				{
					int f1 =  globalCounts.documentFrequencies.get(w);
					int f2 = globalCounts.classDocumentFrequency;
					int N = this.nDocs;
					Collocation c = new Collocation();
					c.base = w;
					c.collocate = "ynw";
					c.score = score.score(N, f, f1, f2);
					c.f = f;
					c.f1 = f1;
					c.f2 = f2;
					collocations.add(c);
				}
			}
		}

		class MyDocumentTask extends DocumentTask
		{
			Counts localCounts;
			public MyDocumentTask(int did)
			{
				super(did);
			}

			@Override
			public  boolean handleDocumentContent(String docId, List<String> propertyNames,  List<List<String>> propertyValuesPerProperty)
			{
				boolean hasTargetClass=false;
				boolean isRetweet = false;
				List<String> words=null;
				List<String> puncts =null;
				for (int i=0; i < propertyNames.size(); i++)
				{
					String p = propertyNames.get(i);
					if (p.equals(theAttribute))
					{
						words = propertyValuesPerProperty.get(i);
					} else if (p.equals("punct"))
					{
						puncts = propertyValuesPerProperty.get(i);
					}
				}
				String md5 = twitter.Utilities.getTweetMD5(words, puncts);
				if (checkSeenBefore(md5)) // tweet seen before....
					return true;
				for (int i=0; i < words.size(); i++)
				{
					String w= words.get(i);
					String p = puncts.get(i);

					if (!p.contains("@") && !p.contains("#"))
						localCounts.documentFrequencies.increment(w);

					if (classOfWords.contains(w))
					{
						hasTargetClass = true;
					}
					if (w.equals("RT"))
					{
						isRetweet = true;
					}
				}

				if  (hasTargetClass && !isRetweet)
				{
					localCounts.classDocumentFrequency++;
					for (int i=0; i < words.size(); i++)
					{
						String w= words.get(i);
						String p = puncts.get(i);

						if (!p.contains("@") && !p.contains("#"))
							localCounts.cooccurrenceFrequencies.increment(w);
					}
				} else
				{
					//System.err.println("Nothing found in document!");
				}

				//System.out.println(StringUtils.join(propertyValuesPerProperty.get(0),  " ") + "\n");

				return true;
			}
			@Override
			public void handleToken(List<String> propertyNames, String[] propertyValues)
			{
				// TODO Auto-generated method stub
				String z=filterToken(propertyNames,  propertyValues);
				if (z != null)
					partialCounter.increment(z);
			}

			public void run() 
			{
				this.thread = Thread.currentThread();
				synchronized (MyTokenVisitor.this)
				{
					localCounts = countsMap.get(""+this.thread.getId());
					if (localCounts == null)
					{
						localCounts= new Counts();
						countsMap.put(""+this.thread.getId(), localCounts);
					}
				}
				loopOverTokensInDocument(this.documentId, this, this);
			}
		}

		public MyTokenVisitor(Searcher s, List<String> l)
		{	
			super(s,l);	
		}

		protected void handleDocument(int did)
		{
			DocumentTask t = new MyDocumentTask(did);
			pool.execute(t);
		}
	}

	public static void main(String[] args)
	{
		try
		{
			Searcher s = Searcher.open(new File(args[0]));
			String wordClassFileName = args[1];
			String[] props = {theAttribute, "punct"};
			ClassCollocation cc = new ClassCollocation();
			cc.readWordClass(wordClassFileName);
			cc.extractCollocations(s, args[2], props);
			System.err.println("duplicates: " + cc.nDuplicates);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected void extractCollocations(Searcher s, String filter, String[] props)
	{
		MyTokenVisitor m = this.new MyTokenVisitor(s, Arrays.asList(props));
		m.loopOverAllTokensinDocumentsSatisfyingMetadataFilter(filter);
		m.shutdown();
		m.merge();
		//System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		Collections.sort(collocations, new CollocationExample.ScoreComparator());
		for (int i=0; i < 100000	 && i < collocations.size(); i++)
		{
			System.out.println(collocations.get(i));
		}
	}
}
