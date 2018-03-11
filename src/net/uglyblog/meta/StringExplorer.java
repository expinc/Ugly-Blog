package net.uglyblog.meta;

import java.util.Map;
import java.util.TreeMap;

public abstract class StringExplorer
{
	private static Map<String, String> stringList;
	
	
	static
	{
		initStringList();
	}
	
	
	public static void initStringList()
	{
		stringList = new TreeMap<String, String>();
		stringList.put("login fail message stub", "<!-- login fail message -->");
		stringList.put("login fail message content", "<p style=\"color:red\">Wrong account or password!</p>");
		stringList.put("register fail message stub", "<!-- register fail message -->");
		stringList.put("register fail message content",
				"<p style=\"color:red\">"
				+ "Register failed!\n"
				+ "Maybe the account already exists or the format is invalid.\n"
				+ "Account name format: 3 ~ 32 characters of letters, digits or underscore.\n"
				+ "Password format: 4 ~ 32 characters of letters, digits or underscore.\n"
				+ "</p>");
	}
	
	
	public static String getString(String index)
	{
		return stringList.get(index);
	}	
}
