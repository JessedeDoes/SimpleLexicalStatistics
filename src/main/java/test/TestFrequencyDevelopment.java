package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import trend.FrequencyDevelopment;
import trend.TimeScale;
import trend.TimeScale.Type;
import trend.TrendAnalysis;
import util.Counter;
import blacklabapi.FrequencyInformation;

public class TestFrequencyDevelopment
{
	public static void main(String [] args)
	{
		try
		{
			TimeScale scale = TimeScale.getTimeScale(Type.DAY, 01, 30);
			FrequencyDevelopment fd = new FrequencyDevelopment(args[0], scale);
			List<FrequencyInformation> l1 = fd.getDevelopment(args[1]);
			if (args.length <= 2)
			{
				for (FrequencyInformation fi: l1) System.out.println(fi);
				TrendAnalysis ta = new TrendAnalysis(l1);
				System.out.println(ta.toJSON().toJSONString());
			}
			else
			{
				List<FrequencyInformation> l2 = fd.getDevelopment(args[2]);
				List<String> metadataValues = new ArrayList<String>();
				Counter<String> cs = new Counter<String>();
				Map<String, FrequencyInformation> m1 = new HashMap<String, FrequencyInformation>();
				Map<String, FrequencyInformation> m2 = new HashMap<String, FrequencyInformation>();
				
				for (FrequencyInformation fi: l1) 
				{
					String mp = fi.metadataAsString(false);
					
					cs.increment(mp);
					m1.put(mp, fi);
				}
				
				for (FrequencyInformation fi: l2) 
				{
					String mp = fi.metadataAsString(false);
					cs.increment(mp);
					m2.put(mp, fi);
				}
				
				metadataValues = cs.keyList();
				Collections.sort(metadataValues);
				System.out.println("year\tn1\tn2\tf1\tf2\tN");
				
				for (String s: metadataValues)
				{
					FrequencyInformation f1  = m1.get(s);
					FrequencyInformation f2  = m2.get(s);
				    double n1 = f1 != null?f1.normalizedFrequency():0;
				    double n2 = f2 != null?f2.normalizedFrequency():0;
				    long N = f1==null?f2.totalNumberOfTokens:f1.totalNumberOfTokens;
				    int F1 = f1==null?0:f1.frequency;
				    int F2 = f2==null?0:f2.frequency;
					System.out.println(s + "\t" +  n1 + "\t" + n2 + "\t" + 
							F1 + "\t" + F2 + "\t" + N);
				}
			}
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}

}
