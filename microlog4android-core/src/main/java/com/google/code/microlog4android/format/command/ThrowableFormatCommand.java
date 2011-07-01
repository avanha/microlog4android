/*
 * Copyright 2008 The Microlog project @sourceforge.net
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

package com.google.code.microlog4android.format.command;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.google.code.microlog4android.Level;

/**
 * Converts the <code>Throwable</code> to a message.
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 */
public class ThrowableFormatCommand implements FormatCommandInterface {
	public static final int NO_MAX_LINES = -1; 
	
	private int maxLines = NO_MAX_LINES;
	
	/**
	 * @see com.google.code.microlog4android.format.command.FormatCommandInterface#init(String)
	 */
	public void init(String initString) {
		// Do nothing.	
	}

	/**
	 * Set the log data.
	 * 
	 * @see FormatCommandInterface#execute(String, String, long, Level, Object, Throwable)
	 */
	public String execute(String clientID, String name, long time, Level level, Object message, Throwable throwable) {
	    String result = "";
		
	    if (maxLines != 0 && throwable != null) {
	    	StringWriter sw = new StringWriter();
	    	PrintWriter pw = new PrintWriter(sw);
	    	throwable.printStackTrace(pw);
	    	pw.flush();
	    	pw.close();
	    	result = sw.getBuffer().toString();
	    }
	    	
	    return result;
	}
}
