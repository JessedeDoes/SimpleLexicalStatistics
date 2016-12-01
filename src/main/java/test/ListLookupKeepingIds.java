package test;

import java.io.*;

import util.Counter;

import nl.inl.blacklab.search.Searcher;
import blacklabapi.BlacklabFunctions;
import java.util.*;
public class ListLookupKeepingIds
{
	
	public static Map<String,String> readList(String fileName)
	{
		Map<String,String> m = new HashMap<String,String>();
		try
		{
			BufferedReader b = new BufferedReader(new FileReader(fileName));
			String l;
			while ((l = b.readLine()) != null)
			{
				String[] columns = l.split("\t");
				if (columns.length > 1)
					m.put(columns[0], columns[1]);
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
			Map<String,String> m = readList(args[2]);
			Set<String> V = new HashSet<String>();
			for (String v: m.values())
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
			for (String id:  m.keySet())
			{
				System.out.println(id + "\t" +  m.get(id) + "\t" + results.get(m.get(id)));
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
