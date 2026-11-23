package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.XPath;

/**
 * Ajusta o XSL-FO ao renderer RTF do FOP, que não entende algumas construções aceitas pelo PDF. Opera só sobre a
 * árvore lida do XSL-FO, sem alterar a XSLT nem o template do PDF.
 */
final class DocumentoArticuladoFoRtf {

    static final String AVISO_IMAGEM = "[Imagem omitida: o RTF só aceita imagens JPEG.]";

    private static final String NS_FO = "http://www.w3.org/1999/XSL/Format";
    private static final Pattern URL_FO = Pattern.compile("^url\\(\\s*['\"]?(.*?)['\"]?\\s*\\)$", Pattern.DOTALL);
    private static final Pattern PROPORCIONAL = Pattern.compile("^proportional-column-width\\(\\s*([\\d.]+)\\s*\\)$");
    private static final Pattern COMPRIMENTO = Pattern.compile("^([\\d.]+)\\s*(%|mm|cm|pt|in|px)?$");

    // A4 (210mm) menos as margens do template (25mm e 21mm)
    private static final double LARGURA_UTIL_MM = 164;
    private static final String ESPACO_APOS_CABECALHO = "28pt";

    // Entrelinha "simples" do Word ≈ 1,15 do corpo; o PDF usa 150% (ou 120%) do corpo
    private static final double SIMPLES_DO_WORD = 1.15;
    private static final Pattern FONTE_DO_TEXTO = Pattern.compile("\\\\f(\\d+) Gentium Basic;");

    private DocumentoArticuladoFoRtf() {
    }

    static void adaptar(Document fo) {
        desembrulharLinksExternos(fo);
        substituirImagensNaoJpeg(fo);
        removerTabelasDeCentralizacao(fo);
        fixarLarguraDasColunas(fo);
        deixarOWordReduzirSobrescritos(fo);
        moverCabecalhoParaOCorpo(fo);
    }

    /**
     * O renderer RTF ignora a região {@code before} (cabeçalho da primeira página: brasão, casa legislativa e texto do
     * cabeçalho, presentes conforme {@code imprimirBrasao} e {@code textoCabecalho}). O conteúdo passa para o início do corpo,
     * antes da caixa de pendências, como no PDF.
     */
    private static void moverCabecalhoParaOCorpo(Document fo) {
        Element cabecalho = (Element) xpath("//fo:static-content[@flow-name = 'header-principal']").selectSingleNode(fo);
        Element corpo = (Element) xpath("//fo:flow[@flow-name = 'xsl-region-body']").selectSingleNode(fo);
        if (cabecalho == null || corpo == null) {
            return;
        }
        List<Node> filhos = new ArrayList<>(cabecalho.content());
        List<Node> conteudoDoCorpo = corpo.content();
        int posicao = 0;
        Element ultimoBloco = null;
        // C\u00f3pia: o FOP exige ao menos um bloco no static-content, que continua no FO
        for (Node filho : filhos) {
            Node copia = (Node) filho.clone();
            conteudoDoCorpo.add(posicao++, copia);
            if (copia instanceof Element) {
                ultimoBloco = (Element) copia;
            }
        }
        if (ultimoBloco != null) {
            // O RTF só tem espaço nos blocos que viram parágrafo: o do contêiner externo seria perdido
            Element folha = ultimoBloco;
            List<Element> internos = folha.elements("block");
            while (!internos.isEmpty()) {
                folha = internos.get(internos.size() - 1);
                internos = folha.elements("block");
            }
            folha.addAttribute("space-after", ESPACO_APOS_CABECALHO);
        }
    }

    /** O Word já reduz o texto sobrescrito/subscrito; com o 0.7em do PDF por cima, o número (da nota de rodapé, por exemplo) fica minúsculo. */
    private static void deixarOWordReduzirSobrescritos(Document fo) {
        for (Object no : xpath("//fo:inline[@baseline-shift = 'super' or @baseline-shift = 'sub'][@font-size]").selectNodes(fo)) {
            Element inline = (Element) no;
            inline.remove(inline.attribute("font-size"));
        }
    }

    /**
     * O renderer RTF não converte {@code line-height} e escreve a fonte sem família (o Word cai em Arial se o Gentium Basic não
     * estiver instalado). Aplica a entrelinha do PDF como múltiplo de linha e dá à fonte a família romana, com Georgia de
     * alternativa. Opera sobre o texto RTF, que é ASCII (acentos escapados).
     */
    static String ajustarRtf(String rtf, ParametrosImpressaoDocumentoArticulado parametros) {
        String entrelinha = "\\sl" + Math.round(240 * percentual(parametros.getLineHeight()) / 100 / SIMPLES_DO_WORD) + "\\slmult1 ";
        String ajustado = rtf.replaceFirst("\\\\sectd", Matcher.quoteReplacement("\\sectd " + entrelinha));
        // O FOP marca a nota como \ftnalt (nota de fim): ela iria para o fim do documento, e não para o rodapé da página
        ajustado = ajustado.replaceAll("\\\\footnote\\s*\\\\ftnalt", Matcher.quoteReplacement("\\footnote"));
        // \pard volta a propriedades-padrão de parágrafo e perderia a entrelinha
        ajustado = ajustado.replace("\\pard", "\\pard " + entrelinha);
        return FONTE_DO_TEXTO.matcher(ajustado).replaceAll(r -> Matcher.quoteReplacement(
                "\\f" + r.group(1) + "\\froman\\fcharset0 Gentium Basic{\\*\\falt Georgia};"));
    }

    private static double percentual(String lineHeight) {
        try {
            return Double.parseDouble(lineHeight.replace("%", "").trim());
        } catch (NumberFormatException e) {
            return 150;
        }
    }

    /**
     * A tabela externa de 3 colunas serve só para centralizar a interna (PDF); no RTF ela vira tabela aninhada. Fica só a
     * interna, com a largura da coluna do meio, e perde a centralização.
     */
    private static void removerTabelasDeCentralizacao(Document fo) {
        String externas = "//fo:table[count(fo:table-body/fo:table-row) = 1 and fo:table-body/fo:table-row/fo:table-cell/fo:block/fo:table]";
        for (Object no : xpath(externas).selectNodes(fo)) {
            Element externa = (Element) no;
            Element bloco = (Element) xpath("fo:table-body/fo:table-row/fo:table-cell/fo:block[fo:table]").selectSingleNode(externa);
            Element interna = bloco.element("table");
            List<Element> colunas = externa.elements("table-column");
            if (colunas.size() > 1 && colunas.get(1).attributeValue("column-width") != null) {
                interna.addAttribute("width", colunas.get(1).attributeValue("column-width"));
            }
            bloco.detach();
            List<Node> irmaos = externa.getParent().content();
            irmaos.set(irmaos.indexOf(externa), bloco);
        }
    }

    /**
     * O renderer RTF não resolve {@code proportional-column-width} nem porcentagem: a célula sai com largura zero e o Word
     * não mostra a tabela. As colunas passam a ter largura absoluta, calculada sobre a largura útil da página.
     */
    private static void fixarLarguraDasColunas(Document fo) {
        for (Object no : xpath("//fo:table[not(ancestor::fo:table)]").selectNodes(fo)) {
            Element tabela = (Element) no;
            double total = milimetros(tabela.attributeValue("width"), LARGURA_UTIL_MM, LARGURA_UTIL_MM);
            List<Element> colunas = tabela.elements("table-column");
            if (colunas.isEmpty()) {
                continue;
            }
            double[] fixas = new double[colunas.size()];
            double[] pesos = new double[colunas.size()];
            double somaFixas = 0;
            double somaPesos = 0;
            for (int i = 0; i < colunas.size(); i++) {
                String largura = colunas.get(i).attributeValue("column-width");
                Matcher proporcional = largura == null ? null : PROPORCIONAL.matcher(largura.trim());
                if (proporcional != null && proporcional.matches()) {
                    pesos[i] = Double.parseDouble(proporcional.group(1));
                } else if (largura != null && COMPRIMENTO.matcher(largura.trim()).matches()) {
                    fixas[i] = milimetros(largura, total, 0);
                } else {
                    pesos[i] = 1;
                }
                somaFixas += fixas[i];
                somaPesos += pesos[i];
            }
            double restante = Math.max(0, total - somaFixas);
            for (int i = 0; i < colunas.size(); i++) {
                double mm = pesos[i] > 0 ? restante * pesos[i] / somaPesos : fixas[i];
                colunas.get(i).addAttribute("column-width", formatar(mm));
            }
            tabela.addAttribute("width", formatar(total));
        }
    }

    /** Converte um comprimento FO (ou porcentagem de {@code base}) em mm; sem valor válido, {@code padrao}. */
    private static double milimetros(String valor, double base, double padrao) {
        Matcher m = valor == null ? null : COMPRIMENTO.matcher(valor.trim());
        if (m == null || !m.matches()) {
            return padrao;
        }
        double n = Double.parseDouble(m.group(1));
        String unidade = m.group(2) == null ? "px" : m.group(2);
        switch (unidade) {
            case "%":
                return base * n / 100;
            case "cm":
                return n * 10;
            case "pt":
                return n * 25.4 / 72;
            case "in":
                return n * 25.4;
            case "px":
                return n * 25.4 / 96;
            default:
                return n;
        }
    }

    private static String formatar(double mm) {
        return String.format(Locale.ROOT, "%.1fmm", mm);
    }

    /** O renderer RTF copia o valor bruto de {@code external-destination}, inclusive o {@code url('...')}. */
    private static void desembrulharLinksExternos(Document fo) {
        for (Object no : xpath("//fo:basic-link[@external-destination]").selectNodes(fo)) {
            Element link = (Element) no;
            Matcher m = URL_FO.matcher(link.attributeValue("external-destination").trim());
            if (m.matches()) {
                link.addAttribute("external-destination", m.group(1));
            }
        }
    }

    /** O renderer RTF só aceita JPEG; nos demais formatos ele joga a exceção e o base64 como texto do documento. */
    private static void substituirImagensNaoJpeg(Document fo) {
        for (Object no : xpath("//fo:external-graphic").selectNodes(fo)) {
            Element imagem = (Element) no;
            if (ehJpeg(imagem.attributeValue("src"))) {
                continue;
            }
            Element aviso = DocumentHelper.createElement(QName.get("inline", "fo", NS_FO));
            aviso.addAttribute("color", "#FF0000");
            aviso.setText(AVISO_IMAGEM);
            List<Node> irmaos = imagem.getParent().content();
            irmaos.set(irmaos.indexOf(imagem), aviso);
        }
    }

    private static boolean ehJpeg(String src) {
        if (src == null) {
            return false;
        }
        Matcher m = URL_FO.matcher(src.trim());
        String endereco = (m.matches() ? m.group(1) : src).trim().toLowerCase(Locale.ROOT);
        return endereco.startsWith("data:image/jpeg") || endereco.startsWith("data:image/jpg")
                || endereco.startsWith("data:image/pjpeg");
    }

    private static XPath xpath(String expressao) {
        XPath xpath = DocumentHelper.createXPath(expressao);
        xpath.setNamespaceURIs(java.util.Collections.singletonMap("fo", NS_FO));
        return xpath;
    }

}
