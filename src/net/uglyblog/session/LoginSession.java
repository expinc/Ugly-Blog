package net.uglyblog.session;


public class LoginSession
{
	private static int nextCookieId = 1;
	
	
	static int getNextCookieId()
	{
		return nextCookieId++;
	}
	
	
	private String account;
	private int cookieId;
	private long lastRequestTime;
	
	
	LoginSession(String account, int cookieId)
	{
		this.account = account;
		this.cookieId = cookieId;
		refreshLastRequestTime();
	}
	
	
	String getAccount()
	{
		return account;
	}
	
	
	int getCookieId()
	{
		return cookieId;
	}
	
	
	void refreshLastRequestTime()
	{
		lastRequestTime = System.currentTimeMillis();
	}
	
	
	public long getLastRequestTime()
	{
		return lastRequestTime;
	}
}
