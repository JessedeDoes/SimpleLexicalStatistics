package concordance;
import java.io.File;
import java.io.FileWriter;

import util.Counter;
import util.TabSeparatedToExcel;
import word2vec.Distance;
import word2vec.Vectors;
import blacklabapi.BlacklabFunctions;
import blacklabapi.MyHitPropertyHitText;
import nl.inl.blacklab.search.Concordance;
import nl.inl.blacklab.search.Hit;
import nl.inl.blacklab.search.Hits;
import nl.inl.blacklab.search.Kwic;
import nl.inl.blacklab.search.Searcher;
import nl.inl.blacklab.search.Span;
import nl.inl.blacklab.search.grouping.HitPropValue;
import nl.inl.blacklab.search.grouping.HitPropValueContextWords;
import nl.inl.blacklab.search.grouping.HitPropValueString;
import nl.inl.blacklab.search.grouping.HitPropertyDocumentStoredField;
import nl.inl.blacklab.search.grouping.HitPropertyHitText;
import nl.inl.blacklab.search.grouping.HitPropertyMultiple;
import nl.inl.util.XmlUtil;

import java.util.*;
import java.util.function.Function;

import org.apache.lucene.queryparser.flexible.core.util.StringUtils;

/*
 * To do: het zou leuk zijn de query, het aantal resultaten en nog wat leukigheid (spreidingsgrafiekje???) 
 * in een apart blad te zetten
 */

public class TestEvolution
{
	String regexFilter = null;
	Vectors vectors;
	
	static
	{
		Hits.setDefaultMaxHitsToCount(Integer.MAX_VALUE);
		Hits.setDefaultMaxHitsToRetrieve(Integer.MAX_VALUE);
	};
	
	static class Timeline
	{
		Map<String, float[]> averageVectors = new HashMap<>();
		Counter<String> periodCounter  = new Counter<>();
		
		public void store(String key, float[] v)
		{
			float[] x = averageVectors.get(key);
			if (x == null)
				averageVectors.put(key,v);
			else
			{
				float[] sum = word2vec.Distance.add(x, v);
				averageVectors.put(key,sum);
			}
		}
		
		public void overview()
		{
			List<String> keyList = new ArrayList<>();
			keyList.addAll(averageVectors.keySet());
			Collections.sort(keyList);
			int k=0;
			float[] vPrev = null;
			String sPrev = null;
			for (String s: keyList)
			{
				float[] v  = averageVectors.get(s);
				if (vPrev != null)
				{
					double sim = Distance.cosineSimilarity(vPrev, v);
					System.err.println(String.format("sim(%s,%s)=%f (%d,%d)", sPrev,s,sim, periodCounter.get(sPrev), periodCounter.get(s) ));
				}
				vPrev = v;
				sPrev = s;
			}
			String p0 = keyList.get(0);
			String pn = keyList.get(keyList.size()-1);
			float[] vStart = averageVectors.get(p0);
			float[] vEnd = averageVectors.get(pn);
			double simX =  Distance.cosineSimilarity(vStart, vEnd);
			System.err.println(String.format("sim(%s,%s)=%f (%d,%d)", p0,pn,simX, periodCounter.get(p0), periodCounter.get(pn) ));
		}
	};
	
	Timeline timeline = new Timeline();
	
    static  int year(String s)  { return  Integer.parseInt(s.substring(0,4)); } ;
	static Function<String,String> halveEeuwen = (s) -> s.substring(0,3).replaceAll("[0-4]$","00").replaceAll("[5-9]$","50");
	static Function<String,String> kwartEeuwen = (s) -> (year(s) -( year(s) % 25)) + "" ;
	static Function<String,String> eeuwen = (s)->s.substring(0,2) + "00";
	static Function<String,String> decaden = (s)->s.substring(0,3) + "0";
	static Function<String,String> jaren = (s)->s.substring(0,4);
	
	public TestEvolution(String vectorFileName)
	{
		// TODO Auto-generated constructor stub
		this.vectors = Vectors.readFromFile(vectorFileName);
	}

	public List<String> getCapture(Hit h, String captureName, String propertyName)
	{
		return null;
	}

	static class collapsedProperty extends HitPropertyDocumentStoredField
	{
		Function<String,String> collapse;
		
		public collapsedProperty(Hits hits, String fieldName, 	Function<String,String> collapse)
		{
			super(hits, fieldName);
			this.collapse = collapse;
			// TODO Auto-generated constructor stub
		}
		@Override
		public HitPropValueString get(int i)
		{
			//this.
			HitPropValueString v =  super.get(i);
			String x = collapse.apply(v.serialize().replaceAll("str:", ""));
			HitPropValueString v1 = (HitPropValueString) HitPropValueString.deserialize( x);
			//System.err.println(v1);
			return v1;
		}
	}
	
	public void followAverageVector(Searcher searcher, String corpusQlQuery, String sortField)
	{
	
		try
		{
		

			Hits hits = BlacklabFunctions.filteredSearch(searcher, corpusQlQuery, null);

			System.err.println("Hits: " + hits.size());

			HitPropertyDocumentStoredField sortProperty = definedPropertiesAndContext(hits, sortField, kwartEeuwen);
			
			hits.sort(sortProperty);
			
			System.err.println("Hits sorted...");
			int i=0;
			
			for (Hit h: hits)
			{
				
				Kwic conc = hits.getKwic(h);
				List<String> l = new ArrayList<>();
				
				List<String> hitWords = conc.getMatch("word");
				// System.err.println(hitWords);
				
				l.addAll( conc.getLeft("word"));
				l.addAll(conc.getRight("word"));
				
				String period = sortProperty.get(i).toString();
				timeline.periodCounter.increment(period);
				
				// System.err.println(sortValue);
				
				float[] thisContext = word2vec.Util.getAverageVector(vectors, l, 0);
				timeline.store(period,thisContext);
				i++;
			}

		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		timeline.overview();
	}

	private HitPropertyDocumentStoredField definedPropertiesAndContext( Hits hits, String sortField, Function<String,String> periods)
	{
		HitPropertyHitText lemmaProperty = new HitPropertyHitText(hits,Searcher.DEFAULT_CONTENTS_FIELD_NAME,"lemma");
		HitPropertyHitText wordProperty = new HitPropertyHitText(hits,Searcher.DEFAULT_CONTENTS_FIELD_NAME,"word");
		HitPropertyHitText posProperty = new HitPropertyHitText(hits, Searcher.DEFAULT_CONTENTS_FIELD_NAME, "pos");
		
		// HitPropertyHitText targetProperty = new HitPropertyHitText(hits, Searcher.DEFAULT_CONTENTS_FIELD_NAME, capturedField);
		
		HitPropertyDocumentStoredField sortProperty = new collapsedProperty(hits, sortField,  periods );
		
		HitPropertyMultiple  multi = new HitPropertyMultiple(wordProperty, lemmaProperty, posProperty);



		List<String> neededProps =  multi.needsContext();
		hits.setContextSize(2);
		
		System.err.println("Context properties:"  + neededProps);

		hits.findContext(neededProps); //moet dit nog steeds?
		System.err.println("Found context properties:"  + neededProps);
		return sortProperty;
	}

	public void exportConcordancesToXLS(String exportTo, Searcher searcher, String corpusQlQuery, String filterQuery)
	{
		followAverageVector(searcher, corpusQlQuery, filterQuery );

	}

	public static void main(String[] args)
	{
		TestEvolution exp = new TestEvolution(args[0]);
		exp.regexFilter = ".*[bcfghjklmnpqrsvwxz]e$"; // geen d...
		exp.regexFilter = null;
		try 
		{

			Searcher searcher = Searcher.open(new File(args[1]));
			System.err.println("Searcher opened...");
			
			String query = args[2];

			exp	.followAverageVector(searcher, query, "date");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}


// the old way:
//HitPropValueContextWords x = targetProperty.get(i); // hier wil ik de lijst waarden kunnen ophalen.....

//String[] values = x.toString().split("\\s+");

//tring[] capture = Arrays.copyOfRange(values, start, end);
