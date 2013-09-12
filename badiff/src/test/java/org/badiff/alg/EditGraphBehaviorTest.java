package org.badiff.alg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EditGraphBehaviorTest {
	@Parameters()
	public static Iterable<Object[]> params() throws IOException {
		List<Object[]> p = new ArrayList<Object[]>();
		BufferedReader br = new BufferedReader(new InputStreamReader(InertialGraphBehaviorTest.class.getResourceAsStream("edit.txt")));
		for(String line = br.readLine(); line != null; line = br.readLine()) {
			String[] s = line.split("\t");
			s[0] = s[0].replaceAll("\"", "");
			s[1] = s[1].replaceAll("\"", "");
			p.add(s);
		}
		return p;
	}
	
	private String orig;
	private String target;
	private String summary;
	
	public EditGraphBehaviorTest(String orig, String target, String summary) {
		this.orig = orig;
		this.target = target;
		this.summary = summary;
	}
	
	@Test
	public void testGraph() {
		Graph g = new EditGraph((orig.length() + 1) * (target.length() + 1));
		g.compute(orig.getBytes(), target.getBytes());
		Assert.assertEquals(summary, g.queue().consummerize());
	}

}
