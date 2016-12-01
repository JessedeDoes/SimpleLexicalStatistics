package test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import util.StringUtils;
import nl.inl.blacklab.search.Searcher;
import blacklabapi.DocumentContentHandler;
import blacklabapi.TokenVisitor;

/**
 * Dumps plain text of selected / all documents
 * @author does
 *
 */

public class DumpDocuments implements DocumentContentHandler
{
	String[] properties = {"word"};
	Searcher searcher;

	public DumpDocuments(Searcher searcher)
	{
		this.searcher = searcher;
	}

	public DumpDocuments(String dir)
	{
		try
		{
			this.searcher = Searcher.open(new File(dir));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void dump(String metadataQuery)
	{
		TokenVisitor tv = new  TokenVisitor(searcher, Arrays.asList(properties));
		tv.loopOverAllTokensinDocumentsSatisfyingMetadataFilter(metadataQuery, null, this);
	}

	@Override
	public boolean handleDocumentContent(String docId, List<String> propertyNames,
			List<List<String>> propertyValuesPerProperty)
	{
		String docContent = StringUtils.join(propertyValuesPerProperty.get(0), " ");
		if (RemoveIllegalUnicode.CheckIllegalCharacters(docContent))
			System.out.println(docId + "\t" + docContent);
		return true;
	}

	public static void main(String[] args)
	{
		DumpDocuments dd = new DumpDocuments(args[0]);
		dd.dump(args[1]);
	}
}
