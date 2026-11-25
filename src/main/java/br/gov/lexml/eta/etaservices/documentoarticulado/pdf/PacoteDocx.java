package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * Monta um arquivo {@code .docx} (pacote OPC / OOXML) em memória usando apenas {@code dom4j} e {@code java.util.zip},
 * sem dependências externas.
 */
final class PacoteDocx {

    static final String NS_W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
    static final String NS_R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
    static final String NS_WP = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing";
    static final String NS_A = "http://schemas.openxmlformats.org/drawingml/2006/main";
    static final String NS_PIC = "http://schemas.openxmlformats.org/drawingml/2006/picture";
    static final String NS_XML = "http://www.w3.org/XML/1998/namespace";

    private static final String NS_CT = "http://schemas.openxmlformats.org/package/2006/content-types";
    private static final String NS_PKG_RELS = "http://schemas.openxmlformats.org/package/2006/relationships";

    private static final String REL_DOCUMENT = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";
    private static final String REL_STYLES = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles";
    private static final String REL_SETTINGS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings";
    private static final String REL_HEADER = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/header";
    private static final String REL_FOOTNOTES = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/footnotes";
    private static final String REL_IMAGE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image";
    private static final String REL_HYPERLINK = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink";

    static final Namespace W = Namespace.get("w", NS_W);
    static final Namespace R = Namespace.get("r", NS_R);
    static final Namespace WP = Namespace.get("wp", NS_WP);
    static final Namespace A = Namespace.get("a", NS_A);
    static final Namespace PIC = Namespace.get("pic", NS_PIC);

    enum Parte {
        DOCUMENT,
        HEADER,
        FOOTNOTES
    }

    private static final class Relacionamento {
        final String id;
        final String tipo;
        final String destino;
        final boolean externo;

        Relacionamento(String id, String tipo, String destino, boolean externo) {
            this.id = id;
            this.tipo = tipo;
            this.destino = destino;
            this.externo = externo;
        }
    }

    private final int tamanhoFontePt;
    private final Document documentXml;
    private final Element body;

    private Document headerXml;
    private Element headerRoot;
    private String headerRelId;

    private Document footnotesXml;
    private Element footnotesRoot;
    private int proximoIdNota = 1;

    private final Map<Parte, List<Relacionamento>> relsPorParte = new LinkedHashMap<>();
    private final Map<String, byte[]> midias = new LinkedHashMap<>();
    private final Set<String> extensoesMidia = new LinkedHashSet<>();

    private int proximaImagem = 1;
    private int proximoDocPr = 1;
    private int proximoBookmark = 1;

    PacoteDocx(int tamanhoFontePt) {
        this.tamanhoFontePt = tamanhoFontePt;
        for (Parte parte : Parte.values()) {
            relsPorParte.put(parte, new ArrayList<>());
        }
        registrarRel(Parte.DOCUMENT, REL_STYLES, "styles.xml", false);
        registrarRel(Parte.DOCUMENT, REL_SETTINGS, "settings.xml", false);

        documentXml = DocumentHelper.createDocument();
        Element root = documentXml.addElement(QName.get("document", W));
        root.add(R);
        root.add(WP);
        root.add(A);
        root.add(PIC);
        body = root.addElement(QName.get("body", W));
    }

    Element getBody() {
        return body;
    }

    /** Cria (se ainda não existir) e devolve o elemento raiz {@code w:hdr} de {@code word/header1.xml}. */
    Element obterOuCriarHeader() {
        if (headerXml == null) {
            headerXml = DocumentHelper.createDocument();
            headerRoot = headerXml.addElement(QName.get("hdr", W));
            headerRoot.add(R);
            headerRoot.add(WP);
            headerRoot.add(A);
            headerRoot.add(PIC);
            headerRelId = registrarRel(Parte.DOCUMENT, REL_HEADER, "header1.xml", false);
        }
        return headerRoot;
    }

    String getHeaderRelId() {
        return headerRelId;
    }

    /** Cria uma nova nota de rodapé em {@code word/footnotes.xml} e devolve o elemento {@code w:footnote}. */
    Element criarNotaDeRodape() {
        if (footnotesXml == null) {
            footnotesXml = DocumentHelper.createDocument();
            footnotesRoot = footnotesXml.addElement(QName.get("footnotes", W));
            footnotesRoot.add(R);
            footnotesRoot.add(WP);
            footnotesRoot.add(A);
            footnotesRoot.add(PIC);
            registrarRel(Parte.DOCUMENT, REL_FOOTNOTES, "footnotes.xml", false);

            // Separadores padrão exigidos pelo OOXML (id -1 e 0)
            Element sep = footnotesRoot.addElement(QName.get("footnote", W));
            sep.addAttribute(QName.get("type", W), "separator");
            sep.addAttribute(QName.get("id", W), "-1");
            sep.addElement(QName.get("p", W)).addElement(QName.get("r", W)).addElement(QName.get("separator", W));

            Element cont = footnotesRoot.addElement(QName.get("footnote", W));
            cont.addAttribute(QName.get("type", W), "continuationSeparator");
            cont.addAttribute(QName.get("id", W), "0");
            cont.addElement(QName.get("p", W)).addElement(QName.get("r", W)).addElement(QName.get("continuationSeparator", W));
        }
        int id = proximoIdNota++;
        Element nota = footnotesRoot.addElement(QName.get("footnote", W));
        nota.addAttribute(QName.get("id", W), String.valueOf(id));
        return nota;
    }

    String registrarHyperlinkExterno(Parte parte, String url) {
        return registrarRel(parte, REL_HYPERLINK, url, true);
    }

    String registrarImagem(Parte parte, String extensao, byte[] bytes) {
        String ext = extensao.toLowerCase();
        String nome = "image" + (proximaImagem++) + "." + ext;
        midias.put("word/media/" + nome, bytes);
        extensoesMidia.add(ext);
        return registrarRel(parte, REL_IMAGE, "media/" + nome, false);
    }

    int proximoDocPrId() {
        return proximoDocPr++;
    }

    int proximoBookmarkId() {
        return proximoBookmark++;
    }

    private String registrarRel(Parte parte, String tipo, String destino, boolean externo) {
        List<Relacionamento> lista = relsPorParte.get(parte);
        String id = "rId" + (lista.size() + 1);
        lista.add(new Relacionamento(id, tipo, destino, externo));
        return id;
    }

    /** Serializa todas as partes em um arquivo ZIP ({@code .docx}) em memória. */
    byte[] serializar() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            escreverXml(zos, "[Content_Types].xml", construirContentTypes());
            escreverXml(zos, "_rels/.rels", construirPackageRels());
            escreverXml(zos, "word/styles.xml", construirStyles());
            escreverXml(zos, "word/settings.xml", construirSettings());
            escreverXml(zos, "word/document.xml", documentXml);
            escreverXml(zos, "word/_rels/document.xml.rels", construirRels(relsPorParte.get(Parte.DOCUMENT)));

            if (headerXml != null) {
                escreverXml(zos, "word/header1.xml", headerXml);
                if (!relsPorParte.get(Parte.HEADER).isEmpty()) {
                    escreverXml(zos, "word/_rels/header1.xml.rels", construirRels(relsPorParte.get(Parte.HEADER)));
                }
            }
            if (footnotesXml != null) {
                escreverXml(zos, "word/footnotes.xml", footnotesXml);
                if (!relsPorParte.get(Parte.FOOTNOTES).isEmpty()) {
                    escreverXml(zos, "word/_rels/footnotes.xml.rels", construirRels(relsPorParte.get(Parte.FOOTNOTES)));
                }
            }
            for (Map.Entry<String, byte[]> midia : midias.entrySet()) {
                zos.putNextEntry(new ZipEntry(midia.getKey()));
                zos.write(midia.getValue());
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    private Document construirContentTypes() {
        Document doc = DocumentHelper.createDocument();
        Element types = doc.addElement(QName.get("Types", "", NS_CT));
        adicionarDefault(types, "rels", "application/vnd.openxmlformats-package.relationships+xml");
        adicionarDefault(types, "xml", "application/xml");
        for (String ext : extensoesMidia) {
            adicionarDefault(types, ext, contentTypeImagem(ext));
        }
        adicionarOverride(types, "/word/document.xml",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml");
        adicionarOverride(types, "/word/styles.xml",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml");
        adicionarOverride(types, "/word/settings.xml",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml");
        if (headerXml != null) {
            adicionarOverride(types, "/word/header1.xml",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml");
        }
        if (footnotesXml != null) {
            adicionarOverride(types, "/word/footnotes.xml",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.footnotes+xml");
        }
        return doc;
    }

    private static void adicionarDefault(Element types, String ext, String contentType) {
        Element el = types.addElement(QName.get("Default", "", NS_CT));
        el.addAttribute("Extension", ext);
        el.addAttribute("ContentType", contentType);
    }

    private static void adicionarOverride(Element types, String partName, String contentType) {
        Element el = types.addElement(QName.get("Override", "", NS_CT));
        el.addAttribute("PartName", partName);
        el.addAttribute("ContentType", contentType);
    }

    private static String contentTypeImagem(String ext) {
        switch (ext) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            default:
                return "application/octet-stream";
        }
    }

    private Document construirPackageRels() {
        List<Relacionamento> rels = new ArrayList<>();
        rels.add(new Relacionamento("rId1", REL_DOCUMENT, "word/document.xml", false));
        return construirRels(rels);
    }

    private Document construirRels(List<Relacionamento> rels) {
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement(QName.get("Relationships", "", NS_PKG_RELS));
        for (Relacionamento rel : rels) {
            Element el = root.addElement(QName.get("Relationship", "", NS_PKG_RELS));
            el.addAttribute("Id", rel.id);
            el.addAttribute("Type", rel.tipo);
            el.addAttribute("Target", rel.destino);
            if (rel.externo) {
                el.addAttribute("TargetMode", "External");
            }
        }
        return doc;
    }

    private Document construirStyles() {
        Document doc = DocumentHelper.createDocument();
        Element styles = doc.addElement(QName.get("styles", W));

        String szPadrao = String.valueOf(MedidasFo.ptParaMeiosPontos(tamanhoFontePt));
        String szNota = String.valueOf(MedidasFo.ptParaMeiosPontos(tamanhoFontePt * 0.7));

        Element docDefaults = styles.addElement(QName.get("docDefaults", W));
        Element rPrDefault = docDefaults.addElement(QName.get("rPrDefault", W)).addElement(QName.get("rPr", W));
        Element rFonts = rPrDefault.addElement(QName.get("rFonts", W));
        rFonts.addAttribute(QName.get("ascii", W), "Gentium Basic");
        rFonts.addAttribute(QName.get("hAnsi", W), "Gentium Basic");
        rFonts.addAttribute(QName.get("cs", W), "Gentium Basic");
        rPrDefault.addElement(QName.get("sz", W)).addAttribute(QName.get("val", W), szPadrao);
        rPrDefault.addElement(QName.get("szCs", W)).addAttribute(QName.get("val", W), szPadrao);
        rPrDefault.addElement(QName.get("lang", W)).addAttribute(QName.get("val", W), "pt-BR");

        Element pPrDefault = docDefaults.addElement(QName.get("pPrDefault", W)).addElement(QName.get("pPr", W));
        Element spacing = pPrDefault.addElement(QName.get("spacing", W));
        spacing.addAttribute(QName.get("before", W), "0");
        spacing.addAttribute(QName.get("after", W), "0");
        spacing.addAttribute(QName.get("line", W), "240");
        spacing.addAttribute(QName.get("lineRule", W), "auto");

        // Estilo Normal
        Element normal = styles.addElement(QName.get("style", W));
        normal.addAttribute(QName.get("type", W), "paragraph");
        normal.addAttribute(QName.get("default", W), "1");
        normal.addAttribute(QName.get("styleId", W), "Normal");
        normal.addElement(QName.get("name", W)).addAttribute(QName.get("val", W), "Normal");

        // Estilo FootnoteReference (número sobrescrito)
        Element fnRef = styles.addElement(QName.get("style", W));
        fnRef.addAttribute(QName.get("type", W), "character");
        fnRef.addAttribute(QName.get("styleId", W), "FootnoteReference");
        fnRef.addElement(QName.get("name", W)).addAttribute(QName.get("val", W), "footnote reference");
        fnRef.addElement(QName.get("rPr", W))
                .addElement(QName.get("vertAlign", W)).addAttribute(QName.get("val", W), "superscript");

        // Estilo FootnoteText
        Element fnText = styles.addElement(QName.get("style", W));
        fnText.addAttribute(QName.get("type", W), "paragraph");
        fnText.addAttribute(QName.get("styleId", W), "FootnoteText");
        fnText.addElement(QName.get("name", W)).addAttribute(QName.get("val", W), "footnote text");
        Element fnRPr = fnText.addElement(QName.get("rPr", W));
        fnRPr.addElement(QName.get("sz", W)).addAttribute(QName.get("val", W), szNota);
        fnRPr.addElement(QName.get("szCs", W)).addAttribute(QName.get("val", W), szNota);

        // Estilo Hyperlink (cinza #808080, sem sublinhado, como no PDF)
        Element link = styles.addElement(QName.get("style", W));
        link.addAttribute(QName.get("type", W), "character");
        link.addAttribute(QName.get("styleId", W), "Hyperlink");
        link.addElement(QName.get("name", W)).addAttribute(QName.get("val", W), "Hyperlink");
        link.addElement(QName.get("rPr", W))
                .addElement(QName.get("color", W)).addAttribute(QName.get("val", W), "808080");

        return doc;
    }

    private Document construirSettings() {
        Document doc = DocumentHelper.createDocument();
        Element settings = doc.addElement(QName.get("settings", W));
        settings.addElement(QName.get("defaultTabStop", W)).addAttribute(QName.get("val", W), "708");
        if (footnotesXml != null) {
            Element fnPr = settings.addElement(QName.get("footnotePr", W));
            fnPr.addElement(QName.get("footnote", W)).addAttribute(QName.get("id", W), "-1");
            fnPr.addElement(QName.get("footnote", W)).addAttribute(QName.get("id", W), "0");
        }
        return doc;
    }

    private static void escreverXml(ZipOutputStream zos, String nome, Document doc) throws IOException {
        zos.putNextEntry(new ZipEntry(nome));
        OutputFormat format = OutputFormat.createCompactFormat();
        format.setEncoding("UTF-8");
        format.setTrimText(false);
        ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
        XMLWriter writer = new XMLWriter(xmlOut, format);
        writer.write(doc);
        writer.flush();
        zos.write(xmlOut.toByteArray());
        zos.closeEntry();
    }

}
