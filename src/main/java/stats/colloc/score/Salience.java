package stats.colloc.score;

import stats.colloc.CollocationScore;

public class Salience implements CollocationScore
{
	public double score(long N, int f, int f1, int f2)
	{
		return salience(f,f1,f2,(int) N);
	}

	double salience(int freq, int freq1, int freq2, int totalBigrams)
	{
		double temp = ( freq / (double) freq1 ) / (double) freq2;
		temp *= totalBigrams;
		return ( StrictMath.log(freq) * StrictMath.log(temp) / StrictMath.log(2.0) );
	}
}


