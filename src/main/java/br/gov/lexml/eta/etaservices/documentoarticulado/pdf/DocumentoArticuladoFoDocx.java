package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.pdf.PacoteDocx.A;
import static br.gov.lexml.eta.etaservices.documentoarticulado.pdf.PacoteDocx.PIC;
import static br.gov.lexml.eta.etaservices.documentoarticulado.pdf.PacoteDocx.R;
import static br.gov.lexml.eta.etaservices.documentoarticulado.pdf.PacoteDocx.W;
import static br.gov.lexml.eta.etaservices.documentoarticulado.pdf.PacoteDocx.WP;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.Text;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.PacoteDocx.Parte;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Traduz o XSL-FO do documento articulado (o mesmo usado pelo PDF) para OOXML ({@code .docx}), usando
 * {@link PacoteDocx} e {@link MedidasFo}, sem alterar a XSLT nem o template Velocity.
 */
final class DocumentoArticuladoFoDocx {

    static final String AVISO_IMAGEM = "[Imagem omitida: não foi possível incluí-la no DOCX.]";

    private static final Logger LOG = LoggerFactory.getLogger(DocumentoArticuladoFoDocx.class);
    private static final String NS_FO = "http://www.w3.org/1999/XSL/Format";
    private static final Pattern URL_FO = Pattern.compile("^url\\(\\s*['\"]?(.*?)['\"]?\\s*\\)$", Pattern.DOTALL);
    private static final Pattern DATA_IMAGEM = Pattern.compile("^data:image/([^;,]+);base64,(.*)$",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern PROPORCIONAL = Pattern.compile("^proportional-column-width\\(\\s*([\\d.]+)\\s*\\)$");
    private static final Pattern NUMERO_INICIAL_NOTA = Pattern.compile("^\\s*\\d+\\s+");
    private static final Pattern ESPACOS_CONSECUTIVOS = Pattern.compile("[ \\t\\r\\n]+");

    private final PacoteDocx pacote;
    private int espacoPendenteAntesTwips;

    private DocumentoArticuladoFoDocx(int tamanhoFontePt) {
        this.pacote = new PacoteDocx(tamanhoFontePt);
    }

    /** Converte o XSL-FO completo do documento articulado para bytes de um arquivo {@code .docx} válido. */
    static byte[] gerar(String xslFo, ParametrosImpressaoDocumentoArticulado parametros) {
        DocumentoArticuladoFoHelper helper = new DocumentoArticuladoFoHelper(xslFo);
        return gerar(helper.getFoSemXmpmeta(), parametros);
    }

    static byte[] gerar(Document fo, ParametrosImpressaoDocumentoArticulado parametros) {
        try {
            DocumentoArticuladoFoDocx conversor = new DocumentoArticuladoFoDocx(parametros.getTamanhoFonte());
            conversor.converter(fo, parametros);
            return conversor.pacote.serializar();
        } catch (EtaBackendException e) {
            throw e;
        } catch (Exception e) {
            throw new EtaBackendException("Erro ao gerar o DOCX do documento articulado: " + e.getMessage(), e);
        }
    }

    private void converter(Document fo, ParametrosImpressaoDocumentoArticulado parametros) {
        EstiloFo estiloBase = EstiloFo.raiz(parametros.getTamanhoFonte());

        Element cabecalho = (Element) xpath("//fo:static-content[@flow-name = 'header-principal']").selectSingleNode(fo);
        boolean temCabecalho = cabecalho != null && !cabecalho.elements().isEmpty();
        if (temCabecalho) {
            Element hdr = pacote.obterOuCriarHeader();
            processarFilhosBloco(cabecalho, hdr, Parte.HEADER, estiloBase, MedidasFo.LARGURA_UTIL_MM, false, false);
            garantirParagrafo(hdr);
        }

        Element corpo = (Element) xpath("//fo:flow[@flow-name = 'xsl-region-body']").selectSingleNode(fo);
        if (corpo == null) {
            throw new EtaBackendException("O XSL-FO do documento articulado não contém o fluxo principal (xsl-region-body).");
        }
        EstiloFo estiloCorpo = estiloBase.comAtributos(corpo);
        espacoPendenteAntesTwips = 0;
        processarFilhosBloco(corpo, pacote.getBody(), Parte.DOCUMENT, estiloCorpo, MedidasFo.LARGURA_UTIL_MM, false, false);

        adicionarConfiguracaoDePagina(temCabecalho);
    }

    private void adicionarConfiguracaoDePagina(boolean temCabecalho) {
        Element sectPr = pacote.getBody().addElement(QName.get("sectPr", W));
        if (temCabecalho && pacote.getHeaderRelId() != null) {
            Element hdrRef = sectPr.addElement(QName.get("headerReference", W));
            hdrRef.addAttribute(QName.get("type", W), "first");
            hdrRef.addAttribute(QName.get("id", R), pacote.getHeaderRelId());
        }
        Element pgSz = sectPr.addElement(QName.get("pgSz", W));
        pgSz.addAttribute(QName.get("w", W), String.valueOf(MedidasFo.LARGURA_PAGINA_TWIPS));
        pgSz.addAttribute(QName.get("h", W), String.valueOf(MedidasFo.ALTURA_PAGINA_TWIPS));

        Element pgMar = sectPr.addElement(QName.get("pgMar", W));
        pgMar.addAttribute(QName.get("top", W), String.valueOf(MedidasFo.MARGEM_TOPO_TWIPS));
        pgMar.addAttribute(QName.get("right", W), String.valueOf(MedidasFo.MARGEM_DIREITA_TWIPS));
        pgMar.addAttribute(QName.get("bottom", W), String.valueOf(MedidasFo.MARGEM_BASE_TWIPS));
        pgMar.addAttribute(QName.get("left", W), String.valueOf(MedidasFo.MARGEM_ESQUERDA_TWIPS));
        pgMar.addAttribute(QName.get("header", W), "708");
        pgMar.addAttribute(QName.get("footer", W), "708");
        pgMar.addAttribute(QName.get("gutter", W), "0");

        if (temCabecalho) {
            sectPr.addElement(QName.get("titlePg", W));
        }
    }

    /**
     * Percorre os filhos de um elemento de bloco XSL-FO, emitindo parágrafos, tabelas e listas em {@code destino}.
     * Quando um {@code fo:block} mistura texto/inline com sub-blocos, o texto/inline anterior é fechado em um parágrafo
     * antes de abrir o sub-bloco.
     */
    private void processarFilhosBloco(Element container, Element destino, Parte parte, EstiloFo estilo,
            double larguraDisponivelMm, boolean modoNota, boolean removerNumeroInicialNota) {
        Element paragrafoAtual = null;
        boolean primeiroParagrafoEmitido = false;
        boolean numeroNotaRemovido = !removerNumeroInicialNota;
        boolean temTextoOuInlineDireto = false;

        List<Node> nos = container.content();
        for (Node no : nos) {
            if (no instanceof Text) {
                String texto = normalizarTexto(no.getText(), estilo.colapsarEspacos);
                if (texto.isEmpty()) {
                    continue;
                }
                if (paragrafoAtual == null) {
                    paragrafoAtual = abrirParagrafo(destino, estilo, !primeiroParagrafoEmitido, modoNota);
                    primeiroParagrafoEmitido = true;
                }
                if (!numeroNotaRemovido) {
                    texto = NUMERO_INICIAL_NOTA.matcher(texto).replaceFirst("");
                    numeroNotaRemovido = true;
                }
                if (!texto.isEmpty()) {
                    adicionarRunTexto(paragrafoAtual, texto, estilo);
                }
                temTextoOuInlineDireto = true;
            } else if (no instanceof Element) {
                Element el = (Element) no;
                String nome = el.getName();
                if (ehElementoDeBloco(nome)) {
                    paragrafoAtual = null;
                    processarElementoDeBloco(el, destino, parte, estilo, larguraDisponivelMm, modoNota,
                            !numeroNotaRemovido);
                    numeroNotaRemovido = true;
                    primeiroParagrafoEmitido = true;
                } else {
                    if (paragrafoAtual == null) {
                        paragrafoAtual = abrirParagrafo(destino, estilo, !primeiroParagrafoEmitido, modoNota);
                        primeiroParagrafoEmitido = true;
                    }
                    processarInline(el, paragrafoAtual, parte, estilo, larguraDisponivelMm);
                    temTextoOuInlineDireto = true;
                }
            }
        }

        // Bloco explicitamente vazio (ex.: <fo:block/> dentro de item de lista)
        if (!primeiroParagrafoEmitido && !temTextoOuInlineDireto && "block".equals(container.getName())
                && container.content().isEmpty()) {
            abrirParagrafo(destino, estilo, true, modoNota);
        }
    }

    private void processarElementoDeBloco(Element el, Element destino, Parte parte, EstiloFo estiloPai,
            double larguraDisponivelMm, boolean modoNota, boolean removerNumeroInicialNota) {
        String nome = el.getName();
        switch (nome) {
            case "block":
            case "block-container":
                processarBloco(el, destino, parte, estiloPai, larguraDisponivelMm, modoNota, removerNumeroInicialNota);
                break;
            case "table":
                processarTabela(el, destino, parte, estiloPai, larguraDisponivelMm);
                break;
            case "list-block":
                processarLista(el, destino, parte, estiloPai, larguraDisponivelMm);
                break;
            default:
                processarFilhosBloco(el, destino, parte, estiloPai.comAtributos(el), larguraDisponivelMm, modoNota,
                        removerNumeroInicialNota);
                break;
        }
    }

    private void processarBloco(Element bloco, Element destino, Parte parte, EstiloFo estiloPai,
            double larguraDisponivelMm, boolean modoNota, boolean removerNumeroInicialNota) {
        // Caixa com fundo colorido (ex.: caixa de pendências): tabela 1x1 sem borda para fundo contínuo com padding
        String fundoHex = MedidasFo.corHex(bloco.attributeValue("background-color"));
        if (fundoHex != null && !modoNota) {
            processarCaixaColorida(bloco, destino, parte, estiloPai, larguraDisponivelMm, fundoHex);
            return;
        }

        EstiloFo estilo = estiloPai.comAtributos(bloco);
        int antes = espacoAntes(bloco, estilo.fontePt);
        if (antes > 0) {
            espacoPendenteAntesTwips += antes;
        }

        String idDestino = bloco.attributeValue("id");
        boolean temFilhosBloco = contemElementoDeBloco(bloco);
        int filhosAntes = destino.elements().size();

        if (!temFilhosBloco) {
            // Bloco folha: vira diretamente um parágrafo (com seu próprio margin-bottom / space-after / keepNext / id)
            int depois = espacoDepois(bloco, estilo.fontePt);
            EstiloFo estiloFolha = estilo.comEspacoDepois(depois).comBookmark(idDestino);
            processarBlocoFolha(bloco, destino, parte, estiloFolha, larguraDisponivelMm, modoNota, removerNumeroInicialNota);
        } else {
            // Bloco contêiner: repassa estilo herdável e propaga keep-with-next / keep-together / id
            EstiloFo estiloCont = estilo.comBookmark(idDestino);
            processarFilhosBloco(bloco, destino, parte, estiloCont, larguraDisponivelMm, modoNota, removerNumeroInicialNota);

            List<Element> emitidos = destino.elements().subList(filhosAntes, destino.elements().size());
            aplicarPropriedadesDeContainer(bloco, emitidos);

            int depois = espacoDepois(bloco, estilo.fontePt);
            if (depois > 0) {
                espacoPendenteAntesTwips += depois;
            }
        }
    }

    private void processarBlocoFolha(Element bloco, Element destino, Parte parte, EstiloFo estilo,
            double larguraDisponivelMm, boolean modoNota, boolean removerNumeroInicialNota) {
        Element p = abrirParagrafo(destino, estilo, true, modoNota);
        boolean numeroNotaRemovido = !removerNumeroInicialNota;

        String bookmarkId = estilo.bookmark;
        int bmNum = -1;
        if (bookmarkId != null && !bookmarkId.isEmpty()) {
            bmNum = pacote.proximoBookmarkId();
            Element start = p.addElement(QName.get("bookmarkStart", W));
            start.addAttribute(QName.get("id", W), String.valueOf(bmNum));
            start.addAttribute(QName.get("name", W), bookmarkId);
        }

        List<Node> nos = bloco.content();
        for (Node no : nos) {
            if (no instanceof Text) {
                String texto = normalizarTexto(no.getText(), estilo.colapsarEspacos);
                if (!numeroNotaRemovido && !texto.isEmpty()) {
                    texto = NUMERO_INICIAL_NOTA.matcher(texto).replaceFirst("");
                    numeroNotaRemovido = true;
                }
                if (!texto.isEmpty()) {
                    adicionarRunTexto(p, texto, estilo);
                }
            } else if (no instanceof Element) {
                processarInline((Element) no, p, parte, estilo, larguraDisponivelMm);
            }
        }

        if (bmNum >= 0) {
            Element end = p.addElement(QName.get("bookmarkEnd", W));
            end.addAttribute(QName.get("id", W), String.valueOf(bmNum));
        }
    }

    /**
     * Renderiza blocos com {@code background-color} (como a caixa de pendências) em uma tabela 1×1 sem bordas com
     * sombreamento e margens internas, evitando filetes brancos entre parágrafos no Word e no LibreOffice.
     */
    private void processarCaixaColorida(Element bloco, Element destino, Parte parte, EstiloFo estiloPai,
            double larguraDisponivelMm, String fundoHex) {
        EstiloFo estilo = estiloPai.comAtributos(bloco);
        int larguraTwips = MedidasFo.mmParaTwips(larguraDisponivelMm);

        Element tbl = destino.addElement(QName.get("tbl", W));
        Element tblPr = tbl.addElement(QName.get("tblPr", W));
        adicionarLargura(tblPr, "tblW", larguraTwips);
        adicionarBordasTabela(tblPr, false);
        Element layout = tblPr.addElement(QName.get("tblLayout", W));
        layout.addAttribute(QName.get("type", W), "fixed");

        Element grid = tbl.addElement(QName.get("tblGrid", W));
        grid.addElement(QName.get("gridCol", W)).addAttribute(QName.get("w", W), String.valueOf(larguraTwips));

        Element tr = tbl.addElement(QName.get("tr", W));
        Element tc = tr.addElement(QName.get("tc", W));
        Element tcPr = tc.addElement(QName.get("tcPr", W));
        adicionarLargura(tcPr, "tcW", larguraTwips);
        Element shd = tcPr.addElement(QName.get("shd", W));
        shd.addAttribute(QName.get("val", W), "clear");
        shd.addAttribute(QName.get("color", W), "auto");
        shd.addAttribute(QName.get("fill", W), fundoHex);

        int padGeral = MedidasFo.twips(bloco.attributeValue("padding"), larguraDisponivelMm, estilo.fontePt, 57);
        int padEsq = MedidasFo.twips(bloco.attributeValue("padding-left"), larguraDisponivelMm, estilo.fontePt, padGeral);
        int padDir = MedidasFo.twips(bloco.attributeValue("padding-right"), larguraDisponivelMm, estilo.fontePt, padGeral);
        adicionarMargensCelula(tcPr, padGeral, padEsq, padGeral, padDir);

        int pendenteAntesSalvo = espacoPendenteAntesTwips;
        espacoPendenteAntesTwips = 0;
        double larguraInternaMm = Math.max(20.0, larguraDisponivelMm - (padEsq + padDir) * 25.4 / 1440.0);
        processarFilhosBloco(bloco, tc, parte, estilo.comTextoIndent(0).comMargemEsquerda(0), larguraInternaMm, false, false);
        garantirParagrafo(tc);

        espacoPendenteAntesTwips = pendenteAntesSalvo + espacoDepois(bloco, estilo.fontePt);
    }

    private void processarLista(Element lista, Element destino, Parte parte, EstiloFo estiloPai, double larguraDisponivelMm) {
        EstiloFo estiloLista = estiloPai.comAtributos(lista);
        int recuoEsquerdo = estiloLista.margemEsquerdaTwips;
        int distanciaRotulo = MedidasFo.twips(lista.attributeValue("provisional-distance-between-starts"),
                larguraDisponivelMm, estiloLista.fontePt, MedidasFo.ptParaTwips(estiloLista.fontePt));
        int recuoCorpo = recuoEsquerdo + Math.max(distanciaRotulo, 240);

        List<Element> itens = lista.elements("list-item");
        for (int i = 0; i < itens.size(); i++) {
            Element item = itens.get(i);
            int espacoAntesItem = espacoAntes(item, estiloLista.fontePt);
            if (espacoAntesItem > 0) {
                espacoPendenteAntesTwips += espacoAntesItem;
            }
            Element labelEl = item.element("list-item-label");
            Element bodyEl = item.element("list-item-body");
            String rotulo = labelEl == null ? "" : labelEl.getStringValue().trim();

            if (bodyEl != null) {
                int inicio = destino.elements().size();
                EstiloFo estiloItem = estiloLista.comMargemEsquerda(recuoCorpo).comTextoIndent(0);
                processarFilhosBloco(bodyEl, destino, parte, estiloItem, larguraDisponivelMm, false, false);
                List<Element> gerados = destino.elements().subList(inicio, destino.elements().size());
                if (!gerados.isEmpty() && !rotulo.isEmpty()) {
                    Element primeiro = gerados.get(0);
                    if ("p".equals(primeiro.getName())) {
                        inserirRotuloDeLista(primeiro, rotulo, estiloLista, recuoCorpo, Math.max(distanciaRotulo, 240));
                    }
                }
            }
        }

        int depois = espacoDepois(lista, estiloLista.fontePt);
        if (depois > 0) {
            espacoPendenteAntesTwips += depois;
        }
    }

    private static void inserirRotuloDeLista(Element p, String rotulo, EstiloFo estilo, int recuoCorpoTwips, int hangingTwips) {
        Element pPr = p.element("pPr");
        if (pPr == null) {
            pPr = DocumentHelper.createElement(QName.get("pPr", W));
            p.content().add(0, pPr);
        }
        Element ind = pPr.element("ind");
        if (ind == null) {
            ind = DocumentHelper.createElement(QName.get("ind", W));
            inserirOrdenadoPPr(pPr, ind);
        }
        ind.addAttribute(QName.get("left", W), String.valueOf(recuoCorpoTwips));
        ind.addAttribute(QName.get("hanging", W), String.valueOf(hangingTwips));
        if (ind.attribute("firstLine") != null) {
            ind.remove(ind.attribute("firstLine"));
        }

        Element tabs = pPr.element("tabs");
        if (tabs == null) {
            tabs = DocumentHelper.createElement(QName.get("tabs", W));
            inserirOrdenadoPPr(pPr, tabs);
        }
        Element tab = tabs.addElement(QName.get("tab", W));
        tab.addAttribute(QName.get("val", W), "left");
        tab.addAttribute(QName.get("pos", W), String.valueOf(recuoCorpoTwips));

        Element runRotulo = criarRunTexto(rotulo, estilo);
        Element runTab = DocumentHelper.createElement(QName.get("r", W));
        runTab.addElement(QName.get("tab", W));

        int posInsercao = p.content().indexOf(pPr) + 1;
        p.content().add(posInsercao, runTab);
        p.content().add(posInsercao, runRotulo);
    }

    private void processarTabela(Element tabela, Element destino, Parte parte, EstiloFo estiloPai, double larguraDisponivelMm) {
        // Desembrulha a tabela externa de 3 colunas usada no XSL-FO apenas para centralizar a tabela interna
        boolean centralizada = false;
        String larguraForcada = null;
        Element interna = extrairTabelaInternaDeCentralizacao(tabela);
        if (interna != null) {
            List<Element> colsExt = tabela.elements("table-column");
            if (colsExt.size() > 1) {
                larguraForcada = colsExt.get(1).attributeValue("column-width");
            }
            tabela = interna;
            centralizada = true;
        }

        EstiloFo estiloTabela = estiloPai.comAtributos(tabela).comTextoIndent(0).comMargemEsquerda(0);
        String attrWidth = larguraForcada != null ? larguraForcada : tabela.attributeValue("width");
        double larguraTotalMm = MedidasFo.milimetros(attrWidth, larguraDisponivelMm, estiloTabela.fontePt, larguraDisponivelMm);
        larguraTotalMm = Math.min(larguraTotalMm, larguraDisponivelMm);

        List<Element> colunasFo = tabela.elements("table-column");
        int numColunas = colunasFo.size();
        Element tbody = tabela.element("table-body");
        if (tbody == null) {
            return;
        }
        List<Element> linhasFo = tbody.elements("table-row");
        if (numColunas == 0) {
            numColunas = calcularColunasDasLinhas(linhasFo);
        }
        if (numColunas == 0) {
            return;
        }

        double[] largurasColunasMm = calcularLargurasColunas(colunasFo, numColunas, larguraTotalMm, estiloTabela.fontePt);
        int larguraTotalTwips = 0;
        int[] largurasColunasTwips = new int[numColunas];
        for (int i = 0; i < numColunas; i++) {
            largurasColunasTwips[i] = Math.max(120, MedidasFo.mmParaTwips(largurasColunasMm[i]));
            larguraTotalTwips += largurasColunasTwips[i];
        }

        boolean comBordaTabela = temBorda(tabela);

        Element tbl = destino.addElement(QName.get("tbl", W));
        Element tblPr = tbl.addElement(QName.get("tblPr", W));
        adicionarLargura(tblPr, "tblW", larguraTotalTwips);
        if (centralizada) {
            tblPr.addElement(QName.get("jc", W)).addAttribute(QName.get("val", W), "center");
        }
        adicionarBordasTabela(tblPr, comBordaTabela);
        tblPr.addElement(QName.get("tblLayout", W)).addAttribute(QName.get("type", W), "fixed");

        Element grid = tbl.addElement(QName.get("tblGrid", W));
        for (int w : largurasColunasTwips) {
            grid.addElement(QName.get("gridCol", W)).addAttribute(QName.get("w", W), String.valueOf(w));
        }

        int[] rowspanRestante = new int[numColunas];
        boolean[] rowspanBorda = new boolean[numColunas];

        for (Element linhaFo : linhasFo) {
            Element tr = tbl.addElement(QName.get("tr", W));
            List<Element> celulasFo = linhaFo.elements("table-cell");
            int col = 0;

            for (Element celulaFo : celulasFo) {
                while (col < numColunas && rowspanRestante[col] > 0) {
                    adicionarCelulaContinuacaoRowspan(tr, largurasColunasTwips[col], rowspanBorda[col]);
                    rowspanRestante[col]--;
                    col++;
                }
                if (col >= numColunas) {
                    break;
                }
                int colspan = Math.max(1, inteiro(celulaFo.attributeValue("number-columns-spanned"), 1));
                int rowspan = Math.max(1, inteiro(celulaFo.attributeValue("number-rows-spanned"), 1));
                colspan = Math.min(colspan, numColunas - col);

                int larguraCelulaTwips = 0;
                double larguraCelulaMm = 0;
                for (int c = 0; c < colspan; c++) {
                    larguraCelulaTwips += largurasColunasTwips[col + c];
                    larguraCelulaMm += largurasColunasMm[col + c];
                }

                boolean comBordaCelula = temBorda(celulaFo);
                Element tc = tr.addElement(QName.get("tc", W));
                Element tcPr = tc.addElement(QName.get("tcPr", W));
                adicionarLargura(tcPr, "tcW", larguraCelulaTwips);
                if (colspan > 1) {
                    tcPr.addElement(QName.get("gridSpan", W)).addAttribute(QName.get("val", W), String.valueOf(colspan));
                }
                if (rowspan > 1) {
                    tcPr.addElement(QName.get("vMerge", W)).addAttribute(QName.get("val", W), "restart");
                    for (int c = 0; c < colspan; c++) {
                        rowspanRestante[col + c] = rowspan - 1;
                        rowspanBorda[col + c] = comBordaCelula;
                    }
                }
                if (comBordaCelula) {
                    adicionarBordasCelula(tcPr);
                }
                String padding = celulaFo.attributeValue("padding");
                if (padding != null) {
                    int pad = MedidasFo.twips(padding, larguraCelulaMm, estiloTabela.fontePt, 40);
                    adicionarMargensCelula(tcPr, pad, pad, pad, pad);
                }
                if ("center".equals(celulaFo.attributeValue("display-align"))) {
                    tcPr.addElement(QName.get("vAlign", W)).addAttribute(QName.get("val", W), "center");
                }

                int pendenteSalvo = espacoPendenteAntesTwips;
                espacoPendenteAntesTwips = 0;
                EstiloFo estiloCelula = estiloTabela.comAtributos(celulaFo);
                processarFilhosBloco(celulaFo, tc, parte, estiloCelula, Math.max(10.0, larguraCelulaMm), false, false);
                garantirParagrafo(tc);
                espacoPendenteAntesTwips = pendenteSalvo;

                col += colspan;
            }

            while (col < numColunas && rowspanRestante[col] > 0) {
                adicionarCelulaContinuacaoRowspan(tr, largurasColunasTwips[col], rowspanBorda[col]);
                rowspanRestante[col]--;
                col++;
            }
        }
    }

    private static void adicionarCelulaContinuacaoRowspan(Element tr, int larguraTwips, boolean comBorda) {
        Element tc = tr.addElement(QName.get("tc", W));
        Element tcPr = tc.addElement(QName.get("tcPr", W));
        adicionarLargura(tcPr, "tcW", larguraTwips);
        tcPr.addElement(QName.get("vMerge", W));
        if (comBorda) {
            adicionarBordasCelula(tcPr);
        }
        tc.addElement(QName.get("p", W));
    }

    private static Element extrairTabelaInternaDeCentralizacao(Element tabela) {
        Element tbody = tabela.element("table-body");
        if (tbody == null) {
            return null;
        }
        List<Element> linhas = tbody.elements("table-row");
        if (linhas.size() != 1) {
            return null;
        }
        for (Element cell : linhas.get(0).elements("table-cell")) {
            Element block = cell.element("block");
            if (block != null && block.element("table") != null) {
                return block.element("table");
            }
        }
        return null;
    }

    private static double[] calcularLargurasColunas(List<Element> colunas, int numColunas, double totalMm, double fontePt) {
        double[] fixas = new double[numColunas];
        double[] pesos = new double[numColunas];
        double somaFixas = 0;
        double somaPesos = 0;
        for (int i = 0; i < numColunas; i++) {
            String largura = i < colunas.size() ? colunas.get(i).attributeValue("column-width") : null;
            Matcher prop = largura == null ? null : PROPORCIONAL.matcher(largura.trim());
            if (prop != null && prop.matches()) {
                pesos[i] = Double.parseDouble(prop.group(1));
            } else if (largura != null) {
                double mm = MedidasFo.milimetros(largura, totalMm, fontePt, -1);
                if (mm > 0) {
                    fixas[i] = mm;
                } else {
                    pesos[i] = 1;
                }
            } else {
                pesos[i] = 1;
            }
            somaFixas += fixas[i];
            somaPesos += pesos[i];
        }
        double restante = Math.max(0, totalMm - somaFixas);
        double[] resultado = new double[numColunas];
        for (int i = 0; i < numColunas; i++) {
            resultado[i] = pesos[i] > 0 ? restante * pesos[i] / somaPesos : fixas[i];
        }
        return resultado;
    }

    private static int calcularColunasDasLinhas(List<Element> linhas) {
        int max = 0;
        for (Element linha : linhas) {
            int soma = 0;
            for (Element cell : linha.elements("table-cell")) {
                soma += Math.max(1, inteiro(cell.attributeValue("number-columns-spanned"), 1));
            }
            if (soma > max) {
                max = soma;
            }
        }
        return max;
    }

    private void processarInline(Element el, Element p, Parte parte, EstiloFo estiloPai, double larguraDisponivelMm) {
        String nome = el.getName();
        switch (nome) {
            case "inline":
                processarInlineElemento(el, p, parte, estiloPai, larguraDisponivelMm);
                break;
            case "basic-link":
                processarLink(el, p, parte, estiloPai, larguraDisponivelMm);
                break;
            case "leader":
                processarLeader(el, p, estiloPai);
                break;
            case "footnote":
                processarNotaDeRodape(el, p, estiloPai);
                break;
            case "external-graphic":
                processarImagem(el, p, parte, estiloPai, larguraDisponivelMm);
                break;
            default:
                processarConteudoInline(el, p, parte, estiloPai.comAtributos(el), larguraDisponivelMm);
                break;
        }
    }

    private void processarInlineElemento(Element inline, Element p, Parte parte, EstiloFo estiloPai, double larguraDisponivelMm) {
        // Sobrescrito/subscrito: o Word já reduz o tamanho com w:vertAlign; não aplicar o 0.7em do PDF por cima
        String shift = inline.attributeValue("baseline-shift");
        EstiloFo estilo = ("super".equals(shift) || "sub".equals(shift))
                ? estiloPai.comAtributosSemTamanho(inline)
                : estiloPai.comAtributos(inline);

        String id = inline.attributeValue("id");
        int bmNum = -1;
        if (id != null && !id.isEmpty()) {
            bmNum = pacote.proximoBookmarkId();
            Element start = p.addElement(QName.get("bookmarkStart", W));
            start.addAttribute(QName.get("id", W), String.valueOf(bmNum));
            start.addAttribute(QName.get("name", W), id);
        }

        processarConteudoInline(inline, p, parte, estilo, larguraDisponivelMm);

        if (bmNum >= 0) {
            Element end = p.addElement(QName.get("bookmarkEnd", W));
            end.addAttribute(QName.get("id", W), String.valueOf(bmNum));
        }
    }

    private void processarLink(Element link, Element p, Parte parte, EstiloFo estiloPai, double larguraDisponivelMm) {
        EstiloFo estilo = estiloPai.comAtributos(link);
        String ext = link.attributeValue("external-destination");
        String interno = link.attributeValue("internal-destination");

        Element hyperlink;
        if (ext != null && !ext.trim().isEmpty()) {
            String url = desembrulharUrl(ext);
            String rId = pacote.registrarHyperlinkExterno(parte, url);
            hyperlink = p.addElement(QName.get("hyperlink", W));
            hyperlink.addAttribute(QName.get("id", R), rId);
        } else if (interno != null && !interno.trim().isEmpty()) {
            hyperlink = p.addElement(QName.get("hyperlink", W));
            hyperlink.addAttribute(QName.get("anchor", W), interno.trim());
        } else {
            processarConteudoInline(link, p, parte, estilo, larguraDisponivelMm);
            return;
        }
        processarConteudoInline(link, hyperlink, parte, estilo, larguraDisponivelMm);
    }

    private static void processarLeader(Element leader, Element p, EstiloFo estilo) {
        String padrao = leader.attributeValue("leader-pattern");
        if ("dots".equals(padrao)) {
            Element pPr = p.element("pPr");
            if (pPr == null) {
                pPr = DocumentHelper.createElement(QName.get("pPr", W));
                p.content().add(0, pPr);
            }
            Element tabs = pPr.element("tabs");
            if (tabs == null) {
                tabs = DocumentHelper.createElement(QName.get("tabs", W));
                inserirOrdenadoPPr(pPr, tabs);
            }
            Element tab = tabs.addElement(QName.get("tab", W));
            tab.addAttribute(QName.get("val", W), "right");
            tab.addAttribute(QName.get("leader", W), "dot");
            tab.addAttribute(QName.get("pos", W), String.valueOf(MedidasFo.LARGURA_UTIL_TWIPS));

            Element run = p.addElement(QName.get("r", W));
            aplicarRPr(run, estilo, null);
            run.addElement(QName.get("tab", W));
        }
    }

    private void processarNotaDeRodape(Element footnote, Element p, EstiloFo estiloPai) {
        Element nota = pacote.criarNotaDeRodape();
        String id = nota.attributeValue(QName.get("id", W));

        // Referência sobrescrita no corpo do documento
        Element refRun = p.addElement(QName.get("r", W));
        Element refRPr = refRun.addElement(QName.get("rPr", W));
        refRPr.addElement(QName.get("rStyle", W)).addAttribute(QName.get("val", W), "FootnoteReference");
        refRun.addElement(QName.get("footnoteReference", W)).addAttribute(QName.get("id", W), id);

        // Corpo da nota em word/footnotes.xml
        Element fnBody = footnote.element("footnote-body");
        if (fnBody != null) {
            int pendenteSalvo = espacoPendenteAntesTwips;
            espacoPendenteAntesTwips = 0;
            EstiloFo estiloNota = EstiloFo.raiz(estiloPai.fonteBasePt);
            processarFilhosBloco(fnBody, nota, Parte.FOOTNOTES, estiloNota, MedidasFo.LARGURA_UTIL_MM, true, true);
            espacoPendenteAntesTwips = pendenteSalvo;
        }
        garantirParagrafo(nota);

        // Prefixa o primeiro parágrafo da nota com <w:footnoteRef/> + espaço
        Element primeiroP = nota.element("p");
        if (primeiroP != null) {
            Element runRef = DocumentHelper.createElement(QName.get("r", W));
            runRef.addElement(QName.get("rPr", W))
                    .addElement(QName.get("rStyle", W)).addAttribute(QName.get("val", W), "FootnoteReference");
            runRef.addElement(QName.get("footnoteRef", W));

            Element runEspaco = DocumentHelper.createElement(QName.get("r", W));
            Element t = runEspaco.addElement(QName.get("t", W));
            t.addAttribute(QName.get("space", "xml", PacoteDocx.NS_XML), "preserve");
            t.setText(" ");

            Element pPr = primeiroP.element("pPr");
            int pos = pPr != null ? primeiroP.content().indexOf(pPr) + 1 : 0;
            primeiroP.content().add(pos, runEspaco);
            primeiroP.content().add(pos, runRef);
        }
    }

    private void processarImagem(Element imagem, Element p, Parte parte, EstiloFo estilo, double larguraDisponivelMm) {
        String src = imagem.attributeValue("src");
        String endereco = src == null ? "" : desembrulharUrl(src);
        Matcher m = DATA_IMAGEM.matcher(endereco);
        if (!m.matches()) {
            adicionarAvisoImagem(p, estilo);
            return;
        }
        try {
            String subtipo = m.group(1).toLowerCase(Locale.ROOT);
            byte[] bytes = Base64.getMimeDecoder().decode(m.group(2));
            BufferedImage lida = ImageIO.read(new ByteArrayInputStream(bytes));
            if (lida == null || lida.getWidth() <= 0 || lida.getHeight() <= 0) {
                LOG.warn("Imagem do DOCX omitida: formato sem leitor.");
                adicionarAvisoImagem(p, estilo);
                return;
            }

            String ext = extensaoImagem(subtipo);
            double larguraNaturalMm = lida.getWidth() * 25.4 / 96.0;
            String widthAttr = imagem.attributeValue("width");
            double larguraMm;
            if (widthAttr != null && !widthAttr.trim().isEmpty()) {
                larguraMm = MedidasFo.milimetros(widthAttr, larguraDisponivelMm, estilo.fontePt, larguraDisponivelMm);
                larguraMm = Math.min(larguraMm, larguraDisponivelMm);
            } else {
                larguraMm = Math.min(larguraNaturalMm, larguraDisponivelMm);
            }
            double alturaMm = larguraMm * lida.getHeight() / lida.getWidth();

            long cx = Math.max(1L, MedidasFo.mmParaEmus(larguraMm));
            long cy = Math.max(1L, MedidasFo.mmParaEmus(alturaMm));

            String rId = pacote.registrarImagem(parte, ext, bytes);
            int docPrId = pacote.proximoDocPrId();
            adicionarRunImagem(p, rId, docPrId, cx, cy);
        } catch (IOException | RuntimeException e) {
            LOG.warn("Imagem do DOCX omitida: não foi possível decodificá-la ({}).", e.getClass().getSimpleName());
            adicionarAvisoImagem(p, estilo);
        }
    }

    private static void adicionarAvisoImagem(Element p, EstiloFo estilo) {
        adicionarRunTexto(p, AVISO_IMAGEM, estilo.comCor("FF0000"));
    }

    private static void adicionarRunImagem(Element p, String rId, int docPrId, long cx, long cy) {
        Element run = p.addElement(QName.get("r", W));
        Element drawing = run.addElement(QName.get("drawing", W));
        Element inline = drawing.addElement(QName.get("inline", WP));
        inline.addAttribute("distT", "0");
        inline.addAttribute("distB", "0");
        inline.addAttribute("distL", "0");
        inline.addAttribute("distR", "0");

        Element extent = inline.addElement(QName.get("extent", WP));
        extent.addAttribute("cx", String.valueOf(cx));
        extent.addAttribute("cy", String.valueOf(cy));

        Element docPr = inline.addElement(QName.get("docPr", WP));
        docPr.addAttribute("id", String.valueOf(docPrId));
        docPr.addAttribute("name", "Imagem " + docPrId);

        Element graphic = inline.addElement(QName.get("graphic", A));
        Element graphicData = graphic.addElement(QName.get("graphicData", A));
        graphicData.addAttribute("uri", PacoteDocx.NS_PIC);

        Element pic = graphicData.addElement(QName.get("pic", PIC));
        Element nvPicPr = pic.addElement(QName.get("nvPicPr", PIC));
        Element cNvPr = nvPicPr.addElement(QName.get("cNvPr", PIC));
        cNvPr.addAttribute("id", String.valueOf(docPrId));
        cNvPr.addAttribute("name", "Imagem " + docPrId);
        nvPicPr.addElement(QName.get("cNvPicPr", PIC));

        Element blipFill = pic.addElement(QName.get("blipFill", PIC));
        Element blip = blipFill.addElement(QName.get("blip", A));
        blip.addAttribute(QName.get("embed", R), rId);
        blipFill.addElement(QName.get("stretch", A)).addElement(QName.get("fillRect", A));

        Element spPr = pic.addElement(QName.get("spPr", PIC));
        Element xfrm = spPr.addElement(QName.get("xfrm", A));
        Element off = xfrm.addElement(QName.get("off", A));
        off.addAttribute("x", "0");
        off.addAttribute("y", "0");
        Element ext = xfrm.addElement(QName.get("ext", A));
        ext.addAttribute("cx", String.valueOf(cx));
        ext.addAttribute("cy", String.valueOf(cy));
        Element prstGeom = spPr.addElement(QName.get("prstGeom", A));
        prstGeom.addAttribute("prst", "rect");
        prstGeom.addElement(QName.get("avLst", A));
    }

    private void processarConteudoInline(Element container, Element destinoInline, Parte parte, EstiloFo estilo,
            double larguraDisponivelMm) {
        for (Node no : container.content()) {
            if (no instanceof Text) {
                String texto = normalizarTexto(no.getText(), estilo.colapsarEspacos);
                if (!texto.isEmpty()) {
                    adicionarRunTexto(destinoInline, texto, estilo);
                }
            } else if (no instanceof Element) {
                processarInline((Element) no, destinoInline, parte, estilo, larguraDisponivelMm);
            }
        }
    }

    private Element abrirParagrafo(Element destino, EstiloFo estilo, boolean consumirEspacoAntes, boolean modoNota) {
        Element p = destino.addElement(QName.get("p", W));
        Element pPr = p.addElement(QName.get("pPr", W));

        if (modoNota) {
            pPr.addElement(QName.get("pStyle", W)).addAttribute(QName.get("val", W), "FootnoteText");
        }
        if (estilo.keepWithNext) {
            pPr.addElement(QName.get("keepNext", W));
        }
        if (estilo.keepTogether) {
            pPr.addElement(QName.get("keepLines", W));
        }

        int antes = consumirEspacoAntes ? espacoPendenteAntesTwips : 0;
        if (consumirEspacoAntes) {
            espacoPendenteAntesTwips = 0;
        }
        int depois = estilo.espacoDepoisTwips;
        int entrelinha = estilo.entrelinhaWord;

        Element spacing = pPr.addElement(QName.get("spacing", W));
        if (antes > 0) {
            spacing.addAttribute(QName.get("before", W), String.valueOf(antes));
        }
        if (depois > 0) {
            spacing.addAttribute(QName.get("after", W), String.valueOf(depois));
        }
        spacing.addAttribute(QName.get("line", W), String.valueOf(entrelinha));
        spacing.addAttribute(QName.get("lineRule", W), "auto");

        if (estilo.margemEsquerdaTwips > 0 || estilo.recuoPrimeiraLinhaTwips != 0) {
            Element ind = pPr.addElement(QName.get("ind", W));
            if (estilo.margemEsquerdaTwips > 0) {
                ind.addAttribute(QName.get("left", W), String.valueOf(estilo.margemEsquerdaTwips));
            }
            if (estilo.recuoPrimeiraLinhaTwips > 0) {
                ind.addAttribute(QName.get("firstLine", W), String.valueOf(estilo.recuoPrimeiraLinhaTwips));
            } else if (estilo.recuoPrimeiraLinhaTwips < 0) {
                ind.addAttribute(QName.get("hanging", W), String.valueOf(-estilo.recuoPrimeiraLinhaTwips));
            }
        }

        if (estilo.alinhamento != null) {
            pPr.addElement(QName.get("jc", W)).addAttribute(QName.get("val", W), estilo.alinhamento);
        }

        return p;
    }

    private static void aplicarPropriedadesDeContainer(Element bloco, List<Element> emitidos) {
        if (emitidos.isEmpty()) {
            return;
        }
        boolean keepNext = ehKeepAlways(bloco.attributeValue("keep-with-next.within-page"))
                || ehKeepAlways(bloco.attributeValue("keep-with-next"));
        boolean keepTogether = ehKeepAlways(bloco.attributeValue("keep-together.within-page"))
                || ehKeepAlways(bloco.attributeValue("keep-together"));

        for (int i = 0; i < emitidos.size(); i++) {
            Element el = emitidos.get(i);
            if (!"p".equals(el.getName())) {
                continue;
            }
            boolean marcarKeepNext = (keepTogether && i < emitidos.size() - 1) || (keepNext && i == emitidos.size() - 1);
            if (marcarKeepNext) {
                Element pPr = el.element("pPr");
                if (pPr == null) {
                    pPr = DocumentHelper.createElement(QName.get("pPr", W));
                    el.content().add(0, pPr);
                }
                if (pPr.element("keepNext") == null) {
                    inserirOrdenadoPPr(pPr, DocumentHelper.createElement(QName.get("keepNext", W)));
                }
            }
        }
    }

    private static void adicionarRunTexto(Element destino, String texto, EstiloFo estilo) {
        destino.add(criarRunTexto(texto, estilo));
    }

    private static Element criarRunTexto(String texto, EstiloFo estilo) {
        Element run = DocumentHelper.createElement(QName.get("r", W));
        aplicarRPr(run, estilo, null);
        Element t = run.addElement(QName.get("t", W));
        t.addAttribute(QName.get("space", "xml", PacoteDocx.NS_XML), "preserve");
        t.setText(texto);
        return run;
    }

    private static void aplicarRPr(Element run, EstiloFo estilo, String rStyle) {
        Element rPr = DocumentHelper.createElement(QName.get("rPr", W));
        if (rStyle != null) {
            rPr.addElement(QName.get("rStyle", W)).addAttribute(QName.get("val", W), rStyle);
        }
        if (estilo.fonteFamilia != null && !"Gentium Basic".equals(estilo.fonteFamilia)) {
            Element rFonts = rPr.addElement(QName.get("rFonts", W));
            rFonts.addAttribute(QName.get("ascii", W), estilo.fonteFamilia);
            rFonts.addAttribute(QName.get("hAnsi", W), estilo.fonteFamilia);
            rFonts.addAttribute(QName.get("cs", W), estilo.fonteFamilia);
        }
        if (estilo.negrito) {
            rPr.addElement(QName.get("b", W));
        }
        if (estilo.italico) {
            rPr.addElement(QName.get("i", W));
        }
        if (estilo.corHex != null) {
            rPr.addElement(QName.get("color", W)).addAttribute(QName.get("val", W), estilo.corHex);
        }
        if (Math.abs(estilo.fontePt - estilo.fonteBasePt) > 0.05) {
            String sz = String.valueOf(MedidasFo.ptParaMeiosPontos(estilo.fontePt));
            rPr.addElement(QName.get("sz", W)).addAttribute(QName.get("val", W), sz);
            rPr.addElement(QName.get("szCs", W)).addAttribute(QName.get("val", W), sz);
        }
        if (estilo.sublinhado) {
            rPr.addElement(QName.get("u", W)).addAttribute(QName.get("val", W), "single");
        }
        if (estilo.vertAlign != null) {
            rPr.addElement(QName.get("vertAlign", W)).addAttribute(QName.get("val", W), estilo.vertAlign);
        }
        if (!rPr.elements().isEmpty()) {
            run.add(rPr);
        }
    }

    private static void inserirOrdenadoPPr(Element pPr, Element novo) {
        int ordemNovo = ordemPPr(novo.getName());
        List<Element> filhos = pPr.elements();
        for (int i = 0; i < filhos.size(); i++) {
            if (ordemPPr(filhos.get(i).getName()) > ordemNovo) {
                pPr.content().add(pPr.content().indexOf(filhos.get(i)), novo);
                return;
            }
        }
        pPr.add(novo);
    }

    private static int ordemPPr(String nome) {
        switch (nome) {
            case "pStyle": return 1;
            case "keepNext": return 2;
            case "keepLines": return 3;
            case "shd": return 4;
            case "tabs": return 5;
            case "spacing": return 6;
            case "ind": return 7;
            case "jc": return 8;
            default: return 99;
        }
    }

    private static void adicionarLargura(Element pai, String nome, int twips) {
        Element el = pai.addElement(QName.get(nome, W));
        el.addAttribute(QName.get("w", W), String.valueOf(twips));
        el.addAttribute(QName.get("type", W), "dxa");
    }

    private static void adicionarBordasTabela(Element tblPr, boolean comBorda) {
        Element borders = tblPr.addElement(QName.get("tblBorders", W));
        String[] lados = { "top", "left", "bottom", "right", "insideH", "insideV" };
        for (String lado : lados) {
            Element b = borders.addElement(QName.get(lado, W));
            if (comBorda) {
                b.addAttribute(QName.get("val", W), "single");
                b.addAttribute(QName.get("sz", W), "6");
                b.addAttribute(QName.get("space", W), "0");
                b.addAttribute(QName.get("color", W), "000000");
            } else {
                b.addAttribute(QName.get("val", W), "none");
            }
        }
    }

    private static void adicionarBordasCelula(Element tcPr) {
        Element borders = tcPr.addElement(QName.get("tcBorders", W));
        String[] lados = { "top", "left", "bottom", "right" };
        for (String lado : lados) {
            Element b = borders.addElement(QName.get(lado, W));
            b.addAttribute(QName.get("val", W), "single");
            b.addAttribute(QName.get("sz", W), "6");
            b.addAttribute(QName.get("space", W), "0");
            b.addAttribute(QName.get("color", W), "000000");
        }
    }

    private static void adicionarMargensCelula(Element tcPr, int top, int left, int bottom, int right) {
        Element mar = tcPr.addElement(QName.get("tcMar", W));
        adicionarLargura(mar, "top", top);
        adicionarLargura(mar, "left", left);
        adicionarLargura(mar, "bottom", bottom);
        adicionarLargura(mar, "right", right);
    }

    private static void garantirParagrafo(Element container) {
        List<Element> filhos = container.elements();
        if (filhos.isEmpty() || !"p".equals(filhos.get(filhos.size() - 1).getName())) {
            container.addElement(QName.get("p", W));
        }
    }

    private static boolean temBorda(Element el) {
        String border = el.attributeValue("border");
        String style = el.attributeValue("border-style");
        return border != null && !border.trim().isEmpty() && !"none".equalsIgnoreCase(border.trim())
                && !"none".equalsIgnoreCase(style);
    }

    private static boolean ehElementoDeBloco(String nome) {
        return "block".equals(nome) || "block-container".equals(nome) || "table".equals(nome) || "list-block".equals(nome);
    }

    private static boolean contemElementoDeBloco(Element el) {
        for (Element filho : el.elements()) {
            if (ehElementoDeBloco(filho.getName())) {
                return true;
            }
        }
        return false;
    }

    private static int espacoAntes(Element el, double fontePt) {
        int s = MedidasFo.twips(el.attributeValue("space-before"), MedidasFo.LARGURA_UTIL_MM, fontePt, 0);
        int m = MedidasFo.twips(el.attributeValue("margin-top"), MedidasFo.LARGURA_UTIL_MM, fontePt, 0);
        return Math.max(s, m);
    }

    private static int espacoDepois(Element el, double fontePt) {
        int s = MedidasFo.twips(el.attributeValue("space-after"), MedidasFo.LARGURA_UTIL_MM, fontePt, 0);
        int m = MedidasFo.twips(el.attributeValue("margin-bottom"), MedidasFo.LARGURA_UTIL_MM, fontePt, 0);
        int p = MedidasFo.twips(el.attributeValue("padding-bottom"), MedidasFo.LARGURA_UTIL_MM, fontePt, 0);
        return Math.max(s, m) + p;
    }

    private static boolean ehKeepAlways(String valor) {
        return "always".equalsIgnoreCase(valor);
    }

    private static String normalizarTexto(String texto, boolean colapsar) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        if (!colapsar) {
            return texto;
        }
        String normalizado = ESPACOS_CONSECUTIVOS.matcher(texto).replaceAll(" ");
        return " ".equals(normalizado) && texto.contains("\n") ? "" : normalizado;
    }

    private static String desembrulharUrl(String valor) {
        Matcher m = URL_FO.matcher(valor.trim());
        return (m.matches() ? m.group(1) : valor).trim();
    }

    private static String extensaoImagem(String subtipo) {
        switch (subtipo) {
            case "jpeg":
            case "jpg":
            case "pjpeg":
                return "jpg";
            case "gif":
                return "gif";
            case "bmp":
                return "bmp";
            case "png":
            default:
                return "png";
        }
    }

    private static int inteiro(String valor, int padrao) {
        if (valor == null) {
            return padrao;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    private static XPath xpath(String expressao) {
        XPath xpath = DocumentHelper.createXPath(expressao);
        xpath.setNamespaceURIs(Collections.singletonMap("fo", NS_FO));
        return xpath;
    }

    /** Estado imutável das propriedades XSL-FO herdadas ao longo da travessia da árvore. */
    private static final class EstiloFo {
        final double fonteBasePt;
        final double fontePt;
        final String fonteFamilia;
        final boolean negrito;
        final boolean italico;
        final boolean sublinhado;
        final String vertAlign;
        final String corHex;
        final String alinhamento;
        final int margemEsquerdaTwips;
        final int recuoPrimeiraLinhaTwips;
        final int entrelinhaWord;
        final int espacoDepoisTwips;
        final boolean keepWithNext;
        final boolean keepTogether;
        final boolean colapsarEspacos;
        final String bookmark;

        private EstiloFo(double fonteBasePt, double fontePt, String fonteFamilia, boolean negrito, boolean italico,
                boolean sublinhado, String vertAlign, String corHex, String alinhamento, int margemEsquerdaTwips,
                int recuoPrimeiraLinhaTwips, int entrelinhaWord, int espacoDepoisTwips, boolean keepWithNext,
                boolean keepTogether, boolean colapsarEspacos, String bookmark) {
            this.fonteBasePt = fonteBasePt;
            this.fontePt = fontePt;
            this.fonteFamilia = fonteFamilia;
            this.negrito = negrito;
            this.italico = italico;
            this.sublinhado = sublinhado;
            this.vertAlign = vertAlign;
            this.corHex = corHex;
            this.alinhamento = alinhamento;
            this.margemEsquerdaTwips = margemEsquerdaTwips;
            this.recuoPrimeiraLinhaTwips = recuoPrimeiraLinhaTwips;
            this.entrelinhaWord = entrelinhaWord;
            this.espacoDepoisTwips = espacoDepoisTwips;
            this.keepWithNext = keepWithNext;
            this.keepTogether = keepTogether;
            this.colapsarEspacos = colapsarEspacos;
            this.bookmark = bookmark;
        }

        static EstiloFo raiz(double fonteBasePt) {
            return new EstiloFo(fonteBasePt, fonteBasePt, "Gentium Basic", false, false, false, null, null, null,
                    0, 0, 240, 0, false, false, true, null);
        }

        EstiloFo comAtributos(Element el) {
            return aplicar(el, true);
        }

        EstiloFo comAtributosSemTamanho(Element el) {
            return aplicar(el, false);
        }

        private EstiloFo aplicar(Element el, boolean lerTamanho) {
            double novaFonte = lerTamanho && el.attributeValue("font-size") != null
                    ? MedidasFo.pontos(el.attributeValue("font-size"), fontePt, fontePt)
                    : fontePt;
            String novaFamilia = el.attributeValue("font-family") != null
                    ? el.attributeValue("font-family").trim() : fonteFamilia;

            boolean novoNegrito = negrito;
            String fw = el.attributeValue("font-weight");
            if ("bold".equalsIgnoreCase(fw)) {
                novoNegrito = true;
            } else if ("normal".equalsIgnoreCase(fw)) {
                novoNegrito = false;
            }

            boolean novoItalico = italico;
            String fs = el.attributeValue("font-style");
            if ("italic".equalsIgnoreCase(fs)) {
                novoItalico = true;
            } else if ("normal".equalsIgnoreCase(fs)) {
                novoItalico = false;
            }

            boolean novoSublinhado = sublinhado;
            String td = el.attributeValue("text-decoration");
            if ("underline".equalsIgnoreCase(td)) {
                novoSublinhado = true;
            } else if ("none".equalsIgnoreCase(td)) {
                novoSublinhado = false;
            }

            String novoVertAlign = vertAlign;
            String bs = el.attributeValue("baseline-shift");
            if ("super".equalsIgnoreCase(bs)) {
                novoVertAlign = "superscript";
            } else if ("sub".equalsIgnoreCase(bs)) {
                novoVertAlign = "subscript";
            }

            String novaCor = corHex;
            String c = el.attributeValue("color");
            if (c != null) {
                String hex = MedidasFo.corHex(c);
                if (hex != null) {
                    novaCor = hex;
                }
            }

            String novoAlinhamento = alinhamento;
            String ta = el.attributeValue("text-align");
            if (ta != null) {
                switch (ta.trim().toLowerCase(Locale.ROOT)) {
                    case "center":
                        novoAlinhamento = "center";
                        break;
                    case "right":
                    case "end":
                        novoAlinhamento = "right";
                        break;
                    case "justify":
                        novoAlinhamento = "both";
                        break;
                    case "left":
                    case "start":
                        novoAlinhamento = "left";
                        break;
                    default:
                        break;
                }
            }

            int novaMargemEsq = el.attributeValue("margin-left") != null
                    ? MedidasFo.twips(el.attributeValue("margin-left"), MedidasFo.LARGURA_UTIL_MM, novaFonte, margemEsquerdaTwips)
                    : margemEsquerdaTwips;
            if (el.attributeValue("start-indent") != null) {
                novaMargemEsq = MedidasFo.twips(el.attributeValue("start-indent"), MedidasFo.LARGURA_UTIL_MM, novaFonte, novaMargemEsq);
            }

            int novoRecuo = el.attributeValue("text-indent") != null
                    ? MedidasFo.twips(el.attributeValue("text-indent"), MedidasFo.LARGURA_UTIL_MM, novaFonte, recuoPrimeiraLinhaTwips)
                    : recuoPrimeiraLinhaTwips;

            int novaEntrelinha = el.attributeValue("line-height") != null
                    ? MedidasFo.entrelinhaWord(el.attributeValue("line-height"))
                    : entrelinhaWord;

            boolean novoKeepNext = ehKeepAlways(el.attributeValue("keep-with-next.within-page"))
                    || ehKeepAlways(el.attributeValue("keep-with-next"));
            boolean novoKeepTogether = ehKeepAlways(el.attributeValue("keep-together.within-page"))
                    || ehKeepAlways(el.attributeValue("keep-together"));

            boolean novoColapsar = colapsarEspacos;
            if ("false".equalsIgnoreCase(el.attributeValue("white-space-collapse"))) {
                novoColapsar = false;
            }

            return new EstiloFo(fonteBasePt, novaFonte, novaFamilia, novoNegrito, novoItalico, novoSublinhado,
                    novoVertAlign, novaCor, novoAlinhamento, novaMargemEsq, novoRecuo, novaEntrelinha, 0,
                    novoKeepNext, novoKeepTogether, novoColapsar, null);
        }

        EstiloFo comCor(String hex) {
            return new EstiloFo(fonteBasePt, fontePt, fonteFamilia, negrito, italico, sublinhado, vertAlign, hex,
                    alinhamento, margemEsquerdaTwips, recuoPrimeiraLinhaTwips, entrelinhaWord, espacoDepoisTwips,
                    keepWithNext, keepTogether, colapsarEspacos, bookmark);
        }

        EstiloFo comEspacoDepois(int twips) {
            return new EstiloFo(fonteBasePt, fontePt, fonteFamilia, negrito, italico, sublinhado, vertAlign, corHex,
                    alinhamento, margemEsquerdaTwips, recuoPrimeiraLinhaTwips, entrelinhaWord, twips,
                    keepWithNext, keepTogether, colapsarEspacos, bookmark);
        }

        EstiloFo comMargemEsquerda(int twips) {
            return new EstiloFo(fonteBasePt, fontePt, fonteFamilia, negrito, italico, sublinhado, vertAlign, corHex,
                    alinhamento, twips, recuoPrimeiraLinhaTwips, entrelinhaWord, espacoDepoisTwips,
                    keepWithNext, keepTogether, colapsarEspacos, bookmark);
        }

        EstiloFo comTextoIndent(int twips) {
            return new EstiloFo(fonteBasePt, fontePt, fonteFamilia, negrito, italico, sublinhado, vertAlign, corHex,
                    alinhamento, margemEsquerdaTwips, twips, entrelinhaWord, espacoDepoisTwips,
                    keepWithNext, keepTogether, colapsarEspacos, bookmark);
        }

        EstiloFo comBookmark(String id) {
            return new EstiloFo(fonteBasePt, fontePt, fonteFamilia, negrito, italico, sublinhado, vertAlign, corHex,
                    alinhamento, margemEsquerdaTwips, recuoPrimeiraLinhaTwips, entrelinhaWord, espacoDepoisTwips,
                    keepWithNext, keepTogether, colapsarEspacos, id);
        }
    }

}
