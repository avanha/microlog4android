package com.google.code.microlog4android.appender;

import java.io.IOException;

import com.google.code.microlog4android.Level;

public class ConsoleAppender extends AbstractAppender {
	private static final String[] PROPERTY_NAMES = new String[] {};
	
	@Override
	public void doLog(String clientID, String name, long time, Level level, Object message, Throwable t) {
		if (formatter != null) {
			switch (level) {
			case FATAL:
			case ERROR:
			case WARN:
			case INFO:
			case DEBUG:
			case TRACE:
				System.out.println(formatter.format(clientID, name, time, level, message, t));
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void clear() {
		// Nothing to do
	}

	@Override
	public void close() throws IOException {
		// Always open
	}

	@Override
	public void open() throws IOException {
		// Always open
	}
	
	public long getLogSize() {
		return SIZE_UNDEFINED;
	}
	
	public String[] getPropertyNames() {
		return PROPERTY_NAMES;
	}
	
	public void setProperty(String name, String value) {
		// NOOP - No properties supported.
	}
}
