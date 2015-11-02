/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.massisframework.sweethome3d.metadata;

import com.massisframework.sweethome3d.additionaldata.AdditionalDataWriter;
import com.massisframework.sweethome3d.additionaldata.AdditionalDataReader;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 *
 * @author Rafael Pax
 */
public class HomeMetadataLoader implements AdditionalDataReader, AdditionalDataWriter {

    protected static final String METADATA_FOLDER_NAME;
    protected static final String METADATA_FILENAME;
    protected static final String ID_KEY;

    static
    {
        ResourceBundle resource = ResourceBundle.getBundle(
                HomeMetadataLoader.class.getName());
        METADATA_FOLDER_NAME = resource.getString("METADATA_FOLDER_NAME");
        METADATA_FILENAME = resource.getString("METADATA_FILENAME");
        ID_KEY = resource.getString("ID_KEY");
    }
    private static String HOME_UUID_KEY = UUID.randomUUID().toString();
    private static HashMap<String, BuildingMetadataManager> managers = new HashMap<>();

    @Override
    public void readAdditionalData(final HomeApplication application, Home home,
            File homeFile) throws IOException
    {
        final String homeUUID = UUID.randomUUID().toString();
        home.setVisualProperty(HOME_UUID_KEY, homeUUID);
        try (FileSystem fs = FileSystems.newFileSystem(URI.create(
                "jar:" + homeFile.toURI()), new HashMap<String, String>()))
        {
            //We have to create a new OutputStream and write the metadata to it
            Path metadataPath = fs.getPath(METADATA_FOLDER_NAME,
                    METADATA_FILENAME);
            if (Files.exists(metadataPath))
            {
                try (InputStreamReader reader = new InputStreamReader(
                        Files.newInputStream(metadataPath,
                        StandardOpenOption.READ)))
                {
                    final BuildingMetadata metadata = new Gson().fromJson(reader,
                            BuildingMetadata.class);
                    if (application != null)
                    {
                        //was loaded from application. instead of adding it directly,
                        // we wait until is fully loaded
                        application.addHomesListener(
                                new CollectionListener<Home>() {
                            @Override
                            public void collectionChanged(
                                    CollectionEvent<Home> ev)
                            {
                                final Home _home = ev.getItem();
                                switch (ev.getType())
                                {
                                    case ADD:
                                        if (homeUUID.equals(
                                                _home.getVisualProperty(
                                                HOME_UUID_KEY)))
                                        {

                                            application.removeHomesListener(this);
                                            managers.put(homeUUID,
                                                    new BuildingMetadataManager(
                                                    _home,
                                                    metadata));
                                        }
                                        break;
                                    case DELETE:
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });

                    } else
                    {
                        managers.put(homeUUID, new BuildingMetadataManager(home,
                                metadata));
                    }
                }
                System.err.println("Metadata is present. Home: " + home);
            } else
            {
                managers.put(homeUUID, new BuildingMetadataManager(home));
            }
        }

    }

    @Override
    public void writeAdditionalData(Home home, File homeFile) throws IOException
    {
        try (FileSystem fs = FileSystems.newFileSystem(URI.create(
                "jar:" + homeFile.toURI()), new HashMap<String, String>()))
        {
            //We have to create a new OutputStream and write the metadata to it
            Path metadataPath = fs.getPath(METADATA_FOLDER_NAME,
                    METADATA_FILENAME);
            //ensure parent folders are created
            Files.createDirectories(metadataPath.getParent());
            // get output Stream
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    Files.newOutputStream(metadataPath,
                    StandardOpenOption.CREATE)))
            {

                BuildingMetadata buildingData = getBuildingMetadata(home);
                new Gson().toJson(buildingData, writer);
            }
        }
    }

    private static BuildingMetadata getBuildingMetadata(Home home)
    {
        return getBuildingMetadataManager(home).getBuildingData();

    }

    public static BuildingMetadataManager getBuildingMetadataManager(Home home)
    {
        String homeUUID = (String) home.getVisualProperty(HOME_UUID_KEY);
        if (homeUUID == null)
        {
            homeUUID = UUID.randomUUID().toString();
            home.setVisualProperty(HOME_UUID_KEY, homeUUID);
            managers.put(homeUUID, new BuildingMetadataManager(home));
            System.err.println("Added manager for home with id " + homeUUID);
        }
        System.err.println("Retrieving manager for home with id " + homeUUID);
        return managers.get(homeUUID);

    }
}
