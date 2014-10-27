package org.badiff.patcher.client;

import java.util.ArrayList;
import java.util.List;

import org.badiff.patcher.PathDiff;
import org.badiff.patcher.SerializedDigest;

public class PathAction extends ArrayList<PathDiff> {
	public static enum Direction {
		FAST_FORWARD,
		PAUSE,
		REWIND,
	}
	
	protected SerializedDigest pathId;
	protected Direction direction;
	
	public PathAction(SerializedDigest pathId, Direction direction) {
		this.pathId = pathId;
		this.direction = direction;
	}
	
	public SerializedDigest getPathId() {
		return pathId;
	}
	
	public Direction getDirection() {
		return direction;
	}
}
