package org.badiff.patcher.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.badiff.patcher.PathDiff;
import org.badiff.patcher.SerializedDigest;

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
}
