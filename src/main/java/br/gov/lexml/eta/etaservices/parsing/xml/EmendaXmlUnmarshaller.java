package br.gov.lexml.eta.etaservices.parsing.xml;

import br.gov.lexml.eta.etaservices.printing.Emenda;
import br.gov.lexml.eta.etaservices.printing.ModoEdicaoEmenda;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EmendaXmlUnmarshaller {
    public Emenda fromXml(final String xml) throws DocumentException {

        SAXReader reader = new SAXReader();
        Document doc = reader.read(new StringReader(xml));

        return parseDocument(doc.getRootElement());
    }

    private Emenda parseDocument(final Element rootElement) {
        final String local = rootElement.attributeValue("local");
        final String dataAttribute = rootElement.attributeValue("data");
        final LocalDate data = LocalDate.parse(dataAttribute);
        final Node metadadosNode = rootElement.selectSingleNode("/Metadados");
        List<Node> metadados = metadadosNode.selectNodes("");
        Instant dataUltimaModificacao;
        String aplicacao;
        String versaoAplicacao;
        ModoEdicaoEmenda modoEdicao;
        final Map<String, String> meta = new LinkedHashMap<>();
        for (Node n : metadados) {
            switch (n.getName()) {
                case "DataUltimaModificacao":
                    String dt = n.getStringValue();
                    dataUltimaModificacao = Instant.parse(dt);
                    break;
                case "Aplicacao":
                    aplicacao = n.getStringValue();
                    break;
                case "VersaoAplicacao":
                    versaoAplicacao = n.getStringValue();
                    break;
                case "ModoEdicao":
                    String me = n.getStringValue();
                    modoEdicao = ModoEdicaoEmenda.parse(me);
                    break;
                default:
                    meta.put(n.getName(), n.getStringValue());
                    break;
            }

        }

        return null;
    }

}
