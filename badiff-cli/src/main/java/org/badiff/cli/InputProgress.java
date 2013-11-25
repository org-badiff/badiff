package org.badiff.cli;

import org.badiff.cli.io.ListenableRandomInput;
import org.badiff.cli.io.RandomInputListener;
import org.badiff.io.RandomInput;

public class InputProgress implements RandomInputListener {
	public ListenableRandomInput[] inputs;
	
	protected long pos;
	protected long posReported;
	protected long last;
	
	public InputProgress(ListenableRandomInput... inputs) {
		this.inputs = inputs;
		for(ListenableRandomInput input : inputs) {
			input.addListener(this);
			last += input.last();
		}
	}
	
	@Override
	public void moved(RandomInput thiz) {
		pos = 0;
		for(ListenableRandomInput input : inputs)
			pos += input.position();
		if(pos - posReported >= last / 100) {
			System.out.print(".");
			System.out.flush();
			posReported = pos;
		}
		if(pos == last)
			System.out.println();
	}
}
