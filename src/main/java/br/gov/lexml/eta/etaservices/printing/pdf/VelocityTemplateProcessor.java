package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.util.EtaFileUtil;

public class VelocityTemplateProcessor {

    private static final Logger log = LoggerFactory.getLogger(VelocityTemplateProcessor.class);

    private static final String TEMPLATE_RESOURCE = "/template-velocity-emenda.xml";

    private final TemplateLoader templateLoader;

    private String velocityResult;

    public VelocityTemplateProcessor(final TemplateLoader templateLoader) {
        this.templateLoader = templateLoader;
    }

    /**
     * Process a Velocity template. Returns a FOP pure code.
     *
     * @return a final FO code processed by Velocity
     * @throws IOException - if there is some issue reading the resource
     */
    public String getTemplateResult(final Emenda emenda) throws IOException {

        if (velocityResult == null) {

            String finalTemplate = this.templateLoader.loadTemplate(TEMPLATE_RESOURCE);

            //REPLACEMENTS

            if (log.isDebugEnabled()) {
                log.debug("finalTemplate: {}", finalTemplate);
            }

            velocityResult = getVelocityResult(finalTemplate, emenda);
        }

        return velocityResult;
    }

    /**
     * Returns an FO code from a template
     *
     * @param template a string that contains skeleton and all templates from current MadocDocument
     * @return FO code after velocity processing
     */
    private String getVelocityResult(String template, final Emenda emenda) {

        VelocityEngine ve = new VelocityEngine();

        ve.setProperty("runtime.log.logsystem.log4j.logger", getClass().getName());

        ve.init();

        VelocityContext ctx = new VelocityContext();

        VelocityExtension vex = new VelocityExtension(ctx, ve);
        
        ctx.put("emenda", emenda);
        ctx.put("ve", vex);
                
        if (emenda.getOpcoesImpressao().isImprimirBrasao()) {
        	InputStream brasaoStream ;
        	if (emenda.isMateriaCongressoNacional()) {
        		brasaoStream = VelocityTemplateProcessor.class.getResourceAsStream("/static/assets/img/brasao_cn.jpg");
        	} else {
        		brasaoStream = VelocityTemplateProcessor.class.getResourceAsStream("/static/assets/img/brasao.jpg");
        	}
        	
        	String brasaoBase64 = EtaFileUtil.readFromImageAsBase64String(brasaoStream);
        	ctx.put("brasao", brasaoBase64);
        }
        
        if (emenda.getNotasRodape() != null && !emenda.getNotasRodape().isEmpty()) {        	
        	emenda.getNotasRodape().forEach(nr -> {
        		String notaRodape  = nr.getTexto().replaceFirst("<p>", "<p>" + nr.getNumero() + " ");
        		ctx.put(nr.getId(), vex.html2fo(notaRodape));	
        	});
        }

        StringWriter w = new StringWriter();
        ve.evaluate(ctx, w, "defaultTemplate", template);
        String result = w.toString();

        if (StringUtils.isEmpty(result)) {
            return "";
        }

        // Retira espaços duplicados e espaço antes de pontuação
        result = result.replaceAll("\\s{2,}", " ");
        result = result.replaceAll("\\s([.,;:!?])", "$1");

        log.debug("getVelocityResult: {}", result);

        return result;
    }
}