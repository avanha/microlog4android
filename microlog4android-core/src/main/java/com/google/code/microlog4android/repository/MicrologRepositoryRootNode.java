package com.google.code.microlog4android.repository;

import com.google.code.microlog4android.Logger;

public class MicrologRepositoryRootNode extends MicrologRepositoryNode {
	public MicrologRepositoryRootNode(String name, Logger logger) {
		super(name, logger);
	}

	public void reset() {
		children.clear();
	}
}
