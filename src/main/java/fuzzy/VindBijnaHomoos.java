package fuzzy;

import java.io.File;
import java.util.List;
import java.util.*;


import test.ListLookupKeepingIds;

/**
 * Voor spelling: zoek woorden die maximaal een letter verschil hebben
 * @author does
 *
 */
public class VindBijnaHomoos
{
	public static void main(String[] args)
	{
		ApproximateMatcher t = new ApproximateMatcher();
		t.setMinimumWordLengthToMatch(7);
		t.setMaximumDistance(1);
		
		Map<String,String> lemmaMap = ListLookupKeepingIds.readList(args[0]);
		Map<String, Set<String>> lemmaToIdMap = new HashMap<String, Set<String>>();
		
		for (String id: lemmaMap.keySet())
		{
			String lemma = lemmaMap.get(id);
			String lemmaLC = lemma.toLowerCase();
			Set<String> s = lemmaToIdMap.get(lemmaLC);
			if (s == null)
			{	
				s = new HashSet<String>();
				lemmaToIdMap.put(lemmaLC, s);
			}
			s.add(id);
		}
		
		List<String> l = new ArrayList();
		
		l.addAll(lemmaMap.values());
		
		Collections.sort(l);
		
		Map<String,List<String>>  bijnaHomoos = t.matchUsingIndex(l,l);
		
		for (String id: lemmaMap.keySet())
		{
			String lemma = lemmaMap.get(id);
			List<String> matches = bijnaHomoos.get(lemma);
			
			if (matches != null && matches.size() > 0)
			{
				for (String l2: matches)
				{
					Set<String> idz = lemmaToIdMap.get(l2);
					System.out.println(id + "\t"  + lemma +  "\t"  + l2 + "\t" + idz);
				}
			}
		}
	}
}
