

package trend;

import java.io.*;

import util.BasicStatistics;

/**
 * 

 *<p>As defined by <a href="#CITEREFTheil1950">Theil (1950</a>), the Theil–Sen estimator of a set of two-dimensional points <span class="texhtml">(<i>x<sub>i</sub></i>,<i>y<sub>i</sub></i>)</span> is the median <span class="texhtml mvar" style="font-style:italic;">m</span> of the slopes <span class="texhtml">(<i>y<sub>j</sub></i> − <i>y<sub>i</sub></i>)/(<i>x<sub>j</sub></i> − <i>x<sub>i</sub></i>)</span> determined by all pairs of sample points. <a href="#CITEREFSen1968">Sen (1968</a>) extended this definition to handle the case in which two data points have the same <span class="texhtml mvar" style="font-style:italic;">x</span>-coordinate. In Sen's definition, one takes the median of the slopes defined only from pairs of points having distinct <span class="texhtml mvar" style="font-style:italic;">x</span>-coordinates.</p>
<p>Once the slope <span class="texhtml mvar" style="font-style:italic;">m</span> has been determined, one may determine a line through the sample points by setting the <span class="texhtml mvar" style="font-style:italic;">y</span>-intercept <span class="texhtml mvar" style="font-style:italic;">b</span> to be the median of the values <span class="texhtml"><i>y<sub>i</sub></i> − <i>mx<sub>i</sub></i></span>.<sup id="cite_ref-rl03_8-0" class="reference"><a href="#cite_note-rl03-8"><span>[</span>8<span>]</span></a></sup> As Sen observed, this estimator is the value that makes the <a href="/wiki/Kendall_tau_rank_correlation_coefficient" title="Kendall tau rank correlation coefficient">Kendall tau rank correlation coefficient</a> comparing the sample data values <span class="texhtml mvar" style="font-style:italic;">y<sub>i</sub></span> with their estimated values <span class="texhtml"><i>mx<sub>i</sub></i> + <i>b</i></span> become approximately zero.</p>

 *Sen, Pranab Kumar (1968), "Estimates of the regression coefficient based on Kendall's tau", Journal of the American Statistical Association 63: 1379–1389, JSTOR 2285891, MR 0258201 
 *http://www.stat.ncsu.edu/information/library/mimeo.archive/ISMS_1987_1690R.pdf
 *
 **************************************************************/
public class TheilSenRegression
{
	private int n=0;
	private static double[] s;
	public double slope, intercept;

	public TheilSenRegression(){} 

	public void doRegression(int N, double[] X, double[] Y)
	{
		int nPairs = (int) (N*(N-1)/2);
		double[] slopes = new double[nPairs+2];
		double[] intercepts = new double[N];
		double xi=0.0, yi=0.0;
		int cnt=0;
	
		for(int i=0; i<(N-1); i++)
		{
			xi=X[i];
			yi=Y[i];
			for (int j=i+1;j<N;j++)
			{
				slopes[cnt] = (double) ((Y[j]-yi)/(X[j]-xi)); // Oops: problem if equal
				cnt++;
			}
		}

		this.slope = BasicStatistics.median(slopes,nPairs); 
		for (int k=0; k<N; k++)
		{
			intercepts[k]=Y[k]-(slope*X[k]);
		}
		this.intercept = BasicStatistics.median(intercepts, N); 
	}

	public double getSlope()
	{
		return slope;
	}
	
	public double getIntercept()
	{
		return intercept;
	}
}
