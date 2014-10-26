package org.badiff.patcher.client;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.badiff.patcher.PathDigest;
import org.badiff.patcher.SerializedDigest;
import org.badiff.util.Digests;

public class PathDiffChain {
	protected Map<SerializedDigest, PathDiffLink> links;
	
	public PathDiffChain() {
		links = new HashMap<SerializedDigest, PathDiffLink>();
	}
	
	public void add(PathDiffLink link) {
		links.put(link.getTo(), link);
		link.setPrev(links.get(link.getFrom()));
	}
	
	public PathDiffLink get(PathDigest pd) {
		MessageDigest md = Digests.digest(Digests.DEFAULT_ALGORITHM);
		md.update(new SerializedDigest(Digests.DEFAULT_ALGORITHM, pd.getPath()).getDigest());
		md.update(pd.getDigest().getDigest());
		return links.get(new SerializedDigest(md.getAlgorithm(), md.digest()));
	}
}
