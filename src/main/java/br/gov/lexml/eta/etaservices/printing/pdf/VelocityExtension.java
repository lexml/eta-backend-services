package br.gov.lexml.eta.etaservices.printing.pdf;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VelocityExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(VelocityExtension.class);

	private HTML2FOConverter html2foConverter;

	private VelocityContext ctx;

	private VelocityEngine velocityEngine;

	public VelocityExtension(VelocityContext ctx, VelocityEngine velocityEngine) {
		try {
			this.ctx = ctx;
			this.velocityEngine = velocityEngine;
			html2foConverter = new HTML2FOConverter();
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
			LOGGER.error("Falha na conversão para FO", e);
			return e.getLocalizedMessage();
		}
	}
	
	public String html2foTextoLivre(String html) {

		// Talvez venha a ser necessário para tratar imagens grandes
//		html = trataImagens(html);
		
//		System.out.println("---------------------------");
//		System.out.println(html.replaceAll("src=\".+?\"", "src=\"IMAGEM\""));
//		System.out.println("---------------------------");
		
		String htmlAttrFo = html
			.replaceAll("(class=\"[^\"]*)estilo-ementa", "margin-left=\"6.5cm\" text-indent=\"0\" $1")
			.replaceAll("(class=\"[^\"]*)estilo-norma-alterada", "margin-left=\"2cm\" $1");
		
		return this.html2fo(htmlAttrFo);
	}

//	private String trataImagens(String html) {
//		StringBuilder sb = new StringBuilder();
//				
//		Matcher m = Pattern.compile("<img .+?>").matcher(html);
//		while(m.find()) {
//			String tag = m.group();
//			System.out.println(tag.replaceAll("src=\".+?\"", "src=\"IMAGEM\""));
//			m.appendReplacement(sb, "Imagem<br>" + tag);
//		}
//		m.appendTail(sb);
//		
//		return sb.toString();
//	}

	public String citacao2html(String citacao) {
		return citacao.replace("Rotulo>", "strong>")
				.replaceAll("(?i)<omissis ?/>", "<span class=\"omissis\"></span>")
				.replaceAll("(class=\"[^\"]*)agrupador", "align=\"center\" text-indent=\"0\" $1")
				.replaceAll("(class=\"[^\"]*)(?:sub)?secao", "font-weight=\"bold\" $1")
				.replace("class=\"ementa", "style=\"margin-left: 40%; text-indent: 0\"");
	}
	
	/**
	 * Return a size of something
	 * 
	 * @param o
	 * @return
	 */
	public int size(Object o) {

		if (o == null) {
			return 0;
		}

		if (o instanceof Object[]) {
			return ((Object[]) o).length;
		}

		if (o instanceof Collection) {
			return ((Collection<?>) o).size();
		}

		return o.toString().length();
	}
	
	public boolean isEmpty(Object o) {
		return size(o) == 0;
	}
	
	public String addStyle(String html, String tagName, String style) {
		
		StringBuffer sb = new StringBuffer();
		
		Pattern tagPattern = Pattern.compile("<" + tagName + "\\b.*?>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher mTag = tagPattern.matcher(html);
		
		while(mTag.find()) {
			String tag = mTag.group();
			if (tag.contains("style=\"")) {
				tag = tag.replace("style=\"", "style=\"" + style + ";");
			}
			else {
				tag = tag.replace(">", " style=\"" + style + "\">");
			}
			mTag.appendReplacement(sb, Matcher.quoteReplacement(tag));
		}
		mTag.appendTail(sb);
		
		return sb.toString();
	}
	
	public String getDataIso() {
		return LocalDateTime.now().atOffset(ZoneOffset.ofHours(-3)).toString();
	}

}
