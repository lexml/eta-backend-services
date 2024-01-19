package br.gov.lexml.eta.etaservices.printing.pdf;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.pdf.codec.Base64;

public class VelocityExtension {

	private static final int MAX_IMAGE_WIDTH = 800;

	private static final Logger log = LoggerFactory.getLogger(VelocityExtension.class);

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
//			log.info("HTML\n---------------------------\n" + html);
			String fo = html2foConverter.html2fo(StringEscapeUtils.unescapeHtml4(html));
//			log.info("FO\n---------------------------\n" + fo);
			return VelocityExtensionUtils.render(fo, ctx, velocityEngine);
		} catch (Exception e) {
			log.error("Falha na conversão para FO", e);
			return e.getLocalizedMessage();
		}
	}
	
	public String html2foTextoLivre(String html) {
		
		html = trataImagens(html);
		
//		System.out.println("---------------------------");
//		System.out.println(html);
//		System.out.println("---------------------------");
		
		html = addStyle(html, "(p|ol|ul)", "margin-bottom: $pMarginBottom");
		
		// Retira margin-bottom para classe ql-margin-bottom-0px
		html = multipleReplaceAll(html, "<[^>]+class=\"[^\"]*ql-margin-bottom-0px.+?>", 
				m -> m.quoteReplacement(m.group().replace("$pMarginBottom", "0")));
		
		// Retira margin-bottom padrão dentro de tabelas
		html = multipleReplaceAll(html, "<table .+?</table>", 
				m -> m.quoteReplacement(m.group().replace("$pMarginBottom", "0")));
		
		String htmlAttrFo = html
			.replaceAll("<p(.+?)><img", "<p$1 class=\"align-center\"><img")
			.replaceAll("(class=\"[^\"]*)estilo-ementa", "margin-left=\"6.5cm\" text-indent=\"0\" $1")
			.replaceAll("(class=\"[^\"]*)estilo-norma-alterada", "margin-left=\"3cm\" text-indent=\"1.5cm\" $1")
			.replaceAll("(class=\"[^\"]*)ql-text-indent-0px", "text-indent=\"0\" $1")
			.replaceAll("texto=\"(.*?)\" ", "");
		
		return this.html2fo(htmlAttrFo);
	}
	
	private String trataImagens(String html) {
		return multipleReplaceAll(html, "<img .+?>", (m) -> {
			String tag = m.group();
			Matcher mSrc = Pattern.compile("src=\"data:image/(.+?);base64,(.+?)\"").matcher(tag);
			if (mSrc.find()) {
				try {	
					String imageType = mSrc.group(1);
					byte[] bytes = Base64.decode(mSrc.group(2));
					
					boolean isPng = imageType.equals("png");
					String extension = isPng? ".png": ".jpeg";  
					File f = File.createTempFile("imagem-", extension);
					
					if(isPng) {
						// Não altera o PNG
						FileUtils.writeByteArrayToFile(f, bytes);
					}
					else {						
						// Demais imagens são convertidas em jpeg (inclusive as originalmente em jpeg)
						// O FOP parece entender melhor imagens jpeg geradas pelo ImageIO.
						BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
						ImageIO.write(image, "JPG", f);
					}
					
					return tag.replaceAll("src=\".+?\"", "src=\"file:" + f.getAbsolutePath() + "\"");
				}
				catch(Exception e) {
					log.error("Falha ao tratar imagem para o PDF.", e);
					return html;
				}
			}
			return html;
		});
	}
	
	private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
	    Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
	    BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
	    outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
	    return outputImage;
	}	

	public String citacao2html(String citacao) {
//		log.info("\n citacao antes ---------------------------------\n" + citacao);
		citacao = citacao.replace("Rotulo>", "strong>")
				.replace("<Alteracao>", "<div margin-left=\"3cm\" text-indent=\"1.5cm\">")
				.replace("</Alteracao>", "</div>")
				.replaceAll("(?i)<omissis ?/>", "<span class=\"omissis\"></span>")
				.replaceAll("(class=\"[^\"]*)agrupador", "align=\"center\" text-indent=\"0\" $1")
				.replaceAll("(class=\"[^\"]*)(?:sub)?secao", "font-weight=\"bold\" $1")
				.replace("class=\"ementa", "style=\"margin-left: 40%; text-indent: 0\"");
//		log.info("\n citacao depois ---------------------------------\n" + citacao);
		return citacao;
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

	private String multipleReplaceAll(String str, String pattern, Function<Matcher, String> replacer) {
		StringBuilder sb = new StringBuilder();

		Matcher m = Pattern.compile(pattern).matcher(str);
		while (m.find()) {
			m.appendReplacement(sb, replacer.apply(m));
		}
		m.appendTail(sb);

		return sb.toString();
	}

}
