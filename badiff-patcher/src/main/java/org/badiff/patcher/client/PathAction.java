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
import org.apache.commons.io.IOUtils;
import org.badiff.io.EmptyInputStream;
import org.badiff.patcher.PathDiff;
import org.badiff.patcher.SerializedDigest;
import org.badiff.util.Data;
import org.badiff.util.Digests;

public class PathAction {
	
	public static enum Direction {
		FAST_FORWARD,
		PAUSE,
		REWIND,
		REPLACE,
		REPLACE_AND_REWIND,
	}
	
	protected SerializedDigest pathId;
	protected Direction direction;
	protected List<PathDiff> diffs;
	
	public PathAction(SerializedDigest pathId, Direction direction) {
		this.pathId = pathId;
		this.direction = direction;
		diffs = new ArrayList<PathDiff>();
	}
	
	public PathAction(SerializedDigest pathId, PathAction replaceAndRewind) {
		this.pathId = pathId;
		direction = Direction.REPLACE_AND_REWIND;
		diffs = new ArrayList<PathDiff>(replaceAndRewind.diffs);
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
			case REPLACE_AND_REWIND:
				pd = client.localRewind(pd);
				break;
			case PAUSE:
			case REPLACE:
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
			if(from.exists()) {
				FileUtils.copyFile(from, tmp);
				if(!tmp.renameTo(to))
					throw new IOException("Unable to replace " + to);
			} else {
				if(to.exists() && !to.delete())
					throw new IOException("Unable to delete " + to);
			}
				
			return;
		}
		if(direction == Direction.REPLACE) {
			InputStream in = client.getWorkingCopy(pathId);
			if(in == null)
				to.delete();
			else {
				FileOutputStream out = new FileOutputStream(to);
				IOUtils.copy(in, out);
				out.close();
				in.close();
			}
			return;
		}
		if(direction == Direction.REPLACE_AND_REWIND) {
			from = File.createTempFile(from.getName(), ".tmp");
			from.deleteOnExit();
			InputStream in = client.getWorkingCopy(pathId);
			if(in == null)
				from.delete();
			else {
				FileOutputStream out = new FileOutputStream(from);
				IOUtils.copy(in, out);
				out.close();
				in.close();
			}
			if(diffs.size() == 0) {
				FileUtils.copyFile(from, tmp);
				if(!tmp.renameTo(to))
					throw new IOException("Unable to replace " + to);
				return;
			}
		}
		for(PathDiff pd : diffs) {
			switch(direction) {
			case FAST_FORWARD:
				pd = client.localFastForward(pd);
				break;
			case REWIND:
			case REPLACE_AND_REWIND:
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
