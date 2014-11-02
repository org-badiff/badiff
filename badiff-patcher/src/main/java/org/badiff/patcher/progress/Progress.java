package org.badiff.patcher.progress;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.EventListenerList;

public class Progress {
	private class Context {
		public int tasks;
		public int completed;
		public String note;
	}
	
	private List<Context> contexts;
	private EventListenerList listeners;
	
	public Progress() {
		contexts = new ArrayList<Context>();
		listeners = new EventListenerList();
		
		Context ctx = new Context();
		ctx.tasks = 1;
		contexts.add(ctx);
	}
	
	public void addProgressListener(ProgressListener l) {
		listeners.add(ProgressListener.class, l);
	}
	
	public void removeProgressListener(ProgressListener l) {
		listeners.remove(ProgressListener.class, l);
	}
	
	protected void fireProgressUpdated() {
		Object[] ll = listeners.getListenerList();
		ProgressEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == ProgressListener.class) {
				if(e == null)
					e = new ProgressEvent(this);
				((ProgressListener) ll[i+1]).progressUpdated(e);
			}
		}
	}
	
	public void push(int tasks) {
		Context ctx = new Context();
		ctx.tasks = tasks;
		contexts.add(ctx);
		fireProgressUpdated();
	}
	
	public void note(String note) {
		contexts.get(contexts.size() - 1).note = note;
		fireProgressUpdated();
	}
	
	public void add(int tasks) {
		contexts.get(contexts.size() - 1).tasks += tasks;
		fireProgressUpdated();
	}
	
	public void complete(int tasks) {
		contexts.get(contexts.size() - 1).completed += tasks;
		fireProgressUpdated();
	}
	
	public void pop(boolean complete) {
		contexts.remove(contexts.size() - 1);
		if(complete)
			contexts.get(contexts.size() - 1).completed++;
		fireProgressUpdated();
	}
	
	public int getDepth() {
		return contexts.size() - 1;
	}
	
	public double getProgress() {
		double progress = 0;
		for(int i = contexts.size() - 1; i >= 0; i--) {
			Context ctx = contexts.get(i);
			progress /= ctx.tasks;
			progress += (ctx.completed / (double) ctx.tasks);
		}
		return progress;
	}
	
	public List<String> getNotes() {
		List<String> notes = new ArrayList<String>();
		for(Context ctx : contexts)
			if(ctx.note != null)
				notes.add(ctx.note);
		return notes;
	}
}
