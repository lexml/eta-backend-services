package br.gov.lexml.eta.etaservices.parsing.xml;

import br.gov.lexml.eta.etaservices.printing.Emenda;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.StringReader;

public class EmendaXmlUnmarshaller {
    public Emenda fromXml(final String xml) throws DocumentException {

        SAXReader reader = new SAXReader();
        Document doc = reader.read(new StringReader(xml));

        return parseDocument(doc.getRootElement());
    }

    private Emenda parseDocument(final Element rootElement) {
        String local = rootElement.attributeValue("local");
        String dataAttribute = rootElement.attributeValue("data");

        return null;
    }

}
