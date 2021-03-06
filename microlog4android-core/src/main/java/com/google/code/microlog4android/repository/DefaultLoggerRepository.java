/*
 * Copyright 2009 The Microlog project @sourceforge.net
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.microlog4android.repository;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import com.google.code.microlog4android.Level;
import com.google.code.microlog4android.Logger;

import android.util.Log;


/**
 * The <code>LoggerRepository</code> creates and contains all
 * <code>Logger</code> object(s).
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @author Jarle Hansen (hansjar@gmail.com)
 * @since 0.1
 * 
 */
public enum DefaultLoggerRepository implements LoggerRepository, CommonLoggerRepository {
	INSTANCE;

	private static final String TAG = "Microlog.DefaultLoggerRepository";
	private MicrologRepositoryRootNode rootNode;
	private Hashtable<String, MicrologRepositoryNode> leafNodeHashtable = new Hashtable<String, MicrologRepositoryNode>(43);

	/**
	 * Create a <code>LoggerRepository</code>.
	 */
	private DefaultLoggerRepository() {
		Logger rootLogger = new Logger("", this);
		rootLogger.setLevel(Level.DEBUG);
		rootNode = new MicrologRepositoryRootNode("", rootLogger);
	}

	/**
	 * @see com.google.code.microlog4android.repository.LoggerRepository#getRootLogger()
	 */
	public Logger getRootLogger() {
		return rootNode.getLogger();
	}

	/**
	 * @see com.google.code.microlog4android.repository.LoggerRepository#getLogger(java.lang.String)
	 */
	public synchronized Logger getLogger(String name) {
		return getLogger(name, /*registerLeadNode*/true);
	}
	
	/**
	 * Returns the logger for the specified name, but allows control over whether or not the node is registered as a leaf node.
	 * (Useful for testing)
	 *  
	 * @param name 
	 *            The name of the logger to retrieve.
	 * @param registerLeafNode
	 *            Pass <code>true</code> to register the logger as a leaf node if it isn't one already.
	 * @return The logger for the name.
	 */
	public synchronized Logger getLogger(String name, boolean registerLeafNode) {
		
		MicrologRepositoryNode node = leafNodeHashtable.get(name);
		
		Logger logger;
		
		if (node == null) {
			logger = new Logger(name, this);
			logger = addLogger(logger, registerLeafNode);
		} else {
			logger = node.getLogger();
		}
		
		return logger;
	}

	/**
	 * Adds the specified <code>Logger</code> to the tree.
	 * 
	 * @param logger 
	 *            the <code>Logger</code> to add.
	 * @return The logger passed in or the 
	 */
	Logger addLogger(Logger newLogger) {
		return addLogger(newLogger, /*registerLeafNode*/true);
	}
		
	/**
	 * Adds the specified <code>Logger</code> to the tree.  Allows control over whether the node is 
	 * added to the leafNodeHashtable.
	 * 
	 * @param logger 
	 *            the <code>Logger</code> to add.
	 * @param registerLeadNode
	 *            Controls whether the node is added to the leafNodeHashtable.
	 * @return The logger passed in or the 
	 */
	Logger addLogger(Logger newLogger, boolean registerLeafNode) {
		MicrologRepositoryNode currentNode = rootNode;
		String[] pathComponents = LoggerNamesUtil.getLoggerNameComponents(newLogger.getName());
		
		// Create intermediate nodes
		for (int i = 0; i < pathComponents.length; i++) {
			MicrologRepositoryNode child = currentNode.getChildNode(pathComponents[i]);

			if (child == null) {
				// No child => add the child
				currentNode = createNewChildNode(pathComponents[i], currentNode);
			} else {
				currentNode = child;
			}
		}

		// If requested, add the node to the leafNodeHashtable so it can be found directly next time
		if (registerLeafNode) {
			leafNodeHashtable.put(newLogger.getName(), currentNode);
		}
		
		return currentNode.getLogger();
	}

	/**
	 * @see com.google.code.microlog4android.repository.LoggerRepository#setLevel(java.lang.String,
	 *      com.google.code.microlog4android.Level)
	 */
	public void setLevel(String name, Level level) {
		getLogger(name).setLevel(level);
		
		/*
		// Check if name the name is a leaf node
		MicrologRepositoryNode leafNode = leafNodeHashtable.get(name);

		if (leafNode != null) {
			leafNode.getLogger().setLevel(level);
		} else {
			MicrologRepositoryNode currentNode = rootNode;
			String[] pathComponents = LoggerNamesUtil.getLoggerNameComponents(name);
			for (String pathComponent : pathComponents) {
				MicrologRepositoryNode child = currentNode.getChildNode(pathComponent);

				if (child == null) {
					// No child => add the child
					currentNode = createNewChildNode(pathComponent, currentNode);
				}
			}

			if (currentNode != null) {
				currentNode.getLogger().setLevel(level);
			}
		}
		*/
	}

	private MicrologRepositoryNode createNewChildNode(final String pathComponent, final MicrologRepositoryNode currentNode) {
		//MicrologRepositoryNode newChild = new MicrologRepositoryNode(pathComponent, currentNode);
		String loggerName;
		
		if (currentNode.getName().length() == 0) {
			loggerName = pathComponent;
		} else {
			loggerName = currentNode.getPath() + LoggerNamesUtil.SEPARATOR + pathComponent;
		}
		
		MicrologRepositoryNode newChild = new MicrologRepositoryNode(pathComponent, 
				new Logger(loggerName, DefaultLoggerRepository.INSTANCE), currentNode);
		currentNode.addChild(newChild);

		return newChild;
	}

	/**
	 * @see com.google.code.microlog4android.repository.LoggerRepository#getEffectiveLevel(net.sf.microlog
	 *      .core.Logger)
	 */
	public Level getEffectiveLevel(String loggerName) {
		Level effectiveLevel = null;
		MicrologRepositoryNode currentNode = leafNodeHashtable.get(loggerName);

		while (effectiveLevel == null && currentNode != null) {
			effectiveLevel = currentNode.getLogger().getLevel();
			currentNode = currentNode.getParent();
		}
		
		// Make sure we return something to avoid a possible NPE if someone screwed up the root logger's level.
		if (effectiveLevel == null)
			effectiveLevel = Level.OFF;

		return effectiveLevel;
	}

	/**
	 * @see com.google.code.microlog4android.repository.LoggerRepository#contains(java.lang.String)
	 */
	public boolean contains(String name) {
		return leafNodeHashtable.containsKey(name);
	}

	/**
	 * @see com.google.code.microlog4android.repository.LoggerRepository#numberOfLeafNodes()
	 */
	public int numberOfLeafNodes() {
		return leafNodeHashtable.size();
	}

	/**
	 * Reset the tree and configuration at the root level.
	 */
	public void reset() {
		rootNode.reset();
		leafNodeHashtable.clear();
		resetConfig();
	}
	
	/**
	 * Reset the level and appender configuration but keep the tree intact. 
	 */
	public void resetConfig() {
		rootNode.getLogger().resetLogger(Level.DEBUG);
		Logger.resetAppenders();

		// Reset each existing logger to force everything to delegate to the root.
		Enumeration<MicrologRepositoryNode> leafNodes = leafNodeHashtable.elements();

		while (leafNodes.hasMoreElements()) {
			MicrologRepositoryNode node = leafNodes.nextElement();
			Logger logger = node.getLogger();
			logger.resetLogger(/*level*/null);
		}
	}

	/**
	 * Shutdown the <code>LoggerRepository</code>, i.e. release all the
	 * resources.
	 */
	public void shutdown() {
		Enumeration<MicrologRepositoryNode> leafNodes = leafNodeHashtable.elements();

		while (leafNodes.hasMoreElements()) {
			MicrologRepositoryNode node = leafNodes.nextElement();
			Logger logger = node.getLogger();

			if (logger != null) {
				try {
					logger.close();
				} catch (IOException e) {
					Log.e(TAG, "Failed to close logger " + logger.getName());
				}
			}
		}
	}
}
