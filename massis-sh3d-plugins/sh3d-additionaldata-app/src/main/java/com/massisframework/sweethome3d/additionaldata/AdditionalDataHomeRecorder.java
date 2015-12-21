/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.massisframework.sweethome3d.additionaldata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 *
 * @author Rafael Pax
 */
public class AdditionalDataHomeRecorder extends HomeFileRecorder {

    private final List<? extends AdditionalDataReader> additionalDataReaders;
    private final List<? extends AdditionalDataWriter> additionalDataWriters;
    private final HomeApplication application;

    /**
     * Creates a home recorder able to write and read homes in uncompressed
     * files.
     */
    public AdditionalDataHomeRecorder()
    {
        this(0);
    }

    /**
     * Creates a home recorder able to write and read homes in files compressed
     * at a level from 0 to 9.
     *
     * @param compressionLevel 0 (uncompressed) to 9 (compressed).
     */
    public AdditionalDataHomeRecorder(int compressionLevel)
    {
        this(compressionLevel, false);
    }

    /**
     * Creates a home recorder able to write and read homes in files compressed
     * at a level from 0 to 9.
     *
     * @param compressionLevel 0-9
     * @param includeOnlyTemporaryContent if <code>true</code>, content
     * instances of <code>TemporaryURLContent</code> class referenced by the
     * saved home as well as the content previously saved with it will be
     * written. If <code>false</code>, all the content instances referenced by
     * the saved home will be written in the zip stream.
     */
    public AdditionalDataHomeRecorder(int compressionLevel,
            boolean includeOnlyTemporaryContent)
    {
        this(compressionLevel, includeOnlyTemporaryContent, null, false,
                new ArrayList<AdditionalDataReader>(),
                new ArrayList<AdditionalDataWriter>());
    }

    public AdditionalDataHomeRecorder(
            List<? extends AdditionalDataReader> additionalDataReaders,
            List<? extends AdditionalDataWriter> additionalDataWriters)
    {
        this(0, false, null, false,
                additionalDataReaders,
                additionalDataWriters);
    }

    public AdditionalDataHomeRecorder(AdditionalDataReader additionalDataReader,
            AdditionalDataWriter additionalDataWriter)
    {
        this(Arrays.asList(additionalDataReader), Arrays.asList(
                additionalDataWriter));
    }

    public AdditionalDataHomeRecorder(AdditionalDataReader additionalDataReader)
    {
        this(0, false, null, false,
                Arrays.asList(additionalDataReader),
                new ArrayList<AdditionalDataWriter>());
    }

    public AdditionalDataHomeRecorder(AdditionalDataWriter additionalDataWriter)
    {
        this(0, false, null, false,
                new ArrayList<AdditionalDataReader>(),
                Arrays.asList(additionalDataWriter));
    }

    public AdditionalDataHomeRecorder(int compressionLevel,
            boolean includeOnlyTemporaryContent,
            UserPreferences preferences,
            boolean preferPreferencesContent,
            List<? extends AdditionalDataReader> additionalDataReaders,
            List<? extends AdditionalDataWriter> additionalDataWriters)
    {
        this(compressionLevel, includeOnlyTemporaryContent, preferences,
                preferPreferencesContent, additionalDataReaders,
                additionalDataWriters, null);
    }

    public AdditionalDataHomeRecorder(int compressionLevel,
            boolean includeOnlyTemporaryContent,
            UserPreferences preferences,
            boolean preferPreferencesContent,
            List<? extends AdditionalDataReader> additionalDataReaders,
            List<? extends AdditionalDataWriter> additionalDataWriters,
            HomeApplication application)
    {
        super(compressionLevel, includeOnlyTemporaryContent, preferences,
                preferPreferencesContent);
        this.additionalDataReaders = additionalDataReaders;
        this.additionalDataWriters = additionalDataWriters;
        this.application = application;
    }

    @Override
    public void writeHome(Home home, String name) throws RecorderException
    {
        super.writeHome(home, name);
        final File homeFile = new File(name);
        for (final AdditionalDataWriter additionalDataWriter : this.additionalDataWriters)
        {
            try
            {
                additionalDataWriter.writeAdditionalData(home, homeFile);
            } catch (final IOException ex)
            {
                throw new RecorderException("Failed to write additional data.",
                        ex);
            }
        }

    }

    @Override
    public Home readHome(String name) throws RecorderException
    {

        //restore metadata from home
        final Home home = super.readHome(name);
        final File homeFile = new File(name);
        try
        {
            for (final AdditionalDataReader additionalDataReader : this.additionalDataReaders)
            {
                additionalDataReader.readAdditionalData(this.application, home,
                        homeFile);
            }
            return home;
        } catch (final IOException ex)
        {
            throw new RecorderException("Error when loading additional data", ex);
        }
    }
}
