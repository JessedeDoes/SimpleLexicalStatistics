package blacklabapi;

import nl.inl.blacklab.search.Hits;
import nl.inl.blacklab.search.Searcher;
import nl.inl.blacklab.search.grouping.HitPropValueContextWords;
import nl.inl.blacklab.search.grouping.HitPropertyHitText;

public class MyHitPropertyHitText extends HitPropertyHitText
{
	public MyHitPropertyHitText(Hits hits, String field,
			String property, String friendlyName) 
	{
		super(hits, field, property);
		this.friendlyName = friendlyName;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public HitPropValueContextWords get(int i)
	{
		try
		{
			return super.get(i);
		} catch (Exception e)
		{
			System.err.println("Error at " + i);
			e.printStackTrace();
			return null;
		}
	}
		
	public String getValue(int i)
	{
		HitPropValueContextWords x = get(i);
		if (x == null)
			return "";
		return x.toString();
	}

	@Override
	public String getName()
	{
		return friendlyName;
	}

	public String friendlyName = "unk";
}