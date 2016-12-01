package blacklabapi;

import nl.inl.blacklab.search.Hits;
import nl.inl.blacklab.search.grouping.HitPropertyDocumentStoredField;

class MyHitPropertyDocumentStoredField extends HitPropertyDocumentStoredField
{
	public MyHitPropertyDocumentStoredField(String fieldName,
			String friendlyName, Hits hits) 
	{
		super(hits, friendlyName, fieldName);
		// TODO Auto-generated constructor stub
	}

	/*
	public List<String> needsContext()
	{
		return new ArrayList<String>();
	}
	 */
}