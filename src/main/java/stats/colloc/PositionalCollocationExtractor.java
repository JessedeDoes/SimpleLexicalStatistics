package stats.colloc;

import java.util.*;

import nl.inl.blacklab.search.Searcher;
import stats.colloc.ClassCollocation.Counts;
import stats.colloc.ClassCollocation.MyTokenVisitor;
import stats.colloc.ClassCollocation.MyTokenVisitor.MyDocumentTask;
import stats.colloc.score.Salience;
import blacklabapi.BlacklabFunctions;
import blacklabapi.MultiThreadedTokenVisitor;
import blacklabapi.MultiThreadedTokenVisitor.DocumentTask;

public class PositionalCollocationExtractor
{
	Set<String> baseWords = new HashSet<String>();
	public int windowSize = 3;
	String theAttribute = "lemma";
	int minimumFrequency = 0;
	CollocationScore score = new Salience();
	Set<Collocation> collocations = new HashSet<Collocation>();
	
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

				if (f >= minimumFrequency && w.matches("^\\p{Ll}+$"))
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
			
				for (int i=0; i < words.size(); i++)
				{
					String w= words.get(i);
					String p = puncts.get(i);

					if (!p.contains("@") && !p.contains("#"))
						localCounts.documentFrequencies.increment(w);

					if (baseWords.contains(w))
					{
						for (int k=i-windowSize; k < i+windowSize; k++)
						{
							if (k > 0 && k != i && k < words.size())
							{
								String w1 = words.get(k);
								//int d = BlacklabFunctions.getDocumentFrequency(searcher, property, w1)
							}
						}
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

}
