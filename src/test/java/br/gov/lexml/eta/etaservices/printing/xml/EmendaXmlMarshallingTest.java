package br.gov.lexml.eta.etaservices.printing.xml;


import static org.junit.jupiter.api.Assertions.*;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;

import br.gov.lexml.eta.etaservices.emenda.*;
import br.gov.lexml.eta.etaservices.printing.json.*;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.builder.Input;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class EmendaXmlMarshallingTest {
    private Source xmlSource;
    private DocumentBuilder builder;
    private EmendaXmlMarshaller marshaller = new EmendaXmlMarshaller();

    @Disabled
    void testMetadados() {

        assertThat(xmlSource)
                .valueByXPath("/Emenda/Metadados/Aplicacao")
                .isEqualToIgnoringCase("");
    }

    @BeforeEach
    void setUp() throws ParserConfigurationException {
        final Emenda emenda = setupEmenda();
        xmlSource = getXmlSource(emenda);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        builder = factory.newDocumentBuilder();
    }

    @Test
    void testColegiadoApreciador() {

        assertThat(xmlSource)
                .valueByXPath("/Emenda/ColegiadoApreciador/@siglaCasaLegislativa")
                .isEqualToIgnoringCase("CN");
    }

    @Test
    void deveGerarCabecalhoEmendaComDataELocalTest() {
        EmendaPojo emenda = new EmendaPojo();
        emenda.setLocal("Local Teste");
        emenda.setData(LocalDate.of(2023, 10, 23));

        StringBuilder sb = new StringBuilder();

        marshaller.geraCabecalhoEmenda(emenda, sb);
        sb.append("</Emenda>");

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element emendaElement = document.getDocumentElement();
        assertEquals("Emenda", emendaElement.getTagName());
        assertEquals("1.0", emendaElement.getAttribute("versaoFormatoArquivo"));
        assertEquals("Local Teste", emendaElement.getAttribute("local"));
        assertEquals("2023-10-23", emendaElement.getAttribute("data"));
    }

    @Test
    void deveGerarCabecalhoEmendaSemDataTest() {
        EmendaPojo emenda = new EmendaPojo();
        emenda.setLocal("Local Teste");
        emenda.setData(null); // Sem data

        StringBuilder sb = new StringBuilder();

        marshaller.geraCabecalhoEmenda(emenda, sb);
        sb.append("</Emenda>");

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element emendaElement = document.getDocumentElement();
        assertEquals("Emenda", emendaElement.getTagName());
        assertEquals("1.0", emendaElement.getAttribute("versaoFormatoArquivo"));
        assertEquals("Local Teste", emendaElement.getAttribute("local"));
        assertEquals("", emendaElement.getAttribute("data")); // Atributo "data" deve estar vazio
    }

    @Test
    void deveGerarMetadadosDadosBasicosTest() {
        EmendaPojo emenda = new EmendaPojo();
        emenda.setDataUltimaModificacao(Instant.parse("2023-10-23T10:00:00Z"));
        emenda.setAplicacao("Aplicação Teste");
        emenda.setVersaoAplicacao("1.0.0");
        emenda.setModoEdicao(ModoEdicaoEmenda.EMENDA);
        emenda.setMetadados(new HashMap<>()); // Sem metadados

        StringBuilder sb = new StringBuilder();
        marshaller.geraMetadados(emenda, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element metadadosElement = document.getDocumentElement();
        assertEquals("Metadados", metadadosElement.getTagName());
        assertEquals("2023-10-23T10:00:00Z", document.getElementsByTagName("DataUltimaModificacao").item(0).getTextContent());
        assertEquals("Aplicação Teste", document.getElementsByTagName("Aplicacao").item(0).getTextContent());
        assertEquals("1.0.0", document.getElementsByTagName("VersaoAplicacao").item(0).getTextContent());
        assertEquals(ModoEdicaoEmenda.EMENDA.getNome(), document.getElementsByTagName("ModoEdicao").item(0).getTextContent());
        assertEquals(0, document.getElementsByTagName("Autor").getLength()); // Sem metadados
        assertEquals(0, document.getElementsByTagName("Revisao").getLength()); // Sem metadados
    }

    @Test
    void deveGerarMetadadosDadosCompletosTest() {
        EmendaPojo emenda = new EmendaPojo();
        emenda.setDataUltimaModificacao(Instant.parse("2023-10-23T10:00:00Z"));
        emenda.setAplicacao("Aplicação Teste");
        emenda.setVersaoAplicacao("1.0.0");
        emenda.setModoEdicao(ModoEdicaoEmenda.EMENDA);

        Map<String, Object> metadados = new HashMap<>();
        metadados.put("Autor", "João");
        metadados.put("Revisao", "1");
        emenda.setMetadados(metadados);

        StringBuilder sb = new StringBuilder();
        marshaller.geraMetadados(emenda, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element metadadosElement = document.getDocumentElement();
        assertEquals("Metadados", metadadosElement.getTagName());
        assertEquals("2023-10-23T10:00:00Z", document.getElementsByTagName("DataUltimaModificacao").item(0).getTextContent());
        assertEquals("Aplicação Teste", document.getElementsByTagName("Aplicacao").item(0).getTextContent());
        assertEquals("1.0.0", document.getElementsByTagName("VersaoAplicacao").item(0).getTextContent());
        assertEquals(ModoEdicaoEmenda.EMENDA.getNome(), document.getElementsByTagName("ModoEdicao").item(0).getTextContent());
        assertEquals("João", document.getElementsByTagName("Autor").item(0).getTextContent());
        assertEquals("1", document.getElementsByTagName("Revisao").item(0).getTextContent());
    }

    @Test
    void deveGerarProposicaoTest() {
        RefProposicaoEmendadaPojo proposicao = new RefProposicaoEmendadaPojo();
        proposicao.setUrn("urn:1234");
        proposicao.setSigla("SIG");
        proposicao.setNumero("123");
        proposicao.setAno("2023");
        proposicao.setEmenta("Ementa de teste");
        proposicao.setIdentificacaoTexto("Identificação teste");
        proposicao.setEmendarTextoSubstitutivo(true);

        StringBuilder sb = new StringBuilder();
        marshaller.geraProposicao(proposicao, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element proposicaoElement = document.getDocumentElement();
        assertEquals("Proposicao", proposicaoElement.getTagName());
        assertEquals("urn:1234", proposicaoElement.getAttribute("urn"));
        assertEquals("SIG", proposicaoElement.getAttribute("sigla"));
        assertEquals("123", proposicaoElement.getAttribute("numero"));
        assertEquals("2023", proposicaoElement.getAttribute("ano"));
        assertEquals("Ementa de teste", proposicaoElement.getAttribute("ementa"));
        assertEquals("Identificação teste", proposicaoElement.getAttribute("identificacaoTexto"));
        assertEquals("true", proposicaoElement.getAttribute("emendarTextoSubstitutivo"));
    }

    @Test
    void deveGerarProposicaoComEmentaEscapadaTest() {
        RefProposicaoEmendadaPojo proposicao = new RefProposicaoEmendadaPojo();
        proposicao.setUrn("urn:1234");
        proposicao.setSigla("SIG");
        proposicao.setNumero("123");
        proposicao.setAno("2023");
        proposicao.setEmenta("Ementa com & e < e >");
        proposicao.setIdentificacaoTexto("Identificação teste");
        proposicao.setEmendarTextoSubstitutivo(true);

        StringBuilder sb = new StringBuilder();
        marshaller.geraProposicao(proposicao, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element proposicaoElement = document.getDocumentElement();
        assertEquals("Ementa com & e", proposicaoElement.getAttribute("ementa"));
    }

    @Test
    void deveGerarColegiadoComComissaoTest() {
        ColegiadoApreciadorPojo colegiado = new ColegiadoApreciadorPojo();
        colegiado.setSiglaCasaLegislativa(SiglaCasaLegislativa.CN);
        colegiado.setTipoColegiado(TipoColegiado.COMISSAO);
        colegiado.setSiglaComissao("COMISSÃO 1");

        StringBuilder sb = new StringBuilder();
        marshaller.geraColegiado(colegiado, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element colegiadoElement = document.getDocumentElement();
        assertEquals("ColegiadoApreciador", colegiadoElement.getTagName());
        assertEquals(SiglaCasaLegislativa.CN.name(), colegiadoElement.getAttribute("siglaCasaLegislativa"));
        assertEquals("COMISSÃO 1", colegiadoElement.getAttribute("siglaComissao"));
        assertEquals("Comissão", colegiadoElement.getAttribute("tipoColegiado"));
    }

    @Test
    void deveGerarColegiadoComPlenarioTest() {
        ColegiadoApreciadorPojo colegiado = new ColegiadoApreciadorPojo();
        colegiado.setSiglaCasaLegislativa(SiglaCasaLegislativa.SF);
        colegiado.setTipoColegiado(TipoColegiado.PLENARIO_VIA_COMISSAO);
        colegiado.setSiglaComissao("COMISSÃO 2");

        StringBuilder sb = new StringBuilder();
        marshaller.geraColegiado(colegiado, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element colegiadoElement = document.getDocumentElement();
        assertEquals("ColegiadoApreciador", colegiadoElement.getTagName());
        assertEquals(SiglaCasaLegislativa.SF.name(), colegiadoElement.getAttribute("siglaCasaLegislativa"));
        assertEquals("COMISSÃO 2", colegiadoElement.getAttribute("siglaComissao"));
        assertEquals("Plenário via Comissão", colegiadoElement.getAttribute("tipoColegiado"));
    }

    @Test
    void deveGerarColegiadoSemComissaoTest() {
        ColegiadoApreciadorPojo colegiado = new ColegiadoApreciadorPojo();
        colegiado.setSiglaCasaLegislativa(SiglaCasaLegislativa.CD);
        colegiado.setTipoColegiado(TipoColegiado.PLENARIO);

        StringBuilder sb = new StringBuilder();
        marshaller.geraColegiado(colegiado, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element colegiadoElement = document.getDocumentElement();
        assertEquals("ColegiadoApreciador", colegiadoElement.getTagName());
        assertEquals(SiglaCasaLegislativa.CD.name(), colegiadoElement.getAttribute("siglaCasaLegislativa"));
        assertEquals(TipoColegiado.PLENARIO.getDescricao(), colegiadoElement.getAttribute("tipoColegiado"));
        assertEquals("", colegiadoElement.getAttribute("siglaComissao")); // Atributo não deve existir
    }

    @Test
    void deveGerarSubstituicaoTermoTest() {
        // Arrange
        SubstituicaoTermoPojo substituicaoTermo = new SubstituicaoTermoPojo();
        substituicaoTermo.setTipo(TipoSubstituicaoTermo.PALAVRA);
        substituicaoTermo.setTermo("termo original");
        substituicaoTermo.setNovoTermo("termo novo");
        substituicaoTermo.setFlexaoGenero(true);
        substituicaoTermo.setFlexaoNumero(false);


        StringBuilder sb = new StringBuilder();
        marshaller.geraSubstituicaoTermo(substituicaoTermo, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element substituicaoTermoElement = document.getDocumentElement();
        assertEquals("SubstituicaoTermo", substituicaoTermoElement.getTagName());
        assertEquals(TipoSubstituicaoTermo.PALAVRA.getDescricao(), substituicaoTermoElement.getAttribute("tipo"));
        assertEquals("termo original", substituicaoTermoElement.getAttribute("termo"));
        assertEquals("termo novo", substituicaoTermoElement.getAttribute("novoTermo"));
        assertEquals("true", substituicaoTermoElement.getAttribute("flexaoGenero"));
        assertEquals("false", substituicaoTermoElement.getAttribute("flexaoNumero"));
    }

    @Test
    void deveGerarSubstituicaoTermoComTermosEscapadosTest() {
        SubstituicaoTermoPojo substituicaoTermo = new SubstituicaoTermoPojo();
        substituicaoTermo.setTipo(TipoSubstituicaoTermo.PALAVRA);
        substituicaoTermo.setTermo("termo & especial < >");
        substituicaoTermo.setNovoTermo("novo termo & especial < >");
        substituicaoTermo.setFlexaoGenero(true);
        substituicaoTermo.setFlexaoNumero(false);

        StringBuilder sb = new StringBuilder();
        marshaller.geraSubstituicaoTermo(substituicaoTermo, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element substituicaoTermoElement = document.getDocumentElement();
        assertEquals("termo & especial < >", substituicaoTermoElement.getAttribute("termo"));
        assertEquals("novo termo & especial < >", substituicaoTermoElement.getAttribute("novoTermo"));
    }

    @Test
    void deveGerarSubstituicaoTermoNuloTest() {
        StringBuilder sb = new StringBuilder();
        marshaller.geraSubstituicaoTermo(null, sb);

        assertEquals(0, sb.length());
    }

    @Test
    void deveGerarEpigrafeComComplementoTest() {
        EpigrafePojo epigrafe = new EpigrafePojo();
        epigrafe.setTexto("Texto de epígrafe");
        epigrafe.setComplemento("Complemento de epígrafe");

        StringBuilder sb = new StringBuilder();
        marshaller.geraEpigrafe(epigrafe, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element epigrafeElement = document.getDocumentElement();
        assertEquals("Epigrafe", epigrafeElement.getTagName());
        assertEquals("Texto de epígrafe", epigrafeElement.getAttribute("texto"));
        assertEquals("Complemento de epígrafe", epigrafeElement.getAttribute("complemento"));
    }

    @Test
    void deveGerarEpigrafeSemComplementoTest() {
        EpigrafePojo epigrafe = new EpigrafePojo();
        epigrafe.setTexto("Texto sem complemento");

        StringBuilder sb = new StringBuilder();
        marshaller.geraEpigrafe(epigrafe, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element epigrafeElement = document.getDocumentElement();
        assertEquals("Epigrafe", epigrafeElement.getTagName());
        assertEquals("Texto sem complemento", epigrafeElement.getAttribute("texto"));
        assertEquals("", epigrafeElement.getAttribute("complemento")); // Atributo não deve existir
    }

    @Test
    void deveGerarEpigrafeComTextoEscapadoTest() {
        EpigrafePojo epigrafe = new EpigrafePojo();
        epigrafe.setTexto("Texto com & e < e >");
        epigrafe.setComplemento("Complemento com & e < e >");

        StringBuilder sb = new StringBuilder();
        marshaller.geraEpigrafe(epigrafe, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element epigrafeElement = document.getDocumentElement();
        assertEquals("Texto com & e < e >", epigrafeElement.getAttribute("texto"));
        assertEquals("Complemento com & e < e >", epigrafeElement.getAttribute("complemento"));
    }

    @Test
    void deveGerarComponentesSemAtributosTest() {
        List<ComponenteEmendado> componentes = new ArrayList<>();
        ComponenteEmendadoPojo componente = new ComponenteEmendadoPojo();
        componente.setUrn("urn:componente1");
        componente.setArticulado(true);
        componente.setDispositivos(new DispositivosEmendaPojo(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        componentes.add(componente);

        StringBuilder sb = new StringBuilder();
        marshaller.geraComponentes(componentes, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element componenteElement = document.getDocumentElement();
        assertEquals("Componente", componenteElement.getTagName());
        assertEquals("urn:componente1", componenteElement.getAttribute("urn"));
        assertEquals("true", componenteElement.getAttribute("articulado"));
        assertEquals("", componenteElement.getAttribute("tituloAnexo")); // Atributo não deve existir
        assertEquals("", componenteElement.getAttribute("rotuloAnexo")); // Atributo não deve existir
    }

    @Test
    void deveGerarComponentesComTodosAtributosTest() {
        List<ComponenteEmendado> componentes = new ArrayList<>();
        ComponenteEmendadoPojo componente = new ComponenteEmendadoPojo();
        componente.setUrn("urn:componente2");
        componente.setArticulado(false);
        componente.setTituloAnexo("Título Anexo");
        componente.setRotuloAnexo("Rótulo Anexo");
        componente.setDispositivos(new DispositivosEmendaPojo(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        componentes.add(componente);

        StringBuilder sb = new StringBuilder();
        marshaller.geraComponentes(componentes, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element componenteElement = document.getDocumentElement();
        assertEquals("Componente", componenteElement.getTagName());
        assertEquals("urn:componente2", componenteElement.getAttribute("urn"));
        assertEquals("false", componenteElement.getAttribute("articulado"));
        assertEquals("Título Anexo", componenteElement.getAttribute("tituloAnexo"));
        assertEquals("Rótulo Anexo", componenteElement.getAttribute("rotuloAnexo"));
    }

    @Test
    void deveGerarComponentesComTresComponentesTest() {
        List<ComponenteEmendado> componentes = new ArrayList<>();

        ComponenteEmendadoPojo componente1 = new ComponenteEmendadoPojo();
        componente1.setUrn("urn:componente3");
        componente1.setArticulado(true);
        componente1.setDispositivos(new DispositivosEmendaPojo(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        componentes.add(componente1);

        ComponenteEmendadoPojo componente2 = new ComponenteEmendadoPojo();
        componente2.setUrn("urn:componente4");
        componente2.setArticulado(false);
        componente2.setTituloAnexo("Título 1");
        componente2.setDispositivos(new DispositivosEmendaPojo(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        componentes.add(componente2);

        ComponenteEmendadoPojo componente3 = new ComponenteEmendadoPojo();
        componente3.setUrn("urn:componente5");
        componente3.setArticulado(true);
        componente3.setRotuloAnexo("Rótulo 1");
        componente3.setDispositivos(new DispositivosEmendaPojo(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        componentes.add(componente3);

        StringBuilder sb = new StringBuilder("<Emenda>"); // Para fins de teste unitário
        marshaller.geraComponentes(componentes, sb);
        sb.append("</Emenda>");

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        assertEquals(3, document.getElementsByTagName("Componente").getLength());

        Element componenteElement1 = (Element) document.getElementsByTagName("Componente").item(0);
        assertEquals("urn:componente3", componenteElement1.getAttribute("urn"));
        assertEquals("true", componenteElement1.getAttribute("articulado"));

        Element componenteElement2 = (Element) document.getElementsByTagName("Componente").item(1);
        assertEquals("urn:componente4", componenteElement2.getAttribute("urn"));
        assertEquals("false", componenteElement2.getAttribute("articulado"));
        assertEquals("Título 1", componenteElement2.getAttribute("tituloAnexo"));

        Element componenteElement3 = (Element) document.getElementsByTagName("Componente").item(2);
        assertEquals("urn:componente5", componenteElement3.getAttribute("urn"));
        assertEquals("true", componenteElement3.getAttribute("articulado"));
        assertEquals("Rótulo 1", componenteElement3.getAttribute("rotuloAnexo"));
    }

    @Test
    void deveGerarAnexosComListaVaziaTest() {
        List<Anexo> anexos = new ArrayList<>();

        StringBuilder sb = new StringBuilder("<Emenda>"); // Para fins de teste unitário
        marshaller.geraAnexos(anexos, sb);
        sb.append("</Emenda>");

        // Convert StringBuilder to Document
        Document document = createDocumentFromString(sb.toString());

        // Assert
        assertNotNull(document);
        assertEquals(0, document.getElementsByTagName("Anexo").getLength());
    }

    private static class AnexoPojoTest extends AnexoPojo {
        String nomeArquivo = "";
        String base64 = "";
        AnexoPojoTest(String nomeArquivo, String base64) {
            this.nomeArquivo = nomeArquivo;
            this.base64 = base64;
        }
        @Override
        public String getBase64() {
            return base64;
        }

        @Override
        public String getNomeArquivo() {
            return nomeArquivo;
        }
    }

    @Test
    void deveGeraAnexosComUmAnexoTest() {
        List<Anexo> anexos = new ArrayList<>();

        AnexoPojo anexo = new AnexoPojoTest("arquivo1.txt", "base64string1");
        anexos.add(anexo);

        StringBuilder sb = new StringBuilder("<Emenda>"); // Para fins de teste unitário
        marshaller.geraAnexos(anexos, sb);
        sb.append("</Emenda>");

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        Element anexoElement = (Element) document.getElementsByTagName("Anexo").item(0);
        assertEquals("arquivo1.txt", anexoElement.getAttribute("nomeArquivo"));
        assertEquals("base64string1", anexoElement.getAttribute("base64"));
    }

    @Test
    void deveGerarAnexosComMultiplosAnexosTest() {
        List<Anexo> anexos = new ArrayList<>();

        Anexo anexo1 = new AnexoPojoTest("arquivo1.txt", "base64string1");
        anexos.add(anexo1);

        Anexo anexo2 = new AnexoPojoTest("arquivo2.jpg", "base64string2");
        anexos.add(anexo2);

        Anexo anexo3 = new AnexoPojoTest("arquivo3.pdf", "base64string3");
        anexos.add(anexo3);

        StringBuilder sb = new StringBuilder("<Emenda>"); // Para fins de teste unitário
        marshaller.geraAnexos(anexos, sb);
        sb.append("</Emenda>");

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        assertEquals(3, document.getElementsByTagName("Anexo").getLength());

        Element anexoElement1 = (Element) document.getElementsByTagName("Anexo").item(0);
        assertEquals("arquivo1.txt", anexoElement1.getAttribute("nomeArquivo"));
        assertEquals("base64string1", anexoElement1.getAttribute("base64"));

        Element anexoElement2 = (Element) document.getElementsByTagName("Anexo").item(1);
        assertEquals("arquivo2.jpg", anexoElement2.getAttribute("nomeArquivo"));
        assertEquals("base64string2", anexoElement2.getAttribute("base64"));

        Element anexoElement3 = (Element) document.getElementsByTagName("Anexo").item(2);
        assertEquals("arquivo3.pdf", anexoElement3.getAttribute("nomeArquivo"));
        assertEquals("base64string3", anexoElement3.getAttribute("base64"));
    }

    @Test
    public void deveGerarDispositivoSuprimidoComTodosOsCamposTest() {
        DispositivoEmendaSuprimidoPojo suprimido = new DispositivoEmendaSuprimidoPojo();
        suprimido.setId("123");
        suprimido.setRotulo("Rotulo1");
        suprimido.setTipo("Tipo1");
        suprimido.setUrnNormaAlterada("urn:norma:alterada");
        StringBuilder sb = new StringBuilder();

        DispositivosEmenda dispositivos = new DispositivosEmendaPojo(List.of(suprimido), new ArrayList<>(), new ArrayList<>());

        marshaller.geraDispositivos(dispositivos, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        assertEquals("Dispositivos", document.getDocumentElement().getNodeName());

        Element dispositivo = (Element) document.getElementsByTagName("DispositivoSuprimido").item(0);
        assertEquals("Tipo1", dispositivo.getAttribute("tipo"));
        assertEquals("123", dispositivo.getAttribute("id"));
        assertEquals("Rotulo1", dispositivo.getAttribute("rotulo"));
        assertEquals("urn:norma:alterada", dispositivo.getAttribute("xml:base"));
    }

    @Test
    public void deveGerarDispositivosSuprimidosSemUrnNormaAlteradaTest() throws Exception {
        DispositivoEmendaSuprimidoPojo suprimido = new DispositivoEmendaSuprimidoPojo();
        suprimido.setId("123");
        suprimido.setRotulo("Rotulo1");
        suprimido.setTipo("Tipo1");
        StringBuilder sb = new StringBuilder();

        DispositivosEmenda dispositivos = new DispositivosEmendaPojo(List.of(suprimido), new ArrayList<>(), new ArrayList<>());

        marshaller.geraDispositivos(dispositivos, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        assertEquals("Dispositivos", document.getDocumentElement().getNodeName());

        Element dispositivo = (Element) document.getElementsByTagName("DispositivoSuprimido").item(0);
        assertEquals("Tipo1", dispositivo.getAttribute("tipo"));
        assertEquals("123", dispositivo.getAttribute("id"));
        assertEquals("Rotulo1", dispositivo.getAttribute("rotulo"));
        assertEquals("", dispositivo.getAttribute("xml:base"));
    }

    @Test
    public void deveGerarMaisDeUmDispositivoSuprimidoTest() {
        DispositivoEmendaSuprimidoPojo suprimido1 = new DispositivoEmendaSuprimidoPojo();
        suprimido1.setId("123");
        suprimido1.setRotulo("Rotulo1");
        suprimido1.setTipo("Tipo1");
        suprimido1.setUrnNormaAlterada("urn:norma:alterada");

        DispositivoEmendaSuprimidoPojo suprimido2 = new DispositivoEmendaSuprimidoPojo();
        suprimido2.setId("456");
        suprimido2.setRotulo("Rotulo2");
        suprimido2.setTipo("Tipo2");

        DispositivosEmenda dispositivos = new DispositivosEmendaPojo(Arrays.asList(suprimido1, suprimido2), new ArrayList<>(), new ArrayList<>());

        StringBuilder sb = new StringBuilder();
        marshaller.geraDispositivos(dispositivos, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);
        assertEquals("Dispositivos", document.getDocumentElement().getNodeName());
        assertEquals(2, document.getElementsByTagName("DispositivoSuprimido").getLength());
        assertEquals(0, document.getElementsByTagName("DispositivoModificado").getLength());
        assertEquals(0, document.getElementsByTagName("DispositivoAdicionado").getLength());

        Element dispositivoElement1 = (Element) document.getElementsByTagName("DispositivoSuprimido").item(0);
        assertNotNull(dispositivoElement1);
        assertEquals("Tipo1", dispositivoElement1.getAttribute("tipo"));
        assertEquals("123", dispositivoElement1.getAttribute("id"));
        assertEquals("Rotulo1", dispositivoElement1.getAttribute("rotulo"));
        assertEquals("urn:norma:alterada", dispositivoElement1.getAttribute("xml:base"));

        Element dispositivoElement2 = (Element) document.getElementsByTagName("DispositivoSuprimido").item(1);
        assertNotNull(dispositivoElement2);
        assertEquals("Tipo2", dispositivoElement2.getAttribute("tipo"));
        assertEquals("456", dispositivoElement2.getAttribute("id"));
        assertEquals("Rotulo2", dispositivoElement2.getAttribute("rotulo"));
        assertEquals("", dispositivoElement2.getAttribute("xml:base"));
    }

    @Test
    public void deveGerarDispositivoModificadoComTodosOsCamposTest() {
        DispositivoEmendaModificadoPojo modificado = new DispositivoEmendaModificadoPojo();
        modificado.setId("123");
        modificado.setRotulo("Rotulo1");
        modificado.setTipo("Tipo1");
        modificado.setUrnNormaAlterada("urn:norma:alterada");
        modificado.setTexto("Texto sem Alteração");
        modificado.setTextoOmitido(true);
        modificado.setAbreAspas(true);
        modificado.setFechaAspas(true);
        modificado.setNotaAlteracao(NotaAlteracao.AC);
        StringBuilder sb = new StringBuilder();

        DispositivosEmenda dispositivos = new DispositivosEmendaPojo(new ArrayList<>(), List.of(modificado), new ArrayList<>());

        marshaller.geraDispositivos(dispositivos, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        assertEquals("Dispositivos", document.getDocumentElement().getNodeName());

        Element dispositivo = (Element) document.getElementsByTagName("DispositivoModificado").item(0);
        assertEquals("Tipo1", dispositivo.getAttribute("tipo"));
        assertEquals("123", dispositivo.getAttribute("id"));
        assertEquals("Rotulo1", dispositivo.getAttribute("rotulo"));
        assertEquals("urn:norma:alterada", dispositivo.getAttribute("xml:base"));
        assertEquals("true", dispositivo.getAttribute("textoOmitido"));
        assertEquals("Texto sem Alteração", dispositivo.getElementsByTagName("Texto").item(0).getTextContent());
        assertEquals("true", dispositivo.getAttribute("abreAspas"));
        assertEquals("true", dispositivo.getAttribute("fechaAspas"));
        assertEquals(NotaAlteracao.AC.name(), dispositivo.getAttribute("notaAlteracao"));
    }

    @Test
    public void deveGerarDispositivosModificadosSemAtributosTest() throws Exception {
        DispositivoEmendaModificadoPojo modificado = new DispositivoEmendaModificadoPojo();
        modificado.setId("123");
        modificado.setRotulo("Rotulo1");
        modificado.setTipo("Tipo1");
        modificado.setTexto("Texto sem Alteração");
        StringBuilder sb = new StringBuilder();

        DispositivosEmenda dispositivos = new DispositivosEmendaPojo(new ArrayList<>(), List.of(modificado), new ArrayList<>());

        marshaller.geraDispositivos(dispositivos, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        assertEquals("Dispositivos", document.getDocumentElement().getNodeName());

        Element dispositivo = (Element) document.getElementsByTagName("DispositivoModificado").item(0);
        assertEquals("Tipo1", dispositivo.getAttribute("tipo"));
        assertEquals("123", dispositivo.getAttribute("id"));
        assertEquals("Rotulo1", dispositivo.getAttribute("rotulo"));
        assertEquals("Texto sem Alteração", dispositivo.getElementsByTagName("Texto").item(0).getTextContent());
        assertEquals("", dispositivo.getAttribute("xml:base"));
        assertEquals("", dispositivo.getAttribute("textoOmitido"));
        assertEquals("", dispositivo.getAttribute("abreAspas"));
        assertEquals("", dispositivo.getAttribute("fechaAspas"));
        assertEquals("", dispositivo.getAttribute("notaAlteracao"));
    }

    @Test
    public void deveGerarMaisDeUmDispositivoModificadoTest() {
        DispositivoEmendaModificadoPojo modificado1 = new DispositivoEmendaModificadoPojo();
        modificado1.setId("123");
        modificado1.setRotulo("Rotulo1");
        modificado1.setTipo("Tipo1");
        modificado1.setTexto("Texto sem Alteração 1");

        DispositivoEmendaModificadoPojo modificado2 = new DispositivoEmendaModificadoPojo();
        modificado2.setId("456");
        modificado2.setRotulo("Rotulo2");
        modificado2.setTipo("Tipo2");
        modificado2.setTexto("Texto sem Alteração 2");

        DispositivosEmenda dispositivos = new DispositivosEmendaPojo(new ArrayList<>(), Arrays.asList(modificado1, modificado2), new ArrayList<>());

        StringBuilder sb = new StringBuilder();
        marshaller.geraDispositivos(dispositivos, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);
        assertEquals("Dispositivos", document.getDocumentElement().getNodeName());
        assertEquals(0, document.getElementsByTagName("DispositivoSuprimido").getLength());
        assertEquals(2, document.getElementsByTagName("DispositivoModificado").getLength());
        assertEquals(0, document.getElementsByTagName("DispositivoAdicionado").getLength());

        Element dispositivoElement1 = (Element) document.getElementsByTagName("DispositivoModificado").item(0);
        assertNotNull(dispositivoElement1);
        assertEquals("Tipo1", dispositivoElement1.getAttribute("tipo"));
        assertEquals("123", dispositivoElement1.getAttribute("id"));
        assertEquals("Rotulo1", dispositivoElement1.getAttribute("rotulo"));
        assertEquals("Texto sem Alteração 1", dispositivoElement1.getElementsByTagName("Texto").item(0).getTextContent());

        Element dispositivoElement2 = (Element) document.getElementsByTagName("DispositivoModificado").item(1);
        assertNotNull(dispositivoElement2);
        assertEquals("Tipo2", dispositivoElement2.getAttribute("tipo"));
        assertEquals("456", dispositivoElement2.getAttribute("id"));
        assertEquals("Rotulo2", dispositivoElement2.getAttribute("rotulo"));
        assertEquals("Texto sem Alteração 2", dispositivoElement2.getElementsByTagName("Texto").item(0).getTextContent());
    }

    @Test
    public void deveGerarDispositivoAdicionadoComTodosOsCamposSemRotuloTest() {
        String tipoDispositivoFilho = "Tipo";
        DispositivoEmendaAdicionadoPojo adicionado = new DispositivoEmendaAdicionadoPojo();
        adicionado.setOndeCouber(true);
        adicionado.setIdPai("idPai");
        adicionado.setIdIrmaoAnterior("idIrmaoAnterior");
        adicionado.setIdPosicaoAgrupador("idPosicaoAgrupador");
        // Filho
        adicionado.setTipo(tipoDispositivoFilho);
        adicionado.setId("1");
        adicionado.setUuid2("uuid2");
        adicionado.setUrnNormaAlterada("urn:norma:alterada");
        adicionado.setExisteNaNormaAlterada(true);
        adicionado.setTextoOmitido(true);
        adicionado.setAbreAspas(true);
        adicionado.setFechaAspas(true);
        adicionado.setNotaAlteracao(NotaAlteracao.AC);
        StringBuilder sb = new StringBuilder();

        DispositivosEmenda dispositivos = new DispositivosEmendaPojo(new ArrayList<>(), new ArrayList<>(), List.of(adicionado));

        marshaller.geraDispositivos(dispositivos, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        assertEquals("Dispositivos", document.getDocumentElement().getNodeName());

        Element dispositivo = (Element) document.getElementsByTagName("DispositivoAdicionado").item(0);
        assertEquals("true", dispositivo.getAttribute("ondeCouber"));
        assertEquals("idPai", dispositivo.getAttribute("idPai"));
        assertEquals("idIrmaoAnterior", dispositivo.getAttribute("idIrmaoAnterior"));
        assertEquals("idPosicaoAgrupador", dispositivo.getAttribute("idPosicaoAgrupador"));

        Element dispositivoFilho = (Element) dispositivo.getElementsByTagName(tipoDispositivoFilho).item(0);
        assertNotNull(dispositivoFilho);
        assertEquals("1", dispositivoFilho.getAttribute("id"));
        assertEquals("uuid2", dispositivoFilho.getAttribute("uuid2"));
        assertEquals("urn:norma:alterada", dispositivoFilho.getAttribute("xml:base"));
        assertEquals("true", dispositivoFilho.getAttribute("existeNaNormaAlterada"));
        assertEquals("s", dispositivoFilho.getAttribute("textoOmitido"));
        assertEquals("s", dispositivoFilho.getAttribute("abreAspas"));
        assertEquals("s", dispositivoFilho.getAttribute("fechaAspas"));
        assertEquals(NotaAlteracao.AC.name(), dispositivoFilho.getAttribute("notaAlteracao"));
    }

    @Test
    public void deveGerarDispositivoAdicionadoComTodosOsCamposComRotuloTest() {
        String tipoDispositivoFilho = "Tipo";
        DispositivoEmendaAdicionadoPojo adicionado = new DispositivoEmendaAdicionadoPojo();
        adicionado.setOndeCouber(true);
        adicionado.setIdPai("idPai");
        adicionado.setIdIrmaoAnterior("idIrmaoAnterior");
        adicionado.setIdPosicaoAgrupador("idPosicaoAgrupador");
        // Filho
        adicionado.setTipo(tipoDispositivoFilho);
        adicionado.setId("1");
        adicionado.setUuid2("uuid2");
        adicionado.setUrnNormaAlterada("urn:norma:alterada");
        adicionado.setExisteNaNormaAlterada(true);
        adicionado.setTextoOmitido(true);
        adicionado.setAbreAspas(true);
        adicionado.setFechaAspas(true);
        adicionado.setNotaAlteracao(NotaAlteracao.AC);
        adicionado.setRotulo("Rotulo");
        adicionado.setTexto("Texto");
        adicionado.setNotaAlteracao(NotaAlteracao.AC);
        StringBuilder sb = new StringBuilder();

        DispositivosEmenda dispositivos = new DispositivosEmendaPojo(new ArrayList<>(), new ArrayList<>(), List.of(adicionado));

        marshaller.geraDispositivos(dispositivos, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        assertEquals("Dispositivos", document.getDocumentElement().getNodeName());

        Element dispositivo = (Element) document.getElementsByTagName("DispositivoAdicionado").item(0);
        assertEquals("true", dispositivo.getAttribute("ondeCouber"));
        assertEquals("idPai", dispositivo.getAttribute("idPai"));
        assertEquals("idIrmaoAnterior", dispositivo.getAttribute("idIrmaoAnterior"));
        assertEquals("idPosicaoAgrupador", dispositivo.getAttribute("idPosicaoAgrupador"));

        Element dispositivoFilho = (Element) dispositivo.getElementsByTagName(tipoDispositivoFilho).item(0);
        assertNotNull(dispositivoFilho);
        assertEquals("1", dispositivoFilho.getAttribute("id"));
        assertEquals("uuid2", dispositivoFilho.getAttribute("uuid2"));
        assertEquals("urn:norma:alterada", dispositivoFilho.getAttribute("xml:base"));
        assertEquals("true", dispositivoFilho.getAttribute("existeNaNormaAlterada"));
        assertEquals("s", dispositivoFilho.getAttribute("textoOmitido"));
        assertEquals("s", dispositivoFilho.getAttribute("abreAspas"));
        assertEquals("s", dispositivoFilho.getAttribute("fechaAspas"));
        assertEquals(NotaAlteracao.AC.name(), dispositivoFilho.getAttribute("notaAlteracao"));


        Element dispositivoRotulo = (Element) dispositivoFilho.getElementsByTagName("Rotulo").item(0);
        Element dispositivoParagrafo = (Element) dispositivoFilho.getElementsByTagName("p").item(0);
        assertNotNull(dispositivoRotulo);
        assertNotNull(dispositivoParagrafo);

        assertEquals("Rotulo", dispositivoRotulo.getTextContent());
        assertEquals("Texto", dispositivoParagrafo.getTextContent());
    }

    @Test
    public void deveGerarComandoEmendaSemCabecalhoComumSemItensTest() {
        ComandoEmendaPojo comandoEmenda = new ComandoEmendaPojo();
        comandoEmenda.setComandos(new ArrayList<>());
        StringBuilder sb = new StringBuilder();

        marshaller.geraComandoEmenda(comandoEmenda, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        Element element = document.getDocumentElement();

        assertEquals("ComandoEmenda", element.getNodeName());
        assertNull(element.getElementsByTagName("CabecalhoComum").item(0));
        assertEquals(0, element.getElementsByTagName("ItemComandoEmenda").getLength());
    }

    @Test
    public void deveGerarComandoEmendaComCabecalhoComumSemItensTest() {
        ComandoEmendaPojo comandoEmenda = new ComandoEmendaPojo();
        comandoEmenda.setComandos(new ArrayList<>());
        comandoEmenda.setCabecalhoComum("Cabeçalho Comum");
        StringBuilder sb = new StringBuilder();

        marshaller.geraComandoEmenda(comandoEmenda, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        Element element = document.getDocumentElement();

        assertEquals("ComandoEmenda", element.getNodeName());
        assertEquals("Cabeçalho Comum", element.getElementsByTagName("CabecalhoComum").item(0).getTextContent());
        assertEquals(0, element.getElementsByTagName("ItemComandoEmenda").getLength());
    }

    @Test
    public void deveGerarComandoEmendaComCabecalhoSemItensComandoCompletosTest() {
        ItemComandoEmendaPojo item = new ItemComandoEmendaPojo();
        item.setCabecalho("Item Cabecalho");

        ComandoEmendaPojo comandoEmenda = new ComandoEmendaPojo();
        comandoEmenda.setComandos(List.of(item));
        comandoEmenda.setCabecalhoComum("Cabeçalho Comum");
        StringBuilder sb = new StringBuilder();

        marshaller.geraComandoEmenda(comandoEmenda, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        Element element = document.getDocumentElement();

        assertEquals("ComandoEmenda", element.getNodeName());
        assertEquals("Cabeçalho Comum", element.getElementsByTagName("CabecalhoComum").item(0).getTextContent());
        assertEquals(1, element.getElementsByTagName("ItemComandoEmenda").getLength());
    }


    @Test
    public void deveGerarComandoEmendaComCabecalhoComDoisItensComandoTest() {
        ItemComandoEmendaPojo item1 = new ItemComandoEmendaPojo();
        item1.setCabecalho("Item Cabecalho 1");

        ItemComandoEmendaPojo item2 = new ItemComandoEmendaPojo();
        item2.setCabecalho("Item Cabecalho 2");

        ComandoEmendaPojo comandoEmenda = new ComandoEmendaPojo();
        comandoEmenda.setComandos(Arrays.asList(item1, item2));
        comandoEmenda.setCabecalhoComum("Cabeçalho Comum");
        StringBuilder sb = new StringBuilder();

        marshaller.geraComandoEmenda(comandoEmenda, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        Element element = document.getDocumentElement();

        assertEquals("ComandoEmenda", element.getNodeName());
        assertEquals("Cabeçalho Comum", element.getElementsByTagName("CabecalhoComum").item(0).getTextContent());
        assertEquals(2, element.getElementsByTagName("ItemComandoEmenda").getLength());
    }

    @Test
    public void deveGerarComandoEmendaComCabecalhoComItensComandoCompletosTest() {
        ItemComandoEmendaPojo item = new ItemComandoEmendaPojo();
        item.setCabecalho("Item Cabecalho");
        item.setRotulo("Texto Rotulo");
        item.setCabecalho("Texto Cabecalho");
        item.setCitacao("Texto Citacao");
        item.setComplemento("Texto Complemento");

        ComandoEmendaPojo comandoEmenda = new ComandoEmendaPojo();
        comandoEmenda.setComandos(List.of(item));
        comandoEmenda.setCabecalhoComum("Cabeçalho Comum");
        StringBuilder sb = new StringBuilder();

        marshaller.geraComandoEmenda(comandoEmenda, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        Element element = document.getDocumentElement();

        assertEquals("ComandoEmenda", element.getNodeName());
        assertEquals("Cabeçalho Comum", element.getElementsByTagName("CabecalhoComum").item(0).getTextContent());
        assertEquals(1, element.getElementsByTagName("ItemComandoEmenda").getLength());

        Element itemElement = (Element) element.getElementsByTagName("ItemComandoEmenda").item(0);
        assertEquals(1, itemElement.getElementsByTagName("Rotulo").getLength());
        assertEquals("Texto Rotulo", element.getElementsByTagName("Rotulo").item(0).getTextContent());
        assertEquals(1, itemElement.getElementsByTagName("Cabecalho").getLength());
        assertEquals("Texto Cabecalho", element.getElementsByTagName("Cabecalho").item(0).getTextContent());
        assertEquals(1, itemElement.getElementsByTagName("Citacao").getLength());
        assertEquals("Texto Citacao", element.getElementsByTagName("Citacao").item(0).getTextContent());
        assertEquals(1, itemElement.getElementsByTagName("Complemento").getLength());
        assertEquals("Texto Complemento", element.getElementsByTagName("Complemento").item(0).getTextContent());
    }

    @Test
    public void testGeraComandoEmendaTextoLivreComMotivoETexto() throws Exception {
        ComandoEmendaTextoLivrePojo comando = new ComandoEmendaTextoLivrePojo();
        comando.setTexto("Texto de teste");
        comando.setMotivo("Motivo de Teste");
        comando.setTextoAntesRevisao("Texto Antes de Revisao");

        StringBuilder sb = new StringBuilder("<Emenda>");
        marshaller.geraComandoEmendaTextoLivre(comando, sb);
        sb.append("</Emenda>");

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        Element elementComandoEmendaTextoLivre = (Element) document.getElementsByTagName("ComandoEmendaTextoLivre").item(0);

        assertEquals("Motivo de Teste", elementComandoEmendaTextoLivre.getAttribute("motivo"));
        assertEquals("Texto de teste", elementComandoEmendaTextoLivre.getTextContent().trim());

        Element elementComandoEmendaTextoLivreAntesRevisao = (Element) document.getElementsByTagName("ComandoEmendaTextoLivreAntesRevisao").item(0);
        assertNotNull(elementComandoEmendaTextoLivreAntesRevisao);
        assertEquals("Texto Antes de Revisao", elementComandoEmendaTextoLivreAntesRevisao.getTextContent().trim());
    }

    @Test
    public void testGeraComandoEmendaTextoLivreSemMotivoESemTexto() throws Exception {
        ComandoEmendaTextoLivrePojo comando = new ComandoEmendaTextoLivrePojo();
        comando.setTexto("Texto Antes de Revisao");

        StringBuilder sb = new StringBuilder();
        marshaller.geraComandoEmendaTextoLivre(comando, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        Element element = document.getDocumentElement();

        assertEquals("ComandoEmendaTextoLivre", element.getNodeName());
        assertEquals("", element.getAttribute("motivo"));
        assertEquals("Texto Antes de Revisao", element.getTextContent().trim());
    }

    @Test
    public void testGeraJustificativaComTextoEscapado() throws Exception {
        String justificativa = "Justificativa com caracteres especiais: <, >, &";

        StringBuilder sb = new StringBuilder();
        marshaller.geraJustificativa(justificativa, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        Element element = document.getDocumentElement();

        assertEquals("Justificativa", element.getNodeName());
        assertEquals("Justificativa com caracteres especiais: <, >, &", element.getTextContent().trim());
    }

    @Test
    public void testGeraJustificativaSemTexto() throws Exception {
        String justificativa = null;

        StringBuilder sb = new StringBuilder();
        marshaller.geraJustificativa(justificativa, sb);

        assertEquals(0, sb.length());
    }

    @Test
    public void testGeraJustificativaAntesRevisaoComTexto() throws Exception {
        String justificativa = "Esta é uma justificativa antes da revisão.";
        StringBuilder sb = new StringBuilder();

        marshaller.geraJustificativaAntesRevisao(justificativa, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);
        Element element = document.getDocumentElement();

        assertEquals("JustificativaAntesRevisao", element.getNodeName());
        assertEquals("Esta é uma justificativa antes da revisão.", element.getTextContent().trim());
    }

    @Test
    public void testGeraJustificativaAntesRevisaoComTextoEscapado() throws Exception {
        String justificativa = "Justificativa com caracteres especiais: <, >, &";
        StringBuilder sb = new StringBuilder();

        marshaller.geraJustificativaAntesRevisao(justificativa, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);
        Element element = document.getDocumentElement();

        assertEquals("JustificativaAntesRevisao", element.getNodeName());
        assertEquals(justificativa, element.getTextContent().trim());
    }

    @Test
    public void testGeraAutoriaComTodosOsCampos() throws Exception {
        AutoriaPojo autoria = new AutoriaPojo();
        autoria.setTipo(TipoAutoria.NAO_IDENTIFICADO);
        autoria.setImprimirPartidoUF(true);
        autoria.setQuantidadeAssinaturasAdicionaisDeputados(3);
        autoria.setQuantidadeAssinaturasAdicionaisSenadores(2);

        ParlamentarPojo parlamentarPojo = new ParlamentarPojo();
        parlamentarPojo.setIdentificacao("12345");
        parlamentarPojo.setNome("João da Silva");
        parlamentarPojo.setSiglaPartido("PSL");
        parlamentarPojo.setSiglaUF("SP");
        parlamentarPojo.setSiglaCasaLegislativa(SiglaCasaLegislativa.CD);
        parlamentarPojo.setSexo(Sexo.M);
        autoria.setParlamentares(List.of(parlamentarPojo));

        ColegiadoAutorPojo colegiado = new ColegiadoAutorPojo();
        colegiado.setNome("Colegiado");
        colegiado.setSigla("C");
        colegiado.setIdentificacao("Identificação");
        autoria.setColegiado(colegiado);

        StringBuilder sb = new StringBuilder();
        marshaller.geraAutoria(autoria, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);
        Element element = document.getDocumentElement();

        assertEquals("Autoria", element.getNodeName());
        assertEquals(TipoAutoria.NAO_IDENTIFICADO.getDescricao(), element.getAttribute("tipo"));
        assertEquals("true", element.getAttribute("imprimirPartidoUF"));
        assertEquals("3", element.getAttribute("quantidadeAssinaturasAdicionaisDeputados"));
        assertEquals("2", element.getAttribute("quantidadeAssinaturasAdicionaisSenadores"));
        assertEquals(1, element.getElementsByTagName("Parlamentar").getLength());
        assertEquals(1, element.getElementsByTagName("Colegiado").getLength());
    }

    @Test
    public void testGeraOpcoesImpressaoComTodosOsCampos() throws Exception {
        OpcoesImpressaoPojo opcoesImpressao = new OpcoesImpressaoPojo();
        opcoesImpressao.setImprimirBrasao(true);
        opcoesImpressao.setTextoCabecalho("Cabeçalho de Teste");
        opcoesImpressao.setTamanhoFonte(12);
        opcoesImpressao.setReduzirEspacoEntreLinhas(false);

        StringBuilder sb = new StringBuilder();
        marshaller.geraOpcoesImpressao(opcoesImpressao, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);
        Element element = document.getDocumentElement();

        assertEquals("OpcoesImpressao", element.getNodeName());
        assertEquals("true", element.getAttribute("imprimirBrasao"));
        assertEquals("Cabeçalho de Teste", element.getAttribute("textoCabecalho"));
        assertEquals("12", element.getAttribute("tamanhoFonte"));
        assertEquals("false", element.getAttribute("reduzirEspacoEntreLinhas"));
    }

    @Test
    public void testGeraOpcoesImpressaoSemTextoCabecalho() throws Exception {
        OpcoesImpressaoPojo opcoesImpressao = new OpcoesImpressaoPojo();
        opcoesImpressao.setImprimirBrasao(false);
        opcoesImpressao.setTamanhoFonte(10);
        opcoesImpressao.setReduzirEspacoEntreLinhas(true);

        StringBuilder sb = new StringBuilder();
        marshaller.geraOpcoesImpressao(opcoesImpressao, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);
        Element element = document.getDocumentElement();

        assertEquals("OpcoesImpressao", element.getNodeName());
        assertEquals("false", element.getAttribute("imprimirBrasao"));
        assertEquals("10", element.getAttribute("tamanhoFonte"));
        assertEquals("true", element.getAttribute("reduzirEspacoEntreLinhas"));
        assertEquals("", element.getAttribute("textoCabecalho"));
    }

    @Test
    public void testGeraRevisoesComElementos() throws Exception {
        List<Revisao> revisoes = new ArrayList<>();
        revisoes.add(new RevisaoElementoPojo("1", "State 1", null, null, "1",  "1"));
        revisoes.add(new RevisaoJustificativaPojo("Type 2", "2", null, "2024-10-24T11:00:00",  "Descricao 2"));
        revisoes.add(new RevisaoTextoLivrePojo("Type 3", "3", null, "2024-10-24T11:00:00",  "Descricao 3"));

        StringBuilder sb = new StringBuilder();
        marshaller.geraRevisoes(revisoes, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);
        Element root = document.getDocumentElement();
        assertNotNull(root);

        List<Element> elements = getOnlyElements(root);

        assertEquals("Revisoes", root.getNodeName());
        assertEquals(3, elements.size());

        Element primeiroElemento = elements.get(0);
        assertEquals("RevisaoElemento", primeiroElemento.getNodeName());
        assertEquals("1", primeiroElemento.getAttribute("actionType"));
        assertEquals("State 1", primeiroElemento.getAttribute("stateType"));
        assertEquals("1", primeiroElemento.getAttribute("idRevisaoElementoPai"));
        assertEquals("1", primeiroElemento.getAttribute("idRevisaoElementoPrincipal"));

        Element segundoElemento = elements.get(1);
        assertEquals("RevisaoJustificativa", segundoElemento.getNodeName());
        assertEquals("2", segundoElemento.getAttribute("id"));
        assertEquals("Type 2", segundoElemento.getAttribute("type"));
        assertEquals("2024-10-24T11:00:00", segundoElemento.getAttribute("dataHora"));
        assertEquals("Descricao 2", segundoElemento.getAttribute("descricao"));

        Element terceiroElemento = elements.get(2);
        assertEquals("RevisaoTextoLivre", terceiroElemento.getNodeName());
        assertEquals("3", terceiroElemento.getAttribute("id"));
        assertEquals("Type 3", terceiroElemento.getAttribute("type"));
        assertEquals("2024-10-24T11:00:00", terceiroElemento.getAttribute("dataHora"));
        assertEquals("Descricao 3", terceiroElemento.getAttribute("descricao"));
    }

    private static List<Element> getOnlyElements(Element root) {
        List<Element> elements = IntStream.range(0, root.getChildNodes().getLength())
                .mapToObj(root.getChildNodes()::item)
                .filter(node -> node instanceof Element)
                .map(node -> (Element) node)
                .collect(Collectors.toList());
        return elements;
    }

    @Test
    public void testGeraNotasRodapeComElementos() throws Exception {
        List<NotaRodape> notasRodape = new ArrayList<>();
        notasRodape.add(new NotaRodapePojo("1", 1, "Nota de Rodapé 1"));
        notasRodape.add(new NotaRodapePojo("2", 2, "Nota de Rodapé 2"));

        StringBuilder sb = new StringBuilder();
        marshaller.geraNotasRodape(notasRodape, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);
        Element root = document.getDocumentElement();
        assertNotNull(root);

        List<Element> elements = getOnlyElements(root);

        assertEquals("NotasRodape", root.getNodeName());
        assertEquals(2, elements.size());

        Element nota1 = elements.get(0);
        assertEquals("1", nota1.getAttribute("id"));
        assertEquals("1", nota1.getAttribute("numero"));
        assertEquals("Nota de Rodapé 1", nota1.getAttribute("texto").trim());

        Element nota2 = elements.get(1);
        assertEquals("2", nota2.getAttribute("id"));
        assertEquals("2", nota2.getAttribute("numero"));
        assertEquals("Nota de Rodapé 2", nota2.getAttribute("texto").trim());
    }

    @Test
    public void testGeraNotasRodapeSemElementos() throws Exception {
        List<NotaRodape> notasRodape = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        marshaller.geraNotasRodape(notasRodape, sb);

        assertEquals(0, sb.length());
    }

    @Test
    public void testGeraPendenciasPreenchimentoComElementos() throws Exception {
        List<String> pendenciasPreenchimento = Arrays.asList("Pendência 1", "Pendência 2");

        StringBuilder sb = new StringBuilder();
        marshaller.geraPendenciasPreenchimento(pendenciasPreenchimento, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);
        Element root = document.getDocumentElement();
        assertNotNull(root);

        List<Element> elements = getOnlyElements(root);

        assertEquals("PendenciasPreenchimento", root.getNodeName());
        assertEquals(2, elements.size());

        Element pendencia1 = elements.get(0);
        assertEquals("PendenciaPreenchimento", pendencia1.getNodeName());
        assertEquals("Pendência 1", pendencia1.getTextContent().trim());

        Element pendencia2 = elements.get(1);
        assertEquals("PendenciaPreenchimento", pendencia2.getNodeName());
        assertEquals("Pendência 2", pendencia2.getTextContent().trim());
    }

    @Test
    public void testGeraPendenciasPreenchimentoSemElementos() throws Exception {
        List<String> pendenciasPreenchimento = List.of();

        StringBuilder sb = new StringBuilder();
        marshaller.geraPendenciasPreenchimento(pendenciasPreenchimento, sb);

        assertEquals(0, sb.length());
    }

    private Emenda setupEmenda() {

        ClassLoader classLoader = getClass().getClassLoader();
        try {
            URL sourceUrl = classLoader.getResource("test1.json");
            assert sourceUrl != null;
            File file = new File(sourceUrl.getFile());
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(text, EmendaPojo.class);                     

        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Document createDocumentFromString(String xml) {
        try {
            return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Source getXmlSource(Emenda emenda) {

        final EmendaXmlMarshaller marshaller = new EmendaXmlMarshaller();

        final String xml = marshaller.toXml(emenda);

        return Input.fromString(xml).build();
    }

}
