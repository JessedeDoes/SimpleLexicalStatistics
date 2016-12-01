package fuzzy;
public class ShortWordPruner
{
  public static void main(String[] args)
   {
     // new LexiconCleaner().matchUsingDistance(args[0]);
     LexiconCleaner cleaner = new LexiconCleaner();
     cleaner.pruneShortWords(args[0]);
     cleaner.outputSafeWords();
   }
}
