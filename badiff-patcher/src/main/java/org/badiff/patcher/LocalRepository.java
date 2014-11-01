package org.badiff.patcher;

import java.io.DataOutput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.badiff.imp.BadiffFileDiff;
import org.badiff.io.Serialization;
import org.badiff.patcher.util.Files;
import org.badiff.patcher.util.Sets;
import org.badiff.util.Data;
import org.badiff.util.Digests;

public class LocalRepository {
	protected File root;
	
	public LocalRepository(File root) {
		if(!root.isDirectory())
			throw new IllegalArgumentException("repository root must be a directory");
		this.root = root;
		if(!getWorkingCopyRoot().isDirectory() && !getWorkingCopyRoot().mkdirs())
			throw new IllegalArgumentException(getWorkingCopyRoot() + " is not a directory");
		if(!getFastForwardRoot().isDirectory() && !getFastForwardRoot().mkdirs())
			throw new IllegalArgumentException(getFastForwardRoot() + " is not a directory");
		if(!getRewindRoot().isDirectory() && !getRewindRoot().mkdirs())
			throw new IllegalArgumentException(getRewindRoot() + " is not a directory");
		if(!getIdRoot().isDirectory() && !getIdRoot().mkdirs())
			throw new IllegalArgumentException(getIdRoot() + " is not a directory");
	}
	
	public void commit(File newWorkingCopyRoot) throws IOException {
		long ts = System.currentTimeMillis();
		
		// compute what needs to happen
		Set<String> fromPaths = new HashSet<String>(Files.listRelativePaths(getWorkingCopyRoot()));
		fromPaths.remove(".__files");
		fromPaths.remove(".__dirs");
		fromPaths.remove(".__lengths");
		fromPaths.remove(".__modified");
		Set<String> toPaths = new HashSet<String>(Files.listRelativePaths(newWorkingCopyRoot));
		Set<String> allPaths = Sets.union(fromPaths, toPaths);
		Set<PathDigest> pathDigests = new HashSet<PathDigest>();
		
		// compute diffs for files that have changed
		for(String path : allPaths) {
			File fromFile = new File(getWorkingCopyRoot(), path);
			File toFile = new File(newWorkingCopyRoot, path);
			SerializedDigest toDigest = new SerializedDigest(Digests.DEFAULT_ALGORITHM, toFile);
			SerializedDigest fromDigest = new SerializedDigest(Digests.DEFAULT_ALGORITHM, fromFile);
			if(fromDigest.equals(toDigest)) {
				pathDigests.add(new PathDigest(path, toDigest));
				continue;
			}
			String prefix = new SerializedDigest(Digests.DEFAULT_ALGORITHM, path).toString();
			
			BadiffFileDiff tmpDiff = new BadiffFileDiff(root, "tmp." + prefix + ".badiff");
			tmpDiff.diff(fromFile, toFile);
			PathDiff pd = new PathDiff(ts, path, tmpDiff);
			tmpDiff.renameTo(new File(getFastForwardRoot(), pd.getName()));
			
			tmpDiff.diff(toFile, fromFile);
			pd = pd.reverse();
			tmpDiff.renameTo(new File(getRewindRoot(), pd.getName()));
			
			pathDigests.add(new PathDigest(path, toDigest));
			
			File id = new File(getIdRoot(), pd.getPathId().toString());
			if(!id.isFile()) {
				OutputStream out = new FileOutputStream(id);
				IOUtils.write(path, out, Charset.forName("UTF-8"));
				out.close();
			}
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

		Set<File> directories = new HashSet<File>();
		for(File f : FileUtils.listFiles(root, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
			directories.add(f.getParentFile());
		}
		
		for(File dir : directories) {
			List<File> files = new ArrayList<File>();
			List<File> dirs = new ArrayList<File>();
			for(File f : dir.listFiles()) {
				if(f.getName().startsWith("."))
					continue;
				if(f.isFile())
					files.add(f);
				if(f.isDirectory())
					dirs.add(f);
			}
			out = new FileOutputStream(new File(dir, ".__files"));
			data = Data.asOutput(out);
			serial.writeObject(data, int.class, files.size());
			for(File f : files)
				serial.writeObject(data, String.class, f.getName());
			out.close();
			
			out = new FileOutputStream(new File(dir, ".__lengths"));
			data = Data.asOutput(out);
			serial.writeObject(data, int.class, files.size());
			for(File f : files)
				serial.writeObject(data, long.class, f.length());
			out.close();
			
			out = new FileOutputStream(new File(dir, ".__modified"));
			data = Data.asOutput(out);
			serial.writeObject(data, int.class, files.size());
			for(File f : files)
				serial.writeObject(data, long.class, f.lastModified());
			out.close();
			
			out = new FileOutputStream(new File(dir, ".__dirs"));
			data = Data.asOutput(out);
			serial.writeObject(data, int.class, dirs.size());
			for(File f : dirs)
				serial.writeObject(data, String.class, f.getName());
			out.close();
		}
	}
	
	public File getRoot() {
		return root;
	}
	
	public File getWorkingCopyRoot() {
		return new File(root, "working_copy");
	}
	
	public File getFastForwardRoot() {
		return new File(root, "ff");
	}
	
	public File getRewindRoot() {
		return new File(root, "rw");
	}
	
	public File getIdRoot() {
		return new File(root, "id");
	}
}
