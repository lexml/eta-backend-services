package br.gov.lexml.eta.etaservices.parsing.xml;

import br.gov.lexml.eta.etaservices.printing.Emenda;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmendaXmlUnmarshallerTest {

    private EmendaXmlUnmarshaller unmarshaller;

    @BeforeEach
    void setUp() {
        unmarshaller = new EmendaXmlUnmarshaller();
    }

    @Test
    void fromXml() throws DocumentException {
        Emenda e = unmarshaller.fromXml("<emenda></emenda>");

        assertThat(e).isNull();

    }
}