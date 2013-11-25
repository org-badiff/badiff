package org.badiff.alg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

public class BehaviorTestDataGenerator {
	protected static Set<String> gatherStrings() throws IOException {
		Set<String> strings = new TreeSet<String>();
		
		for(File java : FileUtils.listFiles(new File("src"), new String[]{"java"}, true)) {
			BufferedReader br = new BufferedReader(new FileReader(java));
			try {
				for(String line = br.readLine(); line != null; line = br.readLine()) {
					line = line.replaceAll("\"", "");
					line = line.trim().replaceAll("\\s+", " ");
					if(line.length() > 4)
						strings.add(line);
				}
			} finally {
				br.close();
			}
		}
		
		return strings;
	}
	
	public static void main(String[] args) throws Exception {
		Iterator<String> si = gatherStrings().iterator();
		
		Graph graph = new AdjustableInertialGraph(200 * 200);
		
		String prev = si.next();
		while(si.hasNext()) {
			String next = si.next();

			graph.compute(prev.getBytes(), next.getBytes());
			
			System.out.print("\"" + prev + "\"\t");
			System.out.print("\"" + next + "\"\t");
			System.out.println(graph.queue().consummerize());
			
			
			prev = next;
		}
	}
}
