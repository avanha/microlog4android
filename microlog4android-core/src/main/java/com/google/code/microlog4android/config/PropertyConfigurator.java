/**
 * 
 */
package com.google.code.microlog4android.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

import com.google.code.microlog4android.Level;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.appender.Appender;
import com.google.code.microlog4android.format.Formatter;
import com.google.code.microlog4android.repository.DefaultLoggerRepository;
import com.google.code.microlog4android.repository.LoggerRepository;

/**
 * The {@link PropertyConfigurator} is used for configuration via a properties
 * file. The properties file should be put in one of the following directories:
 * 
 * <ol>
 * <li>The assets directory
 * <li/>
 * <li>The res/raw directory</li>
 * </ol>
 * 
 * @author Johan Karlsson
 */
public class PropertyConfigurator {
	private static final String TAG = "Microlog.PropertyConfiguration";

	public static String DEFAULT_PROPERTIES_FILENAME = "microlog.properties";

	/**
	 * The key for setting the root logger.
	 */
	public static final String ROOT_LOGGER_KEY = "microlog.rootLogger";

	public static final String MICROLOG_PREFIX = "microlog";

	/**
	 * The key for setting the logger.
	 */
	public static final String LOGGER_PREFIX_KEY = "microlog.logger";

	/**
	 * The key for setting the formatter.
	 */
	public static final String FORMATTER_PREFIX_KEY = "microlog.formatter";

	/**
	 * The key for setting the appender.
	 */
	public static final String APPENDER_PREFIX_KEY = "microlog.appender";

	/**
	 * The key for setting the level.
	 */
	public static final String LOG_LEVEL_PREFIX_KEY = "microlog.level";

	/**
	 * The key for setting the logging tag.
	 */
	public static final String TAG_PREFIX_KEY = "microlog.tag";
	
	/**
	 * Separate prefixes from instance names.
	 */
	public static final String PREFIX_SEPARATOR = ".";
	
	/**
	 * Separates property names for prefixes or instance names
	 */
	public static final String PROPERTY_SEPARATOR = ".";
	
	/**
	 * The appender's formatter property.
	 */
	public static final String FORMATTER_PROPERTY = "formatter";
	
	/**
	 * The default Log level (String)
	 */
	public static final String DEFAULT_LOG_LEVEL_STRING = "DEBUG";
	
	/**
	 * The default log level if none is specified.
	 */
	public static final Level DEFAULT_LOG_LEVEL = Level.DEBUG; 
	
	/**
	 * The property delimiter used in Log4j property files.
	 */
	public static final String LOG4J_PROPERTY_DELIMITER = ","; 

	public static final String[] APPENDER_ALIASES = { "LogCatAppender", "FileAppender", "DatagramAppender" };

	public static final String[] APPENDER_CLASS_NAMES = { "com.google.code.microlog4android.appender.LogCatAppender",
			"com.google.code.microlog4android.appender.FileAppender", "com.google.code.microlog4android.appender.DatagramAppender" };

	public static final String[] FORMATTER_ALIASES = { "SimpleFormatter", "PatternFormatter" };

	public static final String[] FORMATTER_CLASS_NAMES = { "com.google.code.microlog4android.format.SimpleFormatter",
			"com.google.code.microlog4android.format.PatternFormatter" };

	private static final HashMap<String, String> appenderAliases = new HashMap<String, String>(2);

	private static final HashMap<String, String> formatterAliases = new HashMap<String, String>(2);

	private Context context;

	private LoggerRepository loggerRepository;
	
	private Map<String, Appender> appenders;

	static {
		for (int index = 0; index < APPENDER_ALIASES.length; index++) {
			appenderAliases.put(APPENDER_ALIASES[index], APPENDER_CLASS_NAMES[index]);
		}

		for (int index = 0; index < FORMATTER_ALIASES.length; index++) {
			formatterAliases.put(FORMATTER_ALIASES[index], FORMATTER_CLASS_NAMES[index]);
		}
	};

	private PropertyConfigurator(Context context) {
		this.context = context;
		loggerRepository = DefaultLoggerRepository.INSTANCE;
	}
	
	/**
	 * Create a configurator for the specified context.
	 * 
	 * @param context
	 *            the {@link Context} to get the configurator for.
	 * @return a configurator
	 */
	public static PropertyConfigurator getConfigurator(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("The context must not be null");
		}

		return new PropertyConfigurator(context);
	}

	/**
	 * Configure using the default properties filename, i.e
	 * "microlog.properties".
	 */
	public void configure() {
		configure(DEFAULT_PROPERTIES_FILENAME);
	}

	/**
	 * Configure using the specified filename.
	 * 
	 * @param filename
	 *            the filename of the properties file used for configuration.
	 */
	public void configure(String filename) {
		Resources resources = context.getResources();
		AssetManager assetManager = resources.getAssets();
		try {
			InputStream inputStream = assetManager.open(filename);
			Properties properties = loadProperties(inputStream);
			startConfiguration(properties);
		} catch (IOException e) {
			Log.e(TAG, "Failed to open the microlog properties file. Hint: the file should be in the /assets directory "
					+ filename + " " + e);
		}
	}

	/**
	 * Configure using the specified resource Id.
	 * 
	 * @param resId
	 *            the resource Id where to load the properties from.
	 */
	public void configure(int resId) {
		Resources resources = context.getResources();

		try {
			InputStream rawResource = resources.openRawResource(resId);
			Properties properties = loadProperties(rawResource);
			startConfiguration(properties);
		} catch (NotFoundException e) {
			Log.e(TAG, "Did not find the microlog properties resource. Hint: this should be in the /res/raw directory "
					+ e);
		} catch (IOException e) {
			Log.e(TAG, "Failed to read the microlog properties resource." + e);
		}
	}

	/**
	 * Create the specified appender.  The appender is only created but not 
	 * configured. 
	 * 
	 * @param appenderName
	 * @param properties
	 * @return An instance of the appender class.
	 */
	protected Appender createAppender(String appenderName, Properties properties) {
		Appender appender = null;
		
		String appenderClassNameProperty = PropertyConfigurator.APPENDER_PREFIX_KEY +  PREFIX_SEPARATOR + appenderName;
		String appenderClassName = properties.getProperty(appenderClassNameProperty);
			
		if (appenderClassName != null) {
			appenderClassName = checkForAliasAppender(appenderClassName);
			
			try {
				Class<?> appenderClass = Class.forName(appenderClassName);
				appender = (Appender) appenderClass.newInstance();
			} catch (ClassNotFoundException e) {
				Log.e(TAG, "Could not find appender class " + appenderName);
			} catch (InstantiationException e) {
				Log.e(TAG, "Could not instantiate appender class " + appenderName);
			} catch (IllegalAccessException e) {
				Log.e(TAG, "Not allowed to create appender class " + appenderName);
			} 
		}
		
		return appender;
	}
	
	/**
	 * Retrieves or creates the named appender.
	 * 
	 * @param appenderName The name of the appender.
	 * @param properties Properties us to create and configure the appender.
	 * @return The appender or null.
	 */
	protected Appender getAppender(String appenderName, Properties properties) {
		Appender appender = null;
		
		// Check to see if we've already created this appender
		if (appenders != null)
			appender = appenders.get(appenderName);

		if (appender == null) {
			appender = doConfigureAppender(appenderName, properties); 

			if (appender != null) {
				// Store the appender in our map, in case another logger in our configuration
				// makes a reference to it.
				if (appenders == null)
					appenders = new HashMap<String, Appender>();
				
				appenders.put(appenderName, appender);
			}
		}
		
		return appender;
	}

	/**
	 * Adds the given appender to the root logger.  (Legacy MicroLog configuration style)
	 * 
	 * @param string The appender class name or alias.
	 */
	private void addAppender(String string) {

		Logger rootLogger = loggerRepository.getRootLogger();
		String className = appenderAliases.get(string);

		if (className == null) {
			className = string;
		}

		try {
			Class<?> appenderClass = Class.forName(className);
			Appender appender = (Appender) appenderClass.newInstance();

			if (appender != null) {
				Log.i(TAG, "Adding appender " + appender.getClass().getName());
				rootLogger.addAppender(appender);
			}

		} catch (ClassNotFoundException e) {
			Log.e(TAG, "Failed to find appender class: " + e);
		} catch (IllegalAccessException e) {
			Log.e(TAG, "No access to appender class: " + e);
		} catch (InstantiationException e) {
			Log.e(TAG, "Failed to instantiate appender class: " + e);
		} catch (ClassCastException e) {
			Log.e(TAG, "Specified appender class does not implement the Appender interface: " + e);
		}
	}
	
	/**
	 * Check if the specified appender is an alias. If so the appender name is
	 * converted to fully qualified class name.
	 *  
	 * @param appenderName
	 * @return
	 */
	private String checkForAliasAppender(String appenderName) {
		return resolveAlias(appenderName, appenderAliases);
	}
	
	/**
	 * Check if this is an alias for a real formatter. 
	 * 
	 * @param formatterName
	 * @return
	 */
	private String checkForAliasFormatter(String formatterName) {
		return resolveAlias(formatterName, formatterAliases);
	}

	/**
	 * Configures using modern Log4j style syntax with full hierarchy support.
	 * 
	 * @param properties The properties to configure from.
	 */
	private void configureLog4jStyle(Properties properties) {
		loggerRepository.reset();
		String rootLoggerProperty = properties.getProperty(PropertyConfigurator.ROOT_LOGGER_KEY);
		doConfigureLogger(/*path*/null, rootLoggerProperty, properties);
		
		for (Entry<Object,Object> entry : properties.entrySet()) {
			String key = (String)entry.getKey();
			
			if (key.startsWith(LOGGER_PREFIX_KEY)) {
				// Strip off the leading characters.
				String path = key.substring(LOGGER_PREFIX_KEY.length() + 1);
				String value = (String)entry.getValue();
				doConfigureLogger(path, value, properties);
			}
		}
	}

	/**
	 * Configures using the legacy MicroLog style syntax.
	 * 
	 * @param properties The properties to configure from.
	 */
	private void configureSimpleStyle(Properties properties) {
		setLevel(properties);

		String appenderString = properties.getProperty(PropertyConfigurator.APPENDER_PREFIX_KEY, "LogCatAppender");
		List<String> appenderList = parseAppenderString(appenderString);
		setAppenders(appenderList);
		setFormatter(properties);
	}
	
	/**
	 * Creates a formatter for the specified appender.
	 * 
	 * @param appenderName The appender for which to configure a formatter.
	 * @param properties The properties to configure from.
	 * @return A newly created formatter or null.
	 */
	private Formatter createFormatter(String appenderName, Properties properties) {
		Formatter formatter = null;
		
		StringBuilder formatterKey = new StringBuilder(64);
		formatterKey.append(APPENDER_PREFIX_KEY);
		formatterKey.append(PREFIX_SEPARATOR);
		formatterKey.append(appenderName);
		formatterKey.append(PROPERTY_SEPARATOR);
		formatterKey.append(FORMATTER_PROPERTY);
		String formatterClassName = properties.getProperty(formatterKey.toString());
		
		if (formatterClassName == null) {
			Log.e(TAG, "No formatter class defined");
		} else {
			formatterClassName = checkForAliasFormatter(formatterClassName);
			
			try {
				Class<?> formatterClass = Class.forName(formatterClassName);
				formatter = (Formatter) formatterClass.newInstance();
			} catch (ClassNotFoundException e) {
				Log.e(TAG, "Could not find formatter class " + appenderName);
			} catch (InstantiationException e) {
				Log.e(TAG, "Could not instantiate formatter class " + appenderName);
			} catch (IllegalAccessException e) {
				Log.e(TAG, "Not allowed to create formatter class " + appenderName);
			} 
		}
		
		return formatter;
	}
	
	/**
	 * Create and configure the specified appender using the properties.
	 * 
	 * @param appenderName
	 * @param properties
	 * @return The appender or null
	 */
	private Appender doConfigureAppender(String appenderName, Properties properties) {
		Appender appender = createAppender(appenderName, properties);
		
		if (appender != null) {
			Formatter formatter = doConfigureFormatter(appenderName, properties);
			
			if (formatter != null) {
				appender.setFormatter(formatter);
			}
			
			String[] propertyNames = appender.getPropertyNames();
			
			if (propertyNames != null && propertyNames.length > 0) {
				StringBuilder propertyKeyBuffer = new StringBuilder(64);
				
				for (int i = 0; i < propertyNames.length; i++) {
					// Clear the buffer
					propertyKeyBuffer.delete(0, propertyKeyBuffer.length());
					
					// Build up the full property name
					propertyKeyBuffer.append(APPENDER_PREFIX_KEY);
					propertyKeyBuffer.append(PREFIX_SEPARATOR);
					
					if (appenderName != null) {
						propertyKeyBuffer.append(appenderName);
						propertyKeyBuffer.append(PROPERTY_SEPARATOR);
					}
					
					propertyKeyBuffer.append(propertyNames[i]);
					
					String value = properties.getProperty(propertyKeyBuffer.toString());
					
					if (value != null) {
						Log.i(TAG, "Setting property " + propertyNames[i] + "=" + value);
						appender.setProperty(propertyNames[i], value);
					}
				}
			}
		}
		
		return appender;
	}
	
	private Logger doConfigureLogger(String path, String settings, Properties properties) {
		Logger logger;
		
		if (path == null || path.length() == 0) {
			logger = loggerRepository.getRootLogger();
		} else {
			logger = loggerRepository.getLogger(path);
		}
		
		int endIndex = settings.indexOf(LOG4J_PROPERTY_DELIMITER);
		String levelString;
		
		if (endIndex == -1) {
			endIndex = settings.length();
			levelString = settings; 
		} else {
			levelString = settings.substring(0, endIndex);
		}
		
		Level level = stringToLevel(levelString);
		
		if (level == null) {
			Log.e(TAG, "Level " + levelString + " is not a valid level.");
		} else {
			logger.setLevel(level);
		}
		
		int beginIndex = endIndex + 1;
		
		while (beginIndex < settings.length()) {
			// Find the end of the current appender name.
			endIndex = settings.indexOf(LOG4J_PROPERTY_DELIMITER, beginIndex);
			
			if (endIndex == -1) {
				endIndex = settings.length();
			}
			
			// Get the appender name and update the position
			String appenderName = settings.substring(beginIndex, endIndex).trim();
			
			Appender appender = getAppender(appenderName, properties);
			
			if (appender != null) {
				Log.i(TAG, "Adding appender " + appender);
				logger.addAppender(appender);
			}
			
			// Update the current search start position
			beginIndex = endIndex + 1;			
		} 
		
		return logger;
	}
	
	private Formatter doConfigureFormatter(String appenderName, Properties properties) {
		// Create the formatter
		Formatter formatter = createFormatter(appenderName, properties);

		// If we we created it, configure it's supported properties
		if (formatter != null) {
			String[] formatterProperties = formatter.getPropertyNames();
			
			if (formatterProperties != null && formatterProperties.length > 0) {
				StringBuilder propertyKeyBuffer = new StringBuilder(64);
				
				for (int i = 0; i < formatterProperties.length; i++) {
					// Clear the buffer
					propertyKeyBuffer.delete(0, propertyKeyBuffer.length());
					
					// Build up the full property name
					propertyKeyBuffer.append(APPENDER_PREFIX_KEY);
					propertyKeyBuffer.append(PREFIX_SEPARATOR);
					propertyKeyBuffer.append(appenderName);
					propertyKeyBuffer.append(PROPERTY_SEPARATOR);
					propertyKeyBuffer.append(FORMATTER_PROPERTY);
					propertyKeyBuffer.append(PROPERTY_SEPARATOR);
					propertyKeyBuffer.append(formatterProperties[i]);
					
					String value = properties.getProperty(propertyKeyBuffer.toString());
					
					if (value != null) {
						Log.i(TAG, "Setting property " + formatterProperties[i] + "=" + value);
						formatter.setProperty(formatterProperties[i], value);
					}
				}
			}
		}
		
		return formatter;
	}
	
	/**
	 * Load the properties
	 * 
	 * @param inputStream
	 *            the {@link InputStream} to read from
	 * @return the {@link Properties} object containing the properties read from
	 *         the {@link InputStream}
	 * @throws IOException
	 *             if the loading fails.
	 */
	private Properties loadProperties(InputStream inputStream) throws IOException {
		Properties properties = new Properties();
		properties.load(inputStream);
		return properties;
	}
	
	private List<String> parseAppenderString(String appenderString) {
		StringTokenizer tokenizer = new StringTokenizer(appenderString, ";,");
		List<String> appenderList = new ArrayList<String>();

		while (tokenizer.hasMoreElements()) {
			String appender = (String) tokenizer.nextElement();
			appenderList.add(appender);
		}

		return appenderList;
	}

	/**
	 * Returns the class name for the specified name in the given map, or if there is no mapping
	 * the name itself since it wasn't an alias.
	 * 
	 * @param name The name to look for.
	 * @param aliases The map of aliases to real names.
	 * @return The resolved alias or the original name if it wasn't an alias.
	 */
	private String resolveAlias(String name, Map<String, String> aliases) {
		String className;
		String aliasedClassName = aliases.get(name);
		
		if (aliasedClassName == null) {
			className = name;
		} else {
			className = aliasedClassName;
		}
		
		return className;
	}
	
	private void setAppenders(List<String> appenderList) {
		for (String string : appenderList) {
			addAppender(string);
		}
	}

	/**
	 * Sets the formatter on all appenders attached to the root logger.
	 * 
	 * @param properties The properties to configure from.
	 */
	private void setFormatter(Properties properties) {

		String formatterString = (String) properties.getProperty(FORMATTER_PREFIX_KEY, "PatternFormatter");

		String className = null;

		if (formatterString != null) {
			className = formatterAliases.get(formatterString);
		}

		if (className == null) {
			className = formatterString;
		}

		try {
			Class<?> formatterClass = Class.forName(className);
			Formatter formatter = (Formatter) formatterClass.newInstance();
			
			// TODO Add property setup of the formatter.
			
			if(formatter != null){
				Logger rootLogger = loggerRepository.getRootLogger();
				
				int numberOfAppenders = rootLogger.getNumberOfAppenders();
				for(int appenderNo=0; appenderNo < numberOfAppenders; appenderNo++){
					Appender appender = rootLogger.getAppender(appenderNo);
					appender.setFormatter(formatter);
				}
			}
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "Failed to find Formatter class: " + e);
		} catch (InstantiationException e) {
			Log.e(TAG, "Failed to instantiate formtter: " + e);
		} catch (IllegalAccessException e) {
			Log.e(TAG, "No access to formatter class: " + e);
		} catch (ClassCastException e) {
			Log.e(TAG, "Specified formatter class does not implement the Formatter interface: " + e);
		}		
	}

	/**
	 * Sets the level using the legacy MicroLog style syntax.
	 * 
	 * @param properties The properties to configure from.
	 */
	private void setLevel(Properties properties) {
		String levelString = (String) properties.get(PropertyConfigurator.LOG_LEVEL_PREFIX_KEY);
		Level level = stringToLevel(levelString);

		if (level != null) {
			loggerRepository.getRootLogger().setLevel(level);
			Log.i(TAG, "Root level: " + loggerRepository.getRootLogger().getLevel());
		}
	}
	
	/**
	 * Start the configuration
	 * 
	 * @param properties
	 */
	private void startConfiguration(Properties properties) {

		if (properties.containsKey(PropertyConfigurator.ROOT_LOGGER_KEY)) {
			configureLog4jStyle(properties);
		} else {
			Log.i(TAG, "Configure using the simple style (aka classic style)");
			configureSimpleStyle(properties);
		}
	}
	
	/**
	 * Convert a <code>String</code> containing a level to a <code>Level</code>
	 * object.
	 * 
	 * @return the level that corresponds to the levelString if it was a valid
	 *         <code>String</code>, <code>null</code> otherwise.
	 */
	private Level stringToLevel(String levelString) {
		return Level.valueOf(levelString);
	}
}
