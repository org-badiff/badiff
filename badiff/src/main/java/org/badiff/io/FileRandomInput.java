package org.badiff.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

public class FileRandomInput extends DataInputStream implements RandomInput {
	protected File file;
	protected long pos;
	
	public FileRandomInput(File file) throws IOException {
		super(new FileInputStream(file));
		this.file = file;
	}

	@Override
	public long first() {
		return 0;
	}

	@Override
	public long last() {
		return file.length();
	}

	@Override
	public long position() {
		return pos;
	}

	@Override
	public void seek(long pos) throws IOException {
		skip(pos - this.pos);
	}

	@Override
	public int read() throws IOException {
		int b = super.read();
		pos++;
		return b;
	}

	@Override
	public long skip(long n) throws IOException {
		if(n > 0)
			return in.skip(n);
		else if(n < 0) {
			if(-n > pos)
				n = -pos;
			in.close();
			in = new FileInputStream(file);
			in.skip(pos + n);
			return n;
		} else
			return 0;
	}
	
}
