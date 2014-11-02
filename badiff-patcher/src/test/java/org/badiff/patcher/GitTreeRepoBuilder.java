package org.badiff.patcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;

public class GitTreeRepoBuilder {
	public static void main(String[] args) throws Exception {
		File treeRepo = new File("target/tree-repo");
		if(!treeRepo.mkdirs())
			throw new IOException("Cannot create new directory " + treeRepo);
		LocalRepository repo = new LocalRepository(treeRepo);
		
		File tree = new File("target/tree");
		
		BufferedReader commits = new BufferedReader(new InputStreamReader(invoke("git", "log", "--pretty=format:%H", "--reverse").getInputStream()));
		for(String line = commits.readLine(); line != null; line = commits.readLine()) {
			System.out.println(line);
			FileUtils.deleteQuietly(tree);
			invoke("git", "read-tree", "-u", "--prefix=badiff-patcher/target/tree", line).waitFor();
			invoke("git", "reset", "--mixed").waitFor();
			repo.commit(tree);
		}
		FileUtils.deleteQuietly(tree);
	}
	
	private static Process invoke(String... cmd) throws IOException {
		return Runtime.getRuntime().exec(cmd);
	}
}
