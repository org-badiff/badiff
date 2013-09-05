package org.badiff.exa;

import java.io.File;

import javax.swing.JFileChooser;

import org.badiff.FileDiffs;
import org.badiff.imp.FileDiff;

public class ComputeFileDiffs {

	public static void main(String[] args) throws Exception {
		File orig, target, diff;
		JFileChooser chooser = new JFileChooser();
		
		if(chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
			return;
		orig = chooser.getSelectedFile();
		
		if(chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
			return;
		target = chooser.getSelectedFile();
		
		if(chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
			return;
		diff = chooser.getSelectedFile();
		
		FileDiff computed = new FileDiffs().diff(orig, target);
		
		diff.delete();
		computed.renameTo(diff);
	}

}
