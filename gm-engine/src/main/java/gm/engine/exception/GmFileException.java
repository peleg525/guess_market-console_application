package gm.engine.exception;

import java.util.Collections;
import java.util.List;

/**
 * Thrown whenever a Guess Market XML file cannot be loaded: the path is invalid, the file is not
 * well-formed / schema-valid XML, or it violates one of the application-level rules (unique event
 * ids, commission range, etc). Carries every problem found, not just the first one, so the user
 * gets a full picture of what is wrong with the file in a single attempt.
 */
public class GmFileException extends GmException {

    private final List<String> problems;

    public GmFileException(String singleProblem) {
        this(List.of(singleProblem));
    }

    public GmFileException(List<String> problems) {
        super(buildMessage(problems));
        this.problems = Collections.unmodifiableList(problems);
    }

    public List<String> getProblems() {
        return problems;
    }

    private static String buildMessage(List<String> problems) {
        StringBuilder sb = new StringBuilder("The file is not valid:");
        for (String problem : problems) {
            sb.append(System.lineSeparator()).append("  - ").append(problem);
        }
        return sb.toString();
    }
}
