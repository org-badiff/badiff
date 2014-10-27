package org.badiff.patcher.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.badiff.patcher.PathDiff;
import org.badiff.patcher.SerializedDigest;
import org.badiff.util.Data;
import org.badiff.util.Digests;

public class PathAction extends ArrayList<PathDiff> {
	private static final long serialVersionUID = 0;
	
	public static enum Direction {
		FAST_FORWARD,
		PAUSE,
		REWIND 
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
	
	public void apply(RepositoryClient client, File from, File to, File tmp) throws IOException {
		if(direction == Direction.PAUSE) {
			FileUtils.copyFile(from, tmp);
			if(!tmp.renameTo(to))
				throw new IOException("Unable to replace " + to);
			return;
		}
		for(PathDiff pd : this) {
			switch(direction) {
			case FAST_FORWARD:
				pd = client.localFastForward(pd);
				break;
			case REWIND:
				pd = client.localRewind(pd.reverse());
				break;
			default:
				throw new IllegalStateException("Unhandled direction:" + direction);
			}
			
			InputStream orig = new FileInputStream(from);
			OutputStream target = new FileOutputStream(tmp);
			
			pd.getDiff().apply(Data.asInput(orig), Data.asOutput(target));
			
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
