package org.badiff.patcher.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.badiff.io.EmptyInputStream;
import org.badiff.patcher.PathDiff;
import org.badiff.patcher.SerializedDigest;
import org.badiff.util.Data;
import org.badiff.util.Digests;

public class PathAction {
	
	public static enum Direction {
		FAST_FORWARD,
		PAUSE,
		REWIND 
	}
	
	protected SerializedDigest pathId;
	protected Direction direction;
	protected List<PathDiff> diffs;
	
	public PathAction(SerializedDigest pathId, Direction direction) {
		this.pathId = pathId;
		this.direction = direction;
		diffs = new ArrayList<PathDiff>();
	}
	
	public SerializedDigest getPathId() {
		return pathId;
	}
	
	public Direction getDirection() {
		return direction;
	}
	
	public void add(PathDiff pd) {
		if(direction == Direction.REWIND)
			pd = pd.reverse();
		diffs.add(pd);
	}
	
	public void load(RepositoryClient client) throws IOException {
		for(PathDiff pd : diffs) {
			switch(direction) {
			case FAST_FORWARD:
				pd = client.localFastForward(pd);
				break;
			case REWIND:
				pd = client.localRewind(pd);
				break;
			case PAUSE:
				break;
			}
		}
	}
	
	@Override
	public String toString() {
		return direction.toString() + diffs;
	}
	
	public void apply(RepositoryClient client, File from, File to, File tmp) throws IOException {
		if(direction == Direction.PAUSE) {
			FileUtils.copyFile(from, tmp);
			if(!tmp.renameTo(to))
				throw new IOException("Unable to replace " + to);
			return;
		}
		for(PathDiff pd : diffs) {
			switch(direction) {
			case FAST_FORWARD:
				pd = client.localFastForward(pd);
				break;
			case REWIND:
				pd = client.localRewind(pd);
				break;
			default:
				throw new IllegalStateException("Unhandled direction:" + direction);
			}
			
			tmp.getParentFile().mkdirs();
			
			InputStream orig;
			if(from.canRead())
				orig = new FileInputStream(from);
			else
				orig = new EmptyInputStream();
			OutputStream target = new FileOutputStream(tmp);
			
			pd.getDiff().apply(Data.asInput(orig), Data.asOutput(target));
			
			to.getParentFile().mkdirs();
			
			orig.close();
			target.close();
			if(!tmp.renameTo(to))
				throw new IOException("Unable to replace " + to);
			
			if(Arrays.equals(Digests.defaultZeroes(), pd.getTo().getDigest())) {
				if(!to.delete())
					throw new IOException("Unable to delete " + to);
			}
		}
	}
}
