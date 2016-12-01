package test;

import java.io.*;

import util.Counter;

import nl.inl.blacklab.search.Searcher;
import blacklabapi.BlacklabFunctions;
import java.util.*;
public class CompareFrequencies
{
	
	public static Map<String,String> readList(String fileName, Map<String,String> m1, Map<String,String> m2)
	{
		Map<String,String> m = new HashMap<String,String>();
		try
		{
			BufferedReader b = new BufferedReader(new FileReader(fileName));
			String l;
			int k=1;
			while ((l = b.readLine()) != null)
			{
				String id = String.format("%06d", k++);
				String[] columns = l.split("\t");
				if (columns.length > 0)
					m1.put(id, columns[0]);
				if (columns.length > 1)
					m2.put(id, columns[1]);
				else
				{
					System.err.println("Weird line " + l);
					System.exit(1);
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return m;
	}
	
	
	public static void main(String[] args)
	{
		try
		{
		
			String property = args[1];
			Map<String,String> m1 = new HashMap<String,String>();
			Map<String,String> m2 = new HashMap<String,String>();
			Map<String,String> m = readList(args[2],m1,m2);
			Set<String> V = new HashSet<String>();
			for (String v: m1.values())
			{
				V.add(v);
			}
			for (String v: m2.values())
			{
				V.add(v);
			}
			String[] B = new String[V.size()];
			int a = 0;
			for (String v: V) B[a++] = v;
	
			System.err.println(B.length);
			String filter = null;
			
			Searcher searcher = Searcher.open(new File(args[0]));
			
			if (args.length > 3)
			{
				System.err.println("using filter: " + args[3]);
				filter = args[3];
			}
			Counter<String> results = BlacklabFunctions.listLookup(searcher, B, filter, property);
			for (String id:  m1.keySet())
			{
				System.out.println(id + "\t" +  m1.get(id) + "\t" + results.get(m1.get(id)) + "\t" + m2.get(id) + "\t" + results.get(m2.get(id)) );
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
