package com.google.code.microlog4android.integration.tests.appender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.code.microlog4android.Level;
import com.google.code.microlog4android.appender.Appender;
import com.google.code.microlog4android.format.Formatter;

public class JunitTestAppender implements Appender {
	private List<String> loggerList = new ArrayList<String>();

	public void clear() {
	}

	public void close() throws IOException {
	}

	public void doLog(String clientID, String name, long time, Level level, Object message, Throwable t) {
		loggerList.add((String)message);
	}

	public Formatter getFormatter() {
		return null;
	}

	public long getLogSize() {
		return 0;
	}

	public boolean isLogOpen() {
		return false;
	}

	public void open() throws IOException {
	}

	public void setFormatter(Formatter arg0) {
	}
	
	public List<String> getLoggerList() {
		return loggerList;
	}
	
	public String[] getPropertyNames() {
		return null;
	}
	
	public void setProperty(String name, String value) {
	}
}
