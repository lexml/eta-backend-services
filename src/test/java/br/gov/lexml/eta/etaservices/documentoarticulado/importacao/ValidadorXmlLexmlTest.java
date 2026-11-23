package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Year;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import br.gov.lexml.eta.etaservices.documentoarticulado.importacao.ValidadorXmlLexml.ResultadoXml;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

class ValidadorXmlLexmlTest {

    private final ValidadorXmlLexml validador = new ValidadorXmlLexml();

    @Test
    void validarSchemaDeveSerDesabilitadoPorPadraoEmAmbosOsModos() {
        assertThat(OpcoesParseProposicao.aproveitamento().isValidarSchema()).isFalse();
        assertThat(OpcoesParseProposicao.completo().isValidarSchema()).isFalse();
        assertThat(OpcoesParseProposicao.builder().validarSchema(true).build().isValidarSchema()).isTrue();
    }

    @Test
    void devePreservarDocumentoExemploComMetadadoProprietarioNoModoCompleto() throws IOException {
        String exemplo = lerRecurso("/documentoarticulado/documento-articulado-exemplo.xml");

        ResultadoXml resultado = validador.validarEPreparar(
                exemplo,
                OpcoesParseProposicao.builder()
                        .modo(ModoParseProposicao.COMPLETO)
                        .validarSchema(false)
                        .build());

        assertThat(resultado.xml).isEqualTo(exemplo);
        assertThat(resultado.xml).contains("lexedit:Metadado");
        assertThat(resultado.xml).contains("URN=\"urn:lex:br:senado.federal:projeto.lei:999999;9999\"");
        assertThat(resultado.avisos).isEmpty();
    }

    @Test
    void devePreservarUrnValidaEMetadadoProprietarioNoModoAproveitamento() throws IOException {
        String exemplo = lerRecurso("/documentoarticulado/documento-articulado-exemplo.xml")
                .replace("urn:lex:br:senado.federal:projeto.lei:999999;9999",
                        "urn:lex:br:senado.federal:projeto.lei;pl:2025;10");

        ResultadoXml resultado = validador.validarEPreparar(exemplo, OpcoesParseProposicao.aproveitamento());

        assertThat(resultado.xml).isEqualTo(exemplo);
        assertThat(resultado.xml).contains("lexedit:Metadado");
        assertThat(resultado.xml).contains("URN=\"urn:lex:br:senado.federal:projeto.lei;pl:2025;10\"");
    }

    @Test
    void deveSubstituirPlaceholderLexmlUrnIdNoModoAproveitamento() {
        String xml = xmlBasico("urn:lex:br:senado.federal:projeto.lei;pls:LEXML_URN_ID");

        ResultadoXml padrao = validador.validarEPreparar(xml, OpcoesParseProposicao.aproveitamento());
        String anoAtual = String.valueOf(Year.now().getValue());
        assertThat(padrao.xml).contains("URN=\"urn:lex:br:senado.federal:projeto.lei;pls:" + anoAtual + ";999999\"");

        ResultadoXml customizado = validador.validarEPreparar(
                xml,
                OpcoesParseProposicao.builder()
                        .modo(ModoParseProposicao.APROVEITAMENTO)
                        .ano(2026)
                        .numero(42)
                        .build());
        assertThat(customizado.xml).contains("URN=\"urn:lex:br:senado.federal:projeto.lei;pls:2026;42\"");
    }

    @Test
    void deveCriarOuNormalizarUrnQuandoAusenteOuInvalidaNoModoAproveitamentoEPreservarNoCompleto() {
        String semIdentificacao = "<LexML xmlns=\"http://www.lexml.gov.br/1.0\">"
                + "<ProjetoNorma><Norma><ParteInicial><Epigrafe id=\"epigrafe\">PROJETO DE LEI</Epigrafe>"
                + "<Ementa id=\"ementa\">Ementa.</Ementa><Preambulo id=\"preambulo\"><p>O CONGRESSO:</p></Preambulo>"
                + "</ParteInicial><Articulacao><Artigo id=\"art1\"><Rotulo>Art. 1º</Rotulo>"
                + "<Caput id=\"art1_cpt\"><p>Texto.</p></Caput></Artigo></Articulacao></Norma></ProjetoNorma></LexML>";

        ResultadoXml aproveitamento = validador.validarEPreparar(
                semIdentificacao, OpcoesParseProposicao.aproveitamento());
        assertThat(aproveitamento.xml).contains(
                "URN=\"urn:lex:br:senado.federal:projeto.lei;pl:" + Year.now().getValue() + ";999999\"");

        String comUrnVazia = xmlBasico("");
        ResultadoXml normalizada = validador.validarEPreparar(comUrnVazia, OpcoesParseProposicao.aproveitamento());
        assertThat(normalizada.xml).contains(
                "URN=\"urn:lex:br:senado.federal:projeto.lei;pl:" + Year.now().getValue() + ";999999\"");

        ResultadoXml completo = validador.validarEPreparar(
                comUrnVazia,
                OpcoesParseProposicao.builder()
                        .modo(ModoParseProposicao.COMPLETO)
                        .validarSchema(false)
                        .build());
        assertThat(completo.xml).isEqualTo(comUrnVazia);
    }

    @Test
    void deveValidarComSchemaQuandoSolicitadoEDevolverOuFalharConformeOpcao() {
        String xmlInvalidoNoSchema = "<LexML xmlns=\"http://www.lexml.gov.br/1.0\">"
                + "<Metadado><Identificacao URN=\"urn:lex:br:senado.federal:projeto.lei;pl:2026;1\"/></Metadado>"
                + "<ProjetoNorma><Norma><ParteInicial><Epigrafe id=\"epigrafe\">PROJETO DE LEI</Epigrafe>"
                + "<Ementa id=\"ementa\">Ementa.</Ementa><Preambulo id=\"preambulo\"><p>O CONGRESSO:</p></Preambulo>"
                + "</ParteInicial><Articulacao><Artigo id=\"art1\"><ElementoInesperado/></Artigo></Articulacao>"
                + "</Norma></ProjetoNorma></LexML>";

        ResultadoXml tolerante = validador.validarEPreparar(
                xmlInvalidoNoSchema,
                OpcoesParseProposicao.builder()
                        .modo(ModoParseProposicao.APROVEITAMENTO)
                        .validarSchema(true)
                        .falharComAvisos(false)
                        .build());
        assertThat(tolerante.avisos).isNotEmpty();

        assertThatThrownBy(() -> validador.validarEPreparar(
                xmlInvalidoNoSchema,
                OpcoesParseProposicao.builder()
                        .modo(ModoParseProposicao.COMPLETO)
                        .validarSchema(true)
                        .falharComAvisos(true)
                        .build()))
                .isInstanceOf(EtaBackendException.class);
    }

    @Test
    void deveRejeitarXmlVazioMalformadoNaoLexmlSemArticulacaoOuComDoctype() throws IOException {
        String emendaNaoLexml = lerRecurso("/emenda-teste1.xml");
        String xxe = "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                + xmlBasico("urn:lex:br:senado.federal:projeto.lei;pl:2026;1");
        String semArticulacao = "<LexML xmlns=\"http://www.lexml.gov.br/1.0\">"
                + "<Metadado><Identificacao URN=\"urn:lex:br:senado.federal:projeto.lei;pl:2026;1\"/></Metadado>"
                + "<ProjetoNorma><Norma><Articulacao/></Norma></ProjetoNorma></LexML>";

        assertThatThrownBy(() -> validador.validarEPreparar((byte[]) null, OpcoesParseProposicao.aproveitamento()))
                .isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> validador.validarEPreparar("   ", OpcoesParseProposicao.aproveitamento()))
                .isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> validador.validarEPreparar("<LexML><naoFecha>", OpcoesParseProposicao.aproveitamento()))
                .isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> validador.validarEPreparar(emendaNaoLexml, OpcoesParseProposicao.aproveitamento()))
                .isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> validador.validarEPreparar(semArticulacao, OpcoesParseProposicao.aproveitamento()))
                .isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> validador.validarEPreparar(xxe, OpcoesParseProposicao.aproveitamento()))
                .isInstanceOf(EtaBackendException.class);
    }

    private static String xmlBasico(String urn) {
        return "<LexML xmlns=\"http://www.lexml.gov.br/1.0\">"
                + "<Metadado><Identificacao URN=\"" + urn + "\"/></Metadado>"
                + "<ProjetoNorma><Norma><ParteInicial>"
                + "<Epigrafe id=\"epigrafe\">PROJETO DE LEI</Epigrafe>"
                + "<Ementa id=\"ementa\">Ementa.</Ementa>"
                + "<Preambulo id=\"preambulo\"><p>O CONGRESSO:</p></Preambulo>"
                + "</ParteInicial><Articulacao><Artigo id=\"art1\"><Rotulo>Art. 1º</Rotulo>"
                + "<Caput id=\"art1_cpt\"><p>Texto.</p></Caput></Artigo></Articulacao>"
                + "</Norma></ProjetoNorma></LexML>";
    }

    private static String lerRecurso(String caminho) throws IOException {
        try (InputStream is = ValidadorXmlLexmlTest.class.getResourceAsStream(caminho)) {
            assertThat(is).as("Recurso %s", caminho).isNotNull();
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

}

