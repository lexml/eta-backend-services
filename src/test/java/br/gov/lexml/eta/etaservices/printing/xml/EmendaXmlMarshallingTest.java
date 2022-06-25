package br.gov.lexml.eta.etaservices.printing.xml;


import br.gov.lexml.eta.etaservices.printing.Emenda;
import br.gov.lexml.eta.etaservices.printing.ModoEdicaoEmenda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import javax.xml.transform.Source;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.LinkedHashMap;
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
        final Emenda emenda = setupEmenda();
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

        final Map<String, Object> metadados = new LinkedHashMap<>();
        metadados.put("metadado1", "valor1");
        metadados.put("metadado2", "valor2");

        return new EmendaRecord(
                LocalDate.parse("2022-06-01")
                        .atStartOfDay()
                        .atZone(ZoneId.of("America/Sao_Paulo"))
                        .toInstant(),
                "eta",
                "1.0.0",
                ModoEdicaoEmenda.EMENDA,
                metadados,
                new RefProposicaoEmendadaRecord("urn:lex:br:federal:medida.provisoria:800:2022", "MPV", "800", "2022", "Altera a Lei 1.234/56 e dá outras providências", "bcd"),
                new ColegiadoApreciadorRecord(CD, PLENARIO, null),
                new EpigrafeRecord("A Presidência da República em suas atribuições sanciona", ""),
                Collections.singletonList(new ComponenteEmendadoRecord(
                        "urn:...",
                        true,
                        null,
                        null,
                        new DispositivosEmendaRecord(
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.singletonList(
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
                                                Collections.singletonList(
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
                                                                Collections.emptyList())
                                                )))))),
                new ComandoEmendaRecord(null, Collections.emptyList()),
                "justificativa emenda",
                "Brasilia",
                LocalDate.parse("2019-06-01"),
                new AutoriaRecord(PARLAMENTAR, true, 0, 0,
                        Collections.singletonList(
                        new ParlamentarRecord("abcd", "João da Silva", M, "MDB", "SP", CD, "Deputado")
                ), null),
                new OpcoesImpressaoRecord(true, "", false));
    }

    private Source getXmlSource(Emenda emenda) {

        final EmendaXmlMarshaller marshaller = new EmendaXmlMarshaller();

        final String xml = marshaller.toXml(emenda);

        System.out.println(xml);

        return Input.fromString(xml).build();
    }

}