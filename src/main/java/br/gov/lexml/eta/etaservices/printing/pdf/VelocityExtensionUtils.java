package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;


final class VelocityExtensionUtils {

	private VelocityExtensionUtils(){}
	
	static Double stringToDouble(String value) throws NumberFormatException{
		if (value == null){
			return 0.0;
		}
		
		try{
			DecimalFormat df = new DecimalFormat();
			df.setDecimalFormatSymbols(new DecimalFormatSymbols(new Locale("pt", "BR")));
			return df.parse(value).doubleValue();
		} catch (ParseException e){
			try {
				return Double.valueOf(value.trim());
			} catch (NumberFormatException e1){
				return (double) 0;
			}
		}
	}
	
	static String formatCurrencyNumber(String value) throws NumberFormatException{
		double number = stringToDouble(value);
		
		NumberFormat formatter = NumberFormat.getCurrencyInstance();

		return formatter.format(number);
	}

	/**
	 * Processa o renderizador Velocity do conteúdo de vtl
	 */
	public static String render(String vtl, VelocityContext ctx, VelocityEngine velocityEngine) {
		
		if (vtl == null) {
			return null;
		}
		
		StringWriter sw = new StringWriter();
		
		boolean success;
		if (velocityEngine == null) {
			success = Velocity.evaluate(ctx, sw, VelocityExtension.class.getName(), vtl);
		} else {
			success = velocityEngine.evaluate(ctx, sw, VelocityExtension.class.getName(), vtl);
		}
		
		return success? sw.toString(): vtl; 
	}
	
	public static String lowercaseInitial(String html) {
		
		if(html == null) {
			return null;
		}
		
		String brancosIniciais = "", restante = html;
		
		Matcher m = Pattern.compile("^((?:\\s*<[^>]*?>)*\\s*)(.*)$", Pattern.DOTALL).matcher(html);
		if(m.matches()) {
			brancosIniciais = m.group(1);
			restante = m.group(2);
		}
		
		if(restante.length() > 1 && Character.isUpperCase(restante.charAt(0))) {
			return brancosIniciais + Character.toLowerCase(restante.charAt(0)) +
					restante.substring(1);
		}
		
		return html;
	}
	
	public static String removeFinalDot(String html) {
		
		if(html == null) {
			return null;
		}
		
		String inicio = html, brancosFinais = "";
		
		Matcher m = Pattern.compile("^(.*?)((?:\\s*<[^>]*>)*\\s*)$", Pattern.DOTALL).matcher(html);
		if(m.matches()) {
			inicio = m.group(1);
			brancosFinais = m.group(2);
		}
		
		if(inicio.endsWith(".") || inicio.endsWith(",")) {
			return inicio.substring(0, inicio.length() - 1) + brancosFinais;
		}
		
		return html;
	}

}
