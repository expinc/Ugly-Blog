package net.uglyblog.meta;
import java.util.*;


public abstract class FileExplorer
{
	private static String coreWorkPath = "C:/Users/i327636/eclipse-workspace/UglyBlog/";
	private static String webPath = "WebContent/";
	private static Map<String, String> filePaths;	
	
	
	static
	{
		filePaths = new TreeMap<String, String>();
		filePaths.put("index page", webPath + "index.html");
		filePaths.put("dashboard page", webPath + "dashboard.html");
		filePaths.put("system crash page", webPath + "system-crashed.html");
		filePaths.put("register page", webPath + "register.html");
		filePaths.put("no session page", webPath + "no-session.html");
		filePaths.put("system busy page", webPath + "system-busy.html");
		filePaths.put("board page", webPath + "board.html");
		filePaths.put("profile page", webPath + "profile.html");
		filePaths.put("post page", webPath + "post.html");
	}
	
	
	public static String getPath(String index)
	{
		return coreWorkPath + filePaths.get(index);
	}	
}
