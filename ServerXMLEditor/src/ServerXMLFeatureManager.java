/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by John Collier & Logan Kember on 5/27/2016.
 *
 * This class contains all of the functionality for editing/managing the features in Liberty server.xml file
 */
public class ServerXMLFeatureManager {

    private static ArrayList<Feature> featuresList = new ArrayList<>();
    public static Document doc = null;
    public ServerXMLFeatureManager() {

    }

    /**
     * This function adds a feature to the Liberty server's Server.xml file
     * @param feature The feature that we want to add to the server.xml file, see wlp/lib/features for the options
     * @param xmlFile The server.xml file that we're adding the feature to.
     */
    public static void addNewFeature(String feature, File xmlFile) {
        try {
            // Load the xml document
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = docBuilder.parse(xmlFile);

            // Get the featureManager tag
            Node featuresNode = doc.getElementsByTagName("featureManager").item(0);

            // Load the features into an arraylist
            ArrayList<String> serverFeatures = loadFeatures(featuresNode);
            if (serverFeatures.contains(feature)) {
                System.out.println("The feature: "+ feature + " is already in Server.xml");
            }
            else {
                // Server.xml does not currently have this feature, so add it.
                Element newFeature = doc.createElement("feature");
                newFeature.appendChild(doc.createTextNode(feature));
                featuresNode.appendChild(newFeature);

                // The following code is to strip white space from the XML file, required for proper formatting
                // See: http://stackoverflow.com/questions/978810/how-to-strip-whitespace-only-text-nodes-from-a-dom-before-serialization
                XPathFactory xpathFactory = XPathFactory.newInstance();
                XPathExpression xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");
                NodeList emptyTextNodes = (NodeList) xpathExp.evaluate(doc, XPathConstants.NODESET);
                // Remove each empty text node from document.
                for (int i = 0; i < emptyTextNodes.getLength(); i++) {
                    Node emptyTextNode = emptyTextNodes.item(i);
                    emptyTextNode.getParentNode().removeChild(emptyTextNode);
                }

                // Write the content into xml file
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(xmlFile);
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(source, result);

            }
        }
        catch (Exception e) {
            // Maybe do something?
            e.printStackTrace();
            return;
        }
    }

    /**
     * Removes a given feature from a server.xml file
     * @param feature The feature we want to remove from server.xml
     * @param xmlFile The Server.xml file we want to remove the feature from
     */
    public static void removeFeature(String feature, File xmlFile) {
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.parse(xmlFile);

            // Get the parent featureInfo node and the feature node that we need to remove
            String expression = "/server/featureManager";
            XPath xPath =  XPathFactory.newInstance().newXPath();
            Node featureNode = (Node) xPath.compile(expression).evaluate(doc, XPathConstants.NODE);

            expression = "/server/featureManager/feature[./text()='" + feature + "']";
            xPath = XPathFactory.newInstance().newXPath();
            Node featureChild = (Node) xPath.compile(expression).evaluate(doc, XPathConstants.NODE);

            //Removes blank spaces
            Node prev = featureChild.getPreviousSibling();
            if (prev != null && prev.getNodeType()==Node.TEXT_NODE && prev.getNodeValue().trim().length() == 0) {
                featureNode.removeChild(prev);
            }
            //Removes xml tag
            featureNode.removeChild(featureChild);

            // Write the content into xml file
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This function returns an arrayList of all of the features in a server.xml file
     * @param featuresNode The parent 'featureManager' node in Server.xml that contains the features
     * @return An ArrayList of Strings that contains all of the features currently in a server.xml file
     */
    private static ArrayList<String> loadFeatures(Node featuresNode) {
        // Get the nodelist of all of featureList's children
        ArrayList<String> featuresList = new ArrayList<>();
        NodeList childFeatures = featuresNode.getChildNodes();

        // Add each child node to the ArrayList and return the list
        for (int i = 0; i < childFeatures.getLength(); i++) {
            featuresList.add(childFeatures.item(i).getTextContent());
        }
        return featuresList;
    }

    /**
     * This function loads all of the features from the features xml file and loads them into an array list
     * @param xmlFile The xml file that specifies all of the possible features.
     * @return An arraylist of all of features
     */
    public static ArrayList<Feature> loadAllFeatures(File xmlFile) {
        try {
            // Load each feature into the list
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.parse(xmlFile);

            // Get the description and name of each feature and add it to the list of features
            String expression = "/featureInfo/feature/description";
            String expression1 = "/featureInfo/feature/displayName";
            XPath xPath =  XPathFactory.newInstance().newXPath();
            NodeList nl = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {
                String description = nl.item(i).getTextContent();
                String name = nl.item(i).getParentNode().getAttributes().getNamedItem("name").getNodeValue();
                Feature tempFeature = new Feature(name, description);
                featuresList.add(tempFeature);
            }
            nl = (NodeList) xPath.compile(expression1).evaluate(doc, XPathConstants.NODESET);
            for (int i=0; i<nl.getLength(); i++) {
                String displayName = nl.item(i).getTextContent();
                String featureName = nl.item(i).getParentNode().getAttributes().getNamedItem("name").getNodeValue();
                featuresList.get(featuresList.indexOf(new Feature(featureName, ""))).setName(displayName);
            }

            // Get the enables and enabledBy text for the feature
            expression = "/featureInfo/feature/enables";
            xPath = XPathFactory.newInstance().newXPath();
            nl = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
            for (int i=0; i<nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                String name = el.getParentNode().getAttributes().getNamedItem("name").getNodeValue();
                int index = featuresList.indexOf(new Feature(name,""));
                int reverseIndex = featuresList.indexOf(new Feature(el.getTextContent(), ""));
                if (index == -1 || reverseIndex == -1) {
                    System.exit(0);
                }
                featuresList.get(index).addEnables(el.getTextContent());
                featuresList.get(reverseIndex).addEnabledBy(name);
            }
            return featuresList;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds the specified OnError functionality/attribute to the server.xml file
     * @param error Either FAIL, WARN, OR
     * @param xmlFile The server.xml file
     */
    public static void addOnError(String error, File xmlFile) {
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.parse(xmlFile);

            // Set the OnError attribute
            Element httpEndpoint = (Element) doc.getElementsByTagName("httpEndpoint").item(0);
            httpEndpoint.setAttribute("onError", error);

            // Write the content into xml file
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
