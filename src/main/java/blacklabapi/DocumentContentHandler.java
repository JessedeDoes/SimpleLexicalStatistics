package blacklabapi;

import java.util.ArrayList;
import java.util.List;

public  interface DocumentContentHandler
{
	public  boolean handleDocumentContent(String docId, List<String> propertyNames,  List<List<String>> propertyValuesPerProperty);
}
