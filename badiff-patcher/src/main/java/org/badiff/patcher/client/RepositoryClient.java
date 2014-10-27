package org.badiff.patcher.client;

import java.io.DataInput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.badiff.imp.BadiffFileDiff;
import org.badiff.io.Serialization;
import org.badiff.patcher.PatcherSerialization;
import org.badiff.patcher.PathDiff;
import org.badiff.patcher.PathDigest;
import org.badiff.patcher.SerializedDigest;
import org.badiff.util.Data;

public class RepositoryClient {
	protected RepositoryAccess serverAccess;
	
	protected PathDiffChain chain;
	protected Map<String, SerializedDigest> digests;
	
	protected File storage;
	
	public RepositoryClient(RepositoryAccess access) {
		this.serverAccess = access;
		chain = new PathDiffChain();
		digests = new HashMap<String, SerializedDigest>();
		storage = new File(System.getProperty("java.io.tmpdir"), "badiff-patcher");
	}
	
	public void updateChain() throws IOException {
		chain.clear();
		loadChain(new FileRepositoryAccess(storage));
		loadChain(serverAccess);
	}
	
	protected void loadChain(RepositoryAccess access) throws IOException {
		List<RemotePath> diffs = Arrays.asList(access.get("ff").list());
		Collections.sort(diffs, RemotePath.LAST_MODIFIED_ORDER);
		for(RemotePath d : diffs) {
			chain.add(PathDiff.parseName(d.name()));
		}
	}
	
	public void updateDigests() throws IOException {
		digests.clear();
		InputStream in = serverAccess.get("digests").open();
		DataInput data = Data.asInput(in);
		Serialization serial = PatcherSerialization.newInstance();
		int size = serial.readObject(data, int.class);
		for(int i = 0; i < size; i++) {
			PathDigest pd = serial.readObject(data, PathDigest.class);
			digests.put(pd.getPath(), pd.getDigest());
		}
	}
	
	public PathDiff localFastForward(PathDiff pd) throws IOException {
		BadiffFileDiff ff = new BadiffFileDiff(storage, "ff/" + pd.getName());
		if(!ff.exists()) {
			File tmp = new File(ff.getParentFile(), ff.getName() + ".download");
			ff.getParentFile().mkdirs();
			OutputStream out = new FileOutputStream(tmp);
			InputStream in = serverAccess.get("ff/" + pd.getName()).open();
			IOUtils.copy(in, out);
			in.close();
			out.close();
			if(!tmp.renameTo(ff))
				throw new IOException("Unable to replace " + ff);
		}
		return new PathDiff(pd.getName(), ff);
	}
	
	public PathDiff localRewind(PathDiff pd) throws IOException {
		BadiffFileDiff rw = new BadiffFileDiff(storage, "rw/" + pd.getName());
		if(!rw.exists()) {
			File tmp = new File(rw.getParentFile(), rw.getName() + ".download");
			rw.getParentFile().mkdirs();
			OutputStream out = new FileOutputStream(tmp);
			InputStream in = serverAccess.get("rw/" + pd.getName()).open();
			IOUtils.copy(in, out);
			in.close();
			out.close();
			if(!tmp.renameTo(rw))
				throw new IOException("Unable to replace " + rw);
		}
		return new PathDiff(pd.getName(), rw);
	}
	
	public RepositoryAccess getServerAccess() {
		return serverAccess;
	}
	
	public PathDiffChain getChain() {
		return chain;
	}
	
	public Map<String, SerializedDigest> getDigests() {
		return digests;
	}
	
	public File getStorage() {
		return storage;
	}
	
	public void setStorage(File storage) {
		this.storage = storage;
	}
}
