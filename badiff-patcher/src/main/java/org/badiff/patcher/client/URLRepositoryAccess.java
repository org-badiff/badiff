package org.badiff.patcher.client;

import java.io.DataInput;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.badiff.io.Serialization;
import org.badiff.patcher.PatcherSerialization;
import org.badiff.patcher.client.RemotePath.PathType;
import org.badiff.util.Data;

public class URLRepositoryAccess implements RepositoryAccess {
	
	protected URL root;
	
	public URLRepositoryAccess(URL root) {
		this.root = root;
	}
	
	protected URL suburl(String path) {
		try {
			URI base = root.toURI();
			String p = base.getPath() + "/";
			p = p.replaceAll("//$", "/");
			return new URI(
					base.getScheme(),
					base.getUserInfo(),
					base.getHost(),
					base.getPort(),
					p + path,
					null,
					null).toURL();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected InputStream open(String path) {
		try {
			return suburl(path).openStream();
		} catch(IOException e) {
			return null;
		}
	}
	
	protected List<String> subfiles(String path) throws IOException {
		String files = path.replaceAll("/$", "") + "/.__files";
		files = files.replaceAll("^/", "");
		InputStream in = open(files);
		if(in == null)
			throw new IllegalArgumentException("not a directory:" + path);
		Serialization serial = PatcherSerialization.newInstance();
		DataInput data = Data.asInput(in);
		int size = serial.readObject(data, int.class);
		List<String> subfiles = new ArrayList<String>();
		for(int i = 0; i < size; i++) {
			String f = path.replaceAll("/$", "") + "/" + serial.readObject(data, String.class);
			f = f.replaceAll("^/", "");
			subfiles.add(f);
		}
		in.close();
		return subfiles;
	}
	
	protected List<String> subdirs(String path) throws IOException {
		String dirs = path.replaceAll("/$", "") + "/.__dirs";
		dirs = dirs.replaceAll("^/", "");
		InputStream in = open(dirs);
		if(in == null)
			throw new IllegalArgumentException("not a directory:" + path);
		Serialization serial = PatcherSerialization.newInstance();
		DataInput data = Data.asInput(in);
		int size = serial.readObject(data, int.class);
		List<String> subdirs = new ArrayList<String>();
		for(int i = 0; i < size; i++) {
			String d = path.replaceAll("/$", "") + "/" + serial.readObject(data, String.class);
			d = d.replaceAll("^/", "");
			subdirs.add(d);
		}
		in.close();
		return subdirs;
	}

	protected List<Long> sublengths(String path) throws IOException {
		String lengths = path.replaceAll("/$", "") + "/.__lengths";
		lengths = lengths.replaceAll("^/", "");
		InputStream in = open(lengths);
		if(in == null)
			throw new IllegalArgumentException("not a directory:" + path);
		Serialization serial = PatcherSerialization.newInstance();
		DataInput data = Data.asInput(in);
		int size = serial.readObject(data, int.class);
		List<Long> sublengths = new ArrayList<Long>();
		for(int i = 0; i < size; i++)
			sublengths.add(serial.readObject(data, long.class));
		in.close();
		return sublengths;
	}

	protected List<Long> submodified(String path) throws IOException {
		String modified = path.replaceAll("/$", "") + "/.__modified";
		modified = modified.replaceAll("^/", "");
		InputStream in = open(modified);
		if(in == null)
			throw new IllegalArgumentException("not a directory:" + path);
		Serialization serial = PatcherSerialization.newInstance();
		DataInput data = Data.asInput(in);
		int size = serial.readObject(data, int.class);
		List<Long> submodified = new ArrayList<Long>();
		for(int i = 0; i < size; i++)
			submodified.add(serial.readObject(data, long.class));
		in.close();
		return submodified;
	}

	@Override
	public RemotePath get(String path) throws IOException {
		path = path.replaceAll("/$", "");
		String name = path.replaceAll(".*/", "");
		String parent = path.replaceAll("/?[^/]*$", "");
		List<String> nl;
		if((nl = subfiles(parent)).contains(path)) {
			int idx = nl.indexOf(path);	
			return new RemotePath(this, path, PathType.FILE, sublengths(parent).get(idx), submodified(parent).get(idx));
		}
		if(subdirs(parent).contains(path)) {
			return new RemotePath(this, path, PathType.DIRECTORY, 0, 0);
		}
		throw new IOException("Neither file nor directory:" + path + " (parent:" + parent + " name:" + name + ")");
	}

	@Override
	public RemotePath[] list(RemotePath dir) throws IOException {
		List<String> subfiles = subfiles(dir.path());
		List<String> subdirs = subdirs(dir.path());
		List<RemotePath> paths = new ArrayList<RemotePath>();
		for(String f : subfiles)
			paths.add(get(f));
		for(String d : subdirs)
			paths.add(get(d));
		return paths.toArray(new RemotePath[paths.size()]);
	}

	@Override
	public InputStream open(RemotePath file) throws IOException {
		InputStream in = open(file.path());
		if(in == null)
			throw new FileNotFoundException(file.path());
		return in;
	}

}
