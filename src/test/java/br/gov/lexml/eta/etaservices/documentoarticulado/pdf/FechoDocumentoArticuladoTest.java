package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake;
import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.FechoDocumentoArticulado.Assinatura;

class FechoDocumentoArticuladoTest {

    private static final String SENADOR = parlamentar("Davi Alcolumbre", "M", "UNIÃO", "AP", "SF", "Presidente do Senado Federal");
    private static final String SENADORA = parlamentar("Soraya Thronicke", "F", "PSB", "MS", "SF", "");
    private static final String DEPUTADO = parlamentar("João da Silva", "M", "PT", "SP", "CD", "Líder do PT");
    private static final String DEPUTADA = parlamentar("Maria Souza", "F", "PL", "RJ", "CD", "");

    @Test
    void localEDataPorExtenso() throws Exception {
        FechoDocumentoArticulado fecho = FechoDocumentoArticulado.de(documento("\"local\":\"  Sala das Sessões \",\"data\":\"2026-04-24\""));

        assertThat(fecho.getLocal()).isEqualTo("Sala das Sessões");
        assertThat(fecho.getData()).isEqualTo("24 de abril de 2026");
    }

    @Test
    void diaSemZeroEMesEmMinusculas() throws Exception {
        assertThat(FechoDocumentoArticulado.de(documento("\"data\":\"2026-05-01\"")).getData()).isEqualTo("1 de maio de 2026");
    }

    /** Data vazia, inválida ou de outro tipo: o fecho sai com espaços para preencher. */
    @ParameterizedTest
    @ValueSource(strings = { "\"data\":\"\"", "\"data\":\"24/04/2026\"", "\"data\":\"2026-02-30\"", "\"data\":20260424", "" })
    void dataAusenteOuInvalida(String data) throws Exception {
        FechoDocumentoArticulado fecho = FechoDocumentoArticulado.de(documento("\"local\":\"Sala das Sessões\"" + (data.isEmpty() ? "" : "," + data)));

        assertThat(fecho.getLocal()).isEqualTo("Sala das Sessões");
        assertThat(fecho.getData()).isEmpty();
    }

    @Test
    void semLocalNemAutoria() throws Exception {
        FechoDocumentoArticulado fecho = FechoDocumentoArticulado.de(documento("\"data\":\"2026-04-24\""));

        assertThat(fecho.getLocal()).isEmpty();
        assertThat(fecho.getAssinaturas()).isEmpty();
    }

    @Test
    void documentoSemLexeditMetadado() throws Exception {
        FechoDocumentoArticulado fecho = FechoDocumentoArticulado.de(new ObjectMapper().readTree("{\"value\":{\"metadado\":{}}}"));

        assertThat(fecho.getLocal()).isEmpty();
        assertThat(fecho.getData()).isEmpty();
        assertThat(fecho.getAssinaturas()).isEmpty();
    }

    @Test
    void tratamentoPelaCasaEPeloSexoNaOrdemDoDocumento() throws Exception {
        FechoDocumentoArticulado fecho = FechoDocumentoArticulado.de(documento(autoria(true, SENADOR, SENADORA, DEPUTADO, DEPUTADA)));

        assertThat(fecho.getAssinaturas()).extracting(Assinatura::getNome).containsExactly("Senador Davi Alcolumbre",
                "Senadora Soraya Thronicke", "Deputado João da Silva", "Deputada Maria Souza");
    }

    /** Fora da Câmara (inclusive CN ou casa ausente), Senador; sexo diferente de M, feminino. */
    @ParameterizedTest
    @CsvSource({ "M,CN,Senador", "F,,Senadora", ",SF,Senadora", "M,CD,Deputado", "X,CD,Deputada" })
    void tratamentoNosCasosDeBorda(String sexo, String casa, String tratamento) throws Exception {
        String parlamentar = parlamentar("Fulano", sexo == null ? "" : sexo, "P", "UF", casa == null ? "" : casa, "");

        assertThat(FechoDocumentoArticulado.de(documento(autoria(true, parlamentar))).getAssinaturas().get(0).getNome())
                .isEqualTo(tratamento + " Fulano");
    }

    @Test
    void partidoEUfConformeImprimirPartidoUF() throws Exception {
        assertThat(FechoDocumentoArticulado.de(documento(autoria(true, SENADOR, SENADORA))).getAssinaturas())
                .extracting(Assinatura::getPartidoUF).containsExactly("(UNIÃO - AP)", "(PSB - MS)");
        assertThat(FechoDocumentoArticulado.de(documento(autoria(false, SENADOR, SENADORA))).getAssinaturas())
                .extracting(Assinatura::getPartidoUF).containsExactly("", "");
    }

    @Test
    void semPartidoNemUfNaoImprimeParenteses() throws Exception {
        String semPartido = parlamentar("Fulano", "M", "", "", "SF", "");

        assertThat(FechoDocumentoArticulado.de(documento(autoria(true, semPartido))).getAssinaturas().get(0).getPartidoUF()).isEmpty();
    }

    @Test
    void cargoPreenchidoOuVazio() throws Exception {
        assertThat(FechoDocumentoArticulado.de(documento(autoria(true, SENADOR, SENADORA))).getAssinaturas())
                .extracting(Assinatura::getCargo).containsExactly("Presidente do Senado Federal", "");
    }

    @Test
    void autoriaPorComissaoSemAssinaturas() throws Exception {
        FechoDocumentoArticulado fecho = FechoDocumentoArticulado.de(documento("\"local\":\"Sala das Comissões\",\"autoria\":{"
                + "\"tipo\":\"Comissão\",\"colegiadoAutor\":{\"identificacao\":\"4444\",\"nome\":\"Comissão de Assuntos Econômicos\","
                + "\"sigla\":\"CAE\"}}"));

        assertThat(fecho.getLocal()).isEqualTo("Sala das Comissões");
        assertThat(fecho.getAssinaturas()).isEmpty();
    }

    @Test
    void parlamentarSemNomeIgnorado() throws Exception {
        assertThat(FechoDocumentoArticulado.de(documento(autoria(true, parlamentar(" ", "M", "P", "UF", "SF", ""), SENADORA)))
                .getAssinaturas()).extracting(Assinatura::getNome).containsExactly("Senadora Soraya Thronicke");
    }

    // Anexo de parecer (issue #80) -------------------------------------------------------------------------------

    @Test
    void anexoDeParecerSemFechoNemAssinaturasMesmoPreenchidos() throws Exception {
        JsonNode documento = documento("\"anexoParecer\":true,\"local\":\"Sala das Sessões\",\"data\":\"2026-04-24\"," + autoria(true, SENADOR, DEPUTADA));

        assertThat(FechoDocumentoArticulado.anexoParecer(documento)).isTrue();
        FechoDocumentoArticulado fecho = FechoDocumentoArticulado.de(documento);
        assertThat(fecho.getLocal()).isEmpty();
        assertThat(fecho.getData()).isEmpty();
        assertThat(fecho.getAssinaturas()).isEmpty();
    }

    /** Ausente indica falso (issue #80); só o booleano verdadeiro conta. */
    @ParameterizedTest
    @ValueSource(strings = { "\"anexoParecer\":false,", "", "\"anexoParecer\":\"true\",", "\"anexoParecer\":1," })
    void naoEhAnexoDeParecer(String atributo) throws Exception {
        JsonNode documento = documento(atributo + "\"local\":\"Sala das Sessões\",\"data\":\"2026-04-24\"," + autoria(true, SENADOR));

        assertThat(FechoDocumentoArticulado.anexoParecer(documento)).isFalse();
        FechoDocumentoArticulado fecho = FechoDocumentoArticulado.de(documento);
        assertThat(fecho.getLocal()).isEqualTo("Sala das Sessões");
        assertThat(fecho.getData()).isEqualTo("24 de abril de 2026");
        assertThat(fecho.getAssinaturas()).extracting(Assinatura::getNome).containsExactly("Senador Davi Alcolumbre");
    }

    @Test
    void exemplo() {
        FechoDocumentoArticulado fecho = FechoDocumentoArticulado.de(json(ConversorDocumentoArticuladoFake.JSON_EXEMPLO));

        assertThat(fecho.getLocal()).isEqualTo("Sala das Sessões");
        assertThat(fecho.getData()).isEqualTo("24 de abril de 2026");
        assertThat(fecho.getAssinaturas()).extracting(Assinatura::getNome).startsWith("Senador Davi Alcolumbre", "Senadora Soraya Thronicke");
    }

    private static JsonNode json(String recurso) {
        try {
            return new ObjectMapper().readTree(ConversorDocumentoArticuladoFake.recurso(recurso));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /** documento-articulado.json mínimo com os atributos informados no valor do {@code lexedit:Metadado}. */
    private static JsonNode documento(String atributos) throws Exception {
        return new ObjectMapper().readTree("{\"value\":{\"metadado\":{\"metadadoProprietario\":[{\"any\":[{\"name\":{"
                + "\"namespaceURI\":\"http://www.lexml.gov.br/lexedit/1.0\",\"localPart\":\"Metadado\"},\"value\":{" + atributos + "}}]}]}}}");
    }

    private static String autoria(boolean imprimirPartidoUF, String... parlamentares) {
        return "\"autoria\":{\"tipo\":\"Parlamentar\",\"imprimirPartidoUF\":" + imprimirPartidoUF + ",\"parlamentares\":{\"parlamentar\":["
                + String.join(",", parlamentares) + "]}}";
    }

    private static String parlamentar(String nome, String sexo, String partido, String uf, String casa, String cargo) {
        return "{\"nome\":\"" + nome + "\",\"sexo\":\"" + sexo + "\",\"siglaPartido\":\"" + partido + "\",\"siglaUF\":\"" + uf
                + "\",\"siglaCasaLegislativa\":\"" + casa + "\",\"cargo\":\"" + cargo + "\"}";
    }

}
