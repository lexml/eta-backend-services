package br.gov.lexml.eta.etaservices.printing.pdf;


import org.apache.fop.apps.MimeConstants;

/**
 * Faz a transformação do xml/xsl para pdf (usando FOP).
 * <p>
 * <a href="Xml2Pdf.java.html"> <i>Ver código-fonte </i> </a>
 * </p>
 *
 * @version $Revision$ de $Date$
 * @author $Author$
 */
public class Xml2Pdf extends TransformacaoFop {

    public Xml2Pdf() {
        super(MimeConstants.MIME_PDF);
    }

}
