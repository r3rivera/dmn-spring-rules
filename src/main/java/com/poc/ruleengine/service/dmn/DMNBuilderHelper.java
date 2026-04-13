package com.poc.ruleengine.service.dmn;

import com.poc.ruleengine.model.Rule;
import com.poc.ruleengine.model.rules.DecisionRulesetRequest;
import com.poc.ruleengine.model.rules.UserAttributeRuleset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

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
        itemComponent.setAttribute("id", "ID_" + inputName + "Request");
        itemComponent.setAttribute("name", "evaluatedRequest");
        itemComponent.appendChild(createVariableElem(doc, "evaluatedRequest", inputName));
        return itemComponent;
    }

    public static Element createVariableElem(Document doc, String variableName, String type){
        final Element elementTypeRef = doc.createElementNS(DMN_NS, "variable");
        elementTypeRef.setAttribute("id", "var_" + variableName);
        elementTypeRef.setAttribute("name", variableName);
        elementTypeRef.setAttribute("typeRef", type);
        return elementTypeRef;
    }

    public static Element createInputExpressionElem(Document doc, String requestInput, String type){
        final Element elementInputExpression = doc.createElementNS(DMN_NS, "inputExpression");
        elementInputExpression.setAttribute("id", uid());
        elementInputExpression.setAttribute("typeRef", type);
        final Element textElem = doc.createElementNS(DMN_NS, "text");
        textElem.setTextContent(requestInput);
        elementInputExpression.appendChild(textElem);
        return elementInputExpression;
    }

    // Decision Element block for Event Specific
    public static Element createEventDecisionElement(Document doc, DecisionRulesetRequest request,
                                                     String targetInputId){
        final Element decisionElement = doc.createElementNS(DMN_NS, "decision");
        decisionElement.setAttribute("id", uid());
        decisionElement.setAttribute("name", targetInputId + "_DT");

        final String variableName = "eventRequestType";
        final String variableType = "boolean";

        // Variable element
        decisionElement.appendChild(createVariableElem(doc, variableName, variableType));

        // Info Requirement Element
        final Element infoRequirementElem = doc.createElementNS(DMN_NS, "informationRequirement");
        infoRequirementElem.setAttribute("id", uid());
        final Element requiredElement = doc.createElementNS(DMN_NS, "requiredInput");
        requiredElement.setAttribute("href", "#ID_"+targetInputId);
        infoRequirementElem.appendChild(requiredElement);
        decisionElement.appendChild(infoRequirementElem);

        // Decision Table Element
        final Element decisionTableElem = doc.createElementNS(DMN_NS, "decisionTable");
        decisionTableElem.setAttribute("id", uid());
        decisionTableElem.setAttribute("hitPolicy", "FIRST");

        // Input elements
        final Element inputElem1 = doc.createElementNS(DMN_NS, "input");
        inputElem1.setAttribute("id", uid());
        inputElem1.setAttribute("label", "App Code");
        inputElem1.appendChild(createInputExpressionElem(doc,  "evaluatedRequest.applicationCode", "string"));
        decisionTableElem.appendChild(inputElem1);

        final Element inputElem2 = doc.createElementNS(DMN_NS, "input");
        inputElem2.setAttribute("id", uid());
        inputElem2.setAttribute("label", "Event Name");
        inputElem2.appendChild(createInputExpressionElem(doc, "evaluatedRequest.eventName", "string"));
        decisionTableElem.appendChild(inputElem2);

        // Output element
        final Element outputElem = doc.createElementNS(DMN_NS, "output");
        outputElem.setAttribute("id", uid());
        outputElem.setAttribute("name", variableName);
        outputElem.setAttribute("typeRef", variableType);
        decisionTableElem.appendChild(outputElem);

        // Expected Rule Element
        final Element expectedRuleElem = doc.createElementNS(DMN_NS, "rule");
        expectedRuleElem.setAttribute("id", uid());
        expectedRuleElem.appendChild(createRuleEntry(doc,request.getApplicationCode(), "text"));
        expectedRuleElem.appendChild(createRuleEntry(doc,request.getEventName(), "text"));
        expectedRuleElem.appendChild(createRuleOutputEntry(doc,"true", "text"));
        decisionTableElem.appendChild(expectedRuleElem);

        //Default Rule Element
        final Element defaultRuleElem = doc.createElementNS(DMN_NS, "rule");
        defaultRuleElem.setAttribute("id", uid());
        defaultRuleElem.appendChild(createRuleEntry(doc,"-", "text"));
        defaultRuleElem.appendChild(createRuleEntry(doc,"-", "text"));
        defaultRuleElem.appendChild(createRuleOutputEntry(doc,"false", "text"));
        decisionTableElem.appendChild(defaultRuleElem);

        decisionElement.appendChild(decisionTableElem);
        return decisionElement;
    }

    public static Element createUserDecisionElement(Document doc, UserAttributeRuleset userAttributeRuleset, String sourceInput,
                                                     String targetInputId) {

        final Element decisionElement = doc.createElementNS(DMN_NS, "decision");
        decisionElement.setAttribute("id", uid());
        decisionElement.setAttribute("name", targetInputId + "_DT");

        final String variableName = "userInfoMatch";
        final String variableType = "boolean";

        // Variable element
        decisionElement.appendChild(createVariableElem(doc, variableName, variableType));

        // Info Requirement Element
        final Element infoRequirementElem = doc.createElementNS(DMN_NS, "informationRequirement");
        infoRequirementElem.setAttribute("id", uid());
        final Element requiredElement = doc.createElementNS(DMN_NS, "requiredInput");
        requiredElement.setAttribute("href", "#ID_"+sourceInput);
        infoRequirementElem.appendChild(requiredElement);
        decisionElement.appendChild(infoRequirementElem);

        // Decision Table Element
        final Element decisionTableElem = doc.createElementNS(DMN_NS, "decisionTable");
        decisionTableElem.setAttribute("id", uid());
        decisionTableElem.setAttribute("hitPolicy", "FIRST");

        final List<Rule> userRules = userAttributeRuleset.getRules();
        for(Rule rule: userRules) {
            final Element inputElem1 = doc.createElementNS(DMN_NS, "input");
            inputElem1.setAttribute("id", uid());
            inputElem1.setAttribute("label", rule.getFieldName());
            inputElem1.appendChild(createInputExpressionElem(doc, "evaluatedRequest.evaluatedUser." + rule.getFieldName(), rule.getFieldType()));
            decisionTableElem.appendChild(inputElem1);
        }
        final Element outputElem = doc.createElementNS(DMN_NS, "output");
        outputElem.setAttribute("id", uid());
        outputElem.setAttribute("name", variableName);
        outputElem.setAttribute("typeRef", variableType);
        decisionTableElem.appendChild(outputElem);

        final Element expectedRuleElem = doc.createElementNS(DMN_NS, "rule");
        expectedRuleElem.setAttribute("id", uid());
        for(Rule rule: userRules){
            if("string".equalsIgnoreCase(rule.getFieldType())) {
                expectedRuleElem.appendChild(createRuleEntry(doc, rule.getExpectedValue(), "text"));
            }else{
                expectedRuleElem.appendChild(createRuleEntry(doc, rule.getExpectedValue(), rule.getFieldType()));
            }
        }
        decisionTableElem.appendChild(expectedRuleElem);
        decisionElement.appendChild(decisionTableElem);
        return decisionElement;
    }


    // Rule Element Input Entry
    private static Element createRuleEntry(Document doc, String expectedStringValue, String expectedType){
        final Element expectedRuleEntryElem = doc.createElementNS(DMN_NS, "inputEntry");
        expectedRuleEntryElem.setAttribute("id", uid());
        final Element t = doc.createElementNS(DMN_NS, expectedType);
        if("text".equalsIgnoreCase(expectedType) && !"-".equalsIgnoreCase(expectedStringValue)){
            t.setTextContent("\""+expectedStringValue+"\"");
        } else{
            t.setTextContent(expectedStringValue);
        }
        expectedRuleEntryElem.appendChild(t);
        return  expectedRuleEntryElem;
    }

    // RUle Element Output Entry
    private static Element createRuleOutputEntry(Document doc, String expectedStringValue, String expectedType){
        final Element expectedRuleEntryElem = doc.createElementNS(DMN_NS, "outputEntry");
        expectedRuleEntryElem.setAttribute("id", uid());
        final Element t = doc.createElementNS(DMN_NS, expectedType);
        t.setTextContent(expectedStringValue);
        expectedRuleEntryElem.appendChild(t);
        return  expectedRuleEntryElem;
    }




    private static String uid() {
        return "_" + UUID.randomUUID().toString().substring(0, 8);
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
