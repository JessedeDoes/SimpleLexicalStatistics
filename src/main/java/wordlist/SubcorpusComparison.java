package wordlist;
import java.util.*;
import java.util.Map.Entry;

import blacklabapi.PropertyFilter;
import stats.colloc.CollocationScore;
import stats.colloc.score.LL;
import stats.colloc.score.MI;
import stats.colloc.score.TScore;
import util.Counter;
import wordlist.filter.LemmaWithPoS;
import wordlist.filter.Word;
import nl.inl.blacklab.search.Searcher;




/**
 * This is a nascent implementation of,  among others, the Kilgarriff <i>Simple Maths for Keywords</i> method for extracting subcorpus-specific vocabulary.
 * <br>
 * For each word, one computes ,as in indicator of keyness, the smoothed frequency ratio <br>
 * <i>
 
 * (normalized frequency in focus Corpus + smoothingParameter)  /  (normalized frequency in reference corpus + smoothingParameter)
 * 
 * </i>
 * <br>
 * Frequencies are normalized to "frequency per million tokens".
 * <p>
 * To do:
 * <ul>
 * <li>Document frequencies
 * <li>Dispersion, reduced frequencies
 * <li>Stable lexical markers - use other metadata to check stabel correlation with parameters defining the subcorpus
 * </ul>
 */

public class SubcorpusComparison 
{
	public List<PairOfFrequencies> counts = new ArrayList<PairOfFrequencies>();
	boolean includeAll= false;
	CollocationScore collocationScore = new MI();
	Searcher searcher = null;
	int minimumFrequency = 10;
	boolean strict = false; // only words not occurring in reference corpus at all
	
	// involving the other collocation criteria:
	// (long N, int f, int f1, int f2)
	// Vul in:
	// N = n1+n2
	// f --> f1 (frequency of combination)
	// f1 --> f1 + f2
	// f2 --> size of corpus 1
	
	public void setStrict(boolean b)
	{
		this.strict = b;
	}
	
	public void setMinimumFrequency(int f)
	{
		this.minimumFrequency = f;
	}
	
	public static class PairOfFrequencies
	{
		long fFocus; // frequency in corpus 1
		long n1; // size of corpus 1 
		long fReference; // frequency in corpus 2
		long n2; // size of corpus 2
		double weight;
		double collocationScore;
		public String type;
		
		
		double getWeight(double smoothingParameter)
		{
			double r =  (this.normalizedFocusFrequency() + smoothingParameter) /
					              (this.normalizedReferenceFrequency() + smoothingParameter);
			return r;
		}
		
		double scoreWithCollocationScore(CollocationScore cs)
		{
		   return cs.score(n1+n2, (int) fFocus, (int) (fFocus+fReference), (int) n1); // of laatste twee omdraaien??
		}
		
		double normalizedFocusFrequency()
		{
			return fFocus / (double) n1 * 1e6;
		}
		
		double normalizedReferenceFrequency()
		{
			return fReference / (double) n1 * 1e6;
		}
		
		public String toString()
		{
			return  type + "\tweight:" + f(weight) + "\tscore:" + f(collocationScore) + "\t" +  fFocus + "=" + f(normalizedFocusFrequency()) 
			+ "\t" + fReference + "=" + normalizedReferenceFrequency();
		}
	}
	

	public static class WeightComparator implements Comparator<PairOfFrequencies> 
	{
		int smoothingParameter=5;
		
		public WeightComparator(int smoothingParameter) 
		{
			this.smoothingParameter=smoothingParameter;
		}
		
		public int compare(PairOfFrequencies a, PairOfFrequencies b) 
		{
			if (a.weight < b.weight)
				return 1;
			if (a.weight == b.weight)
				return 0;
			return -1;
		}
	}
	
	public static class ScoreComparator implements Comparator<PairOfFrequencies> 
	{
		public int compare(PairOfFrequencies a, PairOfFrequencies b) 
		{
			if (a.collocationScore < b.collocationScore)
				return 1;
			if (a.collocationScore == b.collocationScore)
				return 0;
			return -1;
		}
	}
	
	public List<PairOfFrequencies> compare(Counter<String> w1, Counter<String> w2, int smoothingParameter, int minimumFrequency)
	{
		this.counts.clear();
		
		int N1 = w1.sumOfCounts();
		int N2 = w2.sumOfCounts();
		
		for (Entry<String,Integer> e: w1.entrySet())
		{
			PairOfFrequencies c = new PairOfFrequencies();
			if (strict && w2.get(e.getKey()) > 0)
			{
				continue;
			} else
			{
				c.fReference = 0;
			}
			counts.add(c);
			c.fFocus = e.getValue();
			c.n1 =  N1;
			c.fReference = w2.get(e.getKey());
			c.n2 =  N2;
			c.type = e.getKey();
			c.weight = c.getWeight(smoothingParameter);
		}
		
		if (!strict && includeAll) for (Entry<String,Integer> e: w2.entrySet())
		{
			String key = e.getKey();
			if (w1.get(key) > 0)
			{
				continue;
			}
			
			PairOfFrequencies c = new PairOfFrequencies();
			c.type = key;
			c.fFocus = 0;
			c.n1 = N1;
			c.fReference = e.getValue();
			c.n2 =  N2;
			c.weight = c.getWeight(smoothingParameter);
			counts.add(c);
		}
		
		for (PairOfFrequencies p: counts)
		{
			p.collocationScore = p.scoreWithCollocationScore(collocationScore);
		}
		
		Collections.sort(counts, new WeightComparator(smoothingParameter));
		Collections.sort(counts, new ScoreComparator());
		
		return counts;
		//printCounts(minimumFrequency);
	}

	protected void printCounts(int minimumFrequency)
	{
		for (PairOfFrequencies c : counts)
		{
			if (c.fFocus > minimumFrequency)
				System.out.println(
						c.type + "\tweight:" + f(c.weight) + "\tscore:" + f(c.collocationScore) + "\t" +  c.fFocus + "=" + f(c.normalizedFocusFrequency()) 
						+ "\t" + c.fReference + "=" + c.normalizedReferenceFrequency());
		}
	}
	
	public static String f(double d)
	{
		return String.format("%.1f", d);
	}
	
	public  List<PairOfFrequencies> compareSubcorpora(String indexDir, String filter1, String filter2)
	{
		WordListMaker ft = new WordListMaker(indexDir);
		ft.setTokenFilter(new Word("-"));
		
		Counter<String> l1 = ft.makeWordList(filter1);
		//ft.setTokenFilter(new LemmaWithPoS("-"));
		Counter<String> l2 =  ft.makeWordList(filter2);
		return this.compare(l1, l2, 10, 10);
	}
	
	public  List<PairOfFrequencies> compareSubcorpora(Searcher s, String filter1, String filter2)
	{
		WordListMaker ft = new WordListMaker(s);
		
		ft.setTokenFilter(new Word("-"));
		
		Counter<String> l1 = ft.makeWordList(filter1);
		
		
		
		Counter<String> l2 =  ft.makeWordList(filter2);
		
		return this.compare(l1, l2, 10, 10);
	}
	
	public  List<PairOfFrequencies> compareSubcorpora(String filter1, String filter2)
	{
		return compareSubcorpora(this.searcher, filter1, filter2);
	}
}

