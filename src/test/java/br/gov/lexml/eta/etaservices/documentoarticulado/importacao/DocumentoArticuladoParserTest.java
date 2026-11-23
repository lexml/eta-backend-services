package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake;
import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoPdfGenerator;
import br.gov.lexml.eta.etaservices.parecer.AnexoPDFA;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

class DocumentoArticuladoParserTest {

    private static final String JSONIX_STUB = "{\"name\":{\"localPart\":\"LexML\"}}";

    @Test
    void deveParsearDocxCompletoNoModoAproveitamentoDescartandoEpigrafeFechoEJustificacao() throws IOException {
        ConversorCapturador conversor = new ConversorCapturador(JSONIX_STUB);
        DocumentoArticuladoParser parser = new DocumentoArticuladoParser(conversor);

        byte[] docx = DocxTesteBuilder.comParagrafos(
                "PROJETO DE LEI Nº 123, DE 2024",
                "(Do Sr. Deputado Fulano)",
                "Institui o Programa de Testes Automatizados e altera a Lei nº 1.000, de 2000.",
                "O CONGRESSO NACIONAL decreta:",
                "Título I",
                "Disposições Preliminares",
                "Art. 1º Fica instituído o Programa de Testes Automatizados.",
                "§ 1º São diretrizes do Programa:",
                "I – a transparência:",
                "a) publicação aberta;",
                "1. em formato estruturado.",
                "Art. 2º Desviar recursos do Programa:",
                "Pena – reclusão, de 2 (dois) a 4 (quatro) anos.",
                "Art. 3º A Lei nº 1.000, de 2000, passa a vigorar com a seguinte alteração:",
                "\"Art. 10. Os dados serão públicos.\" (NR)",
                "Art. 4º Esta Lei entra em vigor na data de sua publicação.",
                "Sala das Sessões, 10 de abril de 2024.",
                "Deputado Fulano",
                "JUSTIFICAÇÃO",
                "Texto explicativo que não integra a norma jurídica.");

        ResultadoParseProposicao resultado = parser.parseDocx(new ByteArrayInputStream(docx));

        assertThat(resultado.getJsonix()).isEqualTo(JSONIX_STUB);
        assertThat(conversor.ultimaEntradaXml()).isEqualTo(resultado.getXml());
        assertThat(resultado.getXml())
                .contains("<Ementa id=\"ementa\">Institui o Programa de Testes Automatizados")
                .contains("<Artigo id=\"art1\">")
                .contains("<Pena id=\"art2_cpt_pena\">")
                .contains("<Alteracao")
                .contains("art10")
                .contains("<Artigo id=\"art4\">")
                .doesNotContain("Texto explicativo que não integra")
                .doesNotContain("Sala das Sessões");
        assertThat(resultado.getXml()).matches("(?s).*URN=\"urn:lex:br:senado\\.federal:projeto\\.lei;pl:\\d{4};999999\".*");
    }

    @Test
    void deveAproveitarEmentaEArticulacaoDeNormaPromulgadaEMedidaProvisoriaNoModoAproveitamento() {
        ConversorCapturador conversor = new ConversorCapturador(JSONIX_STUB);
        DocumentoArticuladoParser parser = new DocumentoArticuladoParser(conversor);

        String leiPromulgada = String.join("\n",
                "LEI Nº 14.133, DE 1º DE ABRIL DE 2021",
                "Lei de Licitações e Contratos Administrativos.",
                "O PRESIDENTE DA REPÚBLICA",
                "Faço saber que o Congresso Nacional decreta e eu sanciono a seguinte Lei:",
                "Art. 1º Esta Lei estabelece normas gerais de licitação.");

        ResultadoParseProposicao resLei = parser.parseTxt(leiPromulgada);
        assertThat(resLei.getXml())
                .contains("<Ementa id=\"ementa\">Lei de Licitações e Contratos Administrativos.</Ementa>")
                .contains("<Artigo id=\"art1\">");

        String mp = String.join("\n",
                "MEDIDA PROVISÓRIA Nº 1.234, DE 2024",
                "Institui crédito extraordinário.",
                "O PRESIDENTE DA REPÚBLICA, no uso da atribuição que lhe confere o art. 62 da Constituição:",
                "Art. 1º Fica aberto crédito extraordinário.");

        ResultadoParseProposicao resMp = parser.parseTxt(mp);
        assertThat(resMp.getXml())
                .contains("<Ementa id=\"ementa\">Institui crédito extraordinário.</Ementa>")
                .contains("<Artigo id=\"art1\">");
    }

    @Test
    void deveAceitarTextoSemEpigrafeSemPreambuloComCabecalhoInformalOuApenasArticulacaoNoModoAproveitamento() {
        ConversorCapturador conversor = new ConversorCapturador(JSONIX_STUB);
        DocumentoArticuladoParser parser = new DocumentoArticuladoParser(conversor);

        ResultadoParseProposicao semEpigrafe = parser.parseTxt(String.join("\n",
                "Dispõe sobre o incentivo à leitura.",
                "O CONGRESSO NACIONAL decreta:",
                "Art. 1º Fica instituído o incentivo."));
        assertThat(semEpigrafe.getXml())
                .contains("Dispõe sobre o incentivo à leitura.")
                .contains("<Artigo id=\"art1\">");

        ResultadoParseProposicao semEpigrafeNemPreambulo = parser.parseTxt(String.join("\n",
                "Minuta interna - versão preliminar",
                "",
                "Dispõe sobre a política de dados abertos.",
                "Art. 1º Os órgãos públicos divulgarão bases abertas."));
        assertThat(semEpigrafeNemPreambulo.getXml())
                .contains("Dispõe sobre a política de dados abertos.")
                .doesNotContain("Minuta interna")
                .contains("<Artigo id=\"art1\">");

        ResultadoParseProposicao soArticulacao = parser.parseTxt(String.join("\n",
                "Art. 1º Fica criada a comissão especial.",
                "Parágrafo único. A comissão terá cinco membros."));
        assertThat(soArticulacao.getXml())
                .contains("<Artigo id=\"art1\">")
                .contains("<Paragrafo id=\"art1_par1u\">");
    }

    @Test
    void deveRetornarAvisosNaoFataisPorPadraoEFalharQuandoFalharComAvisosHabilitado() {
        ConversorCapturador conversor = new ConversorCapturador(JSONIX_STUB);
        DocumentoArticuladoParser parser = new DocumentoArticuladoParser(conversor);

        String txtComSaltoNumeracao = String.join("\n",
                "Dispõe sobre teste de numeração.",
                "Art. 1º Primeiro artigo.",
                "Art. 3º Terceiro artigo fora de sequência.");

        ResultadoParseProposicao tolerante = parser.parseTxt(txtComSaltoNumeracao);
        assertThat(tolerante.temAvisos()).isTrue();
        assertThat(tolerante.getAvisos()).isNotEmpty();
        assertThat(tolerante.getMensagensAvisos()).isNotEmpty();
        assertThat(tolerante.getJsonix()).isEqualTo(JSONIX_STUB);

        OpcoesParseProposicao estrito = OpcoesParseProposicao.builder()
                .modo(ModoParseProposicao.APROVEITAMENTO)
                .falharComAvisos(true)
                .build();
        assertThatThrownBy(() -> parser.parseTxt(txtComSaltoNumeracao, estrito))
                .isInstanceOf(EtaBackendException.class);
    }

    @Test
    void deveFalharQuandoNaoHouverDispositivosOuQuandoHouverAspasSemFechamento() {
        DocumentoArticuladoParser parser = new DocumentoArticuladoParser(new ConversorCapturador(JSONIX_STUB));

        String apenasProsa = String.join("\n",
                "PROJETO DE LEI Nº 10, DE 2025",
                "Institui norma sem artigos.",
                "O CONGRESSO NACIONAL decreta:",
                "Texto corrido sem nenhum dispositivo articulado.");
        assertThatThrownBy(() -> parser.parseTxt(apenasProsa))
                .isInstanceOf(EtaBackendException.class);

        String aspasAbertas = String.join("\n",
                "Art. 1º A Lei nº 1, de 2000, passa a vigorar com a seguinte redação:",
                "\"Art. 2º Dispositivo sem fechamento de aspas.");
        assertThatThrownBy(() -> parser.parseTxt(aspasAbertas))
                .isInstanceOf(EtaBackendException.class);
    }

    @Test
    void deveParsearNoModoCompletoComPerfilExplicitoERefazerEpigrafeQuandoInformadoNumeroEAno() {
        ConversorCapturador conversor = new ConversorCapturador(JSONIX_STUB);
        DocumentoArticuladoParser parser = new DocumentoArticuladoParser(conversor);

        String proposicaoCompleta = String.join("\n",
                "PROJETO DE LEI DO SENADO Nº 10, DE 2024",
                "Dispõe sobre o estatuto de testes.",
                "O CONGRESSO NACIONAL decreta:",
                "Art. 1º Fica instituído o estatuto.");

        OpcoesParseProposicao opcoes = OpcoesParseProposicao.builder()
                .modo(ModoParseProposicao.COMPLETO)
                .autoridade("senado.federal")
                .tipoNorma("projeto.lei;pls")
                .numero(123)
                .ano(2025)
                .build();

        ResultadoParseProposicao resultado = parser.parseTxt(proposicaoCompleta, opcoes);
        assertThat(resultado.getJsonix()).isEqualTo(JSONIX_STUB);
        assertThat(resultado.getXml())
                .contains("urn:lex:br:senado.federal:projeto.lei;pls:")
                .contains("123")
                .contains("2025")
                .contains("<Epigrafe")
                .contains("<Ementa")
                .contains("<Preambulo")
                .contains("<Artigo id=\"art1\">");
    }

    @Test
    void deveFalharNoModoCompletoQuandoFaltarEpigrafeOuEmentaOuPerfilDesconhecido() {
        DocumentoArticuladoParser parser = new DocumentoArticuladoParser(new ConversorCapturador(JSONIX_STUB));

        String semEpigrafe = String.join("\n",
                "Dispõe sobre algo.",
                "O CONGRESSO NACIONAL decreta:",
                "Art. 1º Texto.");
        assertThatThrownBy(() -> parser.parseTxt(
                semEpigrafe, OpcoesParseProposicao.completo("senado.federal", "projeto.lei;pls")))
                .isInstanceOf(EtaBackendException.class);

        String semEmenta = String.join("\n",
                "PROJETO DE LEI Nº 10, DE 2025",
                "O CONGRESSO NACIONAL decreta:",
                "Art. 1º Texto.");
        assertThatThrownBy(() -> parser.parseTxt(semEmenta, OpcoesParseProposicao.completo()))
                .isInstanceOf(EtaBackendException.class);

        OpcoesParseProposicao perfilInexistente = OpcoesParseProposicao.completo("casa.inexistente", "tipo.inexistente");
        assertThatThrownBy(() -> parser.parseTxt(
                "PROJETO DE LEI Nº 10, DE 2025\nEmenta.\nO CONGRESSO NACIONAL decreta:\nArt. 1º Texto.",
                perfilInexistente))
                .isInstanceOf(EtaBackendException.class);
    }

    @Test
    void deveExtrairPdfGeradoPelaBibliotecaEDevolverJsonix() throws IOException {
        ConversorDocumentoArticuladoFake conversorPdf = ConversorDocumentoArticuladoFake.comExemplo();
        DocumentoArticuladoPdfGenerator gerador = new DocumentoArticuladoPdfGenerator(conversorPdf);
        String jsonExemplo = ConversorDocumentoArticuladoFake.recurso("/documentoarticulado/documento-articulado-exemplo.json");
        String xmlEsperado = ConversorDocumentoArticuladoFake.recurso("/documentoarticulado/documento-articulado-exemplo.xml");

        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        gerador.generate(jsonExemplo, pdfOut);

        DocumentoArticuladoParser parser = new DocumentoArticuladoParser(conversorPdf);
        ResultadoParseProposicao resultado = parser.parsePdf(new ByteArrayInputStream(pdfOut.toByteArray()));

        assertThat(resultado.getXml()).isEqualTo(xmlEsperado);
        assertThat(resultado.getJsonix()).isEqualTo(jsonExemplo);
        assertThat(resultado.temAvisos()).isFalse();
    }

    @Test
    void deveExtrairPdfComMultiplosAnexosIgnorandoDemaisArquivos() throws IOException {
        ConversorDocumentoArticuladoFake conversorPdf = ConversorDocumentoArticuladoFake.comExemplo();
        DocumentoArticuladoPdfGenerator gerador = new DocumentoArticuladoPdfGenerator(conversorPdf);
        String jsonExemplo = ConversorDocumentoArticuladoFake.recurso("/documentoarticulado/documento-articulado-exemplo.json");
        String xmlEsperado = ConversorDocumentoArticuladoFake.recurso("/documentoarticulado/documento-articulado-exemplo.xml");

        try (InputStream anexoPdf = getClass().getResourceAsStream("/anexoA.pdf");
                InputStream anexoDocx = getClass().getResourceAsStream("/anexoB.docx")) {
            assertThat(anexoPdf).isNotNull();
            assertThat(anexoDocx).isNotNull();

            List<AnexoPDFA> anexos = Arrays.asList(
                    AnexoPDFA.builder().nomeArquivo("anexoA.pdf").file(anexoPdf).imprimir(true).build(),
                    AnexoPDFA.builder().nomeArquivo("anexoB.docx").file(anexoDocx).imprimir(false).build());

            ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
            gerador.generate(jsonExemplo, anexos, pdfOut);

            DocumentoArticuladoParser parser = new DocumentoArticuladoParser(conversorPdf);
            ResultadoParseProposicao resultado = parser.parsePdf(new ByteArrayInputStream(pdfOut.toByteArray()));

            assertThat(resultado.getXml()).isEqualTo(xmlEsperado);
            assertThat(resultado.getJsonix()).isEqualTo(jsonExemplo);
            assertThat(resultado.temAvisos()).isFalse();
        }
    }

    @Test
    void deveRejeitarPdfSemAnexoDocumentoArticuladoEDespacharPorTipoEContentType() throws IOException {
        ConversorCapturador conversor = new ConversorCapturador(JSONIX_STUB);
        DocumentoArticuladoParser parser = new DocumentoArticuladoParser(conversor);

        try (InputStream pdfInvalido = getClass().getResourceAsStream("/test1.pdf")) {
            assertThat(pdfInvalido).isNotNull();
            assertThatThrownBy(() -> parser.parsePdf(pdfInvalido))
                    .isInstanceOf(EtaBackendException.class);
        }

        byte[] docx = DocxTesteBuilder.comParagrafos("Ementa teste.", "Art. 1º Disposição inicial.");
        String jsonDocx = parser.parsePorContentTypeParaJson(
                new ByteArrayInputStream(docx),
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        assertThat(jsonDocx).isEqualTo(JSONIX_STUB);

        byte[] txtBytes = "Ementa TXT.\nArt. 1º Texto TXT.".getBytes(StandardCharsets.UTF_8);
        String jsonTxt = parser.parseParaJson(new ByteArrayInputStream(txtBytes), TipoArquivoProposicao.TXT);
        assertThat(jsonTxt).isEqualTo(JSONIX_STUB);

        String exemploXml = IOUtils.toString(
                getClass().getResourceAsStream("/documentoarticulado/documento-articulado-exemplo.xml"),
                StandardCharsets.UTF_8);
        ResultadoParseProposicao resXml = parser.parsePorContentType(
                new ByteArrayInputStream(exemploXml.getBytes(StandardCharsets.UTF_8)),
                "text/xml; charset=UTF-8");
        assertThat(resXml.getJsonix()).isEqualTo(JSONIX_STUB);
        assertThat(resXml.getXml()).contains("lexedit:Metadado");
        assertThat(conversor.totalChamadas()).isEqualTo(3);
    }

    @Test
    void deveIsolarEpigrafesDeSubstitutivoESubemendaSubstitutivaDaEmenta() {
        DocumentoArticuladoParser parser = new DocumentoArticuladoParser(new ConversorCapturador(JSONIX_STUB));

        ResultadoParseProposicao subLinhaUnica = parser.parseTxt(String.join("\n",
                "SUBSTITUTIVO AO PROJETO DE LEI Nº   , DE 2026",
                "Dispõe sobre a modernização normativa.",
                "O CONGRESSO NACIONAL decreta:",
                "Art. 1º Texto da proposição."));
        assertThat(subLinhaUnica.getXml())
                .contains("<Ementa id=\"ementa\">Dispõe sobre a modernização normativa.</Ementa>")
                .contains("<Artigo id=\"art1\">");

        ResultadoParseProposicao subemenda = parser.parseTxt(String.join("\n",
                "SUBEMENDA SUBSTITUTIVA AO PROJETO DE LEI Nº    , DE 2026",
                "Altera dispositivos da legislação vigente.",
                "O CONGRESSO NACIONAL decreta:",
                "Art. 1º Texto alterado."));
        assertThat(subemenda.getXml())
                .contains("<Ementa id=\"ementa\">Altera dispositivos da legislação vigente.</Ementa>")
                .contains("<Artigo id=\"art1\">");

        ResultadoParseProposicao subDuasLinhasSigla = parser.parseTxt(String.join("\n",
                "SUBSTITUTIVO",
                "AO PL Nº 123, DE 2026",
                "Institui diretrizes gerais.",
                "Art. 1º Ficam instituídas as diretrizes."));
        assertThat(subDuasLinhasSigla.getXml())
                .contains("<Ementa id=\"ementa\">Institui diretrizes gerais.</Ementa>")
                .contains("<Artigo id=\"art1\">");
    }

    @Test
    void deveReconhecerEpigrafesCustomizadasNoModoAproveitamento() {
        DocumentoArticuladoParser parser = new DocumentoArticuladoParser(new ConversorCapturador(JSONIX_STUB));
        String texto = String.join("\n",
                "INDICAÇÃO Nº 42, DE 2026",
                "(REFERENTE AO PROCESSO ADMINISTRATIVO 10)",
                "Relatoria: Comissão Especial",
                "Sugere ao Poder Executivo medidas de transparência.",
                "O CONGRESSO NACIONAL decreta:",
                "Art. 1º Ficam recomendadas as medidas de transparência.");

        OpcoesParseProposicao opcoes = OpcoesParseProposicao.builder()
                .adicionarRegexEpigrafe("^INDICAÇÃO\\b")
                .adicionarRegexEpigrafeContinuacao("^\\(?\\s*REFERENTE\\s+AO\\s+PROCESSO\\b")
                .adicionarRegexPosEpigrafe("^Relatoria:.*")
                .build();

        ResultadoParseProposicao resultado = parser.parseTxt(texto, opcoes);
        assertThat(resultado.getXml())
                .contains("<Ementa id=\"ementa\">Sugere ao Poder Executivo medidas de transparência.</Ementa>")
                .contains("INDICAÇÃO Nº 42, DE 2026")
                .contains("<Artigo id=\"art1\">");
    }

    @Test
    void devePermitirEpigrafeCustomizadaNoModoCompletoPreservandoObrigatoriedade() {
        DocumentoArticuladoParser parser = new DocumentoArticuladoParser(new ConversorCapturador(JSONIX_STUB));
        String texto = String.join("\n",
                "SUBEMENDA SUBSTITUTIVA AO PROJETO DE LEI Nº 10, DE 2026",
                "Altera a norma vigente.",
                "O CONGRESSO NACIONAL decreta:",
                "Art. 1º Texto completo.");

        OpcoesParseProposicao semCustomizacao = OpcoesParseProposicao.completo("senado.federal", "projeto.lei;pls");
        assertThatThrownBy(() -> parser.parseTxt(texto, semCustomizacao))
                .isInstanceOf(EtaBackendException.class);

        OpcoesParseProposicao comCustomizacao = OpcoesParseProposicao.builder()
                .modo(ModoParseProposicao.COMPLETO)
                .autoridade("senado.federal")
                .tipoNorma("projeto.lei;pls")
                .regexEpigrafeAdicionais(Arrays.asList("^SUBEMENDA SUBSTITUTIVA\\b"))
                .build();

        ResultadoParseProposicao resultado = parser.parseTxt(texto, comCustomizacao);
        assertThat(resultado.getXml())
                .contains("urn:lex:br:senado.federal:projeto.lei;pls:")
                .contains("<Ementa id=\"ementa\">Altera a norma vigente.</Ementa>")
                .contains("<Artigo id=\"art1\">");
    }

    @Test
    void deveAplicarOpcoesPadraoDoConstrutorEPermitirSobrescritaPorChamada() {
        OpcoesParseProposicao opcoesPadrao = OpcoesParseProposicao.builder()
                .adicionarRegexEpigrafe("^MOÇÃO\\b")
                .build();
        DocumentoArticuladoParser parser = new DocumentoArticuladoParser(
                new ConversorCapturador(JSONIX_STUB), opcoesPadrao);

        ResultadoParseProposicao viaPadrao = parser.parseTxt(String.join("\n",
                "MOÇÃO Nº 5, DE 2026",
                "Manifesta apoio à iniciativa.",
                "Art. 1º Fica registrada a moção."));
        assertThat(viaPadrao.getXml())
                .contains("<Ementa id=\"ementa\">Manifesta apoio à iniciativa.</Ementa>")
                .contains("<Artigo id=\"art1\">");

        ResultadoParseProposicao sobrescrito = parser.parseTxt(
                "PROJETO DE LEI DO SENADO Nº 1, DE 2026\nEmenta completa.\nO CONGRESSO NACIONAL decreta:\nArt. 1º Texto.",
                OpcoesParseProposicao.completo("senado.federal", "projeto.lei;pls"));
        assertThat(sobrescrito.getXml())
                .contains("urn:lex:br:senado.federal:projeto.lei;pls:")
                .contains("<Ementa id=\"ementa\">Ementa completa.</Ementa>");
    }

    @Test
    void deveRejeitarRegexInvalidoNoBuilderDeOpcoes() {
        assertThatThrownBy(() -> OpcoesParseProposicao.builder().adicionarRegexEpigrafe("[invalido").build())
                .isInstanceOf(EtaBackendException.class)
                .hasMessageContaining("epígrafe");
        assertThatThrownBy(() -> OpcoesParseProposicao.builder().adicionarRegexEpigrafeContinuacao("(naoFecha").build())
                .isInstanceOf(EtaBackendException.class)
                .hasMessageContaining("continuação");
        assertThatThrownBy(() -> OpcoesParseProposicao.builder().adicionarRegexPosEpigrafe("*").build())
                .isInstanceOf(EtaBackendException.class)
                .hasMessageContaining("pós-epígrafe");
    }

    private static final class ConversorCapturador implements ConversorDocumentoArticulado {

        private final String jsonRetorno;
        private final AtomicReference<String> ultimoXml = new AtomicReference<>();
        private final AtomicInteger chamadas = new AtomicInteger();

        private ConversorCapturador(String jsonRetorno) {
            this.jsonRetorno = jsonRetorno;
        }

        @Override
        public String jsonParaXml(String json) {
            throw new UnsupportedOperationException("Não utilizado neste teste");
        }

        @Override
        public String xmlParaJson(String xml) {
            ultimoXml.set(xml);
            chamadas.incrementAndGet();
            return jsonRetorno;
        }

        String ultimaEntradaXml() {
            return ultimoXml.get();
        }

        int totalChamadas() {
            return chamadas.get();
        }
    }

}
