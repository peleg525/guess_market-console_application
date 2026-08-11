package gm.engine.xml;

import jakarta.xml.bind.annotation.XmlValue;

public class GmOptionXml {

    @XmlValue
    private String value;

    public String getValue() {
        return value;
    }
}
