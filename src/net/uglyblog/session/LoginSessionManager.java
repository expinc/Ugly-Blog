package net.uglyblog.session;

import java.util.*;


public final class LoginSessionManager
{
	private static Map<Integer, LoginSession> sessions;
	private static Map<String, Integer> accounts2Cookies;
	private static int maxSessions = 32;
	private static long sessionInterval = 5 * 60 * 1000;	// 5 minutes
	
	
	static
	{
		sessions = new TreeMap<Integer, LoginSession>();
		accounts2Cookies = new TreeMap<String, Integer>();
	}
	
	
	public static synchronized int create(String account)
	{
		int cookieId = 0;
		int countSessions = clean();
		
		Integer currentCookie = accounts2Cookies.get(account);
		if (null != currentCookie)
		{
			sessions.remove(currentCookie);
			accounts2Cookies.remove(account);
		}
		else if (maxSessions != countSessions)
		{
			cookieId = LoginSession.getNextCookieId();
			sessions.put(cookieId, new LoginSession(account, cookieId));
			accounts2Cookies.put(account, cookieId);
		}
		return cookieId;
	}
	
	
	public static synchronized String checkSession(int cookieId)
	{
		String account = null;
		LoginSession session = sessions.get(cookieId);
		if (null != session)
		{
			account = session.getAccount();
			session.refreshLastRequestTime();
		}
		
		return account;
	}
	
	
	public static String getSessionAccount(int cookieId)
	{
		String account = null;
		LoginSession session = sessions.get(cookieId);
		if (null != session)
			account = session.getAccount();		
		return account;
	}
	
	
	public static void terminateSession(int cookieId)
	{
		LoginSession session = sessions.get(cookieId);
		if (null != session)
		{
			String account = session.getAccount();
			accounts2Cookies.remove(account);
			sessions.remove(cookieId);
		}
	}
	
	
	private static int clean()
	{
		List<Integer> sessionCookiesToClean = new LinkedList<Integer>();
		for (Map.Entry<Integer, LoginSession> session : sessions.entrySet())
		{
			long lastRequestTime = session.getValue().getLastRequestTime();
			if (System.currentTimeMillis() - lastRequestTime > sessionInterval)
				sessionCookiesToClean.add(session.getKey());
		}
		
		for (Integer sessionCookieToClean : sessionCookiesToClean)
		{
			String account = sessions.get(sessionCookieToClean).getAccount();
			accounts2Cookies.remove(account);
			sessions.remove(sessionCookieToClean);
		}
		
		return sessions.size();
	}
}
