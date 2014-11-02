package org.badiff.patcher.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.badiff.patcher.PathDiff;
import org.badiff.patcher.SerializedDigest;
import org.badiff.patcher.client.PathAction.Direction;

public class PathDiffChain {
	protected RepositoryClient client;
	protected Set<String> paths;
	protected Map<SerializedDigest, List<PathDiff>> history;
	protected Set<Long> timestamps;
	
	public PathDiffChain(RepositoryClient client) {
		this.client = client;
		paths = new HashSet<String>();
		history = new HashMap<SerializedDigest, List<PathDiff>>();
		timestamps = new TreeSet<Long>();
	}
	
	public void clear() {
		history.clear();
	}
	
	public void add(PathDiff link) throws IOException {
		List<PathDiff> pathHistory = historyFor(link.getPathId());
		if(!pathHistory.contains(link)) {
			paths.add(client.pathForId(link.getPathId()));
			pathHistory.add(link);
			Collections.sort(pathHistory, PathDiff.TS_ORDER);
			timestamps.add(link.getTs());
		}
	}

	public Set<String> getPaths() {
		return paths;
	}
	
	public Set<Long> getTimestamps() {
		return timestamps;
	}
	
	public List<PathDiff> historyFor(SerializedDigest pathId) {
		List<PathDiff> pathHistory = history.get(pathId);
		if(pathHistory == null)
			history.put(pathId, pathHistory = new ArrayList<PathDiff>());
		return pathHistory;
	}
	
	public int indexOf(SerializedDigest pathId, SerializedDigest contentId) {
		List<PathDiff> pathHistory = historyFor(pathId);
		if(pathHistory.size() == 0)
			return -1;
		for(int i = 0; i < pathHistory.size(); i++) {
			if(pathHistory.get(i).getTo().equals(contentId))
				return i + 1;
		}
		if(pathHistory.get(0).getFrom().equals(contentId))
			return 0;
		return -1;
	}
	
	protected Integer indexOf(SerializedDigest pathId, long timestamp) {
		List<PathDiff> pathHistory = historyFor(pathId);
		if(pathHistory.size() == 0)
			return null;
		for(int i = pathHistory.size()-1; i >= 0; i--) {
			if(pathHistory.get(i).getTs() == timestamp)
				return i+1;
			if(pathHistory.get(i).getTs() < timestamp)
				return -(i + 1);
		}
		return 0;
	}
	
	public PathAction actionFor(SerializedDigest pathId, SerializedDigest fromId, SerializedDigest toId) {
		int fromIndex = indexOf(pathId, fromId);
		int toIndex = indexOf(pathId, toId);
		if(fromIndex < 0)
			throw new IllegalArgumentException(pathId + " does not have content id " + fromId);
		if(toIndex < 0)
			throw new IllegalArgumentException(pathId + " does not have content id " + toId);
		
		return actionFor(pathId, fromId, toId, fromIndex, toIndex);
	}
	
	public PathAction actionFor(SerializedDigest pathId, SerializedDigest fromId, long toTimestamp) {
		int fromIndex = indexOf(pathId, fromId);
		int toIndex = indexOf(pathId, toTimestamp);
		boolean inexact = toIndex < 0;
		toIndex = Math.abs(toIndex);
		
		if(fromIndex < 0)
			throw new IllegalArgumentException(pathId + " does not have content id " + fromId);
		
		SerializedDigest toId;
		if(fromIndex == toIndex)
			toId = fromId;
		else if(fromIndex < toIndex)
			toId = historyFor(pathId).get(toIndex - 1).getTo();
		else if(inexact)
			toId = historyFor(pathId).get(Math.max(0, toIndex - 1)).getFrom();
		else
			toId = historyFor(pathId).get(Math.max(toIndex - 1, 0)).getTo();
		
		return actionFor(pathId, fromId, toId, fromIndex, toIndex);
	}
	
	protected PathAction actionFor(
			SerializedDigest pathId, 
			SerializedDigest fromId, 
			SerializedDigest toId, 
			int fromIndex, 
			int toIndex) {
	
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
			return pa;
		} else {
			PathAction pa = new PathAction(pathId, Direction.REWIND);
			PathDiff head = history.get(fromIndex - 1);
			pa.add(head);
			SerializedDigest contentId = head.getFrom();
			for(int i = fromIndex - 2; i >= toIndex; i--) {
				head = history.get(i);
				if(contentId.equals(head.getTo())) {
					pa.add(head);
					contentId = head.getFrom();
				}
			}
			return pa;
		}
	}
}
