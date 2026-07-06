package br.gov.lexml.eta.etaservices.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

public class XMLUtil {

    /**
     * Remove caracteres inválidos para XML (XML 1.0) de uma string.
     * Retorna null se a entrada for null.
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(input.length());
        int i = 0;
        final int len = input.length();

        while (i < len) {
            int cp = input.codePointAt(i);
            if (isValidXmlChar(cp)) {
                sb.appendCodePoint(cp);
            }
            i += Character.charCount(cp);
        }

        return sb.toString();
    }

    private static boolean isValidXmlChar(int cp) {
        // XML 1.0 valid chars:
        // #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
        return cp == 0x9 || cp == 0xA || cp == 0xD
                || (cp >= 0x20 && cp <= 0xD7FF)
                || (cp >= 0xE000 && cp <= 0xFFFD)
                || (cp >= 0x10000 && cp <= 0x10FFFF);
    }
    
    public static void setElementContentFromXmlFragment(Element target, String fragment) {
        // Normaliza null
        if (fragment == null) {
            target.setText("");
            return;
        }

        // Opcional: sanitize conforme seu utilitário (mantive o uso existente)
        String sanitized = XMLUtil.sanitize(fragment).trim();

        // Envolver em wrapper para garantir uma raiz única (aceita texto + elementos mistos)
        String wrapped = "<wrapper>" + sanitized + "</wrapper>";

        try {
            Document doc = DocumentHelper.parseText(wrapped);
            Element wrapper = doc.getRootElement();

            // Mover cada node filho do wrapper para o elemento alvo.
            // Usamos detach() para remover do documento temporário antes de adicionar.
            while (wrapper.nodeIterator().hasNext()) {
                Node node = (Node) wrapper.nodeIterator().next();
                node.detach();        // remove do wrapper
                target.add(node);     // adiciona (texto ou elemento) ao target
            }
        } catch (DocumentException e) {
            // Se parse falhar (por exemplo, fragment com & inválido), gravar como texto (escapado)
            target.setText(sanitized);
        }
    }    

}
