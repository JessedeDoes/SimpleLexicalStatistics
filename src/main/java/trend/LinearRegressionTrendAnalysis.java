package trend;

import java.util.List;

import org.apache.commons.math3.stat.regression.RegressionResults;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import blacklabapi.FrequencyInformation;

public class LinearRegressionTrendAnalysis extends TrendAnalysis
{
	SimpleRegression regression;
	
	public LinearRegressionTrendAnalysis(List<FrequencyInformation> frequencies)
	{
		super(frequencies);
		// TODO Auto-generated constructor stub
	}

	protected void regression(double[] X, double[] Y)
	{
		 regression = new SimpleRegression();
		 
		for (int i=0; i  < X.length; i++)
		{
			regression.addData(X[i],Y[i]);
		}

		RegressionResults result = regression.regress();

		this.intercept =  regression.getIntercept();
		this.slope = regression.getSlope();
		this.test = regression.getSignificance();
	}
}
