package org.badiff.patcher.progress;

import java.util.EventListener;

public interface ProgressListener extends EventListener {
	public void progressUpdated(ProgressEvent e);
}
