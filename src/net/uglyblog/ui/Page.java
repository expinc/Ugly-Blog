package net.uglyblog.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import net.uglyblog.meta.FileExplorer;


public abstract class Page
{
	protected List<String> content = null;
	protected Set<String> stubs = null;
	
	
	Page()
	{
		content = new LinkedList<String>();
		stubs = new TreeSet<String>();
		initStubs();
	}
	
	
	protected void initStubs()
	{
	}
	
	
	public boolean replaceStub(String stub, String content)
	{
		String stubStr;
		if (stubs.contains(stub))
			stubStr = "<!-- " + stub + " -->";
		else
			return false;
		
		boolean foundStub = false;
		for (int i = 0; i < this.content.size(); ++i)
		{
			if (this.content.get(i).contains(stubStr))
			{
//				if (this.content.get(i + 1).contains("<!---->"))
//					this.content.add(i + 1, content);
//				else
//					this.content.set(i + 1, content);
				if (this.content.get(i + 1).contains("<!---->"))
					this.content.add(i, content);
				foundStub = true;
				break;
			}
		}
		return foundStub;
	}
	
	
	public String getContent()
	{
		StringBuilder contentBuilder = new StringBuilder();
		for (String line : content)
			contentBuilder.append(line);
		return contentBuilder.toString();
	}
	
	
	protected boolean loadContent(String pageName)
	{        
		String path = FileExplorer.getPath(pageName);
		boolean result = true;
		try
		{
			content = Files.readAllLines(Paths.get(path));
		}
		catch (IOException e)
		{
			content = new LinkedList<String>();
			result = false;
		}
		return result;
	}
}
