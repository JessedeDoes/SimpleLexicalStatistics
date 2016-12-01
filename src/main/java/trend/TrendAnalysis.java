package trend;

import org.json.simple.JSONObject;

import blacklabapi.FrequencyInformation;

import java.util.*;

public class TrendAnalysis implements json.JSONSerializable
{
	List<FrequencyInformation> frequencies;
	
	public double test=0;
	public double slope=0;
	public double intercept=0;

	public TrendAnalysis(List<FrequencyInformation> frequencies)
	{
		this.frequencies = frequencies;
		double[] X = new double[frequencies.size()];
		double[] Y = new double[frequencies.size()];
		for (int i=0; i < frequencies.size(); i++)
		{
			FrequencyInformation f = frequencies.get(i);
			X[i] = f.time; // of afhankelijk van scale maken
			Y[i] = f.normalizedFrequency();
		}
		regression(X, Y);
	}

	protected void regression(double[] X, double[] Y)
	{
		this.test = new MannKendallTest().test(X, Y, X.length);
		TheilSenRegression tsr = new TheilSenRegression();
		tsr.doRegression(X.length, X, Y);
		this.slope = tsr.getSlope();
		this.intercept = tsr.getIntercept();
	}
	
	public JSONObject toJSON()
	{
		JSONObject r = new JSONObject();
		r.put("slope",  slope);
		r.put("test", test);
		r.put("intercept", intercept);
		r.put("development",  json.JSONObjects.toJSONArray(this.frequencies));
		return r;
	}
}
