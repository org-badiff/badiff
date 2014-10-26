package org.badiff.patcher.client;

import java.security.MessageDigest;
import java.util.Arrays;

import org.badiff.patcher.PathDiff;
import org.badiff.patcher.SerializedDigest;
import org.badiff.util.Digests;

public class PathDiffLink {
	protected String name;
	protected SerializedDigest prefix;
	protected SerializedDigest from;
	protected SerializedDigest to;
	
	protected PathDiffLink prev;
	
	public PathDiffLink(String name) {
		this.name = name;
		String[] f = name.split("\\.");
		
		prefix = new SerializedDigest(f[0]);
		SerializedDigest fromRaw = new SerializedDigest(f[1]);
		SerializedDigest toRaw = new SerializedDigest(f[2]);
		
		MessageDigest md = Digests.digest(Digests.DEFAULT_ALGORITHM);
		
		md.update(prefix.getDigest());
		md.update(fromRaw.getDigest());
		from = new SerializedDigest(md.getAlgorithm(), md.digest());
		
		md.update(prefix.getDigest());
		md.update(toRaw.getDigest());
		to = new SerializedDigest(md.getAlgorithm(), md.digest());
	}
	
	public PathDiffLink createPrevious() {
		String[] f = name.split("\\.");
		SerializedDigest pr = new SerializedDigest(f[0]);
		SerializedDigest fr = new SerializedDigest(pr.getAlgorithm(), new byte[pr.getDigest().length]);
		SerializedDigest to = new SerializedDigest(f[1]);
		return new PathDiffLink(PathDiff.getName(pr, fr, to));
	}
	
	public String getName() {
		return name;
	}
	
	public SerializedDigest getPrefix() {
		return prefix;
	}
	
	public SerializedDigest getFrom() {
		return from;
	}
	
	public SerializedDigest getTo() {
		return to;
	}
	
	public PathDiffLink getPrev() {
		return prev;
	}
	
	public void setPrev(PathDiffLink next) {
		this.prev = next;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if(obj instanceof PathDiffLink) {
			PathDiffLink other = (PathDiffLink) obj;
			return
					prefix.equals(other.prefix)
					&& from.equals(other.from)
					&& to.equals(other.to)
					&& (prev == null ? other.prev == null : prev.equals(other.prev));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return prefix.hashCode() ^ from.hashCode() ^ to.hashCode();
	}
}
