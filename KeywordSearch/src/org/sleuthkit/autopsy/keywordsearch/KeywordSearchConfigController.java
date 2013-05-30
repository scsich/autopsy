/*
 * Autopsy Forensic Browser
 *
 * Copyright 2013 Basis Technology Corp.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sleuthkit.autopsy.coreutils.ModuleSettings;
import org.sleuthkit.autopsy.coreutils.PlatformUtil;
import org.sleuthkit.autopsy.coreutils.StringExtract;

/**
 * Controller object to coordinate data flow between simple and advanced
 * configuration panels and also controls persistence of configuration data.
 */
public class KeywordSearchConfigController implements PropertyChangeListener {
    
    // constants for property change support
    public static final String SKIP_KNOWN = "skip_known";
    public static final String UPDATE_FREQUENCY = "update_frequency";
    public static final String SCRIPTS = "scripts";
    public static final String STRING_EXTRACT_OPTIONS = "string_extract_options";
    public static final String KEYWORD_LISTS = "keyword_lists";
    
    private String context;
    
    private boolean skipKnown;
    private KeywordSearchIngestModule.UpdateFrequency UpdateFreq;
    private List<StringExtract.StringExtractUnicodeTable.SCRIPT> stringExtractScripts;
    //private Map<String,String> stringExtractOptions;
    private EnumSet<AbstractFileExtract.ExtractOptions> stringExtractOptions;
    
    // Keyword search config data is not controlled by a single object but is
    // handled by these two classes.
    private KeywordSearchSettings keywordSearchSettings;
    private KeywordSearchListsAbstract keywordSearchLists;
    
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public KeywordSearchConfigController() {
        this(ModuleSettings.DEFAULT_CONTEXT);
    }

    public KeywordSearchConfigController(String context) {
        this.context = context;
        keywordSearchSettings = new KeywordSearchSettings(context);
        
        // create path to the XML file to store keyword lists
        String listsFilePath = PlatformUtil.getUserConfigDirectory() + File.separator + "keywords-" + context + ".xml";
        
        // and create a KeywordSearchListsAbstract object
        keywordSearchLists = new KeywordSearchListsXML(listsFilePath);
        keywordSearchLists.reload();
        
        // add this as listener of changes to all keyword lists
        List<KeywordSearchListsAbstract.KeywordSearchList> kwLists = keywordSearchLists.getListsL();
        for (KeywordSearchListsAbstract.KeywordSearchList keywordSearchList : kwLists) {
            keywordSearchList.addPropertyChangeListener(this);
        }
        
        // initialize data
        skipKnown = keywordSearchSettings.getSkipKnown();
        UpdateFreq = keywordSearchSettings.getUpdateFrequency();
        stringExtractScripts = keywordSearchSettings.getStringExtractScripts();
        stringExtractOptions = keywordSearchSettings.getStringExtractOptions();
    }
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    public boolean isSkipKnown() {
        return skipKnown;
    }

    public KeywordSearchIngestModule.UpdateFrequency getUpdateFreq() {
        return UpdateFreq;
    }

    public List<StringExtract.StringExtractUnicodeTable.SCRIPT> getStringExtractScripts() {
        return stringExtractScripts;
    }
    
    public boolean isStringExtractOptionSet(AbstractFileExtract.ExtractOptions extractOption) {
        return stringExtractOptions.contains(extractOption);
    }

    public Map<String, String> getStringExtractOptionsMap() {
        Map<String, String> optionsMap = new HashMap<>();
        optionsMap.put(AbstractFileExtract.ExtractOptions.EXTRACT_UTF8.toString(),
                Boolean.toString(stringExtractOptions.contains(AbstractFileExtract.ExtractOptions.EXTRACT_UTF8)));
        optionsMap.put(AbstractFileExtract.ExtractOptions.EXTRACT_UTF16.toString(),
                Boolean.toString(stringExtractOptions.contains(AbstractFileExtract.ExtractOptions.EXTRACT_UTF16)));
        return optionsMap;
    }
    
    public KeywordSearchListsAbstract.KeywordSearchList getKeywordSearchList(String listName) {
        return keywordSearchLists.getList(listName);
    }
    
    public List<KeywordSearchListsAbstract.KeywordSearchList> getKeywordSearchLists() {
        return keywordSearchLists.getListsL();
    }
    
    /**
     * If locked is true, returns only the locked lists. If false, returns only
     * the unlocked lists.
     * @param locked
     * @return 
     */
    public List<KeywordSearchListsAbstract.KeywordSearchList> getKeywordSearchLists(boolean  locked) {
        return keywordSearchLists.getListsL(locked);
    }
    
    public List<String> getKeywordSearchListNames() {
        return keywordSearchLists.getListNames();
    }
    
    public KeywordSearchListsAbstract.KeywordSearchList getListWithKeyword(String keyword) {
        return keywordSearchLists.getListWithKeyword(keyword);
    }

    public void setSkipKnown(boolean skipKnown) {
        boolean oldVal = this.skipKnown;
        this.skipKnown = skipKnown;
        pcs.firePropertyChange(SKIP_KNOWN, oldVal, skipKnown);
        
        // tell keywordSearchSettings
        keywordSearchSettings.setSkipKnown(skipKnown);
    }

    public void setUpdateFreq(KeywordSearchIngestModule.UpdateFrequency UpdateFreq) {
        KeywordSearchIngestModule.UpdateFrequency oldVal = this.UpdateFreq;
        this.UpdateFreq = UpdateFreq;
        pcs.firePropertyChange(UPDATE_FREQUENCY, oldVal, UpdateFreq);
        
        // tell keywordSearchSettings
        keywordSearchSettings.setUpdateFrequency(UpdateFreq);
    }

    public void setStringExtractScripts(List<StringExtract.StringExtractUnicodeTable.SCRIPT> stringExtractScripts) {
        List<StringExtract.StringExtractUnicodeTable.SCRIPT> oldVal = this.stringExtractScripts;
        this.stringExtractScripts = new ArrayList<>(stringExtractScripts);
        pcs.firePropertyChange(SCRIPTS, oldVal, stringExtractScripts);
        
        // tell keywordSearchSettings
        keywordSearchSettings.setStringExtractScripts(stringExtractScripts);
    }
    
    public void setStringExtractOption(AbstractFileExtract.ExtractOptions extractOption, boolean isSet) {
        //stringExtractOptions.put(key, val);
        boolean wasSet = stringExtractOptions.contains(extractOption);
        
        if (isSet == wasSet) {
            // nothing to change
            return;
        }
        
        // mutate our copy
        if (isSet) {
            stringExtractOptions.add(extractOption);
        } else {
            stringExtractOptions.remove(extractOption);
        }
        
        // tell keywordSearchSettings
        keywordSearchSettings.setStringExtractOption(extractOption, isSet);
        
        pcs.firePropertyChange(STRING_EXTRACT_OPTIONS, wasSet, isSet);
    }
    
    public void addKeywordList(KeywordSearchListsAbstract.KeywordSearchList list) {
        keywordSearchLists.addList(list);
        keywordSearchLists.save();
        list.addPropertyChangeListener(this);
        pcs.firePropertyChange(KEYWORD_LISTS, null, list);
    }
    
    public void addKeywordList(String name, List<Keyword> newList) {
        keywordSearchLists.addList(name, newList);
        keywordSearchLists.save();
        KeywordSearchListsAbstract.KeywordSearchList list = keywordSearchLists.getList(name);
        list.addPropertyChangeListener(this);
        pcs.firePropertyChange(KEYWORD_LISTS, null, name);
    }
    
    public void removeKeywordList(String name) {
        KeywordSearchListsAbstract.KeywordSearchList list = keywordSearchLists.getList(name);
        list.removePropertyChangeListener(this);
        keywordSearchLists.deleteList(name);
        keywordSearchLists.save();
        pcs.firePropertyChange(KEYWORD_LISTS, name, null);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // the only changes that we should be listening for here are changes to
        // to the keyword lists
        keywordSearchLists.save();
        pcs.firePropertyChange(KEYWORD_LISTS, 0, 1);
    }
}
