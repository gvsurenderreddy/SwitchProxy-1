/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.unilj.nuk.switchproxy;

import java.util.HashMap;
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
	
	/**
	 * Task history.
	 */
	private Vector<RenderTask> commitedTasks = new Vector<RenderTask>();
	/**
	 * How many elements history should hold.
	 */
	private int commitedTasksMaxSize = 1000;
	
	public void setCommitedTasksMaxSize(int size) {
		if(size < 1) {
			throw new IllegalArgumentException("Max size must be between 1 and Integer.MAX_VALUE");
		}
		commitedTasksMaxSize = size;
	}
	public int getCommitedTasksMaxSize() {
		return commitedTasksMaxSize;
	}

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
		log.debug(String.format("Matching url with rules", url));
		
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
//		log.debug("Client asks for new task.");
		
		RenderTask task = taskQueue.poll();
		if(task != null) {
			log.info("New task retrived from list: " + task);
			
			activeTasks.put(task.getId(), task);
		}
		
		return task;
	}
	
	/**
	 * TODO headers
	 * @param id
	 * @param content 
	 */
	public void passContent(String id, String content, HashMap<String, String> headers) {
		try {
			log.info("Commiting task: " + id);
			
			RenderTask task = activeTasks.get(id);
			task.setHeaders(headers);
			task.setContent(content);
			
			// enforcing history max(last n elements) size
			if(commitedTasks.size() >= commitedTasksMaxSize) {
				commitedTasks.remove(0);
			}
			
			commitedTasks.add(task);
		}
		catch(Exception e) {
			log.error("Error while commiting content: ", e);
		}
		
		// resume all waiting threads
//		notify();
	}
	
}
