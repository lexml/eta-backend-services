package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converte medidas e cores do XSL-FO para as unidades usadas no OOXML (twips, half-points, EMUs e entrelinha).
 */
final class MedidasFo {

    // Página A4 (210mm x 297mm) e margens do template (25mm topo, 20mm base, 25mm esquerda, 21mm direita)
    static final int LARGURA_PAGINA_TWIPS = 11906;
    static final int ALTURA_PAGINA_TWIPS = 16838;
    static final int MARGEM_TOPO_TWIPS = 1417;
    static final int MARGEM_BASE_TWIPS = 1134;
    static final int MARGEM_ESQUERDA_TWIPS = 1417;
    static final int MARGEM_DIREITA_TWIPS = 1191;
    static final double LARGURA_UTIL_MM = 164.0;
    static final int LARGURA_UTIL_TWIPS = LARGURA_PAGINA_TWIPS - MARGEM_ESQUERDA_TWIPS - MARGEM_DIREITA_TWIPS;

    static final long EMUS_POR_POLEGADA = 914400L;
    static final long EMUS_POR_MM = 36000L;
    static final long EMUS_POR_PX = 9525L;

    // Entrelinha "simples" do Word ≈ 1,15 do corpo; o PDF usa 150% (ou 120%) do corpo
    private static final double SIMPLES_DO_WORD = 1.15;

    private static final Pattern COMPRIMENTO = Pattern.compile("^(-?(?:\\d+(?:\\.\\d+)?|\\.\\d+))\\s*(%|mm|cm|pt|in|px|em)?$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern HEX_6 = Pattern.compile("^#?([0-9a-fA-F]{6})$");
    private static final Pattern HEX_3 = Pattern.compile("^#?([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])$");

    private MedidasFo() {
    }

    /** Converte um comprimento XSL-FO em milímetros; devolve {@code padraoMm} se ausente ou inválido. */
    static double milimetros(String valor, double baseMm, double fontePt, double padraoMm) {
        if (valor == null) {
            return padraoMm;
        }
        Matcher m = COMPRIMENTO.matcher(valor.trim());
        if (!m.matches()) {
            return padraoMm;
        }
        double n = Double.parseDouble(m.group(1));
        String unidade = m.group(2) == null ? "px" : m.group(2).toLowerCase(Locale.ROOT);
        switch (unidade) {
            case "%":
                return baseMm * n / 100.0;
            case "cm":
                return n * 10.0;
            case "pt":
                return n * 25.4 / 72.0;
            case "in":
                return n * 25.4;
            case "px":
                return n * 25.4 / 96.0;
            case "em":
                return (n * fontePt) * 25.4 / 72.0;
            case "mm":
            default:
                return n;
        }
    }

    /** Converte um comprimento XSL-FO em pontos tipográficos (útil para {@code font-size}). */
    static double pontos(String valor, double fonteBasePt, double padraoPt) {
        if (valor == null) {
            return padraoPt;
        }
        Matcher m = COMPRIMENTO.matcher(valor.trim());
        if (!m.matches()) {
            return padraoPt;
        }
        double n = Double.parseDouble(m.group(1));
        String unidade = m.group(2) == null ? "pt" : m.group(2).toLowerCase(Locale.ROOT);
        switch (unidade) {
            case "%":
                return fonteBasePt * n / 100.0;
            case "em":
                return fonteBasePt * n;
            case "mm":
                return n * 72.0 / 25.4;
            case "cm":
                return n * 10.0 * 72.0 / 25.4;
            case "in":
                return n * 72.0;
            case "px":
                return n * 72.0 / 96.0;
            case "pt":
            default:
                return n;
        }
    }

    /** Converte um comprimento XSL-FO para twips (1/20 pt). */
    static int twips(String valor, double baseMm, double fontePt, int padraoTwips) {
        if (valor == null) {
            return padraoTwips;
        }
        Matcher m = COMPRIMENTO.matcher(valor.trim());
        if (!m.matches()) {
            return padraoTwips;
        }
        return mmParaTwips(milimetros(valor, baseMm, fontePt, 0));
    }

    static int mmParaTwips(double mm) {
        return (int) Math.round(mm * 1440.0 / 25.4);
    }

    static int ptParaTwips(double pt) {
        return (int) Math.round(pt * 20.0);
    }

    static int ptParaMeiosPontos(double pt) {
        return (int) Math.round(pt * 2.0);
    }

    static long mmParaEmus(double mm) {
        return Math.round(mm * EMUS_POR_MM);
    }

    /**
     * Converte {@code line-height} do XSL-FO (ex.: {@code 150%}, {@code 120%}, {@code 1.5em}) para o valor
     * proporcional de {@code w:spacing/@w:line} (em 240 avos de linha simples do Word, {@code w:lineRule="auto"}).
     */
    static int entrelinhaWord(String lineHeight) {
        if (lineHeight == null || lineHeight.trim().isEmpty()) {
            return 240;
        }
        Matcher m = COMPRIMENTO.matcher(lineHeight.trim());
        if (!m.matches()) {
            return 240;
        }
        double n = Double.parseDouble(m.group(1));
        String unidade = m.group(2) == null ? "" : m.group(2).toLowerCase(Locale.ROOT);
        double fator;
        if ("%".equals(unidade)) {
            fator = n / 100.0;
        } else if ("em".equals(unidade) || unidade.isEmpty()) {
            fator = n;
        } else {
            return 240;
        }
        if (fator <= 0) {
            return 240;
        }
        return (int) Math.max(200, Math.round(240.0 * fator / SIMPLES_DO_WORD));
    }

    /** Normaliza uma cor CSS/XSL-FO para hexadecimal de 6 dígitos sem {@code #}, ou {@code null} se desconhecida. */
    static String corHex(String cor) {
        if (cor == null || cor.trim().isEmpty()) {
            return null;
        }
        String c = cor.trim().toLowerCase(Locale.ROOT);
        switch (c) {
            case "yellow":
                return "FFFF00";
            case "red":
                return "FF0000";
            case "black":
                return "000000";
            case "white":
                return "FFFFFF";
            case "gray":
            case "grey":
                return "808080";
            case "blue":
                return "0000FF";
            case "green":
                return "008000";
            default:
                break;
        }
        Matcher m6 = HEX_6.matcher(c);
        if (m6.matches()) {
            return m6.group(1).toUpperCase(Locale.ROOT);
        }
        Matcher m3 = HEX_3.matcher(c);
        if (m3.matches()) {
            return (m3.group(1) + m3.group(1) + m3.group(2) + m3.group(2) + m3.group(3) + m3.group(3))
                    .toUpperCase(Locale.ROOT);
        }
        return null;
    }

}
