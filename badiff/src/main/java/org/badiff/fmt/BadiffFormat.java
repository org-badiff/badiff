package org.badiff.fmt;

import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.badiff.Diff;
import org.badiff.Op;
import org.badiff.imp.FileBadiff;
import org.badiff.io.DataInputInputStream;
import org.badiff.io.DataOutputOutputStream;
import org.badiff.io.DefaultSerialization;
import org.badiff.io.RandomInput;
import org.badiff.io.Serialization;
import org.badiff.io.Serialized;
import org.badiff.io.SmallNumberSerialization;
import org.badiff.q.OpQueue;
import org.badiff.util.Streams;

public class BadiffFormat implements InputFormat, OutputFormat {

	@Override
	public void exportDiff(Diff diff, RandomInput orig, DataOutput out)
			throws IOException {
		DataOutputOutputStream dout = new DataOutputOutputStream(out);
		FileBadiff bd = new FileBadiff(File.createTempFile("badiff", ".tmp"));
		
		bd.store(diff.queue());
		
		FileInputStream bdi = new FileInputStream(bd);
		Streams.copy(bdi, dout);
		bdi.close();
		
		bd.delete();
	}

	@Override
	public OpQueue importDiff(RandomInput orig, RandomInput ext)
			throws IOException {
		DataInputInputStream din = new DataInputInputStream(ext);
		FileBadiff bd = new FileBadiff(File.createTempFile("badiff", ".tmp"));
		bd.deleteOnExit();
		
		FileOutputStream bdo = new FileOutputStream(bd);
		Streams.copy(din, bdo);
		bdo.close();

		return new BadiffFormatOpQueue(bd);
		
	}

	private class BadiffFormatOpQueue extends OpQueue {
		private FileBadiff bd;
		private OpQueue q;
	
		private BadiffFormatOpQueue(FileBadiff bd) throws IOException {
			this.bd = bd;
			q = bd.queue();
		}
	
		@Override
		protected boolean pull() {
			Op e = q.poll();
			if(e != null)
				prepare(e);
			else
				bd.delete();
			return e != null;
		}
	}
	
	

}
