package org.badiff.patcher;

import java.io.DataOutput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.badiff.imp.BadiffFileDiff;
import org.badiff.io.Serialization;
import org.badiff.patcher.util.Files;
import org.badiff.patcher.util.Sets;
import org.badiff.util.Data;
import org.badiff.util.Digests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalRepository {
	private static final Logger log = LoggerFactory.getLogger(LocalRepository.class);
	
	protected File root;
	
	public LocalRepository(File root) {
		if(!root.isDirectory())
			throw new IllegalArgumentException("repository root must be a directory");
		this.root = root;
		if(!getWorkingCopyRoot().isDirectory() && !getWorkingCopyRoot().mkdirs())
			throw new IllegalArgumentException(getWorkingCopyRoot() + " is not a directory");
		if(!getPathDiffsRoot().isDirectory() && !getPathDiffsRoot().mkdirs())
			throw new IllegalArgumentException(getPathDiffsRoot() + " is not a directory");
	}
	
	public void commit(File newWorkingCopyRoot) throws IOException {
		long ts = System.currentTimeMillis();
		
		// compute what needs to happen
		Set<String> diffsNames = new HashSet<String>(Files.listRelativePaths(getPathDiffsRoot()));
		Set<String> fromPaths = new HashSet<String>(Files.listRelativePaths(getWorkingCopyRoot()));
		Set<String> toPaths = new HashSet<String>(Files.listRelativePaths(newWorkingCopyRoot));
		Set<PathDigest> pathDigests = new HashSet<PathDigest>();
		
		Set<String> deletedPaths = Sets.subtraction(fromPaths, toPaths);
		Set<String> createdOrModifiedPaths = toPaths;
		
		Set<String> deletedPrefixes = new HashSet<String>();
		for(String path : deletedPaths)
			deletedPrefixes.add(new SerializedDigest(Digests.DEFAULT_ALGORITHM, path).toString());
		
		// remote diffs for files that have been deleted
		Iterator<String> dni = diffsNames.iterator();
		while(dni.hasNext()) {
			String diffName = dni.next();
			if(deletedPrefixes.contains(diffName.split("\\.")[0])) {
				log.info("Dropping stale diff:" + diffName);
				if(!new File(getPathDiffsRoot(), diffName).delete())
					throw new IOException("unable to delete " + diffName);
				dni.remove();
			}
		}
		
		// compute diffs for files that have changed
		for(String path : createdOrModifiedPaths) {
			File fromFile = new File(getWorkingCopyRoot(), path);
			File toFile = new File(newWorkingCopyRoot, path);
			SerializedDigest toDigest = new SerializedDigest(Digests.DEFAULT_ALGORITHM, toFile);
			if(!fromFile.exists()) {
				pathDigests.add(new PathDigest(path, toDigest));
				continue;
			}
			SerializedDigest fromDigest = new SerializedDigest(Digests.DEFAULT_ALGORITHM, fromFile);
			if(fromDigest.equals(toDigest)) {
				pathDigests.add(new PathDigest(path, toDigest));
				continue;
			}
			String prefix = new SerializedDigest(Digests.DEFAULT_ALGORITHM, path).toString();
			
			BadiffFileDiff tmpDiff = new BadiffFileDiff(root, "tmp." + prefix + ".badiff");
			tmpDiff.diff(fromFile, toFile);
			PathDiff pd = new PathDiff(ts, path, tmpDiff);

			Serialization serial = PatcherSerialization.newInstance();
			OutputStream out = new FileOutputStream(new File(getPathDiffsRoot(), pd.getName()));
			serial.writeObject(Data.asOutput(out), PathDiff.class, pd);
			out.close();
			
			tmpDiff.delete();
			
			pathDigests.add(new PathDigest(path, toDigest));
		}
		
		// store the most recent digests
		OutputStream out = new FileOutputStream(new File(root, "digests"));
		DataOutput data = Data.asOutput(out);
		Serialization serial = PatcherSerialization.newInstance();
		serial.writeObject(data, int.class, pathDigests.size());
		for(PathDigest pd : pathDigests)
			serial.writeObject(data, PathDigest.class, pd);
		out.close();
		
		// copy over the new working copy
		File tmpwc = new File(root, "working_copy.tmp");
		File oldwc = new File(root, "working_copy.old");
		
		FileUtils.deleteQuietly(tmpwc);
		tmpwc.mkdirs();
		FileUtils.copyDirectory(newWorkingCopyRoot, tmpwc);
		
		if(!getWorkingCopyRoot().renameTo(oldwc) || !tmpwc.renameTo(getWorkingCopyRoot()))
			throw new IOException("unable to move new working copy into place");
		FileUtils.deleteQuietly(oldwc);
	}
	
	public File getRoot() {
		return root;
	}
	
	public File getWorkingCopyRoot() {
		return new File(root, "working_copy");
	}
	
	public File getPathDiffsRoot() {
		return new File(root, "diffs");
	}
}
