package blacklabapi;

import java.util.List;

public interface TokenHandler
{
	public void handleToken(List<String> propertyNames, String[] propertyValues); 
}
