package br.gov.lexml.eta.etaservices.printing.pdf;

import br.gov.lexml.eta.etaservices.printing.Emenda;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

class VelocityTemplateProcessor {

    private static final Logger log = LoggerFactory.getLogger(VelocityTemplateProcessor.class);

    private static final String TEMPLATE_RESOURCE = "/template-velocity-emenda.xml";

    private final TemplateLoader templateLoader;

    private String velocityResult;

    VelocityTemplateProcessor(final TemplateLoader templateLoader) {
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
                log.debug("finalTemplate: " + finalTemplate);
            }

            // processing velocity
//            velocityResult = replaceHtmlTagsWithFO(getVelocityResult(finalTemplate, emenda));
            velocityResult = getVelocityResult(finalTemplate, emenda);
        }

        return velocityResult;
    }

//    private String replaceHtmlTagsWithFO(final String partialFO) {
//
//        return partialFO
//                .replaceAll("<p>","<fo:block>")
//                .replaceAll("</p>", "</fo:block>")
//                .replaceAll("<em>", "<fo:inline font-style=\"italic\">")
//                .replaceAll("</em>", "</fo:inline>")
//                .replaceAll("<strong>", "<fo:inline font-style=\"bold\">")
//                .replaceAll("</strong>", "</fo:inline>");
//
//        // TODO - what else?
//    }

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

        ctx.put("emenda", emenda);
        ctx.put("fo", new VelocityExtensionHTML2FO(ctx, ve));

        StringWriter w = new StringWriter();
        ve.evaluate(ctx, w, "defaultTemplate", template);
        String result = w.toString();

        if (StringUtils.isEmpty(result)) {
            return "";
        }

        // Retira espaços duplicados e espaço antes de pontuação
        result = result.replaceAll("\\s{2,}", " ");
        result = result.replaceAll("\\s([.,;:!?])", "$1");

        log.debug("getVelocityResult: " + result);

        return result;
    }
}
