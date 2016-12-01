package trend;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import util.StringUtils;

import blacklabapi.FrequencyInformation;

public abstract class  TimeScale implements Comparator<FrequencyInformation>
{
	public abstract int time(FrequencyInformation f);
	public abstract boolean accepts(FrequencyInformation f);


	public String getFilter(FrequencyInformation f)
	{
		List<String> l = new ArrayList<String>();
		for (String s: this.getGroupingProperties())
		{
			l.add(s + ":" + f.metadataProperties.get(s));
		}
		return StringUtils.join(l, " AND ");
	}
	
	public abstract String[] getGroupingProperties();

	public enum Type {DECADE, YEAR, MONTH, DAY, HOUR};

	public static TimeScale getTimeScale(Type type, int l, int u)
	{
		TimeScale t;
		switch (type)
		{
			case YEAR: t = new Years(l,u); break;
			case DAY: t = new Days(l,u); break;
			case MONTH: t = new Months(l,u); break;
			default: t = null; break;
		}
		return t;
	}
	@Override
	public int compare(FrequencyInformation arg0, FrequencyInformation arg1)
	{
		// TODO Auto-generated method stub
		return this.time(arg0) - this.time(arg1);
	}
}