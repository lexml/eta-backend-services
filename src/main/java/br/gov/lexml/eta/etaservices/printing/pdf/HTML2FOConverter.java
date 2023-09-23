package br.gov.lexml.eta.etaservices.printing.pdf;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.tools.ant.filters.StringInputStream;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTML2FOConverter {

	private static final Logger log = LoggerFactory.getLogger(HTML2FOConverter.class);
	
	public HTML2FOConverter() {
	}
	
	public static void main(String[] args) throws Exception {

//		String html = "<p>Testando uma lista:</p> <ol> <li class=\"align-left\">Um</li> <li class=\"align-left\">Dois</li> </ol>";

//		String html = "<p>Texto</p><p><span class=\"omissis\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></p><p>lllll</p>";
		
//		String html = "<p>Testando uma lista:</p> <ul> <li class=\"align-left\">Um</li> <li class=\"align-left\">Dois</li> </ul>";
		
//		<p>Testando uma lista:</p>
//
//		<ol>
//		 <li class="align-left">Um</li>
//		 <li class="align-left">Dois</li>
//		 <li class="align-left">Três</li>
//		</ol>
//
//		<p class="align-left">Outra lista:</p>
//
//		<ul>
//		 <li class="align-left">A</li>
//		 <li class="align-left">B</li>
//		 <li class="align-left">C</li>
//		</ul>
		
		String html = IOUtils.toString(new FileReader("src/test/resources/table.html"));
		
		System.out.println(html);
		System.out.println("-----------------");
		System.out.println(new HTML2FOConverter().html2fo(html));
	}
	
	// HTML to XSL-FO
	public String html2fo(String html) {
		String xhtml = html2xhtml(html);
		return xhtml2fo(xhtml);
	}
	
	private String html2xhtml(String html) {
		if (StringUtils.isEmpty(html)){
			return "";
		}
		
		// Garante tag raiz único para o HtmlCleaner 
		html = "<div>" + html + "</div>";
		
		CleanerProperties props = new CleanerProperties();
		 
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitXmlDeclaration(true);
		props.setOmitHtmlEnvelope(true);
		props.setOmitComments(true);
		 
		// do parsing
		TagNode tagNode = new HtmlCleaner(props).clean(html);
		
		// serialize to xml file
		String ret;
		try {
			ret = new SimpleXmlSerializer(props).getAsString(tagNode);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		
		// Corrige tabelas
		try {
			ret = trataTabelas(ret);
		} catch (Exception e) {
			log.warn("Falha no tratamento das tabelas.", e);
		}

		// Remove tag raiz <div>
		ret = ret.substring(5, ret.length() - 6);
		
		return ret;
	}

	private String xhtml2fo(String xhtml) {

		if (xhtml == null || StringUtils.isEmpty(xhtml.trim())) {
			return "";
		}
		
		String ret;
		
		try {
			xhtml = unescapeHtmlKeepingXMLEntities(xhtml);	
			xhtml = xhtml.replaceAll("&(?![lg]t;)", "&amp;");
			
			TransformerFactory factory = TransformerFactory.newInstance();
	        Transformer transformer = factory.newTransformer(
	        		new StreamSource(getClass().getResourceAsStream("/xhtml2fo.xsl")));
	        
	        
	        // Transforma classes de alinhamento em style
	        xhtml = trataAlinhamentoDePragrafo(xhtml);
	        
	        // Coloca div para poder processar conteúdo inline.
	        xhtml = "<div>" + xhtml + "</div>";
	        
//        	System.out.println(xhtml);
	        if (log.isDebugEnabled()) {
	        	log.debug("xhtml2fo: xhtml depois=\n"+xhtml);
	        }
	        
	        ByteArrayInputStream bis = new ByteArrayInputStream(xhtml.getBytes(UTF_8));
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        
	        transformer.transform(new StreamSource(bis), new StreamResult(bos));
	        
	        ret = new String(bos.toByteArray(), UTF_8);
	        
	        // Retira o block do div
	        ret = ret.substring(ret.indexOf(">") + 1);
	        ret = ret.substring(0, ret.lastIndexOf("<"));
	        
	        if (log.isDebugEnabled()) {
	        	log.debug("xhtml2fo: xsl-fo=\n"+ret);
	        }
	        
		}
		catch(Exception e) {
			log.error("Falha na conversão de XHTML para XSL-FO.", e);
			ret = "[Falha na formatação do campo. (" + e.getMessage() + ")]";
		}
		
		return ret;
		
	}

	/**
	 * Transofrma class="no_indent" em style="text-indent: 0;"
	 */
	private String trataAlinhamentoDePragrafo(String xhtml) {
		
		StringBuffer sb = new StringBuffer();
		
		Pattern tagPattern = Pattern.compile("<.+?>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher mTag = tagPattern.matcher(xhtml);
		
		Pattern alignClassPattern = Pattern.compile("class=\"[^\"]*align-([^\" ]+?)\"");
		String styleAttr="style=\"";
		
		int i; 
		String tag;
		while(mTag.find()) {
			tag = mTag.group();
			Matcher mClass = alignClassPattern.matcher(tag);
			if(mClass.find()) {
				String alignment = mClass.group(1);
				tag = tag.replace(mClass.group(), "");
				i = tag.indexOf(styleAttr);
				String newStyle = "text-align: " + alignment + ";";
				if(alignment.equals("center") || alignment.equals("right")) {
					newStyle += " text-indent: 0;";
				}
				if(i == -1) {
					tag = tag.replace(">", " style=\"" + newStyle + "\">");
				}
				else {
					tag = tag.replace(styleAttr, styleAttr + newStyle + " ");
				}
			}
			mTag.appendReplacement(sb, Matcher.quoteReplacement(tag));
		}
		mTag.appendTail(sb);
		
		return sb.toString();
	}
	
	/* Tabelas ------------------------------------- */
	
	private String trataTabelas(String ret) throws Exception {
		
		if(!ret.contains("table")) {
			return ret;
		}
		
		Document d = new SAXReader().read(new StringInputStream(ret));

		List<Node> tables = d.selectNodes("//table");
		
		for(Node table: tables) {
			trataTabela((Element)table);
		}
		
		OutputFormat format = new OutputFormat();
		format.setSuppressDeclaration(true);

		StringWriter out = new StringWriter();
		
		XMLWriter writer = new XMLWriter(format);
		writer.setWriter(out);
		writer.write(d);
		writer.close();
		
		return out.toString();
	}

	private void trataTabela(Element table) {
		
		List<Node> rows = table.selectNodes("tbody/tr");
		
		TableMatrix m = new TableMatrix();
		
		for(Node r: rows) {
			m.nextRow();
			List<Node> cells = r.selectNodes("th|td");
			for(Node c: cells) {
				m.addCell(getCellInfo((Element)c));
			}
		}
		
		int qtColunas = m.getQtColunas();
		
		// Completa linhas
		Element tbody = (Element) table.selectNodes("tbody").get(0);
		int qtLinhasOrig = rows.size();
		int qtLinhas = m.getQtLinhas();
		for(int i = qtLinhasOrig; i < qtLinhas; i++) {
			tbody.addElement("tr");
		}
		
		// Completa células
		rows = tbody.selectNodes("tr");
		int ri = 0;
		for(Node r: rows) {
			int qtCells = m.countCellsInRow(ri++);
			for(int i = qtCells; i < qtColunas; i++) {
				((Element)r).addElement("td");
			}
		}
		
		String colWidths = m.getColWidths(); 
		table.addAttribute("colwidths", colWidths);
		
//		System.out.println(">>>>>>>>>>>>>>>>>>>> " + colWidths);
		
	}
	
	private static class CellInfo {
		
		int width;
		int colspan = 1;
		int rowspan = 1;
		
		@Override
		public String toString() {
			return "w: " + width + ", c: " + colspan + ", r: " + rowspan;
		}
	}
	
	private static class TableMatrix {
		
		final List<List<CellInfo>> t = new ArrayList<>();
		final List<Integer> qtCells = new ArrayList<>();
		final List<Integer> rowWidths = new ArrayList<>();
		final Map<String, CellInfo> pos = new HashMap<>();
		
		int current = -1; // current row index
		
		int qtColunas = -1;
		
		public void nextRow() {
			current++;
		}

		public int countCellsInRow(int ri) {
			int qtCells = 0;
			for(int ci = 0; ci < getQtColunas(); ci++) {
				CellInfo cell = pos.get(ri + "," + ci);
				if(cell != null) {
					qtCells++;
				}
			}
			return qtCells;
		}

		public void addCell(CellInfo cellInfo) {
			
//			System.out.println("addCell - " + cellInfo );
			
			List<CellInfo> r = getRow(cellInfo);
			r.add(cellInfo);
			for(int i = 0; i < cellInfo.rowspan; i++) {
				int ri = current + i;
				qtCells.set(ri, qtCells.get(ri) + cellInfo.colspan);
				rowWidths.set(ri, rowWidths.get(ri) + cellInfo.width);
			}

			// Registra posicioes
			int ri = current, ci = r.size() - 1;
			while(pos.get(ri + "," + ci) != null) {
				ci++;
			}
			for(int dri = 0; dri < cellInfo.rowspan; dri++) {
				for(int dci = 0; dci < cellInfo.colspan; dci++) {
					pos.put((ri + dri) + "," + (ci + dci), cellInfo);
				}
			}
			
		}

		private List<CellInfo> getRow(CellInfo cellInfo) {
			int rimax = current + cellInfo.rowspan - 1;
			if(rimax >= t.size()) {
				for(int j = t.size(); j <= rimax; j++) {
					t.add(new ArrayList<>());
					qtCells.add(0);
					rowWidths.add(0);
				}
			}
			return t.get(current);
		}
		
		public int getQtLinhas() {
			return t.size();
		}

		public int getQtColunas() {
			if(qtColunas == -1) {
				qtColunas = qtCells.stream().mapToInt(Integer::intValue).max().orElse(0);
			}
			return qtColunas;
		}
		
		public String getColWidths() {
			
			Integer[] colWidths = new Integer[getQtColunas()];
			Arrays.fill(colWidths, 0);
			
			for(int ci = 0; ci < colWidths.length; ci++) {
				colWidths[ci] = calculaLarguraColuna(ci);
			}
			
			return StringUtils.join(colWidths, ",");
		}

		private int calculaLarguraColuna(int ci) {
			
			int qtLinhas = getQtLinhas();
			
			List<Integer> larguras = new ArrayList<>();
			
			for(int ri = 0; ri < qtLinhas; ri++) {
				CellInfo cell = pos.get(ri + "," + ci);
				if(cell != null && cell.colspan == 1) {
					larguras.add(cell.width);
				}
			}
			
			// Dá preferência à média das colunas simples
			if(!larguras.isEmpty()) {
				int sum = larguras.stream().mapToInt(Integer::intValue).sum();
				return sum / larguras.size();
			}
			
			// Calcula em função dos rowspans
			for(int ri = 0; ri < qtLinhas; ri++) {
				CellInfo cell = pos.get(ri + "," + ci);
				if(cell != null) {
					larguras.add(cell.width / cell.rowspan);
				}
			}
			
			int sum = larguras.stream().mapToInt(Integer::intValue).sum();
			
			return sum / larguras.size();
		}

	}

	private static Integer getWidth(Element e) {
		String styles = e.attributeValue("style");
		if(styles != null) {
			for(String style: styles.split(";")) {
				if(style.trim().startsWith("width") && style.contains(":")) {
					String strWidth = style.substring(style.indexOf(":") + 1)
							.replaceAll("\\D", "");
					return Integer.parseInt(strWidth, 10);
				}
			}
		}
		return 300;
	}
	
	private static int getIntegerAttribute(Element n, String attName, int defaultValue) {
		String strValue = n.attributeValue(attName);
		if(strValue != null) {
			try {
				return Integer.parseInt(strValue, 10);
			}
			catch(NumberFormatException e) {
				log.warn("Esperado valor inteiro para o atributo '" + attName + 
						"'. Valor encontrado '" + strValue + "'.");
			}
		}
		return defaultValue;
	}

	private static CellInfo getCellInfo(Element c) {
		CellInfo ci = new CellInfo();
		ci.width = getWidth(c);
		ci.colspan = getIntegerAttribute(c, "colspan", 1); 
		ci.rowspan = getIntegerAttribute(c, "rowspan", 1); 
		return ci;
	}

	public static String unescapeHtmlKeepingXMLEntities(String html) {
		// Mantém escape de < e >
		html = html.replace("&#60;", "&lt;")
				.replace("&#62;", "&gt;").replaceAll("&([gl]t;)", "&amp;$1");
		// Retira demais escapes HTML
		return StringEscapeUtils.unescapeHtml4(html);
	}

}
