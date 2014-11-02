package org.badiff.patcher.progress;

import java.util.EventObject;
import java.util.List;

public class ProgressEvent extends EventObject {
	private static final long serialVersionUID = 0;

	private double progress;
	private List<String> notes;
	private int depth;
	
	public ProgressEvent(Progress source) {
		super(source);
		progress = source.getProgress();
		notes = source.getNotes();
		depth = source.getDepth();
	}

	@Override
	public Progress getSource() {
		return (Progress) super.getSource();
	}
	
	public double getProgress() {
		return progress;
	}
	
	public List<String> getNotes() {
		return notes;
	}
	
	public int getDepth() {
		return depth;
	}
}
