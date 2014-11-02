package org.badiff.patcher.progress;

public class DotsProgressListener implements ProgressListener {
	private int percent;
	
	public DotsProgressListener() {
		percent = 0;
	}

	@Override
	public void progressUpdated(ProgressEvent e) {
		int epc = (int)(100 * e.getProgress());
		while(epc > percent) {
			System.out.print(".");
			percent++;
		}
	}

}
