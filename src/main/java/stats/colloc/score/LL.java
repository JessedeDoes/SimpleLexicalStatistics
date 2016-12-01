package stats.colloc.score;

import stats.colloc.CollocationScore;

public class LL implements CollocationScore
{
	public double score(long N, int f, int f1, int f2)
	{
		return log_likelyhood(f,f1,f2,(int) N);
	}
	
	double log_likelyhood(int freq, int freq1, int freq2, int totalBigrams)
	{
		double n11 = freq;       // pair freq
		double n1p = freq1;      // single freq of first word
		double np1 = freq2;      // single freq of second word
		double n12 = n1p - n11;
		double n21 = np1 - n11;
		double np2 = totalBigrams - 1 - np1;
		double n2p = totalBigrams - 1 - n1p;
		double n22 = np2 - n12;
		double npp = totalBigrams - 1;

		double m11 = n1p * np1 / npp;
		double m12 = n1p * np2 / npp;
		double m21 = n2p * np1 / npp;
		double m22 = n2p * np2 / npp;

		double logLikelihood = 0;

		if ( n11  != 0) { logLikelihood += n11 * StrictMath.log ( n11 / m11 ); }
		if ( n12  != 0) { logLikelihood += n12 * StrictMath.log ( n12 / m12 ); }
		if ( n21  != 0) { logLikelihood += n21 * StrictMath.log ( n21 / m21 ); }
		if ( n22  != 0 ) { logLikelihood += n22 * StrictMath.log ( n22 / m22 ); }

		return ( 2 * logLikelihood );
	}
}
