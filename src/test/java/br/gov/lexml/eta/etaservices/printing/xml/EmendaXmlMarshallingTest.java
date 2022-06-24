package br.gov.lexml.eta.etaservices.printing.xml;


import br.gov.lexml.eta.etaservices.printing.ModoEdicaoEmenda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import javax.xml.transform.Source;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static br.gov.lexml.eta.etaservices.printing.Sexo.M;
import static br.gov.lexml.eta.etaservices.printing.SiglaCasaLegislativa.CD;
import static br.gov.lexml.eta.etaservices.printing.TipoAutoria.PARLAMENTAR;
import static br.gov.lexml.eta.etaservices.printing.TipoColegiado.PLENARIO;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

class EmendaXmlMarshallingTest {

    private Source xmlSource;

    @BeforeEach
    void setUp() {
        final var emenda = setupEmenda();
        xmlSource = getXmlSource(emenda);
    }

    @Test
    void testMetadados() {

        assertThat(xmlSource)
                .valueByXPath("/Emenda/Metadados/Aplicacao")
                .isEqualToIgnoringCase("eta");
    }

    @Test
    void testColegiadoApreciador() {

        assertThat(xmlSource)
                .valueByXPath("/Emenda/ColegiadoApreciador/@siglaCasaLegislativa")
                .isEqualToIgnoringCase("cd");
    }

    private EmendaRecord setupEmenda() {
        return new EmendaRecord(
                LocalDate.parse("2022-06-01")
                        .atStartOfDay()
                        .atZone(ZoneId.of("America/Sao_Paulo"))
                        .toInstant(),
                "eta",
                "1.0.0",
                ModoEdicaoEmenda.EMENDA,
                Map.of("metadado1", "valor1", "metadado2", "valor2"),
                new RefProposicaoEmendadaRecord("urn:lex:br:federal:medida.provisoria:800:2022", "MPV", "800", "2022", "Altera a Lei 1.234/56 e dá outras providências", "bcd"),
                new ColegiadoApreciadorRecord(CD, PLENARIO, null),
                new EpigrafeRecord("A Presidência da República em suas atribuições sanciona", ""),
                List.of(new ComponenteEmendadoRecord(
                        "urn:...",
                        true,
                        null,
                        null,
                         new DispositivosEmendaRecord(
                                 List.of(),
                                 List.of(),
                                 List.of(
                                         new DispositivoEmendaAdicionadoRecord(
                                                 "Artigo",
                                                    "2",
                                                    "Art. 2-1",
                                                 "Lorem ipsum dolor sit amet, consetet",
                                                    false,
                                                 false,
                                                 false,
                                                 null,
                                                 false,
                                                 "0",
                                                 "1",
                                                 "urn:lex:br:federal:medida.provisoria:800:2022",
                                                    false,
                                                 List.of(
                                                         new DispositivoEmendaAdicionadoRecord("Artigo",
                                                                 "2",
                                                                 "Art. 2-1",
                                                                 "Lorem ipsum dolor sit amet, consetet",
                                                                 false,
                                                                 false,
                                                                 false,
                                                                 null,
                                                                 false,
                                                                 "0",
                                                                 "1",
                                                                 "urn:lex:br:federal:medida.provisoria:800:2022",
                                                                 false,
                                                                 List.of())
                                                 )))))),
                new ComandoEmendaRecord(null, List.of()),
                "justificativa emenda",
                "Brasilia",
                LocalDate.parse("2019-06-01"),
                new AutoriaRecord(PARLAMENTAR, true, 0, 0, List.of(
                        new ParlamentarRecord("abcd", "João da Silva", M, "MDB", "SP", CD, "Deputado")
                ), null),
                new OpcoesImpressaoRecord(true, "", false));
    }

    private Source getXmlSource(EmendaRecord emenda) {

        final var marshaller = new EmendaXmlMarshaller();

        final var xml = marshaller.toXml(emenda);

        System.out.println(xml);

        return Input.fromString(xml).build();
    }

}