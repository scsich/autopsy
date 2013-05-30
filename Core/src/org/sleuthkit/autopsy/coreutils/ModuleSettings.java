/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sleuthkit.autopsy.coreutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

/**
 * This class contains the framework to read, add, update, and remove
 * from the property files located at %USERDIR%/Config/x.properties
 */
public class ModuleSettings {
    
    private static Logger logger = Logger.getLogger(ModuleSettings.class.getName());

    // The directory where the properties file is lcoated
    private final static String moduleDirPath = PlatformUtil.getUserConfigDirectory();
    public static final String DEFAULT_CONTEXT = "General";
    public static final String MAIN_SETTINGS = "Case";
    
    private File settingsFile;

    public ModuleSettings(String moduleName) {
        this(moduleName, DEFAULT_CONTEXT);
    }

    public ModuleSettings(String moduleName, String context) {
        String configFileName = moduleName + "-" + context + ".properties";
        settingsFile = new File(moduleDirPath, configFileName);
    }

    /**
     * Makes a new config file if it does not already exist.
     */
    private void makeConfigFile() {
        if (settingsFile.exists()) {
            return;
        }
            
        //File propPath = new File(moduleDirPath + File.separator + moduleName + ".properties");
        File parent = new File(settingsFile.getParent());
        if (!parent.exists()) {
            parent.mkdirs();
        }
        Properties props = new Properties();
        try {
            settingsFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(settingsFile);
            props.store(fos, "");
            fos.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Was not able to create a new properties file.", e);
        }
    }
   
    /**
     * Returns the setting value with the given setting name, or null if there
     * isn't one.
     * @param settingName - The setting name to retrieve. 
     * @return - the value associated with the setting.
     */
    public String getConfigSetting(String settingName) {
        
        // get the Properties object
        Properties properties = null;
        try {
            properties = fetchProperties();
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Attempted to retrieve a configuration setting for which there is not configuration file.");
            return null;
        }
        
        return properties.getProperty(settingName);
    }
    
    /**
     * Returns the given properties file's map of settings.
     * @param moduleName - the name of the config file to read from.
     * @return - the map of all key:value pairs representing the settings of the config.
     * @throws IOException 
     */
    public Map< String, String> getConfigSettings() {
        
        Map<String, String> settings = new HashMap<>();
        
        // get the Properties object
        Properties properties = null;
        try {
            properties = fetchProperties();
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Attempted to retrieve a configuration setting for which there is not configuration file.");
            return settings;
        }
        
        Set<String> keys = properties.stringPropertyNames();
        for (String s : keys) {
            settings.put(s, properties.getProperty(s));
        }
        
        return settings;
    }

    /**
     * Sets the given properties file to the given setting map.
     * @param moduleName - The name of the module to be written to.
     * @param settings - The mapping of all key:value pairs of settings to add to the config.
     */
    public synchronized void setConfigSettings(Map<String, String> settings) {
        
        Properties props = null;
        try {
            props = fetchProperties();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Exception while creating Properties object.");
            return;
        }
        
        for (Map.Entry<String, String> kvp : settings.entrySet()) {
            props.setProperty(kvp.getKey(), kvp.getValue());
        }

        try {
            FileOutputStream fos = new FileOutputStream(settingsFile);
            props.store(fos, "Changed config settings(batch)");
            fos.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "There was a problem writing to the properties file: " + settingsFile, e);
            return;
        }
    }
    
    /**
     * Sets the given properties file to the given settings.
     * @param moduleName - The name of the module to be written to.
     * @param settingName - The name of the setting to be modified.
     * @param settingVal - the value to set the setting to.
     */
    public synchronized void setConfigSetting(String settingName, String settingVal) {
        
        Properties props = null;
        try {
            props = fetchProperties();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Exception while creating Properties object.");
            return;
        }
        
        props.setProperty(settingName, settingVal);

        try {
            FileOutputStream fos = new FileOutputStream(settingsFile);
            props.store(fos, "Changed config settings(single)");
            fos.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "There was a problem writing to the properties file: " + settingsFile, e);
            return;
        }
    }

    /**
     * Removes the given key from the given properties file.
     * @param moduleName - The name of the properties file to be modified.
     * @param key - the name of the key to remove.
     */
    public synchronized void removeProperty(String key){
        
        Properties props = null;
        try {
            props = fetchProperties();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Exception while creating Properties object.");
        }
        
        props.remove(key);

        try {
            FileOutputStream fos = new FileOutputStream(settingsFile);
            props.store(fos, "Removed " + key);
            fos.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not remove property from property file.", e);
        }
    }
    
    /**
     * Returns the properties file as specified by moduleName. 
     * @param moduleName
     * @return Properties file as specified by moduleName.
     * @throws IOException 
     */
    private Properties fetchProperties() throws IOException {
        makeConfigFile();
        InputStream inputStream = new FileInputStream(settingsFile);
        Properties props = new Properties();
        props.load(inputStream);
        inputStream.close();
        return props;
    }
}
