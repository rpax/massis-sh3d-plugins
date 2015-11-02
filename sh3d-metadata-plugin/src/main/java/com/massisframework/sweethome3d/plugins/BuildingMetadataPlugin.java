/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.massisframework.sweethome3d.plugins;

import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;

/**
 *
 * @author Rafael Pax
 */
public class BuildingMetadataPlugin extends Plugin {

    @Override
    public PluginAction[] getActions()
    {
        return new PluginAction[]{new BuildingMetadataPluginAction(this)};
    }
    
}
