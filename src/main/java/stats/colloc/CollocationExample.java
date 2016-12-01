package stats.colloc;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;

import stats.colloc.*;
import stats.colloc.score.ChiSquare;
import stats.colloc.score.Dice;
import stats.colloc.score.LL;
import stats.colloc.score.MI;
import stats.colloc.score.RelativeFrequency;
import stats.colloc.score.Salience;
import stats.colloc.score.TScore;
import util.Counter;
import blacklabapi.ResultGrouper;
import nl.inl.blacklab.search.Hits;
import nl.inl.blacklab.search.Searcher;

import java.util.*;
public class CollocationExample
{
	CollocationScore scoreFunction = new ChiSquare();
	Searcher searcher;
	int N=0;

	Counter<String> F1 = new Counter<String>();
	Counter<String> F2 = new Counter<String>();
	Counter<String> F = new Counter<String>();

	int minF1 = 50;
	int minF = 10;

	public static class ScoreComparator implements Comparator<Collocation> 
	{

		@Override
		public int compare(Collocation arg0, Collocation arg1) 
		{
			// TODO Auto-generated method stub
			return Double.compare(arg1.score, arg0.score);
			//int r = (int) Math.signum(arg1.score - arg0.score);
			//return r;
		}
	}

	List<Collocation> collocations = new ArrayList<Collocation>();
	CollocationScore allScores[] = {new MI(), new Dice(), new RelativeFrequency(), new ChiSquare(), new TScore(), new LL(), new Salience()};

	List<CollocationScore> scoreFunctions = Arrays.asList(allScores);
	
	public void collectData()
	{
		String[] m = {};
		String[] p = { "lemma"};
		ResultGrouper gc = new ResultGrouper(
				searcher,m, p);
		
		String query1 = "[pos=\"ADP.*\"][pos=\"NOU-C.*\"][pos=\"VRB.*\"]" ;
		String query2 = "[pos=\"VRB.*\"][pos=\"ADP.*\"][pos=\"NOU-C.*\"]" ;
		
		String query = query2;
		System.err.println("query: " + query);
		
		
		try
		{
			Counter<Map> results = gc.getGroupedResults(searcher , query, null);
			System.err.println("results:" + results.size());
			for (Map<String,String> map: results.keySet())
			{
				Collocation c = new Collocation();
				c.f = results.get(map);
				N += c.f;
				String[] lemmata = map.get("lemma").split("\\s+");
				
				if  (query.equals(query1))
				{
					c.collocate = lemmata[0] + "," + lemmata[1];
					c.base = lemmata[2];
				}  else
				{
					c.base = lemmata[0];
					c.collocate = lemmata[1] + "," + lemmata[2];
				}
				
				F2.increment(c.collocate,c.f);
				F1.increment(c.base,c.f);
				if (c.f >= minF)
					collocations.add(c);
			}
			for (CollocationScore score: scoreFunctions)
			{
				String name = score.getClass().getSimpleName();
				System.err.println("results for:" + name);
				for (Collocation c: collocations)
				{
					c.f1 = F1.get(c.base);
					c.f2 = F2.get(c.collocate);
					c.score = score.score(N, c.f, c.f1, c.f2);
				}
				Collections.sort(collocations, new ScoreComparator());
				PrintWriter pw = new PrintWriter(new FileWriter("Collocations/results." + name + ".txt"));
				
				for (Collocation c: collocations)
				{
					if (c.f1 >= minF1)
						pw.println(c);
				}
				pw.close();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} 
	}

	public static void main(String[] args)
	{

		String indexDir =  args[0];
		CollocationExample ce = new CollocationExample();
		//Searcher searcher;
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
			ce.searcher = Searcher.open(new File(indexDir));
			ce.collectData();
			//String query2 = args[4];


		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
