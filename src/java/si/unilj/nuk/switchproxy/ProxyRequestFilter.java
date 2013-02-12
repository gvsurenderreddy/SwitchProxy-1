/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.unilj.nuk.switchproxy;

import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;

/**
 *
 * @author mitja
 */
public class ProxyRequestFilter {
	private final static Logger log = Logger.getLogger(ProxyRequestFilter.class);
	
	private Vector<UrlMatchRule> ruleSet = new Vector<UrlMatchRule>();
	private Queue<RenderTask> taskQueue = new ConcurrentLinkedQueue<RenderTask>();
	private Hashtable<String, RenderTask> activeTasks = new Hashtable<String, RenderTask>();
	
	private Vector<RenderTask> commitedTasks = new Vector<RenderTask>();

	public Vector<UrlMatchRule> getRuleSet() {
		return ruleSet;
	}

	public Vector<RenderTask> getCommitedTasks() {
		return commitedTasks;
	}
	
	

	public Queue<RenderTask> getTaskQueue() {
		return taskQueue;
	}

	public Hashtable<String, RenderTask> getActiveTasks() {
		return activeTasks;
	}
	
	// -- Proxy interface -------------------------------------------------------
	
	/**
	 * Tries to match the url with ruleset patterns.
	 * If matched then url is store to task queue and thread suspended until,
	 * remote renderer doesn't fetch task-url, renders it and returns it renderer
	 * DOM back.
	 * 
	 * @param url
	 * @return 
	 */
	public RenderTask match(String url) {
		for(UrlMatchRule r : ruleSet) {
			if(r.isMatched(url)) {
				return process(url, r);
			}
		}
		
		return null;
	}
	
	/**
	 * Do the actual work.
	 * 
	 * TODO, enclose to a separate type
	 * @param url 
	 */
	public RenderTask process(String url, UrlMatchRule rule) {
		RenderTask task = new RenderTask(url, rule);
		taskQueue.add(task);
		
		log.info(String.format("Url(%s) tasked, waiting for rendering to process it", url));
		
		while(true) {
			try {
//				wait();
				// temporary alternative
				Thread.sleep(1000);
			}
			catch(InterruptedException e) {
				log.info(String.format("Thread awoken."));
			}
			
			if(task.isComplete()) {
				log.info(String.format("Task completed, passing content to caller."));
				
				activeTasks.remove(task.getId());
				return task;
			}
		}
	}
	
	// -- Renderer interface ----------------------------------------------------
	
	public RenderTask nextTask() {
		RenderTask task = taskQueue.poll();
		if(task != null) {
			activeTasks.put(task.getId(), task);
		}
		
		return task;
	}
	
	/**
	 * TODO headers
	 * @param id
	 * @param content 
	 */
	public void passContent(String id, String content) {
		try {
			RenderTask task = activeTasks.get(id);
			task.setContent(content);
			
			commitedTasks.add(task);
		}
		catch(Exception e) {
			// Invalid id???
		}
		
		// resume all waiting threads
		
//		notify();
	}
	
}
