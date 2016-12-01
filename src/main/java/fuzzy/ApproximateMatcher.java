package fuzzy;
import org.apache.lucene.util.Version;
import org.apache.lucene.util.automaton.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.search.*;
//import org.apache.lucene.index.IndexReader.AtomicReaderContext;

import java.util.*;
import java.io.*;
import java.nio.file.Path;
import java.util.Map;

public class ApproximateMatcher
{
	LevenshteinAutomata automata;
	Automaton automaton;
	CharacterRunAutomaton runAutomaton; 
	String target;
	IndexWriter iWriter;
	IndexReader iReader;
	IndexSearcher iSearcher;
	Directory index;
	org.apache.lucene.index.IndexWriterConfig iConfig;
	private int maximumDistance=1; 
	public String dummyFieldName= "title";
	private int minimumWordLengthToMatch = 7;

	/*
  public class MyAnalyzer extends org.apache.lucene.analysis.standard.StandardAnalyzer
  {
    public TokenStream tokenStream(String fieldName, Reader reader)
    {
      //TokenStream stream = new org.apache.lucene.analysis.CachingTokenFilter();
      // org.apache.lucene.search.highlight.OffsetLimitTokenFilter(); //  new TokenStream(reader);
      //return stream;
      return null;
    }
  }
	 */

	public void setMaximumDistance(int d)
	{
		maximumDistance = d;
	}

	public void setTarget(String input)
	{
		target=input;
		automata = new LevenshteinAutomata(input, false); // wat betekent die boolean?? (tel verwisseling als primitief)
		automaton = automata.toAutomaton(getMaximumDistance());    
		runAutomaton = new CharacterRunAutomaton(automaton);
	}


	public void closeIndex()
	{
		try
		{
			iWriter.close();
		} catch (Exception e)
		{
		}
	}


	public void createIndex(Collection<String> terms)
	{
		try
		{
			index = new RAMDirectory();
			iConfig = new IndexWriterConfig(new StandardAnalyzer());
			iWriter = new IndexWriter(index, iConfig);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		for (String s1: terms)
		{
			addDoc(iWriter,s1.toLowerCase());
		}
		closeIndex();
		try
		{
			
			iReader = DirectoryReader.open(index);
			iSearcher = new IndexSearcher(iReader);
			// printIndex();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		//addDoc(w, "Lucene in Action");
	}

	private void addDoc(IndexWriter w, String value)
	{
		try
		{
			Document doc = new Document();
			FieldType t = new FieldType();
			t.setTokenized(false);
			t.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
			t.setStored(true);
			
			StringField f = new StringField(dummyFieldName, value, Field.Store.YES);
			//f.setStringValue(value); 
			doc.add(f);
			w.addDocument(doc);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean matchTerm(String term) 
	{
		return runAutomaton.run(term);
	}

	public static List<String> readWords(String fname)
	{
		List<String> l = new ArrayList<String>();
		try
		{
			BufferedReader b = new BufferedReader(new FileReader(fname));
			String s;
			while ( (s = b.readLine()) != null)
			{
				String[] parts = s.split("\t");
				l.add(parts[0]);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return l;
	}

	public List<String> matchList(List<String> l)
	{
		List<String> r = new ArrayList<String>(); 
		for (String s1: l)
		{
			String s = s1.toLowerCase();
			if (this.matchTerm(s))
				r.add(s);
		}
		return r;
	}


	public void printIndex()
	{
		try
		{
			Term x = new Term(dummyFieldName,"");
			Fields fields = MultiFields.getFields(iReader);
			if (fields != null) 
			{

				Terms terms = fields.terms(dummyFieldName);
				if (terms != null) 
				{
					TermsEnum te = terms.iterator();
					while (te.next() != null) // this skips the first term
					{
						org.apache.lucene.util.BytesRef t = te.term();
						System.out.println(t);
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public class MatchCollector implements Collector
	{ 
		private int docBase;
		public boolean exactMatch;
		public boolean allowExactMatch;
		String target;
		Collection<String> matches = null;
		private int bestDistance=Integer.MAX_VALUE;

		public MatchCollector(String target, Collection<String> matches)
		{
			this.target = target;
			this.matches = matches;
		}     

		public void setScorer(Scorer scorer) 
		{
		}

		public boolean acceptsDocsOutOfOrder() 
		{
			return true;
		}

		public void collect(int doc) 
		{
			try
			{
				Document d = iReader.document(doc + docBase);
				String m = d.get(dummyFieldName);
				int dist = LevenshteinDistance(target,m);
				if (dist < bestDistance && dist > 0) bestDistance = dist;
				if (m.equals(target)) exactMatch = true;
				matches.add(m);
			} catch (Exception e)
			{
			}
		}



		//@Override
		public void setNextReader(LeafReaderContext arg0) throws IOException
		{
			// TODO Auto-generated method stub
			this.docBase = arg0.docBase;
		}

		@Override
		public LeafCollector getLeafCollector(LeafReaderContext arg0) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean needsScores() {
			// TODO Auto-generated method stub
			return false;
		}
	};


	public boolean matchTerm(List<String> matches, String s, boolean includeExactMatch)
	{
		Term t = new Term(dummyFieldName,s.toLowerCase());
		FuzzyQuery q = new FuzzyQuery(t,getMaximumDistance(),0);
		try
		{
			Set<String> allMatches = new HashSet<String>();
			MatchCollector collector = new MatchCollector(s, allMatches);
			iSearcher.search(q,collector);
			int d = collector.bestDistance; // but..
			for (String z: allMatches)
			{
				int d1 = LevenshteinDistance(z,s);
				if (d1 <= d && (includeExactMatch  || !z.equals(s)) )
					matches.add(z); 
			}
			return collector.exactMatch;
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return false; 
	}


	public void openIndex(String dirName)
	{
		try
		{
			Path p = (new File(dirName)).toPath();
			index = new NIOFSDirectory(p);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void testIndex()
	{

		FieldType t = new FieldType();
		String fieldname = "aap"; 
		try
		{
			Fields fields = MultiFields.getFields(iReader);
			if (1==1)
			{
				System.err.println("Please supply index name, one of");
				FieldInfos infos = ((LeafReader) iReader).getFieldInfos();

				for (FieldInfo s: infos)
				{
					System.err.println("\t" + s.name);
					fieldname=s.name;
				}
			}

			Term x = new Term(fieldname,"");
			Terms terms = fields.terms(fieldname);
			if (terms != null) 
			{
				TermsEnum te = terms.iterator();
				//TermEnum terms = iReader.terms(x); // terms after x
				while (te.next() != null) // this skips the first term
				{
					org.apache.lucene.util.BytesRef br = te.term();
					System.out.println(br);
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		/*
		while (te.n) // this skips the first term
		{
			Term term = terms.term();
			if (term.field().equals(fieldname))
			{
				System.out.println(term.field() + "\t" + term.text() + "\t" + iReader.docFreq(term));
			} else // gnrmpfkt
			{
				break;
			}
		}
		 */
	}
	public void matchUsingExistingIndex(List<String> l2)
	{

		try
		{
			iReader = DirectoryReader.open(index);
			iSearcher = new IndexSearcher(iReader);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		for (String s: l2)
		{
			if (s.length() <  12) continue;

			List<String> matches = new ArrayList<String>();

			boolean exactMatch = matchTerm(matches, s.toLowerCase(), true);
			int k=0;
			if (matches.size() > 0)
			{
				System.out.println(k++ + "\t" + join(matches," ") + "\t" + s);
			}
		}
	}

	public Map<String, List<String>> matchUsingIndex(List<String> l1, List<String> l2)
	{
		Set<String> L1Set = new HashSet<String>();
		 Map<String, List<String>> allMatches = new HashMap<String, List<String>>();
		 
		for (String s1: l1)
		{
			L1Set.add(s1.toLowerCase());
		}

		createIndex(l1);

		System.err.println("created index.."); 

		boolean sameList = l1 == l2;
		{
			int k=1;
			boolean allowExactMatch = sameList;
			boolean includeExactMatch = false;
			
			for (String s: l2)
			{
				if (s.length() <  getMinimumWordLengthToMatch() ) continue;
				
				if (!allowExactMatch && L1Set.contains(s.toLowerCase())) 
					continue;

				List<String> matches = new ArrayList<String>();
				boolean exactMatch = matchTerm(matches, s.toLowerCase(), includeExactMatch);
				
				if (matches.size() > 0 && !(!allowExactMatch && exactMatch))
				{
					System.err.println(k++ + "\t" + join(matches," ") + "\t" + s);
					allMatches.put(s,matches);
				}
			}
		}
		return allMatches;
	}


	public void matchAll(List<String> l1, List<String> l2)
	{
		int k=1;
		Set<String> L2Set = new HashSet<String>();
		boolean sameList = (l1 == l2);
		for (String s2: l2)
		{
			L2Set.add(s2.toLowerCase());
		}

		for (String s1: l1)
		{
			String s = s1.toLowerCase();
			if (s.contains("-") || (!sameList && L2Set.contains(s)))
			{
				k++;
				continue;
			}
			if (s.length() >= 14)
			{
				System.err.println("set target: " + s);
				setTarget(s);
				List<String> matches = matchList(l2);
				if (matches.size() > 0 && !(!sameList && matches.contains(s)))
				{
					System.out.println("matches: " + matches.size());
					System.out.println(k + "\t" + join(matches," ") + "\t" + s); 
				}
			}
			k++;
		}
	}

	public static String join(List<String> coll, String delimiter)
	{
		if (coll.isEmpty())
			return "";

		StringBuilder sb = new StringBuilder();

		for (String x : coll)
			sb.append(x + delimiter);

		sb.delete(sb.length()-delimiter.length(), sb.length());

		return sb.toString();
	}

	private static int minimum(int a, int b, int c) 
	{
		return Math.min(Math.min(a, b), c);
	}

	public static int LevenshteinDistance(CharSequence str1, CharSequence str2) 
	{
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 0; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
								+ ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
										: 1));

		return distance[str1.length()][str2.length()];
	}

	public static void main(String[] args)
	{
		ApproximateMatcher t = new ApproximateMatcher();
		File f = new File(args[0]);
		List<String> l= null;
		if (f.isDirectory())
		{
			t.openIndex(args[0]);
		} else
			l = ApproximateMatcher.readWords(args[0]);
		if (args.length < 2)
			t.matchUsingIndex(l,l);
		else 
		{
			if (f.isDirectory())
			{
				t.matchUsingExistingIndex(ApproximateMatcher.readWords(args[1]));
			} else
				t.matchUsingIndex(l,ApproximateMatcher.readWords(args[1]));
		}
	}

	public int getMinimumWordLengthToMatch()
	{
		return minimumWordLengthToMatch;
	}

	public void setMinimumWordLengthToMatch(int minimumWordLengthToMatch)
	{
		this.minimumWordLengthToMatch = minimumWordLengthToMatch;
	}

	public int getMaximumDistance()
	{
		return maximumDistance;
	}
}
