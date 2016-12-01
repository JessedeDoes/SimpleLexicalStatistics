/*************************************************************************
 *THIS CLASS PROVIDES THE STATS ANALYSIS FOR DIFFERENT REGRESSION ANALYSIS
 *IT INCLUDES:
 *	DOUBLE SUM(), DOUBLE MEAN(), DOUBLE VAR(), DOUBLE COVAR()
 *http://www.statsoftinc.com/textbook/stanman.html#basic
 *
 **************************************************************************/

package util;
import java.util.*;

public class BasicStatistics
{
	public BasicStatistics(){}

	public static double sum( int first,int last,double vector[])
	{
		double r=0;
		for (int i=first; i <= last; i++)
		{
			r+= vector[i];
		}
		return r;
	}

	public static double median(double[] d, int n)
	{
         Arrays.sort(d,0,n);
		int nhalf = (int) (n/2);
		if (n % 2 != 0)
		{
			return d[nhalf];
		}
		else
		{
			return (double) (d[nhalf-1]+d[nhalf])/2;
		}
	}
	
	public static double mean(double[] dArray, int n)
	{
		double s= sum(0,n-1,dArray);
		double d=(double)s/n;
		return(d);
	}

	public static double variance(double[] dArray, double mean, int n)
	{
		double var=0.0;
		double ss=0.0;
		double[] dev = new double[n];
		for(int i=0;i<n;i++)
		{
			dev[i]=(double) ((dArray[i]-mean)*(dArray[i]-mean));
		}
		ss=sum(0,n-1,dev);
		var=(double)(ss/(n-1));
		return var ;
	}


	public static double coVariance(double[] xArray, double[] yArray, double xMean, double yMean, int n)
	{
		double covar=0.0;
		double ss=0.0;
		double[] dev = new double[n];
		for(int i=0;i<n;i++)
		{
			dev[i]=(double) ((xArray[i]-xMean)*(yArray[i]-yMean));
		}
		ss=sum(0,n-1,dev);
		covar=(double)(ss/(n));
		return covar ;
	}
}
