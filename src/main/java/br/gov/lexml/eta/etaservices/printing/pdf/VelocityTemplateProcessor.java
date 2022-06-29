package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class VelocityTemplateProcessor {

	private static final Logger log = LoggerFactory.getLogger(VelocityTemplateProcessor.class);

	private static String TEMPLATE_RESOURCE = "/template-velocity-emenda.xml";

//	private final HostEditor hostEditor;

//	private Map<String, String> templateReplacements;

	private Map<String, Object> mapaParaContexto;

	private String velocityResult;

	VelocityTemplateProcessor(Map<String, Object> mapaParaContexto /*, HostEditor hostEditor*/) {
		this.mapaParaContexto = mapaParaContexto;
	}

	/**
	 * Process a Velocity template. Returns a FOP pure code.
	 *
	 * @return a final FO code processed by Velocity
	 * @throws Exception
	 */
	public String getTemplateResult() throws Exception {

		if (velocityResult == null) {

			String finalTemplate = IOUtils.toString(getClass().getResourceAsStream(TEMPLATE_RESOURCE), "UTF-8");

			//REPLACEMENTS
//			VelocityTemplateProcessorLanguageExpansion vtple = new VelocityTemplateProcessorLanguageExpansion(madocDocument, hostEditor);
//			finalTemplate = vtple.doExpansions(finalTemplate);
//			templateReplacements = vtple.getTemplateReplacements();

			if (log.isDebugEnabled()){
				log.debug("finalTemplate: " + finalTemplate);
			}

			// processing velocity
			velocityResult = getVelocityResult(finalTemplate);
		}

		return velocityResult;

		/*
		try {
			FileUtils.write(new File("/tmp/saida_final.txt"), finalTemplate);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	/**
	 * Returns an FO code from a template
	 * @param template an string that contains skeleton and all templates from current MadocDocument
	 * @return final FO code
	 */
	private String getVelocityResult(String template) {

		VelocityEngine ve = new VelocityEngine();

	    ve.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
	    	      "org.apache.velocity.runtime.log.Log4JLogChute" );
	    ve.setProperty("runtime.log.logsystem.log4j.logger", getClass().getName());

		ve.init();

		VelocityContext ctx = new VelocityContext();

		// Adiciona dados direto na raiz
		mapaParaContexto.forEach((key, value) -> {
			ctx.put(key, value);
		});

//		// put constants
//		Map<String, String> constants = new HashMap<String, String>();
//		constants.put("DATE_FORMAT", Constants.DATE_FORMAT);
//		constants.put("FULL_DATE_FORMAT", Constants.FULL_DATE_FORMAT);
//		velocityContext.put("consts", constants);
//
//		// put util
//		velocityContext.put("madoc", new VelocityExtension(contextCollection, velocityContext));

//		VelocityContext ctx = getContextFromMadoc();
//		VelocityExtension madoc = (VelocityExtension) ctx.get("madoc");
//		madoc.setVelocityEngine(ve);

		/*
		try {'
			FileWriter fw = new FileWriter("target/template.vm");
			fw.write(template);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

	    StringWriter w = new StringWriter();
		ve.evaluate(ctx, w, "defaultTemplate", template);
		String result = w.toString();

		if (StringUtils.isEmpty(result)){
			return "";
		}

		// Retira espaços duplicados e espaço antes de pontuação
		result = result.replaceAll("\\s{2,}", " ");
		result = result.replaceAll("\\s([.,;:!?])", "$1");

		log.debug("getVelocityResult: " + result);

		return result;
	}
}

