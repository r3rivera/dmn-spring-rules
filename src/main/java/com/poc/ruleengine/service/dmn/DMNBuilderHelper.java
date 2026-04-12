package com.poc.ruleengine.service.dmn;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public final class DMNBuilderHelper {

    public static final String DMN_NS = "https://www.omg.org/spec/DMN/20191111/MODEL/";
    public static final String FEEL_NS = "https://www.omg.org/spec/DMN/20191111/FEEL/";


    /**
     * Creates the ItemDefinition element that
     * DMN requires to identify the structure of the input request that needs
     * to be evaluated.
     *
     * @param doc
     * @return
     */
    public static Element createItemDefinition(Document doc, String name){
        final Element itemDefinitionElement = doc.createElementNS(DMN_NS, "itemDefinition");
        itemDefinitionElement.setAttribute("id", String.format("itemDef_%s", name));
        itemDefinitionElement.setAttribute("name", name);
        return itemDefinitionElement;
    }

    public static Element createItemComponentElement(Document doc, String fieldName, String contentType){
        final Element itemComponent = doc.createElementNS(DMN_NS, "itemComponent");
        itemComponent.setAttribute("id", "ic_"+fieldName);
        itemComponent.setAttribute("name", fieldName);
        itemComponent.appendChild(createTypeRefElem(doc,contentType));
        return itemComponent;
    }

    public static Element createInputDatatElement(Document doc, String inputName) {
        final Element itemComponent = doc.createElementNS(DMN_NS, "inputData");
        itemComponent.setAttribute("id", "inputDataId_" + inputName);
        itemComponent.setAttribute("name", "inputDataName_" + inputName);
        itemComponent.appendChild(createVariableElem(doc, "inputDataName_" + inputName, inputName));
        return itemComponent;
    }

    public static Element createVariableElem(Document doc, String variableName, String type){
        final Element elementTypeRef = doc.createElementNS(DMN_NS, "variable");
        elementTypeRef.setAttribute("id", "var_" + variableName);
        elementTypeRef.setAttribute("name", variableName);
        elementTypeRef.setAttribute("typeRef", type);
        return elementTypeRef;
    }


    /**
     *
     *
     * Builds the <typeRef> element
     * @param doc
     * @param contentType
     * @return
     */
    public static Element createTypeRefElem(Document doc, String contentType){
        final Element elementTypeRef = doc.createElementNS(DMN_NS, "typeRef");
        elementTypeRef.setTextContent(contentType);
        return elementTypeRef;
    }

    public static String sanitize(String s) {
        return s == null ? "unknown" : s.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    public static String toXmlString(Document doc) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StringWriter sw = new StringWriter();
        tf.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }

}
