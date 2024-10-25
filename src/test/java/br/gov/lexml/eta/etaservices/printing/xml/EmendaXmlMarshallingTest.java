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
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class EmendaXmlMarshallingTest {
    private Source xmlSource;
    private DocumentBuilder builder;
    private final EmendaXmlMarshaller marshaller = new EmendaXmlMarshaller();

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

        Document document = DocumentHelper.createDocument();
        marshaller.geraCabecalhoEmenda(emenda, document);

        // Acessa o elemento raiz
        Element emendaElement = document.getRootElement();
        assertEquals("Emenda", emendaElement.getName());
        assertEquals("1.0", emendaElement.attributeValue("versaoFormatoArquivo"));
        assertEquals("Local Teste", emendaElement.attributeValue("local"));
        assertEquals("2023-10-23", emendaElement.attributeValue("data"));
    }

    @Test
    void deveGerarCabecalhoEmendaSemDataTest() {
        EmendaPojo emenda = new EmendaPojo();
        emenda.setLocal("Local Teste");
        emenda.setData(null); // Sem data

        Document document = DocumentHelper.createDocument();
        marshaller.geraCabecalhoEmenda(emenda, document);

        assertNotNull(document);
        Element emendaElement = document.getRootElement();
        assertEquals("Emenda", emendaElement.getName());
        assertEquals("1.0", emendaElement.attributeValue("versaoFormatoArquivo"));
        assertEquals("Local Teste", emendaElement.attributeValue("local"));
        assertEquals(null, emendaElement.attributeValue("data")); // Atributo "data" deve estar vazio
    }

    @Test
    void deveGerarMetadadosDadosBasicosTest() {
        EmendaPojo emenda = new EmendaPojo();
        emenda.setDataUltimaModificacao(Instant.parse("2023-10-23T10:00:00Z"));
        emenda.setAplicacao("Aplicação Teste");
        emenda.setVersaoAplicacao("1.0.0");
        emenda.setModoEdicao(ModoEdicaoEmenda.EMENDA);
        emenda.setMetadados(new HashMap<>()); // Sem metadados

        Document document = DocumentHelper.createDocument();
        marshaller.geraMetadados(emenda, document);

        Element metadadosElement = document.getRootElement();
        assertEquals("Metadados", metadadosElement.getName());
        assertEquals("2023-10-23T10:00:00Z", metadadosElement.selectNodes("//DataUltimaModificacao").get(0).getText());
        assertEquals("Aplicação Teste", metadadosElement.selectNodes("//Aplicacao").get(0).getText());
        assertEquals("1.0.0", metadadosElement.selectNodes("//VersaoAplicacao").get(0).getText());
        assertEquals(ModoEdicaoEmenda.EMENDA.getNome(), metadadosElement.selectNodes("//ModoEdicao").get(0).getText());
        assertEquals(0, metadadosElement.selectNodes("//Autor").size()); // Sem metadados
        assertEquals(0, metadadosElement.selectNodes("//Revisao").size()); // Sem metadados
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

        Document document = DocumentHelper.createDocument();
        marshaller.geraMetadados(emenda, document);

        assertNotNull(document);
        Element metadadosElement = document.getRootElement();
        assertEquals("Metadados", metadadosElement.getName());
        assertEquals("2023-10-23T10:00:00Z", document.selectNodes("//DataUltimaModificacao").get(0).getText());
        assertEquals("Aplicação Teste", document.selectNodes("//Aplicacao").get(0).getText());
        assertEquals("1.0.0", document.selectNodes("//VersaoAplicacao").get(0).getText());
        assertEquals(ModoEdicaoEmenda.EMENDA.getNome(), document.selectNodes("//ModoEdicao").get(0).getText());
        assertEquals("João", document.selectNodes("//Autor").get(0).getText());
        assertEquals("1", document.selectNodes("//Revisao").get(0).getText());
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
        Element proposicaoElement = document.getRootElement();
        assertEquals("Proposicao", proposicaoElement.getName());
        assertEquals("urn:1234", proposicaoElement.attributeValue("urn"));
        assertEquals("SIG", proposicaoElement.attributeValue("sigla"));
        assertEquals("123", proposicaoElement.attributeValue("numero"));
        assertEquals("2023", proposicaoElement.attributeValue("ano"));
        assertEquals("Ementa de teste", proposicaoElement.attributeValue("ementa"));
        assertEquals("Identificação teste", proposicaoElement.attributeValue("identificacaoTexto"));
        assertEquals("true", proposicaoElement.attributeValue("emendarTextoSubstitutivo"));
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
        Element proposicaoElement = document.getRootElement();
        assertEquals("Ementa com & e", proposicaoElement.attributeValue("ementa"));
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
        Element colegiadoElement = document.getRootElement();
        assertEquals("ColegiadoApreciador", colegiadoElement.getName());
        assertEquals(SiglaCasaLegislativa.CN.name(), colegiadoElement.attributeValue("siglaCasaLegislativa"));
        assertEquals("COMISSÃO 1", colegiadoElement.attributeValue("siglaComissao"));
        assertEquals("Comissão", colegiadoElement.attributeValue("tipoColegiado"));
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
        Element colegiadoElement = document.getRootElement();
        assertEquals("ColegiadoApreciador", colegiadoElement.getName());
        assertEquals(SiglaCasaLegislativa.SF.name(), colegiadoElement.attributeValue("siglaCasaLegislativa"));
        assertEquals("COMISSÃO 2", colegiadoElement.attributeValue("siglaComissao"));
        assertEquals("Plenário via Comissão", colegiadoElement.attributeValue("tipoColegiado"));
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
        Element colegiadoElement = document.getRootElement();
        assertEquals("ColegiadoApreciador", colegiadoElement.getName());
        assertEquals(SiglaCasaLegislativa.CD.name(), colegiadoElement.attributeValue("siglaCasaLegislativa"));
        assertEquals(TipoColegiado.PLENARIO.getDescricao(), colegiadoElement.attributeValue("tipoColegiado"));
        assertEquals(null, colegiadoElement.attributeValue("siglaComissao")); // Atributo não deve existir
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
        Element substituicaoTermoElement = document.getRootElement();
        assertEquals("SubstituicaoTermo", substituicaoTermoElement.getName());
        assertEquals(TipoSubstituicaoTermo.PALAVRA.getDescricao(), substituicaoTermoElement.attributeValue("tipo"));
        assertEquals("termo original", substituicaoTermoElement.attributeValue("termo"));
        assertEquals("termo novo", substituicaoTermoElement.attributeValue("novoTermo"));
        assertEquals("true", substituicaoTermoElement.attributeValue("flexaoGenero"));
        assertEquals("false", substituicaoTermoElement.attributeValue("flexaoNumero"));
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
        Element substituicaoTermoElement = document.getRootElement();
        assertEquals("termo & especial < >", substituicaoTermoElement.attributeValue("termo"));
        assertEquals("novo termo & especial < >", substituicaoTermoElement.attributeValue("novoTermo"));
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
        Element epigrafeElement = document.getRootElement();
        assertEquals("Epigrafe", epigrafeElement.getName());
        assertEquals("Texto de epígrafe", epigrafeElement.attributeValue("texto"));
        assertEquals("Complemento de epígrafe", epigrafeElement.attributeValue("complemento"));
    }

    @Test
    void deveGerarEpigrafeSemComplementoTest() {
        EpigrafePojo epigrafe = new EpigrafePojo();
        epigrafe.setTexto("Texto sem complemento");

        StringBuilder sb = new StringBuilder();
        marshaller.geraEpigrafe(epigrafe, sb);

        Document document = createDocumentFromString(sb.toString());

        assertNotNull(document);
        Element epigrafeElement = document.getRootElement();
        assertEquals("Epigrafe", epigrafeElement.getName());
        assertEquals("Texto sem complemento", epigrafeElement.attributeValue("texto"));
        assertEquals(null, epigrafeElement.attributeValue("complemento")); // Atributo não deve existir
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
        Element epigrafeElement = document.getRootElement();
        assertEquals("Texto com & e < e >", epigrafeElement.attributeValue("texto"));
        assertEquals("Complemento com & e < e >", epigrafeElement.attributeValue("complemento"));
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
        Element componenteElement = document.getRootElement();
        assertEquals("Componente", componenteElement.getName());
        assertEquals("urn:componente1", componenteElement.attributeValue("urn"));
        assertEquals("true", componenteElement.attributeValue("articulado"));
        assertEquals(null, componenteElement.attributeValue("tituloAnexo")); // Atributo não deve existir
        assertEquals(null, componenteElement.attributeValue("rotuloAnexo")); // Atributo não deve existir
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
        Element componenteElement = document.getRootElement();
        assertEquals("Componente", componenteElement.getName());
        assertEquals("urn:componente2", componenteElement.attributeValue("urn"));
        assertEquals("false", componenteElement.attributeValue("articulado"));
        assertEquals("Título Anexo", componenteElement.attributeValue("tituloAnexo"));
        assertEquals("Rótulo Anexo", componenteElement.attributeValue("rotuloAnexo"));
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
        assertEquals(3, document.selectNodes("//Componente").size());

        Element componenteElement1 = (Element) document.selectNodes("//Componente").get(0);
        assertEquals("urn:componente3", componenteElement1.attributeValue("urn"));
        assertEquals("true", componenteElement1.attributeValue("articulado"));

        Element componenteElement2 = (Element) document.selectNodes("//Componente").get(1);
        assertEquals("urn:componente4", componenteElement2.attributeValue("urn"));
        assertEquals("false", componenteElement2.attributeValue("articulado"));
        assertEquals("Título 1", componenteElement2.attributeValue("tituloAnexo"));

        Element componenteElement3 = (Element) document.selectNodes("//Componente").get(2);
        assertEquals("urn:componente5", componenteElement3.attributeValue("urn"));
        assertEquals("true", componenteElement3.attributeValue("articulado"));
        assertEquals("Rótulo 1", componenteElement3.attributeValue("rotuloAnexo"));
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
        assertEquals(0, document.selectNodes("//Anexo").size());
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

        Element anexoElement = (Element) document.selectNodes("//Anexo").get(0);
        assertEquals("arquivo1.txt", anexoElement.attributeValue("nomeArquivo"));
        assertEquals("base64string1", anexoElement.attributeValue("base64"));
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

        assertEquals(3, document.selectNodes("//Anexo").size());

        Element anexoElement1 = (Element) document.selectNodes("//Anexo").get(0);
        assertEquals("arquivo1.txt", anexoElement1.attributeValue("nomeArquivo"));
        assertEquals("base64string1", anexoElement1.attributeValue("base64"));

        Element anexoElement2 = (Element) document.selectNodes("//Anexo").get(1);
        assertEquals("arquivo2.jpg", anexoElement2.attributeValue("nomeArquivo"));
        assertEquals("base64string2", anexoElement2.attributeValue("base64"));

        Element anexoElement3 = (Element) document.selectNodes("//Anexo").get(2);
        assertEquals("arquivo3.pdf", anexoElement3.attributeValue("nomeArquivo"));
        assertEquals("base64string3", anexoElement3.attributeValue("base64"));
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

        assertEquals("Dispositivos", document.getRootElement().getName());

        Element dispositivo = (Element) document.selectNodes("//DispositivoSuprimido").get(0);
        assertEquals("Tipo1", dispositivo.attributeValue("tipo"));
        assertEquals("123", dispositivo.attributeValue("id"));
        assertEquals("Rotulo1", dispositivo.attributeValue("rotulo"));
//        assertEquals("urn:norma:alterada", dispositivo.attributeValue("xml:base")); //TODO Verificar namespace
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

        assertEquals("Dispositivos", document.getRootElement().getName());

        Element dispositivo = (Element) document.selectNodes("//DispositivoSuprimido").get(0);
        assertEquals("Tipo1", dispositivo.attributeValue("tipo"));
        assertEquals("123", dispositivo.attributeValue("id"));
        assertEquals("Rotulo1", dispositivo.attributeValue("rotulo"));
        assertEquals(null, dispositivo.attributeValue("xml:base"));
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
        assertEquals("Dispositivos", document.getRootElement().getName());
        assertEquals(2, document.selectNodes("//DispositivoSuprimido").size());
        assertEquals(0, document.selectNodes("//DispositivoModificado").size());
        assertEquals(0, document.selectNodes("//DispositivoAdicionado").size());

        Element dispositivoElement1 = (Element) document.selectNodes("//DispositivoSuprimido").get(0);
        assertNotNull(dispositivoElement1);
        assertEquals("Tipo1", dispositivoElement1.attributeValue("tipo"));
        assertEquals("123", dispositivoElement1.attributeValue("id"));
        assertEquals("Rotulo1", dispositivoElement1.attributeValue("rotulo"));
//        assertEquals("urn:norma:alterada", dispositivoElement1.attributeValue("xml:base")); //TODO Verificar namespace

        Element dispositivoElement2 = (Element) document.selectNodes("//DispositivoSuprimido").get(1);
        assertNotNull(dispositivoElement2);
        assertEquals("Tipo2", dispositivoElement2.attributeValue("tipo"));
        assertEquals("456", dispositivoElement2.attributeValue("id"));
        assertEquals("Rotulo2", dispositivoElement2.attributeValue("rotulo"));
        assertEquals(null, dispositivoElement2.attributeValue("xml:base"));
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

        assertEquals("Dispositivos", document.getRootElement().getName());

        Element dispositivo = (Element) document.selectNodes("//DispositivoModificado").get(0);
        assertEquals("Tipo1", dispositivo.attributeValue("tipo"));
        assertEquals("123", dispositivo.attributeValue("id"));
        assertEquals("Rotulo1", dispositivo.attributeValue("rotulo"));
//        assertEquals("urn:norma:alterada", dispositivo.attributeValue("xml:base")); //TODO Verificar namespace
        assertEquals("true", dispositivo.attributeValue("textoOmitido"));
        assertEquals("Texto sem Alteração", dispositivo.selectNodes("//Texto").get(0).getText());
        assertEquals("true", dispositivo.attributeValue("abreAspas"));
        assertEquals("true", dispositivo.attributeValue("fechaAspas"));
        assertEquals(NotaAlteracao.AC.name(), dispositivo.attributeValue("notaAlteracao"));
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

        assertEquals("Dispositivos", document.getRootElement().getName());

        Element dispositivo = (Element) document.selectNodes("//DispositivoModificado").get(0);
        assertEquals("Tipo1", dispositivo.attributeValue("tipo"));
        assertEquals("123", dispositivo.attributeValue("id"));
        assertEquals("Rotulo1", dispositivo.attributeValue("rotulo"));
        assertEquals("Texto sem Alteração", dispositivo.selectNodes("//Texto").get(0).getText());
        assertEquals(null, dispositivo.attributeValue("xml:base"));
        assertEquals(null, dispositivo.attributeValue("textoOmitido"));
        assertEquals(null, dispositivo.attributeValue("abreAspas"));
        assertEquals(null, dispositivo.attributeValue("fechaAspas"));
        assertEquals(null, dispositivo.attributeValue("notaAlteracao"));
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
        assertEquals("Dispositivos", document.getRootElement().getName());
        assertEquals(0, document.selectNodes("//DispositivoSuprimido").size());
        assertEquals(2, document.selectNodes("//DispositivoModificado").size());
        assertEquals(0, document.selectNodes("//DispositivoAdicionado").size());

        Element dispositivoElement1 = (Element) document.selectNodes("//DispositivoModificado").get(0);
        assertNotNull(dispositivoElement1);
        assertEquals("Tipo1", dispositivoElement1.attributeValue("tipo"));
        assertEquals("123", dispositivoElement1.attributeValue("id"));
        assertEquals("Rotulo1", dispositivoElement1.attributeValue("rotulo"));
        assertEquals("Texto sem Alteração 1", dispositivoElement1.selectNodes("//Texto").get(0).getText());

        Element dispositivoElement2 = (Element) document.selectNodes("//DispositivoModificado").get(1);
        assertNotNull(dispositivoElement2);
        assertEquals("Tipo2", dispositivoElement2.attributeValue("tipo"));
        assertEquals("456", dispositivoElement2.attributeValue("id"));
        assertEquals("Rotulo2", dispositivoElement2.attributeValue("rotulo"));
        assertEquals("Texto sem Alteração 2", dispositivoElement2.selectNodes("//Texto").get(1).getText());
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

        assertEquals("Dispositivos", document.getRootElement().getName());

        Element dispositivo = (Element) document.selectNodes("//DispositivoAdicionado").get(0);
        assertEquals("true", dispositivo.attributeValue("ondeCouber"));
        assertEquals("idPai", dispositivo.attributeValue("idPai"));
        assertEquals("idIrmaoAnterior", dispositivo.attributeValue("idIrmaoAnterior"));
        assertEquals("idPosicaoAgrupador", dispositivo.attributeValue("idPosicaoAgrupador"));

        Element dispositivoFilho = (Element) dispositivo.selectNodes("//" + tipoDispositivoFilho).get(0);
        assertNotNull(dispositivoFilho);
        assertEquals("1", dispositivoFilho.attributeValue("id"));
        assertEquals("uuid2", dispositivoFilho.attributeValue("uuid2"));
//        assertEquals("urn:norma:alterada", dispositivoFilho.attributeValue("xml:base")); //TODO Verificar namespace
        assertEquals("true", dispositivoFilho.attributeValue("existeNaNormaAlterada"));
        assertEquals("s", dispositivoFilho.attributeValue("textoOmitido"));
        assertEquals("s", dispositivoFilho.attributeValue("abreAspas"));
        assertEquals("s", dispositivoFilho.attributeValue("fechaAspas"));
        assertEquals(NotaAlteracao.AC.name(), dispositivoFilho.attributeValue("notaAlteracao"));
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

        assertEquals("Dispositivos", document.getRootElement().getName());

        Element dispositivo = (Element) document.selectNodes("//DispositivoAdicionado").get(0);
        assertEquals("true", dispositivo.attributeValue("ondeCouber"));
        assertEquals("idPai", dispositivo.attributeValue("idPai"));
        assertEquals("idIrmaoAnterior", dispositivo.attributeValue("idIrmaoAnterior"));
        assertEquals("idPosicaoAgrupador", dispositivo.attributeValue("idPosicaoAgrupador"));

        Element dispositivoFilho = (Element) dispositivo.selectNodes("//"+ tipoDispositivoFilho).get(0);
        assertNotNull(dispositivoFilho);
        assertEquals("1", dispositivoFilho.attributeValue("id"));
        assertEquals("uuid2", dispositivoFilho.attributeValue("uuid2"));
//        assertEquals("urn:norma:alterada", dispositivoFilho.attributeValue("xml:base")); //TODO Verificar namespace
        assertEquals("true", dispositivoFilho.attributeValue("existeNaNormaAlterada"));
        assertEquals("s", dispositivoFilho.attributeValue("textoOmitido"));
        assertEquals("s", dispositivoFilho.attributeValue("abreAspas"));
        assertEquals("s", dispositivoFilho.attributeValue("fechaAspas"));
        assertEquals(NotaAlteracao.AC.name(), dispositivoFilho.attributeValue("notaAlteracao"));


        Element dispositivoRotulo = (Element) dispositivoFilho.selectNodes("//Rotulo").get(0);
        Element dispositivoParagrafo = (Element) dispositivoFilho.selectNodes("//p").get(0);
        assertNotNull(dispositivoRotulo);
        assertNotNull(dispositivoParagrafo);

        assertEquals("Rotulo", dispositivoRotulo.getText());
        assertEquals("Texto", dispositivoParagrafo.getText());
    }

    @Test
    public void deveGerarComandoEmendaSemCabecalhoComumSemItensTest() {
        ComandoEmendaPojo comandoEmenda = new ComandoEmendaPojo();
        comandoEmenda.setComandos(new ArrayList<>());
        StringBuilder sb = new StringBuilder();

        marshaller.geraComandoEmenda(comandoEmenda, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        Element element = document.getRootElement();

        assertEquals("ComandoEmenda", element.getName());
        assertEquals(0, element.selectNodes("//ItemComandoEmenda").size());
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

        Element element = document.getRootElement();

        assertEquals("ComandoEmenda", element.getName());
        assertEquals("Cabeçalho Comum", element.selectNodes("//CabecalhoComum").get(0).getText());
        assertEquals(0, element.selectNodes("//ItemComandoEmenda").size());
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

        Element element = document.getRootElement();

        assertEquals("ComandoEmenda", element.getName());
        assertEquals("Cabeçalho Comum", element.selectNodes("//CabecalhoComum").get(0).getText());
        assertEquals(1, element.selectNodes("//ItemComandoEmenda").size());
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

        Element element = document.getRootElement();

        assertEquals("ComandoEmenda", element.getName());
        assertEquals("Cabeçalho Comum", element.selectNodes("//CabecalhoComum").get(0).getText());
        assertEquals(2, element.selectNodes("//ItemComandoEmenda").size());
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

        Element element = document.getRootElement();

        assertEquals("ComandoEmenda", element.getName());
        assertEquals("Cabeçalho Comum", element.selectNodes("//CabecalhoComum").get(0).getText());
        assertEquals(1, element.selectNodes("//ItemComandoEmenda").size());

        Element itemElement = (Element) element.selectNodes("//ItemComandoEmenda").get(0);
        assertEquals(1, itemElement.selectNodes("//Rotulo").size());
        assertEquals("Texto Rotulo", element.selectNodes("//Rotulo").get(0).getText());
        assertEquals(1, itemElement.selectNodes("//Cabecalho").size());
        assertEquals("Texto Cabecalho", element.selectNodes("//Cabecalho").get(0).getText());
        assertEquals(1, itemElement.selectNodes("//Citacao").size());
        assertEquals("Texto Citacao", element.selectNodes("//Citacao").get(0).getText());
        assertEquals(1, itemElement.selectNodes("//Complemento").size());
        assertEquals("Texto Complemento", element.selectNodes("//Complemento").get(0).getText());
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

        Element elementComandoEmendaTextoLivre = (Element) document.selectNodes("//ComandoEmendaTextoLivre").get(0);

        assertEquals("Motivo de Teste", elementComandoEmendaTextoLivre.attributeValue("motivo"));
        assertEquals("Texto de teste", elementComandoEmendaTextoLivre.getText().trim());

        Element elementComandoEmendaTextoLivreAntesRevisao = (Element) document.selectNodes("//ComandoEmendaTextoLivreAntesRevisao").get(0);
        assertNotNull(elementComandoEmendaTextoLivreAntesRevisao);
        assertEquals("Texto Antes de Revisao", elementComandoEmendaTextoLivreAntesRevisao.getText().trim());
    }

    @Test
    public void testGeraComandoEmendaTextoLivreSemMotivoESemTexto() throws Exception {
        ComandoEmendaTextoLivrePojo comando = new ComandoEmendaTextoLivrePojo();
        comando.setTexto("Texto Antes de Revisao");

        StringBuilder sb = new StringBuilder();
        marshaller.geraComandoEmendaTextoLivre(comando, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        Element element = document.getRootElement();

        assertEquals("ComandoEmendaTextoLivre", element.getName());
        assertEquals(null, element.attributeValue("motivo"));
        assertEquals("Texto Antes de Revisao", element.getText().trim());
    }

    @Test
    public void testGeraJustificativaComTextoEscapado() throws Exception {
        String justificativa = "Justificativa com caracteres especiais: <, >, &";

        StringBuilder sb = new StringBuilder();
        marshaller.geraJustificativa(justificativa, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);

        Element element = document.getRootElement();

        assertEquals("Justificativa", element.getName());
        assertEquals("Justificativa com caracteres especiais: <, >, &", element.getText().trim());
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
        Element element = document.getRootElement();

        assertEquals("JustificativaAntesRevisao", element.getName());
        assertEquals("Esta é uma justificativa antes da revisão.", element.getText().trim());
    }

    @Test
    public void testGeraJustificativaAntesRevisaoComTextoEscapado() throws Exception {
        String justificativa = "Justificativa com caracteres especiais: <, >, &";
        StringBuilder sb = new StringBuilder();

        marshaller.geraJustificativaAntesRevisao(justificativa, sb);

        Document document = createDocumentFromString(sb.toString());
        assertNotNull(document);
        Element element = document.getRootElement();

        assertEquals("JustificativaAntesRevisao", element.getName());
        assertEquals(justificativa, element.getText().trim());
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
        Element element = document.getRootElement();

        assertEquals("Autoria", element.getName());
        assertEquals(TipoAutoria.NAO_IDENTIFICADO.getDescricao(), element.attributeValue("tipo"));
        assertEquals("true", element.attributeValue("imprimirPartidoUF"));
        assertEquals("3", element.attributeValue("quantidadeAssinaturasAdicionaisDeputados"));
        assertEquals("2", element.attributeValue("quantidadeAssinaturasAdicionaisSenadores"));
        assertEquals(1, element.selectNodes("//Parlamentar").size());
        assertEquals(1, element.selectNodes("//Colegiado").size());
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
        Element element = document.getRootElement();

        assertEquals("OpcoesImpressao", element.getName());
        assertEquals("true", element.attributeValue("imprimirBrasao"));
        assertEquals("Cabeçalho de Teste", element.attributeValue("textoCabecalho"));
        assertEquals("12", element.attributeValue("tamanhoFonte"));
        assertEquals("false", element.attributeValue("reduzirEspacoEntreLinhas"));
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
        Element element = document.getRootElement();

        assertEquals("OpcoesImpressao", element.getName());
        assertEquals("false", element.attributeValue("imprimirBrasao"));
        assertEquals("10", element.attributeValue("tamanhoFonte"));
        assertEquals("true", element.attributeValue("reduzirEspacoEntreLinhas"));
        assertEquals(null, element.attributeValue("textoCabecalho"));
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
        Element root = document.getRootElement();
        assertNotNull(root);

        List<Element> elements = getOnlyElements(root);

        assertEquals("Revisoes", root.getName());
        assertEquals(3, elements.size());

        Element primeiroElemento = elements.get(0);
        assertEquals("RevisaoElemento", primeiroElemento.getName());
        assertEquals("1", primeiroElemento.attributeValue("actionType"));
        assertEquals("State 1", primeiroElemento.attributeValue("stateType"));
        assertEquals("1", primeiroElemento.attributeValue("idRevisaoElementoPai"));
        assertEquals("1", primeiroElemento.attributeValue("idRevisaoElementoPrincipal"));

        Element segundoElemento = elements.get(1);
        assertEquals("RevisaoJustificativa", segundoElemento.getName());
        assertEquals("2", segundoElemento.attributeValue("id"));
        assertEquals("Type 2", segundoElemento.attributeValue("type"));
        assertEquals("2024-10-24T11:00:00", segundoElemento.attributeValue("dataHora"));
        assertEquals("Descricao 2", segundoElemento.attributeValue("descricao"));

        Element terceiroElemento = elements.get(2);
        assertEquals("RevisaoTextoLivre", terceiroElemento.getName());
        assertEquals("3", terceiroElemento.attributeValue("id"));
        assertEquals("Type 3", terceiroElemento.attributeValue("type"));
        assertEquals("2024-10-24T11:00:00", terceiroElemento.attributeValue("dataHora"));
        assertEquals("Descricao 3", terceiroElemento.attributeValue("descricao"));
    }

    private static List<Element> getOnlyElements(Element root) {
        return root.elements().stream()
                .filter(element -> element instanceof Element)
                .collect(Collectors.toList());

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
        Element root = document.getRootElement();
        assertNotNull(root);

        List<Element> elements = getOnlyElements(root);

        assertEquals("NotasRodape", root.getName());
        assertEquals(2, elements.size());

        Element nota1 = elements.get(0);
        assertEquals("1", nota1.attributeValue("id"));
        assertEquals("1", nota1.attributeValue("numero"));
        assertEquals("Nota de Rodapé 1", nota1.attributeValue("texto").trim());

        Element nota2 = elements.get(1);
        assertEquals("2", nota2.attributeValue("id"));
        assertEquals("2", nota2.attributeValue("numero"));
        assertEquals("Nota de Rodapé 2", nota2.attributeValue("texto").trim());
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
        Element root = document.getRootElement();
        assertNotNull(root);

        List<Element> elements = getOnlyElements(root);

        assertEquals("PendenciasPreenchimento", root.getName());
        assertEquals(2, elements.size());

        Element pendencia1 = elements.get(0);
        assertEquals("PendenciaPreenchimento", pendencia1.getName());
        assertEquals("Pendência 1", pendencia1.getText().trim());

        Element pendencia2 = elements.get(1);
        assertEquals("PendenciaPreenchimento", pendencia2.getName());
        assertEquals("Pendência 2", pendencia2.getText().trim());
    }

    @Test
    public void testGeraPendenciasPreenchimentoSemElementos() throws Exception {
        List<String> pendenciasPreenchimento = List.of();

        StringBuilder sb = new StringBuilder();
        marshaller.geraPendenciasPreenchimento(pendenciasPreenchimento, sb);

        assertEquals(0, sb.length());
    }

    private Document createDocumentFromString(String xml) {
        try {
            SAXReader reader = new SAXReader();
            return reader.read(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
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

}
