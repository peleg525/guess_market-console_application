package gm.engine.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class GmMethodXml {

    @XmlElement(name = "GM-LMSR")
    private GmLmsrXml lmsr;

    public GmLmsrXml getLmsr() {
        return lmsr;
    }
}
