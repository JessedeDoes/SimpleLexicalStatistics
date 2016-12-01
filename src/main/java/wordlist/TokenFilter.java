package wordlist;

import java.util.List;

public interface TokenFilter
{
	public String filterToken(List<String> propertyNames, String[] propertyValues);
	public String[] needsTokenProperties();
}
