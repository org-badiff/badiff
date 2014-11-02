package org.badiff.patcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.badiff.patcher.client.FileRepositoryAccess;
import org.badiff.patcher.client.PathAction;
import org.badiff.patcher.client.PathAction.Direction;
import org.badiff.patcher.client.RepositoryClient;
import org.badiff.patcher.progress.DotsProgressListener;
import org.badiff.patcher.progress.Progress;

public class GitTreeRepoBuilder {
	public static void main(String[] args) throws Exception {
		File treeRepo = new File("target/tree-repo");
		FileUtils.deleteQuietly(treeRepo);
		if(!treeRepo.mkdirs())
			throw new IOException("Cannot create new directory " + treeRepo);
		LocalRepository repo = new LocalRepository(treeRepo);
		
		File tree = new File("target/tree");
		
		BufferedReader commits = new BufferedReader(new InputStreamReader(invoke("git", "log", "--pretty=format:%H", "--reverse").getInputStream()));
		int max = 25;
		for(String line = commits.readLine(); line != null; line = commits.readLine()) {
			System.out.print(line);
			FileUtils.deleteQuietly(tree);
			invoke("git", "read-tree", "-u", "--prefix=badiff-patcher/target/tree", line).waitFor();
			invoke("git", "reset", "--mixed").waitFor();
			Progress p = new Progress();
			p.addProgressListener(new DotsProgressListener());
			repo.commit(tree, p);
			if(--max == 0)
				break;
		}
		FileUtils.deleteQuietly(tree);
		
		RepositoryClient client = new RepositoryClient(new FileRepositoryAccess(treeRepo), new File("target/storage"));
		client.updateDigests();
		client.updateChain();
		List<Long> revts = new ArrayList<Long>();
		for(Long ts : client.getChain().getTimestamps()) {
			revts.add(0, ts);
			System.out.print(ts);
			Map<String, PathAction> pas = client.actionsFor(tree, ts);
			Progress p = new Progress();
			p.addProgressListener(new DotsProgressListener());
			p.push(pas.size());
			for(Map.Entry<String, PathAction> e : pas.entrySet()) {
				if(e.getValue().getDirection() == Direction.PAUSE)
					continue;
				String path = e.getKey();
				System.out.println(path + ":" + e.getValue());
				e.getValue().apply(client, new File(tree, path), new File(tree, path));
				p.complete(1);
			}
			p.pop(true);
		}

		for(Long ts : revts) {
			System.out.print(ts);
			Map<String, PathAction> pas = client.actionsFor(tree, ts);
			Progress p = new Progress();
			p.addProgressListener(new DotsProgressListener());
			p.push(pas.size());
			for(Map.Entry<String, PathAction> e : pas.entrySet()) {
				if(e.getValue().getDirection() == Direction.PAUSE)
					continue;
				String path = e.getKey();
				System.out.println(path + ":" + e.getValue());
				e.getValue().apply(client, new File(tree, path), new File(tree, path));
				p.complete(1);
			}
			p.pop(true);
		}
}
	
	private static Process invoke(String... cmd) throws IOException {
		return Runtime.getRuntime().exec(cmd);
	}
}
