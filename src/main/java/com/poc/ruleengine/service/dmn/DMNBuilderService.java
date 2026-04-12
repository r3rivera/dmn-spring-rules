package com.poc.ruleengine.service.dmn;

import com.poc.ruleengine.domain.RulesetRequest;
import com.poc.ruleengine.domain.RulesetRequest.DecisionTableDef;
import com.poc.ruleengine.domain.RulesetRequest.InputColumnDef;
import com.poc.ruleengine.domain.RulesetRequest.OutputColumnDef;
import com.poc.ruleengine.domain.RulesetRequest.RuleDef;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Builds a DMN 1.3 XML document from a {@link RulesetRequest} and persists it
 * to the file system so the KIE runtime can pick it up.
 */
@Slf4j
@Service
public class DMNBuilderService {

    private static final String DMN_NS = DMNBuilderHelper.DMN_NS;
    private static final String FEEL_NS = DMNBuilderHelper.FEEL_NS;

    /** directory where generated DMN files are stored */
    private final Path storageDir;

    /** rulesetName → absolute file path */
    @Getter
    private final ConcurrentMap<String, Path> registry = new ConcurrentHashMap<>();

    public DMNBuilderService() {
        this.storageDir = Paths.get(System.getProperty("dmn.storage.dir", "dmn-files"));
        try { Files.createDirectories(storageDir); } catch (Exception e) { throw new RuntimeException(e); }
    }

    // ─── public API ────────────────────────────────────────────────────────────

    /**
     * Build and persist a DMN file. Returns the XML string.
     */
    public String buildAndStore(RulesetRequest req) {
        try {
            String xml = buildXml(req);
            Path file = storageDir.resolve(sanitize(req.getRulesetName()) + ".dmn");
            Files.writeString(file, xml);
            registry.put(req.getRulesetName(), file);
            log.info("DMN file stored at {}", file.toAbsolutePath());
            return xml;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build DMN", e);
        }
    }

    public Path getPath(String rulesetName) {
        return registry.get(rulesetName);
    }

    // ─── XML generation ────────────────────────────────────────────────────────

    private String buildXml(RulesetRequest req) throws Exception {

        final Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .newDocument();

        // <definitions> root element
        final Element definitions = doc.createElementNS(DMN_NS, "definitions");
        definitions.setAttribute("id", "definitions_" + sanitize(req.getRulesetName()));
        definitions.setAttribute("name", req.getRulesetName());
        definitions.setAttribute("namespace", req.getNamespace() != null
                ? req.getNamespace() : "https://example.com/dmn");
        definitions.setAttribute("expressionLanguage", FEEL_NS);
        definitions.setAttribute("typeLanguage", FEEL_NS);
        doc.appendChild(definitions);

        // Define specific request structure that will be used to hold
        // the data that will be evaluated
        final Element itemUserProfileDefinition = doc.createElementNS(DMN_NS, "itemDefinition");
        itemUserProfileDefinition.setAttribute("id", "itemDef_UserProfile");
        itemUserProfileDefinition.setAttribute("name", "UserProfile");
        // Age
        itemUserProfileDefinition.appendChild(DMNBuilderHelper.createItemComponentElement(doc,
                "age", "number"));
        // Account Type
        itemUserProfileDefinition.appendChild(DMNBuilderHelper.createItemComponentElement(doc,
                "accountType", "string"));
        // Segment Type
        itemUserProfileDefinition.appendChild(DMNBuilderHelper.createItemComponentElement(doc,
                "segmentType", "string"));
        definitions.appendChild(itemUserProfileDefinition);


        final Element itemEventDefinition = doc.createElementNS(DMN_NS, "itemDefinition");
        itemEventDefinition.setAttribute("id", "itemDef_EventRequest");
        itemEventDefinition.setAttribute("name", "EventRequest");
        itemEventDefinition.appendChild(DMNBuilderHelper.createItemComponentElement(doc,
                "eventName", "string"));
        itemEventDefinition.appendChild(DMNBuilderHelper.createItemComponentElement(doc,
                "applicationCode", "string"));
        itemEventDefinition.appendChild(DMNBuilderHelper.createItemComponentElement(doc,
                "userProfile", "UserProfile"));
        definitions.appendChild(itemEventDefinition);

        // one <inputData> node per top-level input column
        if (req.getInputColumns() != null) {
            for (RulesetRequest.InputColumnDef col : req.getInputColumns()) {
                Element inputData = doc.createElementNS(DMN_NS, "inputData");
                String inputId = "inputData_" + sanitize(col.getName());
                inputData.setAttribute("id", inputId);
                inputData.setAttribute("name", col.getName());

                Element variable = doc.createElementNS(DMN_NS, "variable");
                variable.setAttribute("id", "var_" + sanitize(col.getName()));
                variable.setAttribute("name", col.getName());
                variable.setAttribute("typeRef", col.getTypeRef());
                inputData.appendChild(variable);
                definitions.appendChild(inputData);
            }
        }

        // decision tables
        if (req.getDecisionTables() != null) {
            for (DecisionTableDef dtDef : req.getDecisionTables()) {
                definitions.appendChild(buildDecision(doc, dtDef));
            }
        }

        return toXmlString(doc);
    }

    private Element buildDecision(Document doc, DecisionTableDef dtDef) {

        String decId = "decision_" + sanitize(dtDef.getDecisionName());

        Element decision = doc.createElementNS(DMN_NS, "decision");
        decision.setAttribute("id", decId);
        decision.setAttribute("name", dtDef.getDecisionName());

        // variable
        if (dtDef.getOutputs() != null && !dtDef.getOutputs().isEmpty()) {
            OutputColumnDef firstOut = dtDef.getOutputs().get(0);
            Element variable = doc.createElementNS(DMN_NS, "variable");
            variable.setAttribute("id", "var_" + sanitize(dtDef.getDecisionName()));
            variable.setAttribute("name", firstOut.getName());
            variable.setAttribute("typeRef", firstOut.getTypeRef());
            decision.appendChild(variable);
        }

        // informationRequirement — required inputs
        if (dtDef.getRequiredInputs() != null) {
            for (String ri : dtDef.getRequiredInputs()) {
                Element ir = doc.createElementNS(DMN_NS, "informationRequirement");
                ir.setAttribute("id", uid());
                Element reqInput = doc.createElementNS(DMN_NS, "requiredInput");
                reqInput.setAttribute("href", "#inputData_" + sanitize(ri));
                ir.appendChild(reqInput);
                decision.appendChild(ir);
            }
        }

        // informationRequirement — required decisions
        if (dtDef.getRequiredDecisions() != null) {
            for (String rd : dtDef.getRequiredDecisions()) {
                Element ir = doc.createElementNS(DMN_NS, "informationRequirement");
                ir.setAttribute("id", uid());
                Element reqDec = doc.createElementNS(DMN_NS, "requiredDecision");
                reqDec.setAttribute("href", "#decision_" + sanitize(rd));
                ir.appendChild(reqDec);
                decision.appendChild(ir);
            }
        }

        // <decisionTable>
        String hitPolicy = dtDef.getHitPolicy() != null ? dtDef.getHitPolicy() : "UNIQUE";
        Element dt = doc.createElementNS(DMN_NS, "decisionTable");
        dt.setAttribute("id", "dt_" + sanitize(dtDef.getDecisionName()));
        dt.setAttribute("hitPolicy", hitPolicy);

        // inputs
        if (dtDef.getInputs() != null) {
            for (InputColumnDef in : dtDef.getInputs()) {
                Element input = doc.createElementNS(DMN_NS, "input");
                input.setAttribute("id", uid());
                input.setAttribute("label", in.getLabel() != null ? in.getLabel() : in.getName());
                Element expr = doc.createElementNS(DMN_NS, "inputExpression");
                expr.setAttribute("id", uid());
                expr.setAttribute("typeRef", in.getTypeRef());
                Element text = doc.createElementNS(DMN_NS, "text");
                text.setTextContent(in.getExpression());
                expr.appendChild(text);
                input.appendChild(expr);
                dt.appendChild(input);
            }
        }

        // outputs
        if (dtDef.getOutputs() != null) {
            for (OutputColumnDef out : dtDef.getOutputs()) {
                Element output = doc.createElementNS(DMN_NS, "output");
                output.setAttribute("id", uid());
                output.setAttribute("name", out.getName());
                output.setAttribute("typeRef", out.getTypeRef());
                dt.appendChild(output);
            }
        }

        // rules
        if (dtDef.getRules() != null) {
            for (RuleDef r : dtDef.getRules()) {
                Element rule = doc.createElementNS(DMN_NS, "rule");
                rule.setAttribute("id", uid());
                if (r.getDescription() != null) {
                    Element desc = doc.createElementNS(DMN_NS, "description");
                    desc.setTextContent(r.getDescription());
                    rule.appendChild(desc);
                }
                for (String ie : r.getInputEntries()) {
                    Element entry = doc.createElementNS(DMN_NS, "inputEntry");
                    entry.setAttribute("id", uid());
                    Element t = doc.createElementNS(DMN_NS, "text");
                    t.setTextContent(ie);
                    entry.appendChild(t);
                    rule.appendChild(entry);
                }
                for (String oe : r.getOutputEntries()) {
                    Element entry = doc.createElementNS(DMN_NS, "outputEntry");
                    entry.setAttribute("id", uid());
                    Element t = doc.createElementNS(DMN_NS, "text");
                    t.setTextContent(oe);
                    entry.appendChild(t);
                    rule.appendChild(entry);
                }
                dt.appendChild(rule);
            }
        }

        decision.appendChild(dt);
        return decision;
    }

    // ─── helpers ───────────────────────────────────────────────────────────────

    private String toXmlString(Document doc) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StringWriter sw = new StringWriter();
        tf.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }

    private static String sanitize(String s) {
        return s == null ? "unknown" : s.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private static String uid() {
        return "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}

