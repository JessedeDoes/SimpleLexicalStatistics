package blacklabapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import json.JSONObjects;
import json.JSONSerializable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class FrequencyInformation implements JSONSerializable
{
	public Map<String,String> metadataProperties;
	public boolean multiple = false;
	public int frequency;
	public List<Integer> frequencies = new ArrayList<Integer>();
	public List<String> queries = new ArrayList<String>();
	public int docFrequency;
	public int totalNumberOfTokens;
	public int time;
	public String query = "";
	
	public double normalizedFrequency()
	{
		double rf;
		if (multiple)
			rf = this.normalizedFrequency(0);
		else
			rf = 1e6 * frequency / (double) totalNumberOfTokens;
		return rf;
	}

	public double normalizedFrequency(int i)
	{
		double rf = 1e6 * frequencies.get(i) / (double) totalNumberOfTokens;
		return rf;
	}

	public double getFrequencyRatio(int i, int j)
	{
		return frequencies.get(i)  / (double) frequencies.get(j);
	}

	public Double getFrequencyRatio()
	{
		if (frequencies != null || frequencies.size() > 1)
			return frequencies.get(0)  / (double) frequencies.get(1);
		else return null;
	}

	public String toString()
	{
		String info = metadataProperties.toString();
		return info + "\t" + frequency  + "\t"+ normalizedFrequency() + "\t" + totalNumberOfTokens;
	}

	public String metadataAsString(boolean includeNames)
	{
		if (includeNames)
			return metadataProperties.toString();
		else
		{
			String r="";
			for (String k: metadataProperties.keySet())
			{
				r += metadataProperties.get(k) + ",";
			}
			r = r.replaceAll(",$", "");
			return r;
		}
	}

	public JSONObject toJSON()
	{
		JSONObject o = new JSONObject();
		o.put("frequency", new Integer(frequency));
		o.put("totalNumberOfTokens", new Integer(totalNumberOfTokens));
		o.put("time", new Integer(time));
		o.put("normalizedFrequency", new Double(normalizedFrequency()));
		o.put("multiple", new Boolean(multiple));
		o.put("query",  query);
		JSONArray frequencies = JSONObjects.toJSONArray(this.frequencies);
		o.put("frequencies",  frequencies);
		if (multiple)
		{
			o.put("frequencyRatio", this.getFrequencyRatio());
		}
		JSONArray queries = JSONObjects.toJSONArray(this.queries);
		o.put("queries",  queries);
	
		
		JSONObject metadata = new JSONObject();
		for (String k: this.metadataProperties.keySet())
		{
			metadata.put(k, metadataProperties.get(k));
		}
		o.put("metadata", metadata);
		return o;
	}
}