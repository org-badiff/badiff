package org.badiff.patcher.client;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.badiff.Op;
import org.badiff.patcher.PathDiff;
import org.badiff.patcher.SerializedDigest;
import org.badiff.q.OpQueue;
import org.badiff.q.UndoOpQueue;
import org.badiff.util.Data;

public class PathAction extends ArrayList<PathDiff> {
	public static enum Direction {
		FAST_FORWARD {
			@Override
			public void apply(File from, File to, File tmp, PathDiff pd)
					throws IOException {
				InputStream orig = new FileInputStream(from);
				OutputStream target = new FileOutputStream(tmp);
				
				pd.getDiff().apply(Data.asInput(orig), Data.asOutput(target));
				
				orig.close();
				target.close();
				tmp.renameTo(to);
			}
		},
		PAUSE {
			@Override
			public void apply(File from, File to, File tmp, PathDiff pd)
					throws IOException {
				// do nothing
			}
		},
		REWIND {
			@Override
			public void apply(File from, File to, File tmp, PathDiff pd)
					throws IOException {
				InputStream orig = new FileInputStream(from);
				OutputStream target = new FileOutputStream(tmp);
				DataInput dataIn = Data.asInput(orig);
				DataOutput dataOut = Data.asOutput(target);
				
				OpQueue q = pd.getDiff().queue();
				q = new UndoOpQueue(q);
				while(q.hasNext()) {
					Op op = q.next();
					op.apply(dataIn, dataOut);
				}
				
				orig.close();
				target.close();
				tmp.renameTo(to);
			}
		},
		;
		
		public abstract void apply(File from, File to, File tmp, PathDiff pd) throws IOException;
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
			tmp.renameTo(to);
		}
	}
}
