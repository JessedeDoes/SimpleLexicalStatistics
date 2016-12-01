package fuzzy;
import java.util.*;
//import spellingvariation.*;
import util.*;

public class LexiconCleaner
{
   WordList wordList = new WordList();
   ApproximateMatcher matcher = new ApproximateMatcher();
   Set<String> strings;
   Set<String> riskyWords = new HashSet<String>();
   EvaluationReport report = null;
   int maxShort = 5 ;
   double targetCoverage = 0.99;

   public void pruneShortWords(String listFile)
   {
     wordList = new WordList();
     wordList.readList(listFile);
     strings = wordList.keySet();
     List<Map<Integer,Double>> maps = new  ArrayList<Map<Integer,Double>>();
     List<WordList> lists = new ArrayList<WordList>();
     for (int i=0; i <= maxShort; i++)
     {
        lists.add(new WordList());
     } 
     for (String s: strings)
     {
        int l = s.length();
        if (l <= maxShort)
        {
           lists.get(l).incrementFrequency(s,wordList.getFrequency(s));
        }
     }
     //List<Typefrequency> l = wordList.keyList();
     for (int i=0; i <= maxShort; i++)
     {
        Map<Integer,Double> map = lists.get(i).getCumulativeFrequencyMapping();
        maps.add(map);
        System.err.println("#### " + i + "####");
        List<Integer> l = toList(map.keySet());
        Collections.sort(l);
        for (int f: l)
        {
          System.err.println(f + ":" + map.get(f));
        }
        for (String s: lists.get(i).keySet())
        {
           if (s.length() == i)
           {
             int freq = wordList.getFrequency(s);
             if (map.get(freq) > targetCoverage)
             {
               System.err.println("remove: " + s); 
               riskyWords.add(s);
             }
           }
        }
     }
   } 

   List<Integer> toList(Set<Integer> s)
   {
     ArrayList<Integer> a = new ArrayList<Integer>();
     a.addAll(s);
     return a;
   }
   public void matchUsingDistance(String listFile)
   {
     wordList = new WordList();
     wordList.readList(listFile);
     strings = wordList.keySet();
     matcher.createIndex(strings);
     boolean allowExactMatch = true;
     int k=1;
      for (String s: strings)
      {
        if (s.length() < 3) continue;
        int f = wordList.getFrequency(s);
        //if (f < 5) continue;

        List<String> matches = new ArrayList<String>();

        boolean exactMatch = matcher.matchTerm(matches, s.toLowerCase(), true);
        List<String> restricted = new ArrayList<String>();
        
        for (String m: matches)
        {
          int f1 = wordList.getFrequency(m);
          if (30 * f1 <  f)
          {
            System.out.println(k++ + "\t" + m + "\t" + s); 
            //restricted.add(m);
          }
        }

        if (restricted.size() > 0 && !(!allowExactMatch && exactMatch))
        {
          System.out.println(k++ + "\t" + matcher.join(restricted, " ") + "\t" + s); //  + " (" + f + ")");
        }
      }
   } 

   class CB // extends MemorylessMatcher.Callback
   {
     public Map<String,Double> scores = new HashMap<String,Double>();
     public void handleMatch(String targetWord, String matchedWord, String matchInfo, int cost, double p)
     {
        // if (!targetWord.equals(matchedWord)) System.err.printf("Hoela: %s\t%s\t%s\n", targetWord,matchedWord, matchInfo);
        scores.put(matchedWord,p);
     }
   };

   public void matchWithRules(String ruleFile, String listFile)
   {
      wordList = new WordList();
      wordList.readList(listFile);
      strings = wordList.keySet();
      CB myCallback = new CB();
/*
      MemorylessMatcher m = new MemorylessMatcher(ruleFile);
      m.setMaxSuggestions(5);
      m.setMaxPenaltyIncrement(10000);
      m.setCallback(myCallback);
      Trie lexicon = new Trie();

      for (String s: strings)
         lexicon.root.putWord(s);

      for (String s: strings)
      {
        int f = wordList.getFrequency(s);
        myCallback.scores = new HashMap<String,Double>();  
        //System.err.println(s + ":" + f);
        m.matchWordToLexicon(lexicon,s);
        int k=1;
        for (String s1: myCallback.scores.keySet())
        {
          double score = myCallback.scores.get(s1);
          int f1 = wordList.getFrequency(s1); 
          if (f1 >  50 * f && score > 1e-3) // should also depend on length??
	  { 
             boolean provenError = false;
             if (report != null)
               provenError = report.hasError(s);
             riskyWords.add(s); 
             System.err.println(k++ + "\t" + s1 + ":" + f1 + "\t" + s + ":" + f + ":" + score + "\t" + provenError); 
          }
        }
      }
      */
      //m.lextrie = lexicon;
   }


   public void outputSafeWords()
   {
      List<WordList.TypeFrequency> sensitive = wordList.sensitiveKeyList();
      for (WordList.TypeFrequency tf: sensitive)
      {
         if (!riskyWords.contains(tf.type.toLowerCase()))
		System.out.println(tf.type + "\t" + tf.frequency);
      }
   }

   public static void main(String[] args)
   {
     // new LexiconCleaner().matchUsingDistance(args[0]);
     LexiconCleaner cleaner = new LexiconCleaner();
     if (args.length > 2)
     {
        cleaner.report = new EvaluationReport(args[2]);
     }
     cleaner.matchWithRules(args[0],args[1]);	
     cleaner.outputSafeWords();
   }
}
