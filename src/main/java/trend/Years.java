package trend;

import blacklabapi.FrequencyInformation;

public class Years extends TimeScale
{
	int lowerBound = Integer.MIN_VALUE;
	int upperBound=Integer.MAX_VALUE;
	@Override
	public int time(FrequencyInformation f)
	{
		String y = f.metadataProperties.get("witnessYear_from");
		return Integer.parseInt(y);
	}

	@Override
	public boolean accepts(FrequencyInformation f)
	{
		String y = f.metadataProperties.get("witnessYear_from");
		if  (y.matches("\\d{4}"))
		{
			int year = Integer.parseInt(y);
			return year >= lowerBound && year <= upperBound;
		}
		return false;
	}
	
	
	public Years(int l, int u)
	{
		this.upperBound = u;
		this.lowerBound = l;
	}

	@Override
	public String[] getGroupingProperties()
	{
		String[] p = {"witnessYear_from"};
		return p;
	}
}