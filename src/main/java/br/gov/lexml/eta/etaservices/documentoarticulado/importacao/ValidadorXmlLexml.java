package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;
import br.gov.lexml.parser.pl.errors.ParseProblem;
import br.gov.lexml.parser.pl.validation.Validation;
import scala.jdk.javaapi.CollectionConverters;
import scala.xml.Node;
import scala.xml.XML$;

/**
 * Valida arquivos XML LexML e normaliza a URN no modo {@link ModoParseProposicao#APROVEITAMENTO} para
 * garantir aceitação pelo {@code lexml-eta}.
 */
final class ValidadorXmlLexml {

    static final String NAMESPACE_LEXML = "http://www.lexml.gov.br/1.0";
    private static final Namespace NS_LEXML = Namespace.get(NAMESPACE_LEXML);
    private static final Pattern REGEX_URN_VALIDA = Pattern.compile(
            "^urn:lex:br:[^:\\s]+:[^:\\s]+:\\d{4}(?:-\\d{2}-\\d{2})?;\\d+(?:@[^!\\s]+)?$");
    private static final String PLACEHOLDER_URN_ID = "LEXML_URN_ID";

    ResultadoXml validarEPreparar(byte[] bytes, OpcoesParseProposicao opcoes) {
        if (bytes == null || bytes.length == 0) {
            throw new EtaBackendException("O arquivo XML está vazio.");
        }
        return validarEPreparar(new String(bytes, StandardCharsets.UTF_8), opcoes);
    }

    ResultadoXml validarEPreparar(String xml, OpcoesParseProposicao opcoes) {
        if (xml == null) {
            throw new EtaBackendException("O conteúdo XML informado está vazio.");
        }
        String xmlLimpo = xml.startsWith("\uFEFF") ? xml.substring(1) : xml;
        if (xmlLimpo.trim().isEmpty()) {
            throw new EtaBackendException("O arquivo XML está vazio.");
        }

        Document doc = lerXmlSeguro(xmlLimpo);
        Element root = doc.getRootElement();
        if (root == null || !"LexML".equals(root.getName()) || !NAMESPACE_LEXML.equals(root.getNamespaceURI())) {
            throw new EtaBackendException("O arquivo XML não é um documento LexML válido.");
        }

        Element projetoNorma = root.element(QName.get("ProjetoNorma", NS_LEXML));
        if (projetoNorma == null) {
            throw new EtaBackendException("O arquivo XML não é um documento LexML válido.");
        }
        Element norma = projetoNorma.element(QName.get("Norma", NS_LEXML));
        if (norma == null) {
            norma = projetoNorma.element(QName.get("Projeto", NS_LEXML));
        }
        if (norma == null) {
            throw new EtaBackendException("O documento LexML não contém uma norma ou projeto válido.");
        }
        Element articulacao = norma.element(QName.get("Articulacao", NS_LEXML));
        if (articulacao == null || articulacao.elements().isEmpty()) {
            throw new EtaBackendException("O documento LexML não contém uma articulação válida.");
        }

        String xmlFinal = opcoes.getModo() == ModoParseProposicao.APROVEITAMENTO
                ? normalizarUrnSeNecessario(doc, root, xmlLimpo, opcoes)
                : xmlLimpo;

        List<AvisoParseProposicao> avisos = Collections.emptyList();
        if (opcoes.isValidarSchema()) {
            avisos = validarComSchema(xmlFinal);
            if (opcoes.isFalharComAvisos() && !avisos.isEmpty()) {
                throw new EtaBackendException(formatarErroAvisos(avisos));
            }
        }

        return new ResultadoXml(xmlFinal, avisos);
    }

    private static Document lerXmlSeguro(String xml) {
        SAXReader reader = new SAXReader();
        try {
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (SAXException e) {
            throw new EtaBackendException("Não foi possível configurar o leitor XML seguro.", e);
        }
        try {
            return reader.read(new StringReader(xml));
        } catch (DocumentException e) {
            throw new EtaBackendException("O arquivo XML não é um XML válido.", e);
        }
    }

    private static String normalizarUrnSeNecessario(
            Document doc, Element root, String xmlOriginal, OpcoesParseProposicao opcoes) {
        Element metadado = root.element(QName.get("Metadado", NS_LEXML));
        Element identificacao = metadado == null ? null : metadado.element(QName.get("Identificacao", NS_LEXML));
        String urnAtual = identificacao == null ? null : identificacao.attributeValue("URN");

        if (urnAtual != null && REGEX_URN_VALIDA.matcher(urnAtual.trim()).matches()) {
            return xmlOriginal;
        }

        int ano = opcoes.getAno() != null ? opcoes.getAno() : Year.now().getValue();
        int numero = opcoes.getNumero() != null ? opcoes.getNumero() : 999999;

        String urnNormalizada = null;
        if (urnAtual != null && urnAtual.contains(PLACEHOLDER_URN_ID)) {
            String candidata = urnAtual.trim().replace(PLACEHOLDER_URN_ID, ano + ";" + numero);
            if (REGEX_URN_VALIDA.matcher(candidata).matches()) {
                urnNormalizada = candidata;
            }
        }
        if (urnNormalizada == null) {
            String autoridade = opcoes.getAutoridade() != null ? opcoes.getAutoridade() : "senado.federal";
            String tipoNorma = opcoes.getTipoNorma() != null ? opcoes.getTipoNorma() : "projeto.lei;pl";
            urnNormalizada = "urn:lex:br:" + autoridade + ":" + tipoNorma + ":" + ano + ";" + numero;
        }

        if (metadado == null) {
            metadado = DocumentHelper.createElement(QName.get("Metadado", NS_LEXML));
            root.elements().add(0, metadado);
        }
        if (identificacao == null) {
            identificacao = metadado.addElement(QName.get("Identificacao", NS_LEXML));
        }
        identificacao.addAttribute("URN", urnNormalizada);
        return doc.asXML();
    }

    private static List<AvisoParseProposicao> validarComSchema(String xml) {
        Node elem;
        try {
            elem = XML$.MODULE$.loadString(xml);
        } catch (Exception e) {
            throw new EtaBackendException("Erro ao preparar XML para validação de esquema.", e);
        }
        List<AvisoParseProposicao> avisos = new ArrayList<>();
        for (ParseProblem p : CollectionConverters.asJava(new Validation().validaComSchema(elem))) {
            avisos.add(AvisoParseProposicao.de(p));
        }
        return Collections.unmodifiableList(avisos);
    }

    private static String formatarErroAvisos(List<AvisoParseProposicao> avisos) {
        StringBuilder sb = new StringBuilder("O documento possui inconsistências na validação:");
        for (AvisoParseProposicao aviso : avisos) {
            sb.append("\n- ").append(aviso);
        }
        return sb.toString();
    }

    static final class ResultadoXml {
        final String xml;
        final List<AvisoParseProposicao> avisos;

        ResultadoXml(String xml, List<AvisoParseProposicao> avisos) {
            this.xml = xml;
            this.avisos = avisos;
        }
    }

}
