package gm.engine.xml;

import gm.engine.exception.GmFileException;
import gm.engine.model.CommissionType;
import gm.engine.model.Event;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reads and validates a Guess Market XML file (Exercise 1 format), turning it into ready-to-use
 * {@link Event} objects. Validation happens in three stages, and every problem found at a given
 * stage is reported together rather than stopping at the first one:
 * <ol>
 *     <li>path / file-existence / .xml extension</li>
 *     <li>XSD schema validity</li>
 *     <li>application-level rules that the schema cannot express (unique ids, commission range,
 *         exactly two options, positive liquidity)</li>
 * </ol>
 */
public class GmFileLoader {

    private static final String SCHEMA_RESOURCE = "/gm/engine/xml/GM-EX1-Schema.xsd";
    private static final int MIN_COMMISSION = 0;
    private static final int MAX_COMMISSION = 90;

    public List<Event> load(String rawPath) {
        File file = validatePathAndExtension(rawPath);
        GuessMarketXml root = parseWithSchemaValidation(file);
        List<GmEventXml> xmlEvents = (root.getEvents() == null) ? List.of() : root.getEvents().getEvent();

        List<String> problems = new ArrayList<>();
        validateApplicationRules(xmlEvents, problems);
        if (!problems.isEmpty()) {
            throw new GmFileException(problems);
        }

        return buildModel(xmlEvents);
    }

    private File validatePathAndExtension(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            throw new GmFileException("No file path was entered.");
        }
        String path = rawPath.trim();
        if (!path.toLowerCase(Locale.ROOT).endsWith(".xml")) {
            throw new GmFileException("The file path must end with a \".xml\" extension: \"" + path + "\"");
        }
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new GmFileException("No file was found at path: \"" + path + "\"");
        }
        return file;
    }

    private GuessMarketXml parseWithSchemaValidation(File file) {
        List<String> schemaProblems = new ArrayList<>();
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema;
            try (InputStream xsdStream = GmFileLoader.class.getResourceAsStream(SCHEMA_RESOURCE)) {
                schema = schemaFactory.newSchema(new StreamSource(xsdStream));
            }
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) {
                    // ignored: warnings do not make the file invalid
                }

                @Override
                public void error(SAXParseException exception) {
                    schemaProblems.add(describe(exception));
                }

                @Override
                public void fatalError(SAXParseException exception) {
                    schemaProblems.add(describe(exception));
                }
            });
            validator.validate(new StreamSource(file));
        } catch (SAXException | IOException e) {
            schemaProblems.add("The file could not be parsed as XML: " + e.getMessage());
        }

        if (!schemaProblems.isEmpty()) {
            throw new GmFileException(schemaProblems);
        }

        try {
            JAXBContext context = JAXBContext.newInstance(GuessMarketXml.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (GuessMarketXml) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new GmFileException("The file could not be read: " + e.getMessage());
        }
    }

    private String describe(SAXParseException e) {
        return "Line " + e.getLineNumber() + ": " + e.getMessage();
    }

    private void validateApplicationRules(List<GmEventXml> events, List<String> problems) {
        Map<Integer, List<String>> eventLabelsById = new LinkedHashMap<>();

        for (GmEventXml event : events) {
            String label = describeEvent(event);

            if (event.getId() != null) {
                eventLabelsById.computeIfAbsent(event.getId(), id -> new ArrayList<>()).add(label);
            }

            Integer commission = event.getComision() != null ? event.getComision().getValue() : null;
            if (commission != null && (commission < MIN_COMMISSION || commission > MAX_COMMISSION)) {
                problems.add("Event " + label + " has an invalid commission of " + commission
                        + "%. Commission must be between " + MIN_COMMISSION + " and " + MAX_COMMISSION + " (inclusive).");
            }

            int optionCount = (event.getOptions() != null) ? event.getOptions().getOption().size() : 0;
            if (optionCount != 2) {
                problems.add("Event " + label + " must have exactly two options, but has " + optionCount + ".");
            }

            Integer b = (event.getMethod() != null && event.getMethod().getLmsr() != null)
                    ? event.getMethod().getLmsr().getB() : null;
            if (b != null && b <= 0) {
                problems.add("Event " + label + " has a non-positive liquidity value (b=" + b
                        + "). b must be a positive integer.");
            }
        }

        for (Map.Entry<Integer, List<String>> entry : eventLabelsById.entrySet()) {
            if (entry.getValue().size() > 1) {
                problems.add("Event id " + entry.getKey() + " is used by more than one event: "
                        + String.join(", ", entry.getValue()) + ". Each event must have a unique id.");
            }
        }
    }

    private String describeEvent(GmEventXml event) {
        String idPart = (event.getId() != null) ? "#" + event.getId() : "with no id";
        String namePart = (event.getName() != null) ? "'" + event.getName() + "'" : "(no name)";
        return namePart + " (" + idPart + ")";
    }

    private List<Event> buildModel(List<GmEventXml> xmlEvents) {
        List<Event> result = new ArrayList<>();
        for (GmEventXml x : xmlEvents) {
            List<String> options = x.getOptions().getOption().stream()
                    .map(GmOptionXml::getValue)
                    .map(String::trim)
                    .collect(Collectors.toList());
            CommissionType type = CommissionType.fromXmlValue(x.getComision().getType());
            Event event = new Event(
                    x.getId(),
                    x.getName().trim(),
                    x.getDescription().trim(),
                    x.getComision().getValue(),
                    type,
                    options,
                    x.getMethod().getLmsr().getB());
            result.add(event);
        }
        return result;
    }
}
