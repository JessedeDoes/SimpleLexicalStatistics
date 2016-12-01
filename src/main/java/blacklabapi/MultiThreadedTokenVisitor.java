package blacklabapi;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.inl.blacklab.search.Searcher;
import util.*;

public class MultiThreadedTokenVisitor extends TokenVisitor
{
	protected ExecutorService pool = Executors.newFixedThreadPool(nThreads);

	public MultiThreadedTokenVisitor()
	{

	}
	
	public MultiThreadedTokenVisitor(Searcher searcher, List<String> propList)
	{
		super(searcher,propList);
	}

	public class DocumentTask implements Runnable, TokenHandler, DocumentContentHandler
	{
		protected int documentId;
		public Thread thread = null;
		protected Counter<String> partialCounter;
		
		public DocumentTask(int did)
		{
			this.documentId = did;
		}
		
		@Override
		public void run() 
		{
			this.thread = Thread.currentThread();
			synchronized(MultiThreadedTokenVisitor.this)
			{
				String tid = "" + this.thread.getId();
			
				partialCounter = counterMap.get(tid);
				if (partialCounter == null)
				{
					System.err.println("created map for thread: " + tid);
					partialCounter = new Counter<String>();
					counterMap.put(tid, partialCounter);
				}
			}
			loopOverTokensInDocument(this.documentId, this, this);
		}

		@Override
		public void handleToken(List<String> propertyNames, String[] propertyValues)
		{
			String z = filterToken(propertyNames,  propertyValues);
			if (z != null)
			{
				partialCounter.increment(z);
			}
		}

		@Override
		public boolean handleDocumentContent(String docId, List<String> propertyValues,
				List<List<String>> propertyValuesPerProperty)
		{
			return false;
		}
	}
	
	public String filterToken(List<String> propertyNames, String[] propertyValues)
	{
		String z="";
		for (int i=0; i < propertyNames.size(); i++)
		{
		   z += propertyValues[i] + ((i < propertyNames.size()-1)?",":"");
		}
		return z;
	}
	
	public void shutdown()
	{
		this.pool.shutdown();
		while (!this.pool.isTerminated())
		{
			try 
			{
				Thread.sleep(100);
			} catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		System.err.println("docs found: " + nDocs + " tokens found: " + nTokens);
	}
	
	public void loopOverAllTokensinDocumentsSatisfyingMetadataFilter(String filterQueryString)
	{
		super.loopOverAllTokensinDocumentsSatisfyingMetadataFilter(filterQueryString);
		this.shutdown();
	}
	
	protected void handleDocument(int did)
	{
			DocumentTask t = new DocumentTask(did);
			pool.execute(t);
	}
	
	protected void handleDocument(int did, TokenHandler tokenHandler, 
			DocumentContentHandler documentHandler) // oops ugly, parameters NOT used...
	{
			DocumentTask t = new DocumentTask(did);
			pool.execute(t);
	}
}

