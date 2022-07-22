package br.gov.lexml.eta.etaservices.printing.xml;


import br.gov.lexml.eta.etaservices.printing.AutoriaRecord;
import br.gov.lexml.eta.etaservices.printing.ColegiadoApreciadorRecord;
import br.gov.lexml.eta.etaservices.printing.ComandoEmendaRecord;
import br.gov.lexml.eta.etaservices.printing.ComponenteEmendadoRecord;
import br.gov.lexml.eta.etaservices.printing.DispositivoEmendaAdicionadoRecord;
import br.gov.lexml.eta.etaservices.printing.DispositivosEmendaRecord;
import br.gov.lexml.eta.etaservices.printing.Emenda;
import br.gov.lexml.eta.etaservices.printing.EmendaRecord;
import br.gov.lexml.eta.etaservices.printing.EpigrafeRecord;
import br.gov.lexml.eta.etaservices.printing.ModoEdicaoEmenda;
import br.gov.lexml.eta.etaservices.printing.OpcoesImpressaoRecord;
import br.gov.lexml.eta.etaservices.printing.ParlamentarRecord;
import br.gov.lexml.eta.etaservices.printing.RefProposicaoEmendadaRecord;
import br.gov.lexml.eta.etaservices.printing.json.ArquivoEmenda;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import javax.xml.transform.Source;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private Emenda emenda;

    @BeforeEach
    void setUp() {
        setupEmenda();
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

        ClassLoader classLoader = getClass().getClassLoader();
        try {
            File file = new File(classLoader.getResource("test1.json").getFile());
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            emenda = objectMapper.readValue(text, ArquivoEmenda.class).getEmenda();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Source getXmlSource(Emenda emenda) {

        final EmendaXmlMarshaller marshaller = new EmendaXmlMarshaller();

        final String xml = marshaller.toXml(emenda);

        System.out.println(xml);

        return Input.fromString(xml).build();
    }

}