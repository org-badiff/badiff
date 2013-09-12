package org.badiff.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

public class FileRandomInput extends ByteBufferRandomInput {
	protected FileInputStream in;
	
	public FileRandomInput(File file) throws IOException {
		this(file, true);
	}
	
	public FileRandomInput(File file, boolean load) throws IOException {
		super((ByteBuffer) null);
		in = new FileInputStream(file);
		super.buf = in.getChannel().map(MapMode.READ_ONLY, 0, file.length());
		if(load)
			((MappedByteBuffer) super.buf).load();
	}
	
	@Override
	public void close() throws IOException {
		in.close();
		super.close();
	}
}
