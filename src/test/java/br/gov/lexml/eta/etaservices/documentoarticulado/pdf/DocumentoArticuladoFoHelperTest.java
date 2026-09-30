package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;

class DocumentoArticuladoFoHelperTest {

    private static final String FO_MINIMO = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">"
            + "<fo:declarations>"
            + "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">"
            + "<rdf:Description xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\" xmlns:pdfaid=\"http://www.aiim.org/pdfa/ns/id/\" rdf:about=\"\">"
            + "<xmp:CreateDate>2026-09-24T10:00:00-03:00</xmp:CreateDate>"
            + "<pdfaid:part> 3 </pdfaid:part><pdfaid:conformance>B</pdfaid:conformance>"
            + "</rdf:Description></rdf:RDF></x:xmpmeta>"
            + "</fo:declarations>"
            + "</fo:root>";

    @Test
    void extraiMetadadosEDestacaXmpmeta() {
        DocumentoArticuladoFoHelper helper = new DocumentoArticuladoFoHelper(FO_MINIMO);

        assertThat(helper.getPdfaPart()).isEqualTo("3");
        assertThat(helper.getPdfaConformance()).isEqualTo("B");
        assertThat(helper.getCreateDate()).isEqualTo("2026-09-24T10:00:00-03:00");
        assertThat(helper.isPdfa()).isTrue();
        assertThat(helper.getXmpmeta()).startsWith("<x:xmpmeta").contains("pdfaid:part");
        assertThat(helper.getFoSemXmpmeta().asXML()).doesNotContain("xmpmeta").contains("fo:declarations");
    }

    @Test
    void semXmpmeta() {
        DocumentoArticuladoFoHelper helper = new DocumentoArticuladoFoHelper("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"/>");

        assertThat(helper.getXmpmeta()).isNull();
        assertThat(helper.isPdfa()).isFalse();
    }

    @Test
    void foInvalido() {
        assertThatThrownBy(() -> new DocumentoArticuladoFoHelper("<fo:root"))
                .isInstanceOf(EtaBackendException.class);
    }

}
