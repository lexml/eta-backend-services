package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

/**
 * Tamanhos de fonte e espaçamentos usados na impressão do documento articulado, passados ao template
 * Velocity e à XSLT do conteúdo, a partir das opções de impressão do documento ({@code tamanhoFonte} e
 * {@code reduzirEspacoEntreLinhas}).
 * <p>
 * Regras de cálculo:
 * <ul>
 * <li>{@code maxTamanhoFonte}: 18pt se {@code tamanhoFonte} = 18; senão 16pt;</li>
 * <li>{@code lineHeight}: 120% se "reduzir espaço entre linhas"; senão 150%;</li>
 * <li>{@code pMarginBottom}: 0.45em se "reduzir espaço entre linhas"; senão 0.6em;</li>
 * <li>espaço antes do título da justificação: 22pt se "reduzir"; senão 26pt;</li>
 * <li>espaço antes do corpo da justificação: 10pt se "reduzir"; senão 14pt;</li>
 * <li>espaço antes do local e data: 26pt se "reduzir"; senão 34pt.</li>
 * </ul>
 */
final class ParametrosImpressaoDocumentoArticulado {

    private static final ParametrosImpressaoDocumentoArticulado PADRAO = de(OpcoesImpressaoDocumentoArticulado.padrao());

    private final int tamanhoFonte;
    private final String maxTamanhoFonte;
    private final String lineHeight;
    private final String pMarginBottom;
    private final String espacoAntesTituloJustificacao;
    private final String espacoAntesCorpoJustificacao;
    private final String espacoAntesFecho;

    private ParametrosImpressaoDocumentoArticulado(int tamanhoFonte, boolean reduzirEspacoEntreLinhas) {
        this.tamanhoFonte = tamanhoFonte;
        this.maxTamanhoFonte = tamanhoFonte == 18 ? "18pt" : "16pt";
        this.lineHeight = reduzirEspacoEntreLinhas ? "120%" : "150%";
        this.pMarginBottom = reduzirEspacoEntreLinhas ? "0.45em" : "0.6em";
        this.espacoAntesTituloJustificacao = reduzirEspacoEntreLinhas ? "22pt" : "26pt";
        this.espacoAntesCorpoJustificacao = reduzirEspacoEntreLinhas ? "10pt" : "14pt";
        this.espacoAntesFecho = reduzirEspacoEntreLinhas ? "26pt" : "34pt";
    }

    /** Parâmetros das opções de impressão do documento. */
    static ParametrosImpressaoDocumentoArticulado de(OpcoesImpressaoDocumentoArticulado opcoes) {
        return new ParametrosImpressaoDocumentoArticulado(opcoes.getTamanhoFonte(), opcoes.isReduzirEspacoEntreLinhas());
    }

    /** Valores padrão: fonte 14, destaque 16pt, entrelinha 150%, espaço entre parágrafos 0.6em, 26pt, 14pt e 34pt. */
    static ParametrosImpressaoDocumentoArticulado padrao() {
        return PADRAO;
    }

    /** Tamanho da fonte do texto, em pontos. */
    int getTamanhoFonte() {
        return tamanhoFonte;
    }

    /** Tamanho da fonte de destaque (epígrafe e título da justificação), com unidade. */
    String getMaxTamanhoFonte() {
        return maxTamanhoFonte;
    }

    /** Entrelinha, com unidade. */
    String getLineHeight() {
        return lineHeight;
    }

    /** Espaço após cada parágrafo, com unidade. */
    String getPMarginBottom() {
        return pMarginBottom;
    }

    /** Espaço antes do título "JUSTIFICAÇÃO", com unidade. */
    String getEspacoAntesTituloJustificacao() {
        return espacoAntesTituloJustificacao;
    }

    /** Espaço antes do corpo da justificação, com unidade. */
    String getEspacoAntesCorpoJustificacao() {
        return espacoAntesCorpoJustificacao;
    }

    /** Espaço antes do local e data (fecho), com unidade. */
    String getEspacoAntesFecho() {
        return espacoAntesFecho;
    }

}
