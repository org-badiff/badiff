package org.badiff.patcher.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.badiff.patcher.PathDiff;
import org.badiff.patcher.SerializedDigest;
import org.badiff.patcher.client.PathAction.Direction;

public class PathDiffChain {
	protected Map<SerializedDigest, List<PathDiff>> history;
	
	public PathDiffChain() {
		history = new HashMap<SerializedDigest, List<PathDiff>>();
	}
	
	public void clear() {
		history.clear();
	}
	
	public void add(PathDiff link) {
		List<PathDiff> pathHistory = historyFor(link.getPathId());
		if(!pathHistory.contains(link)) {
			pathHistory.add(link);
			Collections.sort(pathHistory, PathDiff.TS_ORDER);
		}
	}
	
	public Set<SerializedDigest> keys() {
		return history.keySet();
	}
	
	public List<PathDiff> historyFor(SerializedDigest pathId) {
		List<PathDiff> pathHistory = history.get(pathId);
		if(pathHistory == null)
			history.put(pathId, pathHistory = new ArrayList<PathDiff>());
		return pathHistory;
	}
	
	protected int indexOf(SerializedDigest pathId, SerializedDigest contentId) {
		List<PathDiff> pathHistory = historyFor(pathId);
		if(pathHistory.size() == 0)
			return -1;
		if(pathHistory.get(0).getFrom().equals(contentId))
			return 0;
		for(int i = 0; i < pathHistory.size(); i++)
			if(pathHistory.get(i).getTo().equals(contentId))
				return i+1;
		return -1;
	}
	
	public PathAction actionFor(SerializedDigest pathId, SerializedDigest fromId, SerializedDigest toId) {
		int fromIndex = indexOf(pathId, fromId);
		int toIndex = indexOf(pathId, toId);
		if(fromIndex < 0)
			throw new IllegalArgumentException(pathId + " does not have content id " + fromId);
		if(toIndex < 0)
			throw new IllegalArgumentException(pathId + " does not have content id " + toId);
		
		if(fromIndex == toIndex)
			return new PathAction(pathId, Direction.PAUSE);
		
		List<PathDiff> history = historyFor(pathId);

		if(fromIndex < toIndex) {
			PathAction pa = new PathAction(pathId, Direction.FAST_FORWARD);
			PathDiff head = history.get(fromIndex);
			pa.add(head);
			SerializedDigest contentId = head.getTo();
			for(int i = fromIndex + 1; i < toIndex; i++) {
				head = history.get(i);
				if(contentId.equals(head.getFrom())) {
					pa.add(head);
					contentId = head.getTo();
				}
			}
			if(!toId.equals(contentId))
				throw new IllegalStateException("No history link for " + pathId + " from " + fromId + " to " + toId);
			return pa;
		} else {
			PathAction pa = new PathAction(pathId, Direction.REWIND);
			PathDiff head = history.get(fromIndex - 1);
			pa.add(head);
			SerializedDigest contentId = head.getFrom();
			for(int i = fromIndex - 2; i > toIndex; i--) {
				head = history.get(i);
				if(contentId.equals(head.getTo())) {
					pa.add(head);
					contentId = head.getFrom();
				}
			}
			if(!toId.equals(contentId))
				throw new IllegalStateException("No history link for " + pathId + " from " + fromId + " to " + toId);
			return pa;
		}
	}
}
