package br.gov.lexml.eta.etaservices.printing.pdf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.emenda.ModoEdicaoEmenda;
import br.gov.lexml.eta.etaservices.emenda.Sexo;
import br.gov.lexml.eta.etaservices.emenda.SiglaCasaLegislativa;
import br.gov.lexml.eta.etaservices.emenda.TipoAutoria;
import br.gov.lexml.eta.etaservices.emenda.TipoColegiado;
import br.gov.lexml.eta.etaservices.printing.json.AutoriaPojo;
import br.gov.lexml.eta.etaservices.printing.json.ColegiadoApreciadorPojo;
import br.gov.lexml.eta.etaservices.printing.json.ComandoEmendaPojo;
import br.gov.lexml.eta.etaservices.printing.json.ComandoEmendaTextoLivrePojo;
import br.gov.lexml.eta.etaservices.printing.json.ComponenteEmendadoPojo;
import br.gov.lexml.eta.etaservices.printing.json.DispositivoEmendaModificadoPojo;
import br.gov.lexml.eta.etaservices.printing.json.DispositivosEmendaPojo;
import br.gov.lexml.eta.etaservices.printing.json.EmendaPojo;
import br.gov.lexml.eta.etaservices.printing.json.EpigrafePojo;
import br.gov.lexml.eta.etaservices.printing.json.OpcoesImpressaoPojo;
import br.gov.lexml.eta.etaservices.printing.json.ParlamentarPojo;
import br.gov.lexml.eta.etaservices.printing.json.RefProposicaoEmendadaPojo;
import br.gov.lexml.eta.etaservices.printing.xml.EmendaXmlMarshaller;

public class PdfGeneratorTest {
	
    private PdfGeneratorBean pdfGeneratorBean;
    private VelocityTemplateProcessorFactory templateProcessorFactory;
    private EmendaXmlMarshaller emendaXmlMarshaller;
    
    @BeforeEach
    void setUp() {
        emendaXmlMarshaller = new EmendaXmlMarshaller();
        templateProcessorFactory = mock(VelocityTemplateProcessorFactory.class);
        pdfGeneratorBean = new PdfGeneratorBean(templateProcessorFactory, emendaXmlMarshaller);
    }
    
    @Test
    void testGeneratePdfWithEmenda() throws Exception {

    	// Configuração dos dados
        EmendaPojo emenda = new EmendaPojo();

        
        emenda.setDataUltimaModificacao(Instant.now());
        emenda.setAplicacao("");
        emenda.setVersaoAplicacao("");
        emenda.setModoEdicao(ModoEdicaoEmenda.EMENDA);
        emenda.setMetadados(Collections.emptyMap());

        RefProposicaoEmendadaPojo proposicao = new RefProposicaoEmendadaPojo();
        proposicao.setUrn("urn:lex:br:senado.federal:projeto.lei;pl:2024;8");
        proposicao.setSigla("PL");
        proposicao.setNumero("8");
        proposicao.setAno("2024");
        proposicao.setEmenta("Altera a <a href=\"urn:lex:br:federal:lei:1990-09-11;8078\">Lei nº 8.078, de 11 de setembro de 1990</a>, que dispõe sobre a proteção do consumidor e dá outras providências, para dispor sobre o prazo prescricional da ação coletiva de consumo.");
        proposicao.setIdentificacaoTexto("Texto inicial");
        proposicao.setEmendarTextoSubstitutivo(false);
        emenda.setProposicao(proposicao);

        ColegiadoApreciadorPojo colegiado = new ColegiadoApreciadorPojo();
        colegiado.setSiglaCasaLegislativa(SiglaCasaLegislativa.SF);
        colegiado.setTipoColegiado(TipoColegiado.PLENARIO);
        colegiado.setSiglaComissao("");
        emenda.setColegiadoApreciador(colegiado);

        EpigrafePojo epigrafe = new EpigrafePojo();
        epigrafe.setTexto("EMENDA Nº         ");
        epigrafe.setComplemento("(ao PL 8/2024)");
        emenda.setEpigrafe(epigrafe);

        //DispositivosEmendaPojo dispositivos = new DispositivosEmendaPojo();
        //dispositivos.setDispositivosSuprimidos(Collections.emptyList());
        //dispositivos.setDispositivosModificados(Collections.emptyList());
        //dispositivos.setDispositivosAdicionados(Collections.emptyList());
        //componente.setDispositivos(dispositivos);
        
        DispositivoEmendaModificadoPojo dispositivoModificado = new DispositivoEmendaModificadoPojo();
        dispositivoModificado.setTipo("Ementa");
        dispositivoModificado.setId("ementa");
        dispositivoModificado.setRotulo("");
        dispositivoModificado.setTexto("Altera a <a href=\"urn:lex:br:federal:lei:1986-12-19;7565\">Lei nº 7.565, de 19 de dezembro de 1986</a> (Código Brasileiro de Aeronáutica), para instituir gratuidade na correção do nome do passageiro e na transferência de passagem aérea, bem como direito ao cancelamento de passagem por motivo de força maior.");
        List<DispositivoEmendaModificadoPojo> dispositivosModificados = Collections.singletonList(dispositivoModificado);

        // Configuração de DispositivosEmendaPojo com dispositivos preenchidos
        DispositivosEmendaPojo dispositivos = new DispositivosEmendaPojo(new ArrayList<>(), dispositivosModificados, new ArrayList<>());


        
        ComponenteEmendadoPojo componente = new ComponenteEmendadoPojo();
        componente.setUrn("urn:lex:br:senado.federal:projeto.lei;pl:2024;4223");
        componente.setArticulado(true);
        componente.setRotuloAnexo("null");
        componente.setTituloAnexo("null");
        componente.setDispositivos(dispositivos);
        
        //

        emenda.setComponentes(Collections.singletonList(componente));

        ComandoEmendaPojo comandoEmenda = new ComandoEmendaPojo();
        comandoEmenda.setCabecalhoComum("null");
        comandoEmenda.setComandos(Collections.emptyList());
        emenda.setComandoEmenda(comandoEmenda);

        ComandoEmendaTextoLivrePojo comandoEmendaTextoLivre = new ComandoEmendaTextoLivrePojo();
        comandoEmendaTextoLivre.setMotivo(null);
        comandoEmendaTextoLivre.setTexto("");
        emenda.setComandoEmendaTextoLivre(comandoEmendaTextoLivre);

        emenda.setSubstituicaoTermo(null);

        emenda.setJustificativa("<p class=\"align-justify\">teste anonimo</p>");
        emenda.setLocal("Sala das sessões");
        emenda.setData(LocalDate.of(2024, 10, 31));
        
        ParlamentarPojo parlamentar = new ParlamentarPojo();
        parlamentar.setIdentificacao("5322");
        parlamentar.setNome("Romário");
        parlamentar.setSexo(Sexo.M);
        parlamentar.setSiglaPartido("PL");
        parlamentar.setSiglaUF("RJ");
        parlamentar.setSiglaCasaLegislativa(SiglaCasaLegislativa.SF);
        parlamentar.setCargo("teste");

        AutoriaPojo autoria = new AutoriaPojo();
        autoria.setTipo(TipoAutoria.PARLAMENTAR);
        autoria.setImprimirPartidoUF(true);
        autoria.setQuantidadeAssinaturasAdicionaisDeputados(0);
        autoria.setQuantidadeAssinaturasAdicionaisSenadores(0);
        autoria.setParlamentares(Collections.singletonList(parlamentar));
        autoria.setColegiado(null);
        emenda.setAutoria(autoria);

        OpcoesImpressaoPojo opcoesImpressao = new OpcoesImpressaoPojo();
        opcoesImpressao.setImprimirBrasao(false);
        opcoesImpressao.setTextoCabecalho("");
        opcoesImpressao.setReduzirEspacoEntreLinhas(false);
        opcoesImpressao.setTamanhoFonte(16);
        emenda.setOpcoesImpressao(opcoesImpressao);

        emenda.setRevisoes(Collections.emptyList());
        emenda.setPendenciasPreenchimento(Collections.singletonList("Deve ser feita pelo menos uma modificação no texto da proposição para a geração do comando de emenda."));

        // ByteArrayOutputStream para capturar a saída do PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Executa o método de geração do PDF
        pdfGeneratorBean.generate(emenda, outputStream);

        // Verificações
     //   verify(emendaXmlMarshaller, times(1)).toXml(any(Emenda.class));
      //  verify(templateProcessorFactory.get(), times(1)).getTemplateResult(any(Emenda.class));

        // Salvando o PDF em um arquivo
        byte[] pdfBytes = outputStream.toByteArray();
        File pdfFile = new File("emenda.pdf");
        try (FileOutputStream fileOutputStream = new FileOutputStream(pdfFile)) {
            fileOutputStream.write(pdfBytes);
        }

        // Verificação básica de saída
        assertNotNull(pdfBytes);
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);

        System.out.println("PDF salvo em: " + pdfFile.getAbsolutePath());
    }

}
