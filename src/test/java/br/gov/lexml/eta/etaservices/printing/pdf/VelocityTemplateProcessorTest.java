package br.gov.lexml.eta.etaservices.printing.pdf;

import br.gov.lexml.eta.etaservices.printing.AutoriaRecord;
import br.gov.lexml.eta.etaservices.printing.ColegiadoApreciadorRecord;
import br.gov.lexml.eta.etaservices.printing.ComandoEmendaRecord;
import br.gov.lexml.eta.etaservices.printing.Emenda;
import br.gov.lexml.eta.etaservices.printing.EmendaRecord;
import br.gov.lexml.eta.etaservices.printing.EpigrafeRecord;
import br.gov.lexml.eta.etaservices.printing.ItemComandoEmendaRecord;
import br.gov.lexml.eta.etaservices.printing.ModoEdicaoEmenda;
import br.gov.lexml.eta.etaservices.printing.OpcoesImpressaoRecord;
import br.gov.lexml.eta.etaservices.printing.ParlamentarRecord;
import br.gov.lexml.eta.etaservices.printing.RefProposicaoEmendadaRecord;
import br.gov.lexml.eta.etaservices.printing.Sexo;
import br.gov.lexml.eta.etaservices.printing.SiglaCasaLegislativa;
import br.gov.lexml.eta.etaservices.printing.TipoAutoria;
import br.gov.lexml.eta.etaservices.printing.TipoColegiado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import javax.xml.transform.Source;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.xmlunit.assertj3.XmlAssert.assertThat;

class VelocityTemplateProcessorTest {

    @Test
    @DisplayName("Should return the template result when the template is not empty")
    void getTemplateResultWhenTemplateIsNotEmpty() throws Exception {
        String cabecalho = "Gabinete do Senador Ricardo Lima";

        Map<String, Object> mapaParaContexto = new HashMap<>();
        mapaParaContexto.put("textoCabecalho", cabecalho);

        VelocityTemplateProcessor velocityTemplateProcessor =
                new VelocityTemplateProcessor(mapaParaContexto);


        String templateResult = velocityTemplateProcessor.getTemplateResult();
        Source result = Input.fromString(templateResult).build();

        Map<String, String> context = new HashMap<>();
        context.put("fo", "http://www.w3.org/1999/XSL/Format");

        assertThat(result)
                .withNamespaceContext(context)
                .valueByXPath("/fo:root/fo:page-sequence/fo:static-content/fo:block[1]")
                .isEqualToIgnoringCase(cabecalho);
    }

    @Test
    @DisplayName("Should return an empty string when the template is empty")
    void getTemplateResultWhenTemplateIsEmpty() throws Exception {
        Map<String, Object> mapaParaContexto = new HashMap<>();
        VelocityTemplateProcessor velocityTemplateProcessor =
                new VelocityTemplateProcessor(mapaParaContexto);
        String templateResult = velocityTemplateProcessor.getTemplateResult();

        Source result = Input.fromString(templateResult).build();

        Map<String, String> context = new HashMap<>();
        context.put("fo", "http://www.w3.org/1999/XSL/Format");

        assertThat(result)
                .withNamespaceContext(context)
                .valueByXPath("/fo:root/fo:page-sequence/fo:static-content/fo:block[1]")
                .isBlank();
    }

    @Test
    void testaMetadadosEmenda() throws Exception {

        List<ParlamentarRecord> parlamentares = new ArrayList<>();

        parlamentares.add(new ParlamentarRecord("Senador", "Ricardo Lima", Sexo.M, "PL", "DF", SiglaCasaLegislativa.SF, "Senador"));

        List<ItemComandoEmendaRecord> itensComandoEmenda = new ArrayList<>();

        itensComandoEmenda.add(new ItemComandoEmendaRecord("aaa","aa", "Art. 10", "aoaeoeao"));

        Emenda emenda = new EmendaRecord(
                Instant.now(),
                "LexEdit",
                "1.0.0",
                ModoEdicaoEmenda.EMENDA,
                new HashMap<>(),
                new RefProposicaoEmendadaRecord(
                        "", "MPV", "1010", "2022", "Dá nova redação à Lei Maria da Penha", ""),
                new ColegiadoApreciadorRecord(SiglaCasaLegislativa.CD, TipoColegiado.PLENARIO, null),
                new EpigrafeRecord("Do Gabinete do Senador Ricardo Lima",""),
                new ArrayList<>(),
                new ComandoEmendaRecord("Emenda ttt",itensComandoEmenda),
                "",
                "",
                LocalDate.now(),
                new AutoriaRecord(
                        TipoAutoria.PARLAMENTAR,
                true,
                0,
                0,
                parlamentares,
                null), new OpcoesImpressaoRecord(
                        true,"Cabecalho", false));


        Map<String, Object> mapaParaContexto = new HashMap<>();
        mapaParaContexto.put("emenda", emenda);

        VelocityTemplateProcessor velocityTemplateProcessor =
                new VelocityTemplateProcessor(mapaParaContexto);

        String templateResult = velocityTemplateProcessor.getTemplateResult();

        Source result = Input.fromString(templateResult).build();

        Map<String, String> context = new HashMap<>();
        context.put("fo", "http://www.w3.org/1999/XSL/Format");

        assertThat(result)
                .withNamespaceContext(context)
                .valueByXPath("/fo:root/fo:page-sequence/fo:static-content/fo:block[2]")
                .isBlank();
    }

}