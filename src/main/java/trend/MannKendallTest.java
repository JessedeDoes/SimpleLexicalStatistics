package trend;

/**
 * Defined as
 * <p>
 * ((Number of concordant pairs) - (Number of discordant pairs)) / (1/2 * n * (n-1))
 * </p>

 *Any pair of observations (<i>x</i><sub><i>i</i></sub>,&#160;<i>y</i><sub><i>i</i></sub>) and (<i>x</i><sub><i>j</i></sub>,&#160;<i>y</i><sub><i>j</i></sub>) are said to be <i>concordant</i> if the ranks for both elements agree: that is, if both <i>x</i><sub><i>i</i></sub>&#160;&gt;&#160;<i>x</i><sub><i>j</i></sub> and <i>y</i><sub><i>i</i></sub>&#160;&gt;&#160;<i>y</i><sub><i>j</i></sub> or if both <i>x</i><sub><i>i</i></sub>&#160;&lt;&#160;<i>x</i><sub><i>j</i></sub> and <i>y</i><sub><i>i</i></sub>&#160;&lt;&#160;<i>y</i><sub><i>j</i></sub>. They are said to be <i>discordant</i>, if <i>x</i><sub><i>i</i></sub>&#160;&gt;&#160;<i>x</i><sub><i>j</i></sub> and <i>y</i><sub><i>i</i></sub>&#160;&lt;&#160;<i>y</i><sub><i>j</i></sub> or if <i>x</i><sub><i>i</i></sub>&#160;&lt;&#160;<i>x</i><sub><i>j</i></sub> and <i>y</i><sub><i>i</i></sub>&#160;&gt;&#160;<i>y</i><sub><i>j</i></sub>. If <i>x</i><sub><i>i</i></sub>&#160;=&#160;<i>x</i><sub><i>j</i></sub> or <i>y</i><sub><i>i</i></sub>&#160;=&#160;<i>y</i><sub><i>j</i></sub>, the pair is neither concordant nor discordant.</p>

 * <p>
 * @author does
 *
 */
public class MannKendallTest
{
	
	public double test(double[] X, double[] Y, int n)
	{
		double r=0;
		for (int i=1; i < n; i++)
		{
			for (int j=0; j < i; j++)
			{
				double d = Math.signum(X[i] - X[j]) * Math.signum(Y[i] - Y[j]);
				r +=  d;
			}
		}
		
		double N = n;
		
	    return r / (0.5 * N * (N-1));
	}
}
