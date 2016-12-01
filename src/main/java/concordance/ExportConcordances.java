package concordance;
import java.io.File;
import java.io.FileWriter;

import util.TabSeparatedToExcel;
import blacklabapi.BlacklabFunctions;
import blacklabapi.MyHitPropertyHitText;
import nl.inl.blacklab.search.Concordance;
import nl.inl.blacklab.search.Hit;
import nl.inl.blacklab.search.Hits;
import nl.inl.blacklab.search.Searcher;
import nl.inl.blacklab.search.Span;
import nl.inl.blacklab.search.grouping.HitPropertyDocumentStoredField;
import nl.inl.blacklab.search.grouping.HitPropertyHitText;
import nl.inl.blacklab.search.grouping.HitPropertyMultiple;
import nl.inl.util.XmlUtil;

import java.util.*;

/*
 * To do: het zou leuk zijn de query, het aantal resultaten en nog wat leukigheid (spreidingsgrafiekje???) 
 * in een apart blad te zetten
 */

public class ExportConcordances
{
	String regexFilter = null;
	public void exportConcordances(String exportTo, Searcher searcher, String corpusQlQuery, String filterQuery)
	{
		try
		{
			FileWriter f = new FileWriter(exportTo);
		
			Hits.setDefaultMaxHitsToCount(Integer.MAX_VALUE);
			Hits.setDefaultMaxHitsToRetrieve(Integer.MAX_VALUE);
			
			Hits hits = BlacklabFunctions.filteredSearch(searcher, corpusQlQuery, filterQuery);
	
			HitPropertyDocumentStoredField titel = new HitPropertyDocumentStoredField(hits,"title", "Titel") ;
			HitPropertyDocumentStoredField jaar = new HitPropertyDocumentStoredField(hits,"witnessYear_from", "Jaar") ;
			HitPropertyDocumentStoredField variant = new HitPropertyDocumentStoredField(hits,"languageVariant", "nnbn") ;
			
			HitPropertyHitText lemmaProperty = new HitPropertyHitText(hits,Searcher.DEFAULT_CONTENTS_FIELD_NAME,"lemma");
			HitPropertyHitText wordProperty = new HitPropertyHitText(hits,Searcher.DEFAULT_CONTENTS_FIELD_NAME,"word");
			HitPropertyHitText posProperty = new HitPropertyHitText(hits, Searcher.DEFAULT_CONTENTS_FIELD_NAME,"pos");
			
			HitPropertyMultiple  multi = new HitPropertyMultiple(wordProperty, lemmaProperty, posProperty);
			//multi.addCriterium(lemmaProperty);
			//multi.addCriterium(posProperty);
			int i=0;
			
			List<String> neededProps =  multi.needsContext();
	
			
			hits.findContext(neededProps); // ahem? does this work?
			
			System.err.println("Need props:"  + neededProps);
			
			for (Hit h: hits)
			{
				Concordance c = hits.getConcordance(h,15);
				//System.err.println(c.match());
				String plainLeft = XmlUtil.xmlToPlainText(c.left());
				String plainHit = XmlUtil.xmlToPlainText(c.match());
				String plainRight = XmlUtil.xmlToPlainText(c.right());
				
				String year = jaar.get(i).toString();
				String title = titel.get(i).toString();
				String nnbn = variant.get(i).toString();
				//h.
				String pos = posProperty.get(i).toString();
				String lemma = lemmaProperty.get(i).toString();
			
				if (regexFilter == null || plainHit.matches(regexFilter))
					  f.write(year + "\t" + title +  "\t" + nnbn + "\t" + plainLeft + "\t" + plainHit + "\t" + plainRight +  "\t" +  pos+  "\t"  + lemma + "\n");
				
				i++;
			}
			f.close();
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void exportConcordancesToXLS(String exportTo, Searcher searcher, String corpusQlQuery, String filterQuery)
	{
		exportConcordances(exportTo + ".tab",searcher, corpusQlQuery, filterQuery );
		String[] args = {exportTo, "concordances:" + exportTo + ".tab" + ":jaar/titel/taalvariant/linkercontext/hit/rechtercontext/woordsoort/lemma"};
		TabSeparatedToExcel ts2e = new TabSeparatedToExcel();
		//ts2e.setColumnProperty("linkercontext", )
		ts2e.export(args);
	}
	
	public static void main(String[] args)
	{
		ExportConcordances exp = new ExportConcordances();
		exp.regexFilter = ".*[bcfghjklmnpqrsvwxz]e$"; // geen d...
		exp.regexFilter = null;
		try 
		{
			
			Searcher searcher = Searcher.open(new File(args[1]));
			//searcher.s
			String query = args[2];
			String filter = null;
			exp.exportConcordancesToXLS(args[0], searcher, query, filter);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
