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

public class URLRepositoryAccess implements RepositoryAccess {
	
	protected URL root;
	
	protected List<String> files;
	protected List<Long> lengths;
	protected List<Long> modified;
	protected List<String> dirs;
	
	public URLRepositoryAccess(URL root) throws IOException {
		this.root = root;
		
		files = new ArrayList<String>();
		lengths = new ArrayList<Long>();
		modified = new ArrayList<Long>();
		dirs = new ArrayList<String>();
		
		InputStream in = open("files");
		Serialization serial = PatcherSerialization.newInstance();
		int size = serial.readObject(in, Integer.class);
		for(int i = 0; i < size; i++)
			files.add(serial.readObject(in, String.class));
		in.close();
		
		in = open("lengths");
		size = serial.readObject(in, Integer.class);
		for(int i = 0; i < size; i++)
			lengths.add(serial.readObject(in, Long.class));
		in.close();
		
		in = open("modified");
		size = serial.readObject(in, Integer.class);
		for(int i = 0; i < size; i++)
			modified.add(serial.readObject(in, Long.class));
		in.close();
		
		in = open("dirs");
		size = serial.readObject(in, Integer.class);
		for(int i = 0; i < size; i++)
			dirs.add(serial.readObject(in, String.class));
		in.close();
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
	
	@Override
	public RemotePath get(String path) throws IOException {
		path = path.replaceAll("/$", "");
		if(files.contains(path)) {
			int idx = files.indexOf(path);	
			return new RemotePath(this, path, PathType.FILE, lengths.get(idx), modified.get(idx));
		}
		if(dirs.contains(path)) {
			return new RemotePath(this, path, PathType.DIRECTORY, 0, 0);
		}
		throw new FileNotFoundException(path);
	}

	@Override
	public RemotePath[] list(RemotePath dir) throws IOException {
		String path = dir.path();
		if(!path.endsWith("/"))
			path = path + "/";
		List<RemotePath> paths = new ArrayList<RemotePath>();
		for(int i = 0; i < files.size(); i++)
			if(files.get(i).startsWith(path) && !files.get(i).substring(path.length()).contains("/"))
				paths.add(new RemotePath(this, files.get(i), PathType.FILE, lengths.get(i), modified.get(i)));
		for(String d : dirs)
			if(d.startsWith(path) && !d.substring(path.length()).contains("/"))
				paths.add(new RemotePath(this, d, PathType.DIRECTORY, 0, 0));
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
