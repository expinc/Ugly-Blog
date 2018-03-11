package net.uglyblog.bo;


public abstract class BusinessObjectManager
{
	private static boolean doLog = true;
	
	
	public static boolean isLogEnabled()
	{
		return doLog;
	}
	
	
	public static void enableLog(boolean value)
	{
		doLog = value;
	}
}
