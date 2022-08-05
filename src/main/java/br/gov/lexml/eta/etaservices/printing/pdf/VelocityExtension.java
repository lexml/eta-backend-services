package br.gov.lexml.eta.etaservices.printing.pdf;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class VelocityExtension {

	private HTML2FOConverter html2foConverter;

	private VelocityContext ctx;

	private VelocityEngine velocityEngine;

	public VelocityExtension(VelocityContext ctx, VelocityEngine velocityEngine) {
		try {
			this.ctx = ctx;
			this.velocityEngine = velocityEngine;
			Map<String, String> conf = new HashMap<>();
//			conf.put(HTML2FOConverter.CONF_PARAGRAPH_MARGIN_BOTTOM, "$pMarginBottomDefault");
			html2foConverter = new HTML2FOConverter(conf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// HTML to XSL-FO
	public String html2fo(String html) {
		try {
			String fo = html2foConverter.html2fo(StringEscapeUtils.unescapeHtml4(html));
			return VelocityExtensionUtils.render(fo, ctx, velocityEngine);
		} catch (Exception e) {
			System.out.println(e);
			return e.getLocalizedMessage();
		}
	}
	
	public String citacao2html(String citacao) {
		return citacao.replace("Rotulo>", "strong>")
				.replaceAll("(?i)<omissis ?/>", "<span class=\"omissis\"></span>");
	}

}
