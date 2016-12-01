package servlet;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TotalHitCountCollector;
import org.json.simple.JSONArray;

import blacklabapi.BlacklabFunctions;
import blacklabapi.FrequencyInformation;
import blacklabapi.ResultGrouper;
import trend.FrequencyDevelopment;
import trend.LinearRegressionTrendAnalysis;
import trend.TimeScale;
import trend.TrendAnalysis;
import trend.Years;
import util.Counter;
import wordlist.SubcorpusComparison;
import wordlist.WordListMaker;
import wordlist.SubcorpusComparison.PairOfFrequencies;
import nl.inl.blacklab.search.Searcher;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import json.JSONObjects;

/**
 * 
 * @author does
 * Eerste basisfunctionaliteit:
 * Woordenlijst gegeven metadata
 * Verschil woordenlijsten gegeven metadata (keywords en LL)
 * Ontwikkeling frequenties in tijd (2000-2013)
 */

public class Statistics extends  javax.servlet.http.HttpServlet
{

	private static final long serialVersionUID = 1L;
	private String basePath="/datalokaal/Corpus/Indexes/";

	// ToDo: naar configuratiebestandje

	private String[][] indexLocations = 
		{
			{"CHN",  basePath + "CHNDecember2014Index"},
			//{"CHN+", basePath + "CHN+Index"},
			{"KBKranten", basePath + "KBKranten"},
			{"CHN++", basePath + "CHNI2014Index"},
			{"Twitter", basePath + "TwitterIndex"},
		};

	enum Action 
	{
		NONE,
		WORDLIST,
		FREQUENCYDEVELOPMENT,
		BATCHLOOKUP,
		LISTLOOKUP,
		DISTRIBUTION,
		KEYWORDS
	};

	private Map<String,Searcher> searcherMap = new HashMap<String,Searcher>(); 

	private Searcher getSearcher(String name)
	{
		Searcher s;
		if ((s = searcherMap.get(name)) != null)
		{
			return s;
		}
		for (int i=0; i < indexLocations.length; i++)
		{
			if (indexLocations[i][0].equals(name))
			{
				try
				{
					s = Searcher.open(new File(indexLocations[i][1]));
					searcherMap.put(name, s);
					return s;
				} catch (Exception e)
				{

				}
			}
		}
		return null;
	}

	public Map<String,String> cloneParameterMap(HttpServletRequest request)
	{
		Map<String,String> parameterMap = new HashMap<String,String>();
		for (Object s: request.getParameterMap().keySet())
		{
			System.err.println(s + " --> " + request.getParameter((String) s));
			parameterMap.put((String) s, request.getParameter((String) s)); 
		}
		return parameterMap;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException 
	{

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");

		Map<String,String> parameterMap = cloneParameterMap(request);
		FileUpload fup = new FileUpload("/tmp");
		Map<String,File> fileUploadMap = fup.processMultipartFormData(request, parameterMap);

		java.io.PrintWriter out = response.getWriter( );

		String corpusName = parameterMap.get("corpus");
		Searcher searcher = this.getSearcher(corpusName);
		String[] properties =  {"lemma", "word", "pos"};

		Action action = Action.NONE;

		try
		{
			action = Action.valueOf(parameterMap.get("action").toUpperCase());
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		switch(action)
		{
		case WORDLIST:
		{
			makeWordList(parameterMap, fileUploadMap, out, searcher);
			break;
		}

		case KEYWORDS:
		{
			keyWords(parameterMap, fileUploadMap, out, searcher);
			break;
		}

		case DISTRIBUTION:
		{
			String query = parameterMap.get("query");
			String filter = parameterMap.get("filter");
			String p = parameterMap.get("properties");
			String[] metadataProperties = p.split(";");
			String[] dataProperties = {}; 
			ResultGrouper rg = new ResultGrouper(searcher, metadataProperties, dataProperties);
			List<FrequencyInformation >l = rg.getDistribution(query, "");
			String s = JSONObjects.toJSONArray(l).toJSONString();
			out.println(s);
			break;
		}

		case  FREQUENCYDEVELOPMENT:
		{
			frequencyDevelopment(parameterMap, out, searcher);
			break;
		}

		case  BATCHLOOKUP:
		{
			batchLookup(parameterMap, fileUploadMap, out, searcher);
			break;
		}

		case LISTLOOKUP:
		{
			listLookup(parameterMap, fileUploadMap, out, searcher);
			break;
		}
		default:
		{
			out.println("No valid action specified. Doing nothing!");
		}
		}
		FileUpload.cleanup(fileUploadMap);
	}

	private void listLookup(Map<String, String> parameterMap, Map<String, File> fileUploadMap, PrintWriter out, Searcher searcher)
	{
		String rawQuery = parameterMap.get("query");
		File wordList = fileUploadMap.get("whitelist");
		String filter = parameterMap.get("filter");
		if  (wordList != null)
		{
			try
			{
				Counter<String> words = Counter.readFromFile(wordList.getCanonicalPath()); // 
				for (String w: words.keySet())
				{
					rawQuery += ";" + w;
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	
		String[] rawQueries = rawQuery.split("[\\s;]+");
		Counter<String> frequencies = BlacklabFunctions.listLookup(searcher, rawQueries, filter);
		for (String w: frequencies.keyList())
		{
			out.println(w + "\t" + frequencies.get(w));
		}
	}

	protected void keyWords(Map<String, String> parameterMap, Map<String, File> fileUploadMap, java.io.PrintWriter out, Searcher searcher)
	{
		String filter1 = parameterMap.get("filter"); // focus
		String filter2 = parameterMap.get("filter1"); // reference
		SubcorpusComparison comparison = new SubcorpusComparison();
		comparison.setStrict(true);
		comparison.setMinimumFrequency(4);
		BlackAndWhite BnW = BlackAndWhite.getUploadedLists(fileUploadMap);
		for (PairOfFrequencies pof: comparison.compareSubcorpora(searcher, filter1, filter2))
		{
			if ((BnW.white(pof.type) && !BnW.black(pof.type)))
				out.println(pof);
		}
	}

	/**
	 * Ook met file upload doen
	 * @param parameterMap
	 * @param out
	 * @param searcher
	 */
	protected void batchLookup(Map<String, String> parameterMap,  Map<String, File> fileUploadMap, 
			java.io.PrintWriter out, Searcher searcher)
	{
		String rawQuery = parameterMap.get("query");
		File wordList = fileUploadMap.get("wordlist");
		if  (wordList != null)
		{
			try
			{
				Counter<String> words = Counter.readFromFile(wordList.getCanonicalPath());
				for (String w: words.keySet())
				{
					rawQuery += ";" + w;
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		System.err.println("raw Query now:" + rawQuery);
		String filter = parameterMap.get("filter");
		String regressionType = parameterMap.get("regression");
		String scale = parameterMap.get("scale");
		TimeScale.Type scaleType = TimeScale.Type.YEAR;

		if (scale != null && scale.trim().length() > 0)
		{
			scaleType = TimeScale.Type.valueOf(scale.toUpperCase());
			System.err.println("scale type: "  + scaleType);
		}

		boolean useLinearRegression = false;

		if (regressionType != null && regressionType.trim().length() > 0 
				&& regressionType.toLowerCase().contains("linear"))
		{
			useLinearRegression = true;
		}

		int upperBound = 2014;
		int  lowerBound = 1960;

		String lower = parameterMap.get("lower");
		String upper = parameterMap.get("upper");

		if (lower != null)
			lowerBound = Integer.parseInt(lower);
		if (upper != null)
			upperBound = Integer.parseInt(upper);

		TimeScale timeScale = TimeScale.getTimeScale(scaleType,lowerBound, upperBound);

		FrequencyDevelopment fd = new FrequencyDevelopment(searcher, timeScale);

		if (filter != null && filter.trim().length() > 0)
			fd.setFilterQuery(filter);

		String[] rawQueries = rawQuery.split("[\\s;]+");
		List<FrequencyInformation> frequencies = null;

		if (rawQueries.length > 1)
		{
			List<String> queries = new ArrayList<String>();
			int k=0;
			for (int i=0; i < rawQueries.length; i++)
			{
				String  r = rawQueries[i];
				if (r   != null && r.trim().length() > 0)
					queries.add(BlacklabFunctions.singleWordQuery("word", r.trim()));
			}
			String[] qq = queries.toArray(new String[queries.size()]);
			frequencies = fd.getDevelopment(qq);
		}
		else
		{
			String query = BlacklabFunctions.singleWordQuery("word", rawQuery);
			frequencies = fd.getDevelopment(query);
		}

		TrendAnalysis ta = useLinearRegression? 
				new LinearRegressionTrendAnalysis(frequencies): new TrendAnalysis(frequencies);

				out.println(ta.toJSON().toJSONString());
	}

	protected void frequencyDevelopment(Map<String, String> parameterMap,
			java.io.PrintWriter out, Searcher searcher)
	{
		String query = parameterMap.get("query");
		String filter = parameterMap.get("filter");
		String regressionType = parameterMap.get("regression");
		String scale = parameterMap.get("scale");
		TimeScale.Type scaleType = TimeScale.Type.YEAR;

		System.err.println( "Here we are: " + parameterMap + " searcher " + searcher);
		if (scale != null && scale.trim().length() > 0)
		{
			scaleType = TimeScale.Type.valueOf(scale.toUpperCase());
			System.err.println("scale type: "  + scaleType);
		}

		boolean useLinearRegression = false;

		if (regressionType != null && regressionType.trim().length() > 0 
				&& regressionType.toLowerCase().contains("linear"))
		{
			useLinearRegression = true;
		}

		int upperBound = 2014;
		int  lowerBound = 1960;

		String lower = parameterMap.get("lower");
		String upper = parameterMap.get("upper");

		if (lower != null)
			lowerBound = Integer.parseInt(lower);
		if (upper != null)
			upperBound = Integer.parseInt(upper);

		TimeScale timeScale = TimeScale.getTimeScale(scaleType, lowerBound,  upperBound);
		System.err.println("intialized scale.... "  + timeScale);
		
		FrequencyDevelopment fd = new FrequencyDevelopment(searcher,  timeScale);
		
		System.err.println("fd before doing anything:  "  + fd);
		
		if (filter != null && filter.trim().length() > 0)
			fd.setFilterQuery(filter);

		String[] queries = query.split(";");
		
		for (int i=0; i < queries.length; i++)
			if (!queries[i].contains("="))
				queries[i] = "[lemma=\"" + queries[i] + "\"]";
		
		List<FrequencyInformation> frequencies = null;

		if (queries.length > 1)
			frequencies = fd.getDevelopment(queries);
		else
			frequencies = fd.getDevelopment(queries[0]);

		System.err.println(frequencies);
		TrendAnalysis ta = useLinearRegression? 
				new LinearRegressionTrendAnalysis(frequencies): new TrendAnalysis(frequencies);

				out.println(ta.toJSON().toJSONString());
	}


	protected void makeWordList(Map<String, String> parameterMap, Map<String, File> fileUploadMap, java.io.PrintWriter out, Searcher searcher) throws IOException
	{
		String filter1 = parameterMap.get("filter");
		System.err.println("Metadata filter: " + filter1);
		String corpusQuery =  parameterMap.get("query");
		WordListMaker wlm = new WordListMaker(searcher);
		
		wordlist.filter.LowercaseWord wrd = new wordlist.filter.LowercaseWord();
		wlm.setTokenFilter(wrd);
		
		Counter<String> wordList =
				(corpusQuery != null && corpusQuery.contains( "="))? wlm.makeWordList(corpusQuery,filter1)
				:wlm.makeWordList(filter1);
		System.err.println("Found words: " + wordList.size());

		BlackAndWhite BnW = BlackAndWhite.getUploadedLists(fileUploadMap);

		int k=0;
		for (String w: wordList.keyList())
		{
			if (BnW.white(w) &&!BnW.black(w))
			{
				out.println(w  + " " + wordList.get(w));
			}
		}
	}

	public void init()
	{

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, java.io.IOException 
			{
		System.err.println("GET REQUEST:" + request.getQueryString());
		doPost(request,response);
			} 
}
