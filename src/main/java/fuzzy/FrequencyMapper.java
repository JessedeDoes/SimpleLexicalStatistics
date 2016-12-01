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


/**
 * 
 * @author Jesse
 *
 *
 *ABBYY Old German dictionary:

  top 12% gets 100
  50% gets 70
  22% gets 50
  15% gets 30 


French:
   java FrequencyMapper french/IRLexicon.noshort.tf "12:100;30:70;22:50;15:30" > french/IRLexicon.noshort.tc


   his seems a bit unreal when working with "real" corpus data, about half of the words should have freq. 1 
 */

public class FrequencyMapper 
{
	Map<String,Integer> typeFrequency = new HashMap<String,Integer>();
	Map<Integer,Integer> frequencyToWeight = new HashMap<Integer,Integer>();
	
	ValueComparator compareByFrequency = new ValueComparator(typeFrequency);
	List<String> frequencyList = new ArrayList<String>();
	
	private int[][] slices =
	{
			{8,100},
			{8,70},
			{8,50},
			{8,30},
			{64,15}
	};
	
	public int assignWeight(long p)
	{
		int c=0;
		for (int i=0; i < slices.length; i++)
		{
			if (p >= c && p <= c + slices[i][0])
				return slices[i][1];
			c += slices[i][0];
		}
		return 20;
	}
	
	public FrequencyMapper(int[][] slices)
	{
		this.slices = slices;
	}
	
	public FrequencyMapper() 
	{
		// TODO Auto-generated constructor stub
	}

	private void sortByFrequency()
	{	
		frequencyList.addAll(typeFrequency.keySet());
		Collections.sort(frequencyList, compareByFrequency);
	}
	
	public void mapFrequencies()
	{
		double S = frequencyList.size();
		long previousP = -1;
		int previousW = -1;
	  	long previousF = -1;
	
		for (int k=0; k < frequencyList.size(); k++)
		{
			String s = frequencyList.get(k);
			long f = typeFrequency.get(s);
			long relativeRank = previousP;
			if (previousF != f)
				relativeRank = Math.round(100 * (k / S));
			int w;
			if (relativeRank == previousP)
				w = previousW;
			else
			{
				w = assignWeight(relativeRank);
				System.err.println(k + "(r=" + relativeRank + ") " + w + " " + s + " f= " + f);
			}
			//frequencyToWeight.put(typeFrequency.get(frequencyList.get(k)),w);
			System.out.println(frequencyList.get(k) + "\t" + w); // + "\t" + f);
			previousP = relativeRank;
			previousW = w;
			previousF = f;
		}
	}
	
	public void printOut()
	{
		
	}
	
	public static class ValueComparator implements Comparator 
	{

		Map<String,Integer> base;
		public ValueComparator(Map<String,Integer> base) 
		{
			this.base = base;
		}

		public int compare(Object a, Object b) 
		{

			if(base.get(a) < base.get(b)) 
			{
				return 1;
			} else if(base.get(a) == base.get(b)) 
			{
				return ((String) a).compareTo((String) b);
			} else 
			{
				return -1;
			}
		}
	}

	public void readList(String fileName)
	{

		try 
		{
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String s;
			while ((s = r.readLine()) != null)
			{	
				String[] columns = s.split("\\t");
				if (columns.length > 1)
					typeFrequency.put(columns[0],Integer.parseInt(columns[1]));
			}
			sortByFrequency();
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args)
	{
		FrequencyMapper f = new FrequencyMapper();
                if (args.length > 1)
                {
                  String s = args[1];
                  String[] parts = s.split(";");
                  int[][] slices = new int[parts.length][];
                  int k=0;
                  for (String p: parts)
                  {
                     int[] slice = new int[2];
                     slices[k++] = slice;
                     String[] x = p.split(":");
                     slice[0] = Integer.parseInt(x[0]);
                     slice[1] = Integer.parseInt(x[1]); 
                  }
                  f = new FrequencyMapper(slices);
                }
		f.readList(args[0]);
		f.mapFrequencies();
	}
}

