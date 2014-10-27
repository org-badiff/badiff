package org.badiff.patcher.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.badiff.patcher.SerializedDigest;

public class PathDiffChain {
	protected Map<SerializedDigest, List<PathDiffLink>> history;
	
	public PathDiffChain() {
		history = new HashMap<SerializedDigest, List<PathDiffLink>>();
	}
	
	public void clear() {
		history.clear();
	}
	
	public void add(PathDiffLink link) {
		List<PathDiffLink> pathHistory = get(link.getPathId());
		pathHistory.add(link);
		Collections.sort(pathHistory, PathDiffLink.TS_ORDER);
	}
	
	public List<PathDiffLink> get(SerializedDigest pathId) {
		List<PathDiffLink> pathHistory = history.get(pathId);
		if(pathHistory == null)
			history.put(pathId, pathHistory = new ArrayList<PathDiffLink>());
		return pathHistory;
	}
}
