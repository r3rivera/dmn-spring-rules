package com.poc.ruleengine.service.dmn;

import com.poc.ruleengine.domain.InputField;
import com.poc.ruleengine.domain.client.ClientAttribute;
import com.poc.ruleengine.model.rules.DecisionRulesetRequest;
import com.poc.ruleengine.service.database.RulesetStorageService;
import com.poc.ruleengine.service.dmn.input.RuleInputHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

import java.util.List;

import static com.poc.ruleengine.service.dmn.DMNBuilderHelper.sanitize;

/**
 * Generates the DMN file
 */
@Service
@RequiredArgsConstructor
public class DecisionRulesetBuilderService {

    private static final String DMN_NS = DMNBuilderHelper.DMN_NS;
    private static final String FEEL_NS = DMNBuilderHelper.FEEL_NS;

    private final RuleInputHandler ruleInputHandler;
    private final RulesetStorageService rulesetStorageService;

    public String buildDecisionRules(DecisionRulesetRequest request) throws Exception {

        final Document mainDoc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .newDocument();

        final String ruleSetName = String.format("%s_%s",request.getApplicationCode(), request.getEventName());

        // <definitions> root element
        final Element definitions = mainDoc.createElementNS(DMN_NS, "definitions");
        definitions.setAttribute("id", "definitions_" + sanitize(ruleSetName));
        definitions.setAttribute("name", ruleSetName);
        definitions.setAttribute("namespace", "https://poc.ruleengine.com/dmn");
        definitions.setAttribute("expressionLanguage", FEEL_NS);
        definitions.setAttribute("typeLanguage", FEEL_NS);


        // Define the expected input structure
        final String inputClassName = ruleInputHandler.getInputClassName(ClientAttribute.class);
        final List<InputField> inputFields = ruleInputHandler.extractFieldAndType(ClientAttribute.class);
        if(!inputFields.isEmpty()) {
            final Element itemDynamicDefinition = DMNBuilderHelper.createItemDefinition(mainDoc,
                    DMNBuilderHelper.sanitize(inputClassName));
            for(InputField inputField: inputFields){
                itemDynamicDefinition.appendChild(DMNBuilderHelper.createItemComponentElement(mainDoc,
                        inputField.getName(), inputField.getType()));
            }
            definitions.appendChild(itemDynamicDefinition);
        }

        final String eventName = "EventName";
        final Element itemDefinition = DMNBuilderHelper.createItemDefinition(mainDoc, eventName);
        itemDefinition.appendChild(DMNBuilderHelper.createItemComponentElement(mainDoc, "applicationCode", "string"));
        itemDefinition.appendChild(DMNBuilderHelper.createItemComponentElement(mainDoc, "eventName", "string"));
        itemDefinition.appendChild(DMNBuilderHelper.createItemComponentElement(mainDoc, "evaluatedUser", inputClassName));
        definitions.appendChild(itemDefinition);

        // Define that input field that reference the itemDefinition
        definitions.appendChild(DMNBuilderHelper.createInputDatatElement(mainDoc, eventName));

        // Create the decision element for event
        definitions.appendChild(DMNBuilderHelper.createEventDecisionElement(mainDoc, request,eventName + "Request"));

        // Add the Event Definition
        mainDoc.appendChild(definitions);
        final String xmlString = DMNBuilderHelper.toXmlString(mainDoc);
        rulesetStorageService.storeDecisionRules(request.getApplicationCode(), xmlString);
        return xmlString;
    }
}
