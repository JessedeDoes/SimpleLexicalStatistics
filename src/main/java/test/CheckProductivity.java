package test;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
//import org.apache.lucene.index.TermEnum;



import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import blacklabapi.BlacklabFunctions;
import util.Counter;
import wordlist.WordListMaker;
import wordlist.filter.LemmaWithPoS;

import java.util.*;

public class CheckProductivity
{
	WordListMaker wlm;
	public CheckProductivity(String indexDirectory)
	{
		try
		{
			wlm = new WordListMaker(indexDirectory);
			LemmaWithPoS w = new wordlist.filter.LemmaWithPoS("-");
			wlm.setTokenFilter(w);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public Counter<String>  checkProductivity(String w, String meta)
	{
		String q = BlacklabFunctions.singleWordQuery("lemma",  w + ".+") + "|" +  BlacklabFunctions.singleWordQuery("lemma",  ".+" + w);
		Counter<String> c = wlm.makeWordList(q /* corpus */ , meta /* metadata */);
		return c;
		//WordListMaker.printTokenFrequencyList(c);
	}

	public void blaa(Counter<String > c)
	{
		Term t = new Term("contents%lemma@i", "");
		Map<String, Set<String>> m = new HashMap<String, Set<String>>();
		int k=0;
		try
		{
			TermsEnum te = null;
			Fields fields = MultiFields.getFields(wlm.searcher.getIndexReader());
			if (fields != null) 
			{
				
				Terms terms = fields.terms(t.field());
				if (terms != null) 
				{
					te = terms.iterator();
				}
			}
			//TermEnum te = wlm.searcher.getIndexReader().terms(t);
			//Term t1 = null;
			BytesRef text;
			while ((text = te.next()) != null)
			{

				//t1 = te.term();
				//if (!t1.field().equals("contents%lemma@i"))
				//	break;
				String w = text.utf8ToString();
				k++;
				for (int i=0; i < w.length()-1; i++)
				{
					if (w.matches("^\\p{L}+$"))
					{
						String prefix = w.substring(0,i).toLowerCase();
						String suffix = w.substring(i+1,w.length()).toLowerCase();

						if (c.containsKey(prefix))
						{
							if (m.get(prefix)  == null)
								m.put(prefix, new HashSet<String>());
							System.err.println(k + ":" + w);
							m.get(prefix).add(w);
						}
						if (c.containsKey(suffix))
						{
							if (m.get(suffix)  == null)
								m.put(suffix, new HashSet<String>());
							System.err.println(k + ":" + w);
							m.get(suffix).add(w);
						}
					}
				}
			}
			for (String w: m.keySet())
			{
			
				Set<String> compounds = m.get(w);
				System.err.println("compounds with " + w + ": " + compounds.size());
				String[] a = new String[compounds.size()];
				a = compounds.toArray(a);
				Counter<String> w_ = BlacklabFunctions.listLookup(wlm.searcher, a, "witnessYear_from:[0 TO 2007]");
				for (String w1: w_.keyList())
				{
					System.out.println(w + "\t" + w1 + "\t" + w_.get(w1));
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		System.err.println("Terms enumerated!!! " + k);
	}
	public void checkProductivityFromFile(String fileName)
	{
		Counter<String> words = Counter.readFromFile(fileName);
		this.blaa(words);

		if (false) for (String w: words.keySet())
		{
			Counter<String> c  = checkProductivity(w,"witnessYear_from:[0 TO 2007]");

			for (String w1: c.keyList())
			{
				System.out.println(w + "\t" + w1 + "\t" + c.get(w1));
			}
		}
	}

	public static void main(String[] args)
	{
		try
		{
			CheckProductivity cp = new CheckProductivity(args[0]);

			cp.checkProductivityFromFile(args[1]);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
