package org.badiff;

import java.io.File;

import org.badiff.util.Files;
import org.junit.Assert;
import org.junit.Test;

public class PatchesTest {

	@Test
	public void testPatch() throws Exception {
		File origRoot = Files.createTempDirectory("orig", ".root");
		File targetRoot = Files.createTempDirectory("target", ".root");
		
		new File(origRoot, "deleted").createNewFile();
		new File(targetRoot, "created").createNewFile();
		new File(origRoot, "retained").createNewFile();
		new File(targetRoot, "retained").createNewFile();
		
		Patch patch = Patches.patch(origRoot, targetRoot);
		
		Assert.assertEquals(PatchOp.DELETE, patch.get("deleted").getOp());
		Assert.assertEquals(PatchOp.CREATE, patch.get("created").getOp());
		Assert.assertEquals(PatchOp.DIFF, patch.get("retained").getOp());
	}

}
