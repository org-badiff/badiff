package org.badiff.patcher.client;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.badiff.io.Serialization;
import org.badiff.patcher.PatcherSerialization;
import org.badiff.patcher.PathDiff;
import org.badiff.patcher.PathDigest;
import org.badiff.patcher.SerializedDigest;
import org.badiff.util.Data;

public class RepositoryClient {
	protected RepositoryAccess access;
	
	protected PathDiffChain chain;
	protected Map<String, SerializedDigest> digests;
	
	protected File storage;
	
	public RepositoryClient(RepositoryAccess access) {
		this.access = access;
		chain = new PathDiffChain();
		digests = new HashMap<String, SerializedDigest>();
		storage = new File(System.getProperty("java.io.tmpdir"), "badiff-patcher");
	}
	
	public void updateChain() throws IOException {
		chain.clear();
		List<RemotePath> diffs = Arrays.asList(access.get("diffs").list());
		Collections.sort(diffs, RemotePath.LAST_MODIFIED_ORDER);
		for(RemotePath d : diffs) {
			chain.add(new PathDiffLink(PathDiff.parseName(d.name())));
		}
	}
	
	public void updateDigests() throws IOException {
		digests.clear();
		InputStream in = access.get("digests").open();
		DataInput data = Data.asInput(in);
		Serialization serial = PatcherSerialization.newInstance();
		int size = serial.readObject(data, int.class);
		for(int i = 0; i < size; i++) {
			PathDigest pd = serial.readObject(data, PathDigest.class);
			digests.put(pd.getPath(), pd.getDigest());
		}
	}
	
	public RepositoryAccess getAccess() {
		return access;
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
