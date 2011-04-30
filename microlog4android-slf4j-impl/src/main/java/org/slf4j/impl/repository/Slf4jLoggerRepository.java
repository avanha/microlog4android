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
package org.slf4j.impl.repository;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.impl.MicrologLoggerAdapter;

import com.google.code.microlog4android.Level;
import com.google.code.microlog4android.repository.DefaultLoggerRepository;
import com.google.code.microlog4android.repository.LoggerRepository;

/**
 * Implements an Slf4J wrapper around the Microlog <code>DefaultLoggerRepository</code> that wraps each Microlog logger in an
 * Slf4J compatible adapter.
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @author Jarle Hansen (hansjar@gmail.com)
 * @author Andre Vanha (andre@askitservices.com)
 * @since 0.1
 * 
 */
public enum Slf4jLoggerRepository implements ILoggerFactory {
	INSTANCE;

	private LoggerRepository loggerRepository = DefaultLoggerRepository.INSTANCE;

	/**
	 * Create a <code>LoggerRepository</code>.
	 */
	private Slf4jLoggerRepository() {
	}

	/**
	 * @see com.google.code.microlog4android.config.LoggerRepository#getRootLogger()
	 */
	public Logger getRootLogger() {
		return wrapLogger(loggerRepository.getRootLogger());
	}

	/**
	 * @see com.google.code.microlog4android.config.LoggerRepository#getLogger(java.lang.String)
	 */
	public  Logger getLogger(String name) {
		return wrapLogger(loggerRepository.getLogger(name));
	}


	/**
	 * @see com.google.code.microlog4android.config.LoggerRepository#setLevel(java.lang.String,
	 *      com.google.code.microlog4android.Level)
	 */
	public void setLevel(String name, Level level) {
		loggerRepository.setLevel(name, level);
	}

	/**
	 * @see com.google.code.microlog4android.config.LoggerRepository#getEffectiveLevel(net.sf.microlog
	 *      .core.Logger)
	 */
	public Level getEffectiveLevel(String loggerName) {
		com.google.code.microlog4android.Logger logger = loggerRepository.getLogger(loggerName);
		
		return logger.getEffectiveLevel();
	}

	/**
	 * @see com.google.code.microlog4android.config.LoggerRepository#contains(java.lang.String)
	 */
	public boolean contains(String name) {
		return loggerRepository.contains(name);
	}

	/**
	 * @see com.google.code.microlog4android.config.LoggerRepository#numberOfLeafNodes()
	 */
	public int numberOfLeafNodes() {
		return loggerRepository.numberOfLeafNodes();
	}

	/**
	 * Reset the tree.
	 */
	public void reset() {
		loggerRepository.reset();
	}

	/**
	 * Shutdown the <code>LoggerRepository</code>, i.e. release all the
	 * resources.
	 */
	public void shutdown() {
		loggerRepository.shutdown();
	}
	
	private Logger wrapLogger(com.google.code.microlog4android.Logger micrologLogger) {
		MicrologLoggerAdapter wrapper = (MicrologLoggerAdapter)micrologLogger.getWrapper();
		
		if (wrapper == null) {
			wrapper = new MicrologLoggerAdapter(micrologLogger);
			wrapper = (MicrologLoggerAdapter)micrologLogger.setWrapper(wrapper);
		}
		
		return wrapper;
	}
}
