package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Lê do XSL-FO os metadados XMP usados na geração do PDF/A e separa o bloco {@code x:xmpmeta}, que é
 * incluído no PDF à parte. Derivado de {@code printing.pdf.FOHelper}.
 */
class DocumentoArticuladoFoHelper {

    private static final Map<String, String> NAMESPACES = new HashMap<>();
    static {
        NAMESPACES.put("x", "adobe:ns:meta/");
        NAMESPACES.put("xmp", "http://ns.adobe.com/xap/1.0/");
        NAMESPACES.put("pdfaid", "http://www.aiim.org/pdfa/ns/id/");
    }

    private final Document documento;
    private final String xmpmeta;
    private final String pdfaPart;
    private final String pdfaConformance;
    private final String createDate;

    DocumentoArticuladoFoHelper(String xslFo) {
        try {
            documento = new SAXReader().read(new StringReader(xslFo));
        } catch (DocumentException e) {
            throw new EtaBackendException("O XSL-FO do documento articulado não é um XML válido: " + e.getMessage(), e);
        }
        pdfaPart = valor("//pdfaid:part");
        pdfaConformance = valor("//pdfaid:conformance");
        createDate = valor("//xmp:CreateDate");

        Node xmpmetaNode = no("//x:xmpmeta");
        if (xmpmetaNode == null) {
            xmpmeta = null;
        } else {
            xmpmetaNode.detach();
            xmpmeta = xmpmetaNode.asXML();
        }
    }

    /** XSL-FO sem o bloco {@code x:xmpmeta}. */
    Document getFoSemXmpmeta() {
        return documento;
    }

    String getXmpmeta() {
        return xmpmeta;
    }

    String getPdfaPart() {
        return pdfaPart;
    }

    String getPdfaConformance() {
        return pdfaConformance;
    }

    String getCreateDate() {
        return createDate;
    }

    boolean isPdfa() {
        return pdfaConformance != null && ("1".equals(pdfaPart) || "2".equals(pdfaPart) || "3".equals(pdfaPart));
    }

    private Node no(String expressao) {
        XPath xpath = DocumentHelper.createXPath(expressao);
        xpath.setNamespaceURIs(NAMESPACES);
        return xpath.selectSingleNode(documento);
    }

    private String valor(String expressao) {
        Node node = no(expressao);
        if (node == null || node.getStringValue() == null) {
            return null;
        }
        return node.getStringValue().trim();
    }

}
