import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by John on 6/10/2016.
 */
public class ServerXMLDocumentManager {
    private Document doc = null;
    private Editor editor = null;
    private Project project = null;
    private File serverXML = null;
    private VirtualFile vf = null;

    /**
     * Initialize the server.xml document object
     * @param editor The IntelliJ editor that will be displaying the document
     * @param project The IntelliJ project that the server.xml file is part of
     * @param serverXML The server.xml file
     */
    public ServerXMLDocumentManager(Editor editor, Project project, File serverXML) {
        this.editor = editor;
        this.project = project;
        this.serverXML = serverXML;
        loadFile(serverXML);
    }

    /**
     * This function loads a file into the IntelliJ editor. Can also be used
     * to reload the contents of the editor.
     * @param serverXML The server.xml file we want to load into the editor
     */
    public void loadFile(File serverXML) {

        try {
            this.serverXML = serverXML;
            vf = LocalFileSystem.getInstance().findFileByIoFile(this.serverXML);
            FileEditorManager.getInstance(project).openFile(vf, true);
            vf.setWritable(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This function writes changes to the the specified file (usually the server.xml file)
     * @param updatedDocument A file object representing the file we want to write to.
     */
    public void writeChanges(File updatedDocument) {
        this.serverXML = updatedDocument;

        // Upd
        try {
            final byte[] byteDocument = Files.readAllBytes(updatedDocument.toPath());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        vf.setBinaryContent(byteDocument);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            WriteCommandAction.runWriteCommandAction(project, runnable);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * This function adds an XSD schema definition to the server.xml document, allowing for
     * autocomplete to work properly.
     */
    /*
    private void addSchemaDefinition() {
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = docBuilder.parse(serverXML);
            boolean changesMade = false;
            // Generate the schema definition from ws-scehmagen.jar
            String wlpFolder = serverXML.getParentFile().getParentFile().getParentFile().getParent();
            String toolsFolder = wlpFolder+"\\bin\\tools\\";
            String schemaXsd = wlpFolder+"\\server.xsd";
            Runtime.getRuntime().exec("java -jar " + toolsFolder + "ws-schemagen.jar --schemaVersion=1.1 --outputVersion=2 " + schemaXsd);

            // Specify the schema in the server.xml file
            Element root = doc.getDocumentElement();
            if (!root.hasAttribute("xmlns:xsi")) {
                root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                changesMade = true;
            }
            if (!root.hasAttribute("xsi:noNamespaceSchemaLocation")) {
                root.setAttribute("xsi:noNamespaceSchemaLocation", schemaXsd);
                changesMade = true;
            }

            if (changesMade) {
                // Write the content into xml file
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(serverXML);
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(source, result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

}
