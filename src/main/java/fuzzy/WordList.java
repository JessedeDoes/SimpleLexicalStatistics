package fuzzy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class WordList 
{
	private Map<String,Integer> typeFrequency = new HashMap<String,Integer>();

	private HashMap<String,Integer> caseInsensitiveTypeFrequency = new HashMap<String,Integer>();
	private List<TypeFrequency> frequencyList = new ArrayList<TypeFrequency>();
	
	private List<TypeFrequency> caseInsensitiveFrequencyList = new ArrayList<TypeFrequency>();
	
	private ValueComparator comparator = new ValueComparator(typeFrequency);
	private ValueComparator comparatorci = new ValueComparator(caseInsensitiveTypeFrequency);
	private int nTypes = 0;
	private int nTokens = 0;
	
	boolean sorted = false;
	
	public WordList(String fileName)
	{
		readList(fileName);
	}
	
	public WordList() 
	{
		// TODO Auto-generated constructor stub
	}

	public void readList(String fileName)
	{
		try 
		{
			BufferedReader r = new BufferedReader(new 
					InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			String s;
			while ((s = r.readLine()) != null)
			{	
				String[] columns = s.split("\\t");
				
				if (columns.length > 1)
				{
					String w = columns[0];
					int f = Integer.parseInt(columns[1]);
					incrementFrequency(w, f);
				}
			}
			//sortByFrequency();
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void incrementFrequency(String s, int f) 
	{
		sorted = false;
		
		//typeFrequency.put(s,f);
		
		Integer x = typeFrequency.get(s);
		int y = (x != null)?x:0;
		typeFrequency.put(s,f+y);
		
		String w = s.toLowerCase();
		x = caseInsensitiveTypeFrequency.get(w);
		y = (x != null)?x:0;
		caseInsensitiveTypeFrequency.put(w,f+y);
	}
	
	public int getFrequency(String w)
	{
		Integer f = caseInsensitiveTypeFrequency.get(w.toLowerCase());
		if (f == null)
			return -1;
		else
			return f;
	}
	
	public  void sortByFrequency()
	{	
		if (sorted)
		  return;
		
		for (String s:typeFrequency.keySet())
		{
			frequencyList.add(new TypeFrequency(s,typeFrequency.get(s)));
		}
		Collections.sort(frequencyList, comparator);
		for (String s:caseInsensitiveTypeFrequency.keySet())
		{
			caseInsensitiveFrequencyList.add(new TypeFrequency(s,caseInsensitiveTypeFrequency.get(s)));
		}
		Collections.sort(caseInsensitiveFrequencyList, comparatorci);
		sorted = true;
	}
	
	public List<TypeFrequency> keyList()
	{
		sortByFrequency();
		return caseInsensitiveFrequencyList;
	}

	public List<TypeFrequency> sensitiveKeyList()
	{
		sortByFrequency();
		return frequencyList;
	}

        public Set<String> keySet()
        {
             return caseInsensitiveTypeFrequency.keySet(); 
        }
	
	public static class ValueComparator implements Comparator<TypeFrequency> 
	{

		Map<String,Integer> base;
		
		public ValueComparator(Map<String,Integer> _base) 
		{
			//System.err.println(_base);
			this.base = _base;
		}

		public int compare(TypeFrequency a, TypeFrequency b) 
		{
			if (base == null)
				System.err.println("this is not happening!");
			if(a.frequency < b.frequency) 
			{
				return 1;
			} else if(a.frequency == b.frequency) 
			{
				return a.type.compareTo(b.type);
			} else 
			{
				return -1;
			}
		}
	}
	public int getSize()
	{
		return this.typeFrequency.size();
	}
	
	public  List<TypeFrequency> getTypeFrequencyList()
	{
		return keyList();
	}
	
	static class TypeFrequency
	{	
		String type;
		int frequency;
		TypeFrequency(String type, int frequency)
		{
			this.type=type;
			this.frequency=frequency;
		}
		TypeFrequency()
		{
			type=null;
		}
	}

	public Map<Integer,Double> getCumulativeFrequencyMapping()
	{
           List<TypeFrequency> l = sensitiveKeyList(); 
	   int c=0;
	   int prevF = -1;
	   Map<Integer,Integer> map = new HashMap<Integer,Integer> ();
	   Map<Integer,Double> map1 = new HashMap<Integer,Double> ();
           double N=0;
	   for (TypeFrequency tf: l)
	   {
	      if (tf.frequency != prevF && prevF > 0)
		map.put(prevF,c); 
              c+= tf.frequency; 
              N += tf.frequency;
              prevF = tf.frequency;
	   }
           map.put(prevF,c);
	   for (int f: map.keySet())
	   {
              double p = map.get(f) / N;
              map1.put(f,p);
           }
           return map1;
	}
}
