package trend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryparser.classic.ParseException;

import util.Counter;
import wordlist.WordListMaker;
import nl.inl.blacklab.search.Searcher;
import blacklabapi.BlacklabFunctions;
import blacklabapi.FrequencyInformation;
import blacklabapi.ResultGrouper;

import java.util.*;
/**
 * Still rather slow for huge lemmata
 * @author does
 *
 */
public class FrequencyDevelopment
{
	Searcher searcher;
	String[] mdp = {"witnessYear_from"}; // dit moet dus ook nog uit scale komen....
	String[] p = {};

	ResultGrouper rg;
	//int lowerBound = Integer.MIN_VALUE;
	//int upperBound=Integer.MAX_VALUE;
	String filterQuery = null;

	static Map<String,int[]> subcorpusSizeMap = new HashMap<String, int[]>();

	// cached version of getting subcorpus sizes
	
	int[] getSubcorpusSize(String filter)
	{
		try
		{
			String indexDir = this.searcher.getIndexDirectory().getCanonicalPath();
			String key = indexDir + ":" + filter;
			if (subcorpusSizeMap.get(key) != null)
			{
				return subcorpusSizeMap.get(key);
			}
			int[] td = BlacklabFunctions.getSubcorpusSize(this.searcher, filter);
			subcorpusSizeMap.put(key, td);
			return td;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	                                                     
	TimeScale scale;

	public void setFilterQuery(String s)
	{
		this.filterQuery = s;
	}

	public FrequencyDevelopment(String indexDir)
	{
		BlacklabFunctions.removeLimits();
		try
		{
			searcher  = Searcher.open(new File(indexDir));
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		rg = new ResultGrouper(searcher ,  mdp, p);
	}

	public FrequencyDevelopment(String indexDir, TimeScale scale)
	{
		BlacklabFunctions.removeLimits();
		try
		{
			searcher  = Searcher.open(new File(indexDir));
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.scale = scale;
		this.mdp = scale.getGroupingProperties();
		rg = new ResultGrouper(searcher ,  mdp, p);
	}


	public FrequencyDevelopment(Searcher s, int l, int u)
	{
		BlacklabFunctions.removeLimits();
		this.searcher = s;
		//this.lowerBound = l;
		//this.upperBound = u;
		this.scale = new Years(l,u);

		rg = new ResultGrouper(searcher ,  mdp, p);
	}

	public FrequencyDevelopment(Searcher s, TimeScale scale)
	{
		BlacklabFunctions.removeLimits();
		this.searcher = s;
		this.scale = scale;
		this.mdp = scale.getGroupingProperties();

		rg = new ResultGrouper(searcher ,  mdp, p);
	}

	public List<FrequencyInformation> getDevelopment(String[] queries)
	{
		List<Map<String,FrequencyInformation>> lists = new ArrayList<Map<String,FrequencyInformation>>();
		Map<String, FrequencyInformation> years = new HashMap<String, FrequencyInformation>();

		for (String query: queries)
		{
			List<FrequencyInformation> l = getDevelopment(query);
			Map<String,FrequencyInformation> m = new HashMap<String,FrequencyInformation>();
			lists.add(m);
			for (FrequencyInformation x: l)
			{
				String year =x.metadataAsString(false); 
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
				{
					f = fi.frequency;
				
				}
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
		List<String> ql =  Arrays.asList(queries);
		for (FrequencyInformation x: r)
		{
			x.queries = ql;
		}
		Collections.sort(r,  scale);
		return r;
	}

	public List<FrequencyInformation> getDevelopment(String query)
	{
		System.err.println("getDevelopment: " + query);
		List<FrequencyInformation> profile = new ArrayList<FrequencyInformation>();
		try
		{
			Counter<Map> result = rg.getGroupedResults(searcher, query, this.filterQuery);
			for (Map<String,String> m: result.keyList())
			{
				int f = result.get(m);
				FrequencyInformation fi = new FrequencyInformation();
				fi.metadataProperties = m;
				fi.query = query;
				if (this.scale.accepts(fi))
				{
					int y = scale.time(fi);
					fi.time = y;
					String filter = scale.getFilter(fi);
					String combinedFilter = filter;

					if (filterQuery != null && filterQuery.length() > 0)
						combinedFilter = filter + "  AND " + filterQuery;

					//int[] td = BlacklabFunctions.getSubcorpusSize(this.searcher, combinedFilter); 
					System.err.println("combined Filter: " + combinedFilter);
					int[] td = getSubcorpusSize(combinedFilter);
					int tokensInYear = td[0];
					int docs = td[1];
					System.err.println("tokens in year: "  + tokensInYear);
					fi.frequency = f;
					fi.totalNumberOfTokens = tokensInYear;
					fi.docFrequency = docs;
					profile.add(fi);
				} else
				{
					System.err.println("Metadata not accepted by scale "   +  scale.getClass().getName() + " :" + fi.metadataAsString(true));
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		Collections.sort(profile, scale);
		return profile;
	}
}
