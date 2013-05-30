/*
 * Autopsy Forensic Browser
 *
 * Copyright 2012 Basis Technology Corp.
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

package org.sleuthkit.autopsy.keywordsearch;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.coreutils.ModuleSettings;
import org.sleuthkit.autopsy.coreutils.StringExtract;
import org.sleuthkit.autopsy.coreutils.StringExtract.StringExtractUnicodeTable.SCRIPT;
import org.sleuthkit.autopsy.keywordsearch.KeywordSearchIngestModule.UpdateFrequency;

/**
 * This file contains some constants and settings for KeywordSearch.
 */
public class KeywordSearchSettings {
    
    private static final Logger logger = Logger.getLogger(KeywordSearchSettings.class.getName());
    
    public static final String MODULE_NAME = "KeywordSearch";
    static final String PROPERTIES_OPTIONS = MODULE_NAME + "_Options";
    static final String PROPERTIES_NSRL = MODULE_NAME + "_NSRL";
    static final String PROPERTIES_SCRIPTS = MODULE_NAME + "_Scripts";

    private boolean skipKnown;
    private UpdateFrequency UpdateFreq;
    private List<StringExtract.StringExtractUnicodeTable.SCRIPT> stringExtractScripts;
    private EnumSet<AbstractFileExtract.ExtractOptions> stringExtractOptions;
    
    private String context;
    private PropertyChangeSupport pcs;
    private ModuleSettings generalSettings;
    private ModuleSettings nsrlSettings;
    private ModuleSettings scriptSettings;

    /**
     * Creates a KeywordSearchSettings object with default context.
     */
    public KeywordSearchSettings() {
        this(ModuleSettings.DEFAULT_CONTEXT);
    }

    /**
     * @param context The context for these settings. Objects with different
     * contexts will have their settings stored separately from each other.
     */
    public KeywordSearchSettings(String context) {
        this.context = context;
        generalSettings = new ModuleSettings(PROPERTIES_OPTIONS, context);
        nsrlSettings = new ModuleSettings(PROPERTIES_NSRL, context);
        scriptSettings = new ModuleSettings(PROPERTIES_SCRIPTS, context);
        pcs = new PropertyChangeSupport(this);
        setDefaults();
    }
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }
           
    /**
     * Gets the update Frequency from  KeywordSearch_Options.properties
     * @return KeywordSearchIngestModule's update frequency
     */ 
    public UpdateFrequency getUpdateFrequency() {
        return UpdateFreq;
    }

    /**
     * Sets the update frequency and writes to KeywordSearch_Options.properties
     * @param freq Sets KeywordSearchIngestModule to this value.
     */
    void setUpdateFrequency(UpdateFrequency freq){
        generalSettings.setConfigSetting("UpdateFrequency", freq.name());
        UpdateFreq = freq;
    }
    
    /**
     * Sets whether or not to skip adding known good files to the search during index.
     * @param skip 
     */
    void setSkipKnown(boolean skip) {
        nsrlSettings.setConfigSetting("SkipKnown", Boolean.toString(skip));
        skipKnown = skip;
    }
    
   /**
     * Gets the setting for whether or not this ingest is skipping adding known good files to the index.
     * @return skip setting
     */
    boolean getSkipKnown() {
        return skipKnown;
    }

    /**
     * Sets what scripts to extract during ingest
     * @param scripts List of scripts to extract
     */
    void setStringExtractScripts(List<StringExtract.StringExtractUnicodeTable.SCRIPT> scripts) {
        stringExtractScripts.clear();
        stringExtractScripts.addAll(scripts);
        
        //Disabling scripts that weren't selected
        for(String s : scriptSettings.getConfigSettings().keySet()){
            if (! scripts.contains(StringExtract.StringExtractUnicodeTable.SCRIPT.valueOf(s))){
                scriptSettings.setConfigSetting(s, "false");
            }
        }
        //Writing and enabling selected scripts
        for(StringExtract.StringExtractUnicodeTable.SCRIPT s : stringExtractScripts){
            scriptSettings.setConfigSetting(s.name(), "true");
        }
    }
    
    void setStringExtractOption(AbstractFileExtract.ExtractOptions extractOption, boolean isSet) {
        stringExtractOptions.add(extractOption);
        generalSettings.setConfigSetting(extractOption.toString(), Boolean.toString(isSet));
    }

    /**
     * gets the currently set scripts to use
     *
     * @return the list of currently used script
     */
    List<SCRIPT> getStringExtractScripts() {
        return Collections.unmodifiableList(stringExtractScripts);
    }
    
    EnumSet<AbstractFileExtract.ExtractOptions> getStringExtractOptions() {
        return EnumSet.copyOf(stringExtractOptions);
    }

    boolean isStringExtractOptionSet(AbstractFileExtract.ExtractOptions option) {
        return stringExtractOptions.contains(option);
    }

    /**
     * Sets the default values of the KeywordSearch properties files if none already exist.
     */
    private void setDefaults() {
        // set default values only if a value has not already been set.
        
        // skipKnown
        String skipKnownStr = nsrlSettings.getConfigSetting("SkipKnown");
        if (skipKnownStr == null) {
            skipKnownStr = Boolean.toString(true);
            nsrlSettings.setConfigSetting("SkipKnown", skipKnownStr);
        }
        skipKnown = Boolean.parseBoolean(skipKnownStr);

        // update frequency
        String updateFreqStr = generalSettings.getConfigSetting("UpdateFrequency");
        if (updateFreqStr == null) {
            updateFreqStr = UpdateFrequency.AVG.toString();
            generalSettings.setConfigSetting("UpdateFrequency", updateFreqStr);
        }
        UpdateFreq = UpdateFrequency.valueOf(updateFreqStr);
        
        // extract scripts
        Map<String, String> scriptSettingsMap = scriptSettings.getConfigSettings();
        if (scriptSettingsMap == null || scriptSettingsMap.isEmpty()) {
            if (scriptSettingsMap == null) {
                scriptSettingsMap = new HashMap<>();
            }
            scriptSettingsMap.put(SCRIPT.LATIN_1.name(), Boolean.toString(true));
            scriptSettings.setConfigSettings(scriptSettingsMap);
        }
        stringExtractScripts = new ArrayList<>();
        for (Map.Entry<String, String> entry : scriptSettingsMap.entrySet()) {
            boolean scriptIsSet = Boolean.parseBoolean(entry.getValue());
            if (scriptIsSet) {
                stringExtractScripts.add(SCRIPT.valueOf(entry.getKey()));
            }
        }
        
        // extract options
        stringExtractOptions = EnumSet.noneOf(AbstractFileExtract.ExtractOptions.class);
        String utf8 = generalSettings.getConfigSetting(AbstractFileExtract.ExtractOptions.EXTRACT_UTF8.toString());
        if (utf8 == null) {
            generalSettings.setConfigSetting(AbstractFileExtract.ExtractOptions.EXTRACT_UTF8.toString(), Boolean.FALSE.toString());
        } else {
            stringExtractOptions.add(AbstractFileExtract.ExtractOptions.EXTRACT_UTF8);
        }
        String utf16 = generalSettings.getConfigSetting(AbstractFileExtract.ExtractOptions.EXTRACT_UTF16.toString());
        if (utf16 == null) {
            generalSettings.setConfigSetting(AbstractFileExtract.ExtractOptions.EXTRACT_UTF16.toString(), Boolean.FALSE.toString());
        } else {
            stringExtractOptions.add(AbstractFileExtract.ExtractOptions.EXTRACT_UTF16);
        }
    }
}
