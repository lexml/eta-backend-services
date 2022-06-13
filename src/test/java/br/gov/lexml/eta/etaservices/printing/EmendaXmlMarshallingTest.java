package br.gov.lexml.eta.etaservices.printing;


import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import javax.xml.transform.Source;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static br.gov.lexml.eta.etaservices.printing.SiglaCasaLegislativa.CD;
import static br.gov.lexml.eta.etaservices.printing.TipoAutoria.PARLAMENTAR;
import static br.gov.lexml.eta.etaservices.printing.TipoColegiado.PLENARIO;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

class EmendaXmlMarshallingTest {

    private Source xmlSource;

    @BeforeEach
    void setUp() throws JAXBException {
        final var emenda = setupEmenda();
        xmlSource = getXmlSource(emenda);
    }

    @Test
    void testAttribute() {

        assertThat(xmlSource)
                .valueByXPath("/emenda/@aplicacao")
                .isEqualToIgnoringCase("aplicacao");
    }

    @Test
    void testElement() {

        assertThat(xmlSource)
                .valueByXPath("/emenda/colegiado/siglaCasaLegislativa")
                .isEqualToIgnoringCase("cd");
    }

    private Emenda setupEmenda() {
        return new Emenda(
                "2022-06-01",
                "eta",
                "1.0.0",
                ModoEdicaoEmenda.EMENDA,
                Map.of("metadado1", "valor1", "metadado2", "valor2"),
                new RefProposicaoEmendada("1", "MPV", "200", "2022", "aaa", "bcd"),
                new ColegiadoApreciador(CD, PLENARIO, null),
                new Epigrafe("abc", ""),
                List.of(),
                new ComandoEmenda(null, List.of()),
                "justificativa emenda",
                "Brasilia",
                "2019-01-01",
                new Autoria(PARLAMENTAR, true, 0, 0, List.of(), null),
                new OpcoesImpressao(true, "", false));
    }

    private Source getXmlSource(Emenda emenda) throws JAXBException {
        final var jaxbContext = JAXBContext.newInstance(Emenda.class);
        final var marshaller = jaxbContext.createMarshaller();
        final var os = new java.io.ByteArrayOutputStream();
        marshaller.marshal(emenda, os);
        final var xml = os.toString(StandardCharsets.UTF_8);

        System.out.println(xml);
        
        return Input.fromString(xml).build();
    }

}