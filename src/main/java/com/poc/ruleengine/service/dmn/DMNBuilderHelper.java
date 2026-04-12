package com.poc.ruleengine.service.dmn;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class DMNBuilderHelper {

    public static final String DMN_NS = "https://www.omg.org/spec/DMN/20191111/MODEL/";
    public static final String FEEL_NS = "https://www.omg.org/spec/DMN/20191111/FEEL/";




    public static Element createItemComponentElement(Document doc, String fieldName, String contentType){
        final Element itemComponent = doc.createElementNS(DMN_NS, "itemComponent");
        itemComponent.setAttribute("id", "ic_"+fieldName);
        itemComponent.setAttribute("name", fieldName);
        itemComponent.appendChild(createTypeRefElem(doc,contentType));
        return itemComponent;
    }
    /**
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

}
