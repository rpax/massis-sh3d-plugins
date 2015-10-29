/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rpax.massis.sweethome3d.metadata;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.Wall;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import static rpax.massis.sweethome3d.metadata.HomeMetadataLoader.ID_KEY;

/**
 *
 * @author Rafael Pax
 */
public class BuildingMetadataManager {

    private final HashMap<Selectable, HashMap<String, String>> metaMap;
    private int current_max_id;
    private final Home originalHome;

    public BuildingMetadataManager(Home home)
    {
        this.metaMap = new HashMap<>();
        this.current_max_id = 0;
        this.originalHome = home;
    }

    public BuildingMetadataManager(Home home, BuildingMetadata metadata)
    {
        this.metaMap = new HashMap<>();
        this.current_max_id = 0;
        this.originalHome = home;
        Wall[] walls = home.getWalls().toArray(new Wall[0]);
        Room[] rooms = home.getRooms().toArray(new Room[0]);
        HomePieceOfFurniture[] furniture = home.getFurniture().toArray(
                new HomePieceOfFurniture[0]);
        for (int i = 0; i < walls.length; i++)
        {
            this.addMetaData(walls[i], metadata.getWalls().get(i));
        }
        for (int i = 0; i < rooms.length; i++)
        {
            this.addMetaData(rooms[i], metadata.getRooms().get(i));
        }
        for (int i = 0; i < furniture.length; i++)
        {
            this.addMetaData(furniture[i], metadata.getFurniture().get(i));
        }
        //retrieve current_max_id
        for (int i = 0; i < walls.length; i++)
        {
            this.current_max_id = Math.max(Integer.parseInt(this.getMetadata(
                    walls[i]).get(ID_KEY)), current_max_id);
        }
        for (int i = 0; i < rooms.length; i++)
        {
            this.current_max_id = Math.max(Integer.parseInt(this.getMetadata(
                    rooms[i]).get(ID_KEY)), current_max_id);
        }
        for (int i = 0; i < furniture.length; i++)
        {
            this.current_max_id = Math.max(Integer.parseInt(this.getMetadata(
                    furniture[i]).get(ID_KEY)), current_max_id);
        }
        System.err.println("current-max-id: " + current_max_id);
    }

    public final Map<String, String> getMetadata(Selectable element)
    {
        Objects.requireNonNull(element);
        return getOrCreateMetadata(element);
    }

    public void removeMetadata(Selectable element, String key)
    {
        Objects.requireNonNull(element);
        HashMap<String, String> metadata = getOrCreateMetadata(element);
        metadata.remove(key);
        syncWithDescription(element, metadata);
    }

    public void setMetaData(Selectable element, String key, String value)
    {
        Objects.requireNonNull(element);
        HashMap<String, String> metadata = getOrCreateMetadata(element);
        metadata.put(key, value);
        syncWithDescription(element, metadata);
    }

    public void setMetaData(Selectable element, Map<String, String> new_metadata)
    {
        Objects.requireNonNull(element);
        HashMap<String, String> metadata = new HashMap<>(new_metadata);
        this.metaMap.put(element, metadata);
        syncWithDescription(element, metadata);
    }

    public final void addMetaData(Selectable element,
            Map<String, String> new_metadata)
    {

        Objects.requireNonNull(element);
        HashMap<String, String> metadata = getOrCreateMetadata(element);
        metadata.putAll(new_metadata);
        syncWithDescription(element, metadata);
    }

    private HashMap<String, String> getOrCreateMetadata(Selectable element)
    {
        if (!this.metaMap.containsKey(element))
        {
            HashMap<String, String> metadata = new HashMap<>();
            if (element instanceof HomePieceOfFurniture)
            {
                HomePieceOfFurniture hpof = (HomePieceOfFurniture) element;
                try
                {
                    if (hpof.getDescription() != null && !"".equals(
                            hpof.getDescription()))
                    {
                        metadata = new Gson().fromJson(hpof.getDescription(),
                                HashMap.class);
                    }
                } catch (Exception e)
                {
                    //wrong format. 
                    metadata = new HashMap<>();
                }
            }
            metadata.put(ID_KEY, String.valueOf(getID()));
            this.metaMap.put(element, metadata);
            syncWithDescription(element, metadata);
        }
        return this.metaMap.get(element);
    }

    private void syncWithDescription(Selectable element,
            HashMap<String, String> metadata)
    {
        if (element instanceof HomePieceOfFurniture)
        {
            final HomePieceOfFurniture hpof = (HomePieceOfFurniture) element;
            hpof.setDescription(new Gson().toJson(metadata));
        }
    }

    protected BuildingMetadata getBuildingData()
    {
        //ensure everything has metadata
        BuildingMetadata buildingMetadata = new BuildingMetadata();

        //si no estan en reference se quitan.
        ArrayList<Map<String, String>> wallsMeta = new ArrayList<>();
        ArrayList<Map<String, String>> roomsMeta = new ArrayList<>();
        ArrayList<Map<String, String>> hpofMeta = new ArrayList<>();

        for (Wall item : this.originalHome.getWalls())
        {
            wallsMeta.add(getMetadata(item));
        }
        for (Room item : this.originalHome.getRooms())
        {
            roomsMeta.add(getMetadata(item));
        }
        for (HomePieceOfFurniture item : this.originalHome.getFurniture())
        {
            hpofMeta.add(getMetadata(item));
        }
        buildingMetadata.setFurniture(hpofMeta);
        buildingMetadata.setWalls(wallsMeta);
        buildingMetadata.setRooms(roomsMeta);
        return buildingMetadata;
    }

    /**
     *
     * @return una id unica, incremental
     */
    private synchronized Integer getID()
    {
        return ++this.current_max_id;
    }
}
