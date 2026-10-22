package br.gov.lexml.eta.etaservices.parsing.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.gov.lexml.eta.etaservices.emenda.Comentario;
import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.emenda.EmendaJsonGeneratorBean;
import br.gov.lexml.eta.etaservices.emenda.Parlamentar;
import br.gov.lexml.eta.etaservices.emenda.SequenciaComentario;

class EmendaXmlUnmarshallerTest {

    private EmendaXmlUnmarshaller unmarshaller;

    @BeforeEach
    void setUp() {
        unmarshaller = new EmendaXmlUnmarshaller();
    }

    @Test
    void fromXml() throws Exception {
    	
//    	String fileName = "emenda_mpv_905_2019_completa_disp_mpv";
    	String fileName = "mp-com-revisoes";
//    	String fileName = "mp-com-revisoes-2";
    	
    	try {
        	String xml = IOUtils.resourceToString("/" + fileName + ".xml", StandardCharsets.UTF_8);
        	
            Emenda e = unmarshaller.fromXml(xml);

            assertThat(e.getLocal()).isEqualTo("Sala da comissão");
            
            FileWriter fileWriter = new FileWriter("target/" + fileName + ".json");
            
            new EmendaJsonGeneratorBean().writeJson(e, fileWriter);
            
            fileWriter.close();
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}    	
        
    }

    @Test
    void fromXmlComSequenciasComentario() throws Exception {
        String xml = IOUtils.resourceToString("/mp-com-revisoes.xml", StandardCharsets.UTF_8)
                .replace("</Emenda>", blocoSequenciasComentario() + "\n</Emenda>");

        Emenda emenda = unmarshaller.fromXml(xml);

        assertThat(emenda.getSequenciasComentario()).hasSize(1);

        SequenciaComentario sequencia = emenda.getSequenciasComentario().get(0);
        assertThat(sequencia.getId()).isEqualTo("sc123");
        assertThat(sequencia.getIdDispositivo()).isEqualTo("art3_par2");
        assertThat(sequencia.getLocal()).isEqualTo("justificação");
        assertThat(sequencia.getComentarios()).hasSize(1);

        Comentario comentario = sequencia.getComentarios().get(0);
        assertThat(comentario.getDataHora()).isEqualTo("2026-05-18 15:48:26");
        assertThat(comentario.getUsuario().getNome()).isEqualTo("fragomeni");
        assertThat(comentario.getUsuario().getId()).isEqualTo("fragomeni");
        assertThat(comentario.getUsuario().getSigla()).isEqualTo("F");
        assertThat(comentario.getTexto()).isEqualTo("Texto com acentuação & revisão <validada>.");
    }

    @Test
    void fromXmlSemSequenciasComentarioRetornaListaVazia() throws Exception {
        String xml = IOUtils.resourceToString("/mp-com-revisoes.xml", StandardCharsets.UTF_8);

        Emenda emenda = unmarshaller.fromXml(xml);

        assertThat(emenda.getSequenciasComentario()).isEmpty();
    }

    @Test
    void fromXmlComAnexoParecerSoOmiteJustificativa() throws Exception {
        String xml = IOUtils.resourceToString("/mp-com-revisoes.xml", StandardCharsets.UTF_8)
                .replace("</Metadados>", "<AnexoParecer>true</AnexoParecer></Metadados>")
                .replace("</Justificativa>", "</Justificativa><JustificativaAntesRevisao>&lt;p&gt;Antes&lt;/p&gt;</JustificativaAntesRevisao>")
                .replace("</Autoria>", "<Parlamentar identificacao=\"6331\" nome=\"Sergio Moro\" tratamento=\"Senador\""
                        + " siglaPartido=\"UNIÃO\" siglaUF=\"PR\" siglaCasaLegislativa=\"SF\" /></Autoria>")
                .replace("</Emenda>", "<NotasRodape><NotaRodape id=\"1\" numero=\"1\" texto=\"Nota 1\" /></NotasRodape></Emenda>");

        Emenda emenda = unmarshaller.fromXml(xml);
        StringWriter jsonWriter = new StringWriter();
        new EmendaJsonGeneratorBean().writeJson(emenda, jsonWriter);

        assertThat(emenda.isAnexoParecer()).isTrue();
        assertThat(emenda.getJustificativa()).isNull();
        assertThat(emenda.getJustificativaAntesRevisao()).isNull();
        assertThat(emenda.getLocal()).isEqualTo("Sala da comissão");
        assertThat(emenda.getData()).isEqualTo(LocalDate.of(2023, 7, 12));
        assertThat(emenda.getAutoria().getParlamentares()).extracting(Parlamentar::getNome).containsExactly("Sergio Moro");
        assertThat(emenda.getNotasRodape()).hasSize(1);
        assertThat(jsonWriter.toString())
                .doesNotContain("\"justificativa\"")
                .doesNotContain("\"justificativaAntesRevisao\"")
                .contains("\"local\"")
                .contains("\"data\"")
                .contains("\"autoria\"")
                .contains("Sergio Moro");
    }

    @Test
    void fromXmlSemAnexoParecerGeraFalseNoJson() throws Exception {
        String xml = IOUtils.resourceToString("/mp-com-revisoes.xml", StandardCharsets.UTF_8);
        Emenda emenda = unmarshaller.fromXml(xml);
        StringWriter jsonWriter = new StringWriter();

        new EmendaJsonGeneratorBean().writeJson(emenda, jsonWriter);

        assertThat(emenda.isAnexoParecer()).isFalse();
        assertThat(jsonWriter.toString()).contains("\"anexoParecer\" : false");
    }

    private String blocoSequenciasComentario() {
        return "  <SequenciasComentario>\n"
                + "    <SequenciaComentario id=\"sc123\" local=\"justificação\" idDispositivo=\"art3_par2\">\n"
                + "      <Comentario dataHora=\"2026-05-18 15:48:26\">\n"
                + "        <Usuario nome=\"fragomeni\" id=\"fragomeni\" sigla=\"F\"/>\n"
                + "        <Texto>Texto com acentuação &amp; revisão &lt;validada&gt;.</Texto>\n"
                + "      </Comentario>\n"
                + "    </SequenciaComentario>\n"
                + "  </SequenciasComentario>";
    }
}
