package org.badiff.cli.io;

import org.badiff.io.RandomInput;

public class ProgressInputListener implements RandomInputListener {

	protected ListenableRandomInput[] in;
	protected long[] pos;
	
	public ProgressInputListener(ListenableRandomInput... in) {
		this.in = in;
		pos = new long[in.length];
		for(int i = 0; i < in.length; i++) {
			in[i].addListener(this);
		}
		status();
	}
	
	protected int pos(ListenableRandomInput input) {
		for(int i = 0; i < in.length; i++)
			if(in[i] == input)
				return i;
		return -1;
	}
	
	@Override
	public void moved(ListenableRandomInput thiz) {
		int i = pos(thiz);
		long step = thiz.last() / 10;
		if(thiz.position() - pos[i] > step) {
			pos[i] = thiz.position();
			status();
		}
	}

	public void status() {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for(int i = 0; i < in.length; i++) {
			sb.append(sep);
			sb.append((100 * in[i].position() / in[i].last()) + "%");
			sep = " ";
		}
		System.out.println(sb);
	}
}
