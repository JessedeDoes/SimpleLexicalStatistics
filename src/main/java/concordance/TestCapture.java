package concordance;
import java.io.File;
import java.io.FileWriter;

import util.Counter;
import util.TabSeparatedToExcel;
import blacklabapi.BlacklabFunctions;
import blacklabapi.MyHitPropertyHitText;
import nl.inl.blacklab.search.Concordance;
import nl.inl.blacklab.search.Hit;
import nl.inl.blacklab.search.Hits;
import nl.inl.blacklab.search.Kwic;
import nl.inl.blacklab.search.Searcher;
import nl.inl.blacklab.search.Span;
import nl.inl.blacklab.search.grouping.HitPropValueContextWords;
import nl.inl.blacklab.search.grouping.HitPropertyDocumentStoredField;
import nl.inl.blacklab.search.grouping.HitPropertyHitText;
import nl.inl.blacklab.search.grouping.HitPropertyMultiple;
import nl.inl.util.XmlUtil;

import java.util.*;

import org.apache.lucene.queryparser.flexible.core.util.StringUtils;

/*
 * To do: het zou leuk zijn de query, het aantal resultaten en nog wat leukigheid (spreidingsgrafiekje???) 
 * in een apart blad te zetten
 */

public class TestCapture
{
	String regexFilter = null;

	public List<String> getCapture(Hit h, String captureName, String propertyName)
	{
		return null;
	}

	public Map<String, Counter<String>> collectCaptures( Searcher searcher, String corpusQlQuery, String capturedField)
	{
		Map<String, Counter<String>> captureMap = new HashMap<String, Counter<String>>();

		try
		{
			Hits.setDefaultMaxHitsToCount(Integer.MAX_VALUE);
			Hits.setDefaultMaxHitsToRetrieve(Integer.MAX_VALUE);

			Hits hits = BlacklabFunctions.filteredSearch(searcher, corpusQlQuery, null);

			System.err.println("Hits: " + hits.size());

			HitPropertyHitText lemmaProperty = new HitPropertyHitText(hits,Searcher.DEFAULT_CONTENTS_FIELD_NAME,"lemma");
			HitPropertyHitText wordProperty = new HitPropertyHitText(hits,Searcher.DEFAULT_CONTENTS_FIELD_NAME,"word");
			HitPropertyHitText posProperty = new HitPropertyHitText(hits, Searcher.DEFAULT_CONTENTS_FIELD_NAME,"pos");
			HitPropertyHitText targetProperty = new HitPropertyHitText(hits, Searcher.DEFAULT_CONTENTS_FIELD_NAME, capturedField);

			HitPropertyMultiple  multi = new HitPropertyMultiple(wordProperty, lemmaProperty, posProperty);

			int i=0;

			List<String> neededProps =  multi.needsContext();

			System.err.println("Needed properties:"  + neededProps);

			hits.findContext(neededProps); //moet dit nog steeds?

			hits.setContextSize(0);
			for (Hit h: hits)
			{
				if (false)
					continue;
				Kwic conc = hits.getKwic(h);
				List<String> hitWords = conc.getMatch(capturedField);

				Map<String, Span> m = hits.getCapturedGroupMap(h);

				if (m != null)
				{
					List<String> names = new ArrayList<String>();
					List<String> values = new ArrayList<String>();
					for (String n: m.keySet())
					{
						names.add(n);
						Counter<String> map = captureMap.get(n);

						if (map == null)
						{
							map = new Counter<String>();
							captureMap.put(n, map);
						}

						Span s = m.get(n);

						String value = util.StringUtils.join(hitWords.subList( s.start - h.start,  s.end - h.start),  " ");
						map.increment(value);
						values.add(value);

					}
					String namez = util.StringUtils.join(names,"_");
					String valuez = util.StringUtils.join(values,"_X_");
					Counter<String> map = captureMap.get(namez);


					if (map == null)
					{
						map = new Counter<String>();
						captureMap.put(namez, map);
					}
					map.increment(valuez);
				}


				i++;
			}

		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		for (String captureName: captureMap.keySet())
		{
			System.err.println(captureName + "-->" + captureMap.get(captureName).keyList());
		}
		return captureMap;
	}

	public void exportConcordancesToXLS(String exportTo, Searcher searcher, String corpusQlQuery, String filterQuery)
	{
		collectCaptures(searcher, corpusQlQuery, filterQuery );

	}

	public static void main(String[] args)
	{
		TestCapture exp = new TestCapture();
		exp.regexFilter = ".*[bcfghjklmnpqrsvwxz]e$"; // geen d...
		exp.regexFilter = null;
		try 
		{

			Searcher searcher = Searcher.open(new File(args[1]));
			System.err.println("Searcher opened...");
			String query = args[2];

			exp	.collectCaptures(searcher, query, "word");
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
