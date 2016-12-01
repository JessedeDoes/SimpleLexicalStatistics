package blacklabapi;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;





import util.Counter;
import nl.inl.blacklab.search.Hit;
import nl.inl.blacklab.search.Hits;
import nl.inl.blacklab.search.Searcher;
import nl.inl.blacklab.search.grouping.HitGroup;
import nl.inl.blacklab.search.grouping.HitGroups;
import nl.inl.blacklab.search.grouping.HitPropValue;
import nl.inl.blacklab.search.grouping.HitProperty;
import nl.inl.blacklab.search.grouping.HitPropertyDocumentStoredField;
import nl.inl.blacklab.search.grouping.HitPropertyHitText;
import nl.inl.blacklab.search.grouping.HitPropertyLeftContext;
import nl.inl.blacklab.search.grouping.HitPropertyMultiple;
import nl.inl.blacklab.search.grouping.RandomAccessGroup;
import nl.inl.blacklab.search.grouping.ResultsGrouper;


/**
 * 
 * @author does
 * Waarom hebben we deze overbodig uitziende class bovenop de BlackLab grouper eigenlijkook al weer nodig??
 * - ten eerste omdat de blacklab grouper eruit loopt bij frequente woorden door het bijhouden van de Hits
 * Functionaliteit van de Blacklab results grouper is OK
 * Het zou (fature request) handig zijn een versie van het groeperen
 * te hebben die GEEN hits bijhoudt.
 */
public class ResultGrouper
{
	// Sort the hits by the words to the left of the matched text
	Searcher searcher;
	/**
	 * 
	 */

	HitProperty lemmaProperty, wordProperty, posProperty;
	HitPropertyDocumentStoredField jaarProperty;
	HitPropertyMultiple multipleProperty;
	private String[] metadataProperties;
	private String[] wordProperties;

	public void defaultInitProperties(Hits hits)
	{

		// je zou de friendly naam van de hit propery moeten kunnen instellen, net als bij de Document Stored Field ....
		//lemmaProperty = new MyHitPropertyHitText(hits,Searcher.DEFAULT_CONTENTS_FIELD_NAME,"lemma", "lemma");
		//wordProperty = new MyHitPropertyHitText(hits,Searcher.DEFAULT_CONTENTS_FIELD_NAME,"word", "word");
		//posProperty = new MyHitPropertyHitText(hits,Searcher.DEFAULT_CONTENTS_FIELD_NAME,"pos", "PoS");

		lemmaProperty = new HitPropertyHitText(hits,Searcher.DEFAULT_CONTENTS_FIELD_NAME,"lemma");
		wordProperty = new HitPropertyHitText(hits,Searcher.DEFAULT_CONTENTS_FIELD_NAME,"word");
		posProperty = new HitPropertyHitText(hits,Searcher.DEFAULT_CONTENTS_FIELD_NAME,"pos");
		jaarProperty  = new HitPropertyDocumentStoredField(hits, "witnessYear_from", "jaar");

		multipleProperty = new HitPropertyMultiple(lemmaProperty, wordProperty, posProperty,jaarProperty);

		for (HitProperty hp: multipleProperty)
		{
			System.err.println(hp.getName() + " context: " +  hp.needsContext());
		}
	}

	public ResultGrouper(Searcher s)
	{
		this.searcher = s;
		//defaultInitProperties();
	}

	public ResultGrouper(Searcher s, HitPropertyMultiple multipleProperty)
	{
		this.searcher = s;
		this.multipleProperty = multipleProperty;
		//init();
	}

	public ResultGrouper(Searcher searcher, String[] metadataProperties, String[] wordProperties)
	{
		this.searcher = searcher;
		this.metadataProperties = metadataProperties;
		this.wordProperties = wordProperties;



		//HitProperty[] a = new HitProperty[propertyList.size()];
		//a = propertyList.toArray(a);
		//multipleProperty  = new HitPropertyMultiple(a);
		System.err.println("multiple hit property:  " + multipleProperty);
		if (multipleProperty != null)
		{
			for (HitProperty hp: multipleProperty)
			{
				System.err.println(hp.getName() + " context: " +  hp.needsContext());
			}
		}
	}

	protected void initHitProperties(Hits hits, String[] metadataProperties,
			String[] wordProperties)
	{
		multipleProperty  = null;
		boolean first = true;
		List<HitProperty> propertyList =  new ArrayList<HitProperty>();


		for (String s: wordProperties)
		{
			HitPropertyHitText p = 
					new HitPropertyHitText(hits,Searcher.DEFAULT_CONTENTS_FIELD_NAME,s);
			//propertyList.add(p);
			if (multipleProperty == null)
				multipleProperty = new HitPropertyMultiple(p);
			else
				multipleProperty.addCriterium(p);
		}
		for (String s: metadataProperties)
		{
			HitPropertyDocumentStoredField p = new HitPropertyDocumentStoredField(hits, s, s);
			//propertyList.add(p);
			if (multipleProperty == null)
				multipleProperty = new HitPropertyMultiple(p);
			else
				multipleProperty.addCriterium(p);
		}
		List<String> context = multipleProperty.needsContext();
		if (context != null)
		{
			System.err.println("Finding context for multiple property: " + context + " hit size=" + hits.size());
			hits.findContext(context); // Ahem. Not so nice....
		}
	}

	private void collectGroupsOnePortionOld(Hits hits, HitPropertyMultiple theProperty, Counter<Map> mapCounter) 
	{
		System.err.println("######## start grouping ##### ");
		ResultsGrouper grouper = new ResultsGrouper(hits, theProperty);
		List<HitProperty> baseProperties = new ArrayList<HitProperty>();
		for (HitProperty hp: theProperty)
		{
			baseProperties.add(hp);
		}
		for (HitGroup g: grouper.getGroups())
		{
			try
			{
				HitPropValue v = g.getIdentity();
				//System.err.println("Now in group: " + g + " " + v);
				// en nu wil ik de deelwaarden opvragen .. hoe ????? kan via hits, maar dat is misschien een beetje raar...
				getAndStoreGroupProperties(baseProperties, g, mapCounter);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			//System.err.println();
			//System.out.println(g.size());
		}

	}

	private void collectGroupsOnePortion(Hits hits, HitPropertyMultiple theProperty, Counter<Map> mapCounter) 
	{
		System.err.println("######## start grouping ##### ");
		List<String> context = theProperty.needsContext();
		System.err.println(theProperty);
		if (context != null)
		{
			System.err.println("need context for theProperty: "  + context);
			hits.findContext(context); // Ahem. Not so nice....
		}
		HitGroups hg = hits.groupedBy(theProperty);
		System.err.println("Number of groups: " + hg.getGroups().size());
		//ResultsGrouper grouper = new ResultsGrouper(hits, theProperty);
		List<HitProperty> baseProperties = new ArrayList<HitProperty>();
		for (HitProperty hp: theProperty)
		{
			baseProperties.add(hp);
		}
		for (HitGroup g: hg)
		{
			try
			{
				HitPropValue v = g.getIdentity();
				//System.err.println("Now in group: " + g + " " + v);
				// en nu wil ik de deelwaarden opvragen .. hoe ????? kan via hits, maar dat is misschien een beetje raar ...
				getAndStoreGroupProperties(baseProperties, g, mapCounter);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			//System.err.println();
			//System.out.println(g.size());
		}

	}
	private void getAndStoreGroupProperties(List<HitProperty> baseProperties, HitGroup g, Counter<Map> mapCounter) 
	{
		Hit h0 = g.getHits().get(0);
		String asString = "";
		Map<String,String> map = new HashMap<String,String>();

		for (HitProperty hp: baseProperties) // minor problem property names do not reflect attribute
		{
			// hp.setHits(g.getHits()); TODO FIX THIS
			asString += hp.getName() +  "=" + hp.get(0) + ", ";
			map.put(hp.getName(), hp.get(0).toString());
			System.err.println(hp.getName() +  "=" + hp.get(0).toString() + ", ");
		}
		System.err.println("group props map:  " + map);
		mapCounter.increment(map, g.size());
		//stringCounter.increment(asString, g.size());
	}

	public Counter<Map> collectGroups(Hits hits) 
	{
		int k=0;
		int p=5000000;

		// inintialize properties here.....
		initHitProperties(hits, metadataProperties, wordProperties);

		List<Hit> list = new ArrayList<Hit>();
		Counter<Map> mapCounter = new Counter<Map>();
		for (Hit hit: hits)
		{
			list.add(hit);
			k++;
			if (k % p ==0)
			{
				Hits portion=null; // TODO fix this = new Hits(this.searcher,list);
				collectGroupsOnePortion(portion, multipleProperty, mapCounter);
				list.clear();
			}
		}
		if (list.size() > 0)
		{
			Hits portion = null; // TODO fix this = new Hits(this.searcher,list);
			collectGroupsOnePortion(portion, multipleProperty, mapCounter);
		}
		return mapCounter;
	}

	public Counter<Map> printGroupedResults(Searcher searcher, String corpusQlQuery, String filterQueryString) throws ParseException, 	nl.inl.blacklab.queryParser.corpusql.ParseException 
	{
		Hits hits = BlacklabFunctions.filteredSearch(searcher, corpusQlQuery, filterQueryString);
		System.err.println("find finished: " + hits.size());

		/**
		 * Zonder die override kan ik xxx niet toevoegen aan mijn multiple, krijg dan null pointer exception
		 */

		Counter<Map> mapCounter = this.collectGroups(hits);
		for (Map<String,String> m: mapCounter.keySet())
		{
			System.out.println(m + " " + mapCounter.get(m));
		}
		return mapCounter;
	}

	public Counter<Map> getGroupedResults(Searcher searcher, String corpusQlQuery, String filterQueryString) 
			throws ParseException, nl.inl.blacklab.queryParser.corpusql.ParseException 

	{
		Hits hits = BlacklabFunctions.filteredSearch(searcher, corpusQlQuery, filterQueryString);

		System.err.println("find finished, no: hits" + hits.size());

		/**
		 * Zonder die override kan ik xxx niet toevoegen aan mijn multiple, krijg dan null pointer exception
		 */

		Counter<Map> mapCounter = this.collectGroups(hits);
		return mapCounter;
	}

	private static String toFilter(Map<String,String>  m)
	{
		String r = "";
		for (String k: m.keySet())
		{
			String v = m.get(k);
			r += k + ":" + v + " ";
		}
		r = r.replaceAll(" $", "");
		return r;
	}

	public List<FrequencyInformation> getDistribution(String query, String filterQuery)
	{
		List<FrequencyInformation> profile = new ArrayList<FrequencyInformation>();
		try
		{
			Counter<Map> result = this.getGroupedResults(searcher, query, filterQuery);
			for (Map<String,String> m: result.keyList())
			{
				int f = result.get(m);


				String filter = toFilter(m);
				String combinedFilter = filter;
				if (filterQuery != null && filterQuery.length() > 0)
					combinedFilter = filter + "  AND " + filterQuery;

				int[] td = BlacklabFunctions.getSubcorpusSize(searcher, combinedFilter); // hier moet je filterQuery ook meenemen!

				int tokensInYear = td[0];
				int docs = td[1];

				FrequencyInformation fi = new FrequencyInformation();
				fi.metadataProperties = m;
				fi.frequency = f;
				fi.totalNumberOfTokens = tokensInYear;
				fi.docFrequency = docs;
				profile.add(fi);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		//Collections.sort(profile, new CompareBy("witnessYear_from"));
		return profile;
	}

	public List<FrequencyInformation> getDistribution(String[] queries, String filterQuery)
	{
		List<Map<String,FrequencyInformation>> lists = new ArrayList<Map<String,FrequencyInformation>>();
		Map<String, FrequencyInformation> years = new HashMap<String, FrequencyInformation>();

		for (String query: queries)
		{
			List<FrequencyInformation> l = getDistribution(query, filterQuery);
			Map<String,FrequencyInformation> m = new HashMap<String,FrequencyInformation>();
			lists.add(m);
			for (FrequencyInformation x: l)
			{
				String year =x.metadataAsString(true); 
				years.put(year, x);
				m.put(year, x);
			}
		}

		for (String year: years.keySet())
		{
			FrequencyInformation sample = years.get(year);
			for (int i=0; i < lists.size(); i++)
			{
				int f=0;
				FrequencyInformation fi = lists.get(i).get(year);
				if (fi != null)
					f = fi.frequency;
				sample.frequencies.add(f);
				sample.multiple = true;
			}
		}
		List<FrequencyInformation> r = new ArrayList<FrequencyInformation>();
		for (FrequencyInformation x: years.values())
		{
			x.frequency = -1;
			r.add(x);
		}
		//Collections.sort(r,  new CompareBy("witnessYear_from"));
		return r;
	}

	public static void main(String[] args)
	{

		String indexDir =  args[0];

		Searcher searcher;
		try 
		{
			File f = new File(indexDir);
			if (!f.isDirectory())
			{
				System.err.println("gaat niet werken " + indexDir);
				return;
			}
			Hits.setDefaultMaxHitsToCount(Integer.MAX_VALUE);
			Hits.setDefaultMaxHitsToRetrieve(Integer.MAX_VALUE);
			searcher = Searcher.open(new File(indexDir));
			ResultGrouper gc = new ResultGrouper(searcher,args[1].split(","), args[2].split(","));
			String query1 = args[3];
			System.err.println("query: " + query1);
			gc.printGroupedResults(searcher , query1, null);
			//String query2 = args[4];


		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}