/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by John Collier and Logan Kember on 5/26/2016.
 */
public class FeatureEditorFactory extends Component implements ToolWindowFactory {
    private File xmlFile;
    private String wlpFolder;
    private ArrayList<Feature> featureList = new ArrayList<>();
    private Editor editor = null;
    private Project project = null;
    private ServerXMLDocumentManager docManager = null;

    // UI Components
    private JList list;
    private DefaultTableModel tableModel = new DefaultTableModel(0,0);
    private JTable table = new JTable();
    private ListSelectionModel lsm;
    private JScrollPane tableScroll;
    private JPanel myPanel1;
    private JButton addButton;
    private JButton myLoadServerButton;
    private JEditorPane descriptionText;
    private JEditorPane enablesText;
    private JEditorPane enabledByText;
    private JButton removeButton;
    private JComboBox onError;
    private ToolWindow myToolWindow;
    private JFileChooser xmlFileChooser;

    public FeatureEditorFactory()  {

        // Set up the UI features for the feature editor form
        xmlFileChooser = new JFileChooser();
        xmlFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        xmlFileChooser.setDialogTitle("Open server.xml file");
        FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("xml files (*.xml)", "xml");
        xmlFileChooser.setFileFilter(xmlFilter);
        tableScroll.getViewport().add(table);
        lsm = table.getSelectionModel();
        descriptionText.setContentType("text/html");
        enablesText.setContentType("text/html");
        enabledByText.setContentType("text/html");
        System.out.println("Now");
        table.setDefaultEditor(Object.class, null);

        myLoadServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                int returnval = xmlFileChooser.showOpenDialog(FeatureEditorFactory.this);

                // Attempt to load the file that was chosen by the user
                if (returnval == JFileChooser.APPROVE_OPTION) {
                    xmlFile = xmlFileChooser.getSelectedFile();
                    if (xmlFile.getName().substring(xmlFile.getName().length()-4).equals(".xml")) {
                        System.out.println("Opening "+xmlFile.getName());

                        // enable parts of the UI that were disabled because the server.xml wasn't loaded
                        addButton.setEnabled(true);
                        removeButton.setEnabled(true);
                        String columnNames[] = new String[] {"Feature", "Name"};
                        tableModel.setColumnIdentifiers(columnNames);
                        table.setModel(tableModel);
                        table.setEnabled(true);

                        // Load the features into the combo box
                        loadTableFeatures();

                        // Set the editor
                        ServerXMLDocumentManager docMan = new ServerXMLDocumentManager(editor, project, xmlFile);
                    }
                    else {
                        JFrame frame = new JFrame();
                        JOptionPane.showMessageDialog(frame, "You must select an .xml file.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        });

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                getTableSelection();
            }
        });

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                getTableSelection();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                getTableSelection();
            }
        });

        addButton.addActionListener(new ActionListener() {
            // Add the selected feature into server.xml
            public void actionPerformed(ActionEvent e) {
                if (xmlFile == null) {
                    // A server.xml file must be selected first
                    JFrame frame = new JFrame();
                    JOptionPane.showMessageDialog(frame, "You must select an .xml file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    int selection[] = table.getSelectedRows();
                    for (int i=0; i<selection.length; i++) {
                        ServerXMLFeatureManager.addNewFeature((String)table.getValueAt(selection[i], 0), xmlFile);
                    }

                    // Refresh the editor
                    refreshEditor();
                }

            }
        });

        onError.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Update the server.xml file for the new "On Error" selection
                String errorText = (String) onError.getSelectedItem();
                ServerXMLFeatureManager.addOnError(errorText, xmlFile);
                refreshEditor();
            }
        });


        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Load the xml document
                int selection[] = table.getSelectedRows();
                try {
                    String featureName;
                    for (int i=0; i<selection.length; i++) {
                        featureName = (String)table.getValueAt(selection[i], 0);
                        ServerXMLFeatureManager.removeFeature(featureName, xmlFile);
                    }

                    // Refresh the editor
                    refreshEditor();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * Creates the tool window's contents
     * @param project The intellij project that's being worked on.
     * @param toolWindow The tool window that is being created
     */
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        myToolWindow = toolWindow;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(myPanel1, "", false);
        toolWindow.getContentManager().addContent(content);
        descriptionText.setText("");
        enablesText.setText("");
        enabledByText.setText("");


        // Load the editor object (so we can access it later)
        editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        this.project = project;

        // Configure the toolwindow
    }

    /**
     * Loads the Liberty features into a combo box. It finds the features in wlp/lib/features
     */
    private void loadTableFeatures() {
        try {
            wlpFolder = xmlFile.getParentFile().getParentFile().getParentFile().getParent();
            String toolsFolder = wlpFolder+"\\bin\\tools\\";
            String featureXml = wlpFolder+"\\features.xml";

            // Run a java app in a separate system process
            Runtime.getRuntime().exec("java -jar " + toolsFolder + "ws-featurelist.jar " + featureXml);
            featureList = ServerXMLFeatureManager.loadAllFeatures(new File(featureXml));
            for (int i = 0; i < featureList.size(); i++) {
                tableModel.addRow(new Object[]{featureList.get(i).getFeatureName(), featureList.get(i).getName()});
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * Refreshes the current editor in IntelliJ
     */
    private void refreshEditor() {
        docManager.loadFile(xmlFile);
        docManager.writeChanges(xmlFile);
        myToolWindow.activate(null, true);
    }

    private void getTableSelection() {
        if (featureList.size() >= 1) {

            int selection[] = table.getSelectedRows();  //Getting all rows selected by the user
            // If one feature is selected
            if (selection.length==1) {
                int comboIndex = featureList.indexOf(new Feature((String) table.getValueAt(table.getSelectedRow(), 0), ""));
                if (comboIndex == -1) comboIndex = 0;
                String newDesc = featureList.get(comboIndex).getDescription();
                String enables = featureList.get(comboIndex).enablesToString();
                String enabledBy = featureList.get(comboIndex).enabledByToString();
                descriptionText.setText(newDesc);
                enablesText.setText(enables);
                enabledByText.setText(enabledBy);
            }
            else if (selection.length==0){
                descriptionText.setText("");
                enablesText.setText("");
                enabledByText.setText("");
            }
            // If multiple features are selected
            else {
                descriptionText.setText("Multiple features selected.");
                Set<String> enables = new HashSet<>();          //Using HashSets to avoid duplicates
                Set<String> enabledBy = new HashSet<>();
                for (int i=0; i<selection.length; i++) {
                    int comboIndex = featureList.indexOf(new Feature((String)table.getValueAt(selection[i],0),""));
                    if (comboIndex==-1) comboIndex=0;
                    enabledBy.addAll(featureList.get(comboIndex).getEnabledBy());
                    enables.addAll(featureList.get(comboIndex).getEnables());
                }
                if (!enables.isEmpty()) enablesText.setText(enables.toString());
                else enablesText.setText("Does not enable any other features.");
                if (!enabledBy.isEmpty()) enabledByText.setText(enabledBy.toString());
                else enabledByText.setText("Is not enabled by any other features.");
            }
        }
    }
}