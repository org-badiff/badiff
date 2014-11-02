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
import org.badiff.io.FileRandomInput;
import org.badiff.io.RandomInput;
import org.badiff.io.StreamRandomInput;
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
		if(direction == Direction.REWIND || direction == Direction.REPLACE_AND_REWIND)
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
	
	public void apply(RepositoryClient client, File from, File to) throws IOException {
		File tmp = File.createTempFile(from.getName(), ".tmp");
		File tmp2 = File.createTempFile(from.getName(), ".tmp");
		try {
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
				InputStream in = client.getWorkingCopy(pathId);
				if(in == null)
					tmp.delete();
				else {
					FileOutputStream out = new FileOutputStream(tmp);
					IOUtils.copy(in, out);
					out.close();
					in.close();
				}
				from = tmp;
				if(diffs.size() == 0) {
					if(in != null) {
						FileUtils.copyFile(from, tmp);
						if(!tmp.renameTo(to))
							throw new IOException("Unable to replace " + to);
					} else {
						if(to.exists() && !to.delete())
							throw new IOException("Unable to delete " + to);
					}

					return;
				}
			}
			for(PathDiff pd : diffs) {
				System.out.println(pd);
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

				to.getParentFile().mkdirs();
				
				pd.getDiff().apply(from, to);
				
				
				from = to;
			}
			
			if(!to.exists()) {
				String path = client.pathForId(pathId);
				File f = to;
				while(!path.isEmpty()) {
					f.delete();
					f = f.getParentFile();
					path = path.replaceAll("/?[^/]*$", "");
				}
			}

		} finally {
			tmp.delete();
			tmp2.delete();
		}
	}
}
