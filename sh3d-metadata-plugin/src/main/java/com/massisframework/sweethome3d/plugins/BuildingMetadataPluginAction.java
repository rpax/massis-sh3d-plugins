/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.massisframework.sweethome3d.plugins;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.massisframework.sweethome3d.metadata.BuildingMetadataManager;
import com.massisframework.sweethome3d.metadata.HomeMetadataLoader;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;



/**
 *
 * @author Rafael Pax
 */
public class BuildingMetadataPluginAction extends PluginAction {

    private final BuildingMetadataPlugin plugin;
    private List<Selectable> selectedItems = Collections.emptyList();

    public BuildingMetadataPluginAction(final BuildingMetadataPlugin plugin)
    {
        super(BuildingMetadataPluginAction.class.getPackage().getName()+".ApplicationPlugin",
                BuildingMetadataPluginAction.class.getName(),
                plugin.getPluginClassLoader(), true);
        putPropertyValue(Property.MENU, "Tools");
        putPropertyValue(Property.NAME, "Add Metadata");
        this.plugin = plugin;
        this.plugin.getHome().addSelectionListener(
                new SelectionListener() {
            @Override
            public void selectionChanged(SelectionEvent ev)
            {
                selectedItems = new ArrayList<Selectable>();
                //	metadataPlugin.getHome();
                selectedItems.addAll(Home.getWallsSubList(
                        plugin.getHome().getSelectedItems()));
                //metadataPlugin.getHome();
                selectedItems.addAll(Home.getFurnitureSubList(
                        plugin.getHome().getSelectedItems()));
                ///	metadataPlugin.getHome();
                selectedItems.addAll(Home.getRoomsSubList(
                        plugin.getHome().getSelectedItems()));
                if (selectedItems.isEmpty()
                        || selectedItems.size() != 1)
                {
                    setEnabled(false);
                } else
                {
                    setEnabled(true);
                }
            }
        });
    }

    @Override
    public void execute()
    {
        //recuperar los metadatos del edificio.
        BuildingMetadataManager manager = HomeMetadataLoader.getBuildingMetadataManager(
                this.plugin.getHome());


        if (selectedItems.isEmpty() || selectedItems.size() != 1)
        {
            return;
        }
        Selectable furniture = selectedItems.get(0);

        ArrayList<JTextField> keys = new ArrayList<JTextField>();
        ArrayList<JTextField> values = new ArrayList<JTextField>();
        keys.add(new JTextField(""));
        values.add(new JTextField(""));

        Map<String, String> initialMetaData = manager.getMetadata(furniture);
        for (Map.Entry<String, String> entry : initialMetaData.entrySet())
        {
            keys.add(new JTextField(entry.getKey()));
            values.add(new JTextField(entry.getValue()));
        }

        JPanel panel = new JPanel(new GridLayout(keys.size() + 1, 2));
        panel.add(new JLabel("Key"));
        panel.add(new JLabel("Value"));
        for (int i = 0; i < keys.size(); i++)
        {
            panel.add(keys.get(i));
            panel.add(values.get(i));
        }

        int result = JOptionPane.showConfirmDialog(null, panel,
                "Metadata Editor", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION)
        {
            HashMap<String, String> metaData = new HashMap<String, String>();
            for (int i = 0; i < keys.size(); i++)
            {
                String key = String.valueOf(keys.get(i).getText());
                String value = String.valueOf(values.get(i).getText());
                metaData.put(key, value);
            }

            manager.setMetaData(furniture, metaData);
            this.plugin.getHome().setModified(true);
        } else
        {
            // System.out.println("Cancelled");
        }
    }
}
