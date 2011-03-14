package com.google.code.microlog4android.appender;

import java.io.IOException;

import android.util.Log;

import com.google.code.microlog4android.Level;

public class LogCatAppender extends AbstractAppender {
	private static final String[] PROPERTY_NAMES = new String[] { "tag" };
	
	private String tag;
	
	@Override
	public void clear() {
	}

	@Override
	public void doLog(String clientID, String name, long time, Level level, Object message, Throwable t) {
		if (logOpen && formatter != null) {
			String localTag = tag == null ? clientID : tag;
			
			switch (level) {
			case FATAL:
			case ERROR:
				Log.e(localTag, formatter.format(clientID, name, time, level, message, t));
				break;
			
			case WARN:
				Log.w(localTag, formatter.format(clientID, name, time, level, message, t));
				break;
			
			case INFO:
				Log.i(localTag, formatter.format(clientID, name, time, level, message, t));
				break;
				
			case DEBUG:
			case TRACE:
				Log.d(localTag, formatter.format(clientID, name, time, level, message, t));
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void open() throws IOException {
		logOpen = true;
	}

	@Override
	public void close() throws IOException {
		logOpen = false;
	}
	
	public long getLogSize() {
		return SIZE_UNDEFINED;
	}
	
	public String[] getPropertyNames() {
		return PROPERTY_NAMES;
	}
	
	public void setProperty(String name, String value) {
		if (name.equals("tag"))
			tag = value;
	}
}
