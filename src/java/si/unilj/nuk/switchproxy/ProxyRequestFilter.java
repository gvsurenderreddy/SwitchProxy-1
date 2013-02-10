/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.unilj.nuk.switchproxy;

import java.util.Queue;
import java.util.concurrent.SynchronousQueue;
import java.util.Vector;

/**
 *
 * @author mitja
 */
public class ProxyRequestFilter {
	
	private Vector<UrlMatchRule> ruleSet = new Vector<UrlMatchRule>();
	private Queue<RenderTask> taskQueue = new SynchronousQueue<RenderTask>();

	public Vector<UrlMatchRule> getRuleSet() {
		return ruleSet;
	}

	public Queue<RenderTask> getTaskQueue() {
		return taskQueue;
	}
	
}
