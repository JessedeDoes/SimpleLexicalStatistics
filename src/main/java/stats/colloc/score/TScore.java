package stats.colloc.score;

import stats.colloc.CollocationScore;

public class TScore implements CollocationScore
{
	public double score(long N, int f, int f1, int f2)
	{
		return t_score(f,f1,f2,(int) N);
	}

	double t_score(int freq, int freq1, int freq2, int totalBigrams)
	{
		double p_bi = freq / (double) totalBigrams;
		double mu = (freq1 / (double) totalBigrams) * (freq2 / (double) totalBigrams); 
		double s_square = mu * (1 - mu);
		double score = (p_bi - mu) / StrictMath.sqrt(( s_square / (double) totalBigrams)); 
		// fprintf(stderr,"f=%d f1=%d, f2=%d score=%f\n",freq,freq1,freq2,score); 
		return score;
	}
}

