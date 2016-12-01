package stats.colloc.score;

import stats.colloc.CollocationScore;

public class RelativeFrequency implements CollocationScore
{
	public double score(long N, int f, int f1, int f2)
	{
		return ((double) f) / N;
	}
}
