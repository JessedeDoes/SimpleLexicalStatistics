package trend;

import blacklabapi.FrequencyInformation;

public class Days extends TimeScale
{
	int lowerBound = Integer.MIN_VALUE;
	int upperBound=Integer.MAX_VALUE;

	@Override

	public int time(FrequencyInformation f)
	{
		String y = f.metadataProperties.get("witnessYear_from");
		String m = f.metadataProperties.get("witnessMonth_from");
		String d = f.metadataProperties.get("witnessDay_from");
		return Integer.parseInt(d) + 100 * Integer.parseInt(m) + 10000 * Integer.parseInt(y);
	}

	@Override
	public boolean accepts(FrequencyInformation f)
	{
		String y = f.metadataProperties.get("witnessYear_from");
		String m = f.metadataProperties.get("witnessMonth_from");
		String d = f.metadataProperties.get("witnessDay_from");
		if (! ( y.matches("\\d{4}")  && m.matches("\\d{2}") && d.matches("\\d{2}")))
		{
			System.err.println(" +Huh:.... " + y + "/" + m + "/" + d);
			return false;
		} else
		{
			return true;
		}
		/**
		if  (d.matches("\\d{2}"))
		{
			int day = Integer.parseInt(d);
			return day >= lowerBound && day <= upperBound;
		}
		return false;
		**/
	}



	public Days(int l, int u)
	{
		this.upperBound = u;
		this.lowerBound = l;
	}
	@Override
	public String[] getGroupingProperties()
	{
		String[] p = {"witnessYear_from", "witnessMonth_from", "witnessDay_from"};
		return p;
	}
}