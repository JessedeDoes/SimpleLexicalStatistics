package test;



import util.Counter;
import wordlist.SubcorpusComparison;
import wordlist.SubcorpusComparison.PairOfFrequencies;

public class TestFrequencyListComparison
{
	public static void main(String[] args)
	{
		SubcorpusComparison flc = new SubcorpusComparison();
		Counter<String> l0 = Counter.readFromFile(args[0]);
		Counter<String> l1 = Counter.readFromFile(args[1]);
		int smoothingParameter = Integer.parseInt(args[2]);
		int minimumFrequency = Integer.parseInt(args[3]);
		for (PairOfFrequencies pof: flc.compare(l0, l1, smoothingParameter, minimumFrequency))
		{
			System.out.println(pof);
		}
	}
}
