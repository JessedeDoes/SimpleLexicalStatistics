package servlet;

import java.io.File;
import java.util.Map;

import util.Counter;

class BlackAndWhite
{
	public boolean hasWhiteList = false;
	public boolean hasBlackList = false;
	Counter<String> whiteList = new Counter<String>();
	Counter<String> blackList = new Counter<String>();
	
	public boolean white(String w)
	{
		return !hasWhiteList || whiteList.containsKey(w);
	}
	
	public boolean black(String w)
	{
		return hasBlackList && blackList.containsKey(w);
	}

	public static BlackAndWhite getUploadedLists(Map<String,File> fileUploadMap)
	{
		BlackAndWhite BnW = new BlackAndWhite();
		File blackListFile = null;
		File whiteListFile = null;
	
		try
		{
			if (fileUploadMap.get("blacklist") != null)
			{
				blackListFile = fileUploadMap.get("blacklist");
				if (blackListFile != null)
				{
					BnW.blackList = Counter.readFromFile(blackListFile.getCanonicalPath());
					System.err.println("Blacklist words:  " + BnW.blackList.size());
					BnW.hasBlackList = BnW.blackList.size() > 0;
					if (blackListFile.exists())
						blackListFile.delete();
				}
			}
	
			if (fileUploadMap.get("whitelist") != null)
			{
				whiteListFile = fileUploadMap.get("whitelist");
				if (whiteListFile != null)
				{
					BnW.whiteList = Counter.readFromFile(whiteListFile.getCanonicalPath());
					System.err.println("Whitelist words:  " + BnW.whiteList.size());
					BnW.hasWhiteList = BnW.whiteList.size() > 0; 
					if (whiteListFile.exists())
						whiteListFile.delete();
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return BnW;
	}
}