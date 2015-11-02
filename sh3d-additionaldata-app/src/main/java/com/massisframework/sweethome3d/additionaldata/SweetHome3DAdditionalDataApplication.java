/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.massisframework.sweethome3d.additionaldata;

import com.eteks.sweethome3d.SweetHome3D;
import com.eteks.sweethome3d.io.FileUserPreferences;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.DynamicPluginManager;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginManager;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rafael Pax
 */
public class SweetHome3DAdditionalDataApplication extends SweetHome3D {

    /*
     * Harcoded in parent, too
     */
    private static final String APPLICATION_PLUGINS_SUB_FOLDER = "plugins";
    private HomeRecorder additionalDataHomeRecorder;
    private HomeRecorder additionalDataCompressedHomeRecorder;
    private final List<? extends AdditionalDataReader> additionalDataReaders;
    private final List<? extends AdditionalDataWriter> additionalDataWriters;
    private final List<Class<? extends Plugin>> dynamicPlugins;
    private boolean pluginManagerInitialized = false;
    public static void run(
            String[] args,
            List<? extends AdditionalDataReader> additionalDataReaders,
            List<? extends AdditionalDataWriter> additionalDataWriters,
            List<Class<? extends Plugin>> dynamicPlugins)
    {
        new SweetHome3DAdditionalDataApplication(additionalDataReaders,
                additionalDataWriters, dynamicPlugins).init(args);
    }

    public static void main(String[] args)
    {
        new SweetHome3DAdditionalDataApplication().init(args);
    }

    public SweetHome3DAdditionalDataApplication(
            List<? extends AdditionalDataReader> additionalDataReaders,
            List<? extends AdditionalDataWriter> additionalDataWriters,
            List<Class<? extends Plugin>> dynamicPlugins)
    {
        super();
        this.additionalDataReaders = additionalDataReaders;
        this.additionalDataWriters = additionalDataWriters;
        this.dynamicPlugins = dynamicPlugins;

    }

    public SweetHome3DAdditionalDataApplication()
    {
        super();
        this.additionalDataReaders = Arrays.asList(
                new AdditionalDataReaderAdapter());
        this.additionalDataWriters = Arrays.asList(
                new AdditionalDataWriterAdapter());
        this.dynamicPlugins = Collections.emptyList();
    }

    @Override
    protected PluginManager getPluginManager()
    {

        if (!this.pluginManagerInitialized)
        {

            try
            {
                UserPreferences userPreferences = getUserPreferences();
                if (userPreferences instanceof FileUserPreferences)
                {
                    File[] applicationPluginsFolders = ((FileUserPreferences) userPreferences)
                            .getApplicationSubfolders(
                            APPLICATION_PLUGINS_SUB_FOLDER);

                    //Access parent pluginManager by reflection
                    Field f = SweetHome3D.class.getDeclaredField("pluginManager");
                    f.setAccessible(true);

                    f.set(this, new DynamicPluginManager(
                            applicationPluginsFolders, this.dynamicPlugins));
                }
            } catch (IOException ex)
            {
            } catch (NoSuchFieldException | SecurityException ex)
            {
                Logger.getLogger(
                        SweetHome3DAdditionalDataApplication.class.getName()).log(
                        Level.SEVERE,
                        null, ex);
            } catch (IllegalArgumentException | IllegalAccessException ex)
            {
                Logger.getLogger(
                        SweetHome3DAdditionalDataApplication.class.getName()).log(
                        Level.SEVERE,
                        null, ex);
            }
            this.pluginManagerInitialized = true;
        }
        return getPluginManagerFieldValue();
    }

    private PluginManager getPluginManagerFieldValue()
    {
        try
        {
            Field f = SweetHome3D.class.getDeclaredField("pluginManager");
            f.setAccessible(true);
            return (PluginManager) f.get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex)
        {
            Logger.getLogger(
                    SweetHome3DAdditionalDataApplication.class.getName())
                    .log(Level.SEVERE,
                    null, ex);
        }
        return null;
    }

    @Override
    public HomeRecorder getHomeRecorder()
    {
        if (this.additionalDataHomeRecorder == null)
        {
            this.additionalDataHomeRecorder =
                    new AdditionalDataHomeRecorder(0, false,
                    getUserPreferences(),
                    false,
                    additionalDataReaders,
                    additionalDataWriters, this);
        }
        return this.additionalDataHomeRecorder;
    }

    public HomeRecorder getHomeCompressedRecorder()
    {
        if (this.additionalDataCompressedHomeRecorder == null)
        {
            this.additionalDataCompressedHomeRecorder = new AdditionalDataHomeRecorder(
                    9,
                    false, getUserPreferences(), false,
                    additionalDataReaders,
                    additionalDataWriters, this);
        }
        return this.additionalDataCompressedHomeRecorder;
    }

    @Override
    public HomeRecorder getHomeRecorder(HomeRecorder.Type type)
    {
        switch (type)
        {
            case DEFAULT:
                return getHomeRecorder();
            case COMPRESSED:
                return getHomeCompressedRecorder();
            default:
                throw new UnsupportedOperationException();
        }
    }

    
}
