package org.badiff.patcher.client;

import java.security.MessageDigest;
import java.util.Comparator;

import org.badiff.patcher.PathDiff;
import org.badiff.patcher.SerializedDigest;
import org.badiff.util.Digests;

public class PathDiffLink {
	public static final Comparator<PathDiffLink> TS_ORDER = new Comparator<PathDiffLink>() {
		@Override
		public int compare(PathDiffLink o1, PathDiffLink o2) {
			return ((Long) o1.getTs()).compareTo(o2.getTs());
		}
	};
	
	protected String name;
	protected long ts;
	protected SerializedDigest pathId;
	protected SerializedDigest from;
	protected SerializedDigest to;
	
	public PathDiffLink(PathDiff pd) {
		this.name = pd.getName();
		
		ts = pd.getTs();
		pathId = pd.getPathId();
		
		MessageDigest md = Digests.digest(Digests.DEFAULT_ALGORITHM);
		
		md.update(pathId.getDigest());
		md.update(pd.getFrom().getDigest());
		from = new SerializedDigest(md.getAlgorithm(), md.digest());
		
		md.update(pathId.getDigest());
		md.update(pd.getTo().getDigest());
		to = new SerializedDigest(md.getAlgorithm(), md.digest());
	}
	
	public String getName() {
		return name;
	}
	
	public long getTs() {
		return ts;
	}
	
	public SerializedDigest getPathId() {
		return pathId;
	}
	
	public SerializedDigest getFrom() {
		return from;
	}
	
	public SerializedDigest getTo() {
		return to;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if(obj instanceof PathDiffLink) {
			PathDiffLink other = (PathDiffLink) obj;
			return
					ts == other.ts
					&& pathId.equals(other.pathId)
					&& from.equals(other.from)
					&& to.equals(other.to);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return ((int) ts) ^ pathId.hashCode() ^ from.hashCode() ^ to.hashCode();
	}
}
