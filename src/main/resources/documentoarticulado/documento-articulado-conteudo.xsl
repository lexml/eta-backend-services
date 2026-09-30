<?xml version="1.0" encoding="UTF-8"?>
<!--
  Conteúdo impresso no PDF do documento articulado: transforma o documento-articulado.xml (LexML) em um
  fragmento XSL-FO, inserido no fo:flow do template-velocity-documento-articulado.xml.

  Fluxo isolado da emenda: esta XSLT não importa nem referencia xhtml2fo.xsl ou outros recursos da emenda.
  Os valores de formatação foram copiados da emenda e têm a origem indicada em comentário.

  Imprime a parte inicial (epígrafe, ementa e preâmbulo; issue #72, parte 1) e a articulação básica
  (artigo, caput, parágrafo, inciso, alínea e item; parte 2) com os agrupadores (parte 3). Os demais
  elementos da #72 têm templates vazios marcados com a parte responsável.
  XSLT 1.0 (processador do JDK).
-->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:lx="http://www.lexml.gov.br/1.0"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	exclude-result-prefixes="lx xlink">

	<xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="yes" indent="no"/>

	<!-- Parâmetros de impressão (ParametrosImpressaoDocumentoArticulado) -->
	<xsl:param name="maxTamanhoFonte" select="'16pt'"/>
	<xsl:param name="lineHeight" select="'150%'"/>
	<xsl:param name="pMarginBottom" select="'0.6em'"/>

	<!-- Raiz única do fragmento: o fo:flow sempre recebe ao menos um bloco. -->
	<xsl:template match="/">
		<fo:block>
			<xsl:apply-templates select="lx:LexML/lx:ProjetoNorma/lx:Norma"/>
		</fo:block>
	</xsl:template>

	<xsl:template match="lx:Norma">
		<xsl:apply-templates select="*"/>
	</xsl:template>

	<!-- Demais partes da norma ainda não impressas -->
	<xsl:template match="lx:Norma/*" priority="-1"/>
	<!-- Local, data e assinaturas: continuam comentados no template -->
	<xsl:template match="lx:ParteFinal"/>

	<!--
	  Articulação, no lugar da citação com dispositivos da emenda. Emenda: a citação fica dentro do bloco
	  "Comando de emenda" (line-height="$lineHeight", text-align="justify", text-indent="2.5cm") e, no
	  xhtml2fo.xsl, cada p vira um fo:block sem margens próprias: não há espaço extra entre dispositivos.
	  O espaço antes do primeiro dispositivo é o margin-bottom do preâmbulo. Sem as aspas da citação.
	  A estrutura é percorrida no modo "dispositivo"; o texto de cada p, no modo "inline".
	-->
	<xsl:template match="lx:Articulacao">
		<fo:block line-height="{$lineHeight}" text-align="justify" text-indent="2.5cm">
			<xsl:apply-templates select="*" mode="dispositivo"/>
		</fo:block>
	</xsl:template>

	<!--
	  Agrupadores (issue #72, parte 3): rótulo e nome antes do conteúdo.
	  Parte, Livro, Título e Capítulo: rótulo e nome em maiúsculas, rótulo em negrito.
	  Seção e Subseção: rótulo e nome em negrito, com a capitalização do documento.
	-->
	<xsl:template match="lx:Parte | lx:Livro | lx:Titulo | lx:Capitulo" mode="dispositivo">
		<xsl:call-template name="titulo-agrupador">
			<xsl:with-param name="maiusculas" select="true()"/>
			<xsl:with-param name="nomeEmNegrito" select="false()"/>
		</xsl:call-template>
		<xsl:apply-templates select="*[not(self::lx:Rotulo or self::lx:NomeAgrupador)]" mode="dispositivo"/>
	</xsl:template>

	<xsl:template match="lx:Secao | lx:Subsecao" mode="dispositivo">
		<xsl:call-template name="titulo-agrupador">
			<xsl:with-param name="maiusculas" select="false()"/>
			<xsl:with-param name="nomeEmNegrito" select="true()"/>
		</xsl:call-template>
		<xsl:apply-templates select="*[not(self::lx:Rotulo or self::lx:NomeAgrupador)]" mode="dispositivo"/>
	</xsl:template>

	<!-- Agrupador genérico do lexml-eta, fora da lista da #72: só atravessado. -->
	<xsl:template match="lx:Agrupamento" mode="dispositivo">
		<xsl:apply-templates select="*[not(self::lx:Rotulo or self::lx:NomeAgrupador)]" mode="dispositivo"/>
	</xsl:template>

	<!--
	  Título do agrupador: rótulo e nome em linhas próprias, somente texto. Emenda: citacao2html troca
	  class="agrupador" por align="center" text-indent="0" (sem o text-indent o bloco herdaria o recuo de
	  2,5cm da articulação) e seção/subseção por font-weight="bold"; rótulo em strong. Sem espaço extra
	  antes ou depois. keep-together mantém rótulo e nome na mesma página, e keep-with-next mantém o
	  título com o conteúdo seguinte (evita o título sozinho no fim da página).
	-->
	<xsl:template name="titulo-agrupador">
		<xsl:param name="maiusculas"/>
		<xsl:param name="nomeEmNegrito"/>
		<xsl:variable name="rotulo" select="normalize-space(lx:Rotulo)"/>
		<xsl:variable name="nome" select="normalize-space(lx:NomeAgrupador)"/>
		<xsl:if test="$rotulo != '' or $nome != ''">
			<fo:block text-align="center" text-indent="0" keep-together.within-page="always"
				keep-with-next.within-page="always">
				<xsl:if test="$rotulo != ''">
					<fo:block font-weight="bold">
						<xsl:call-template name="caixa">
							<xsl:with-param name="texto" select="$rotulo"/>
							<xsl:with-param name="maiusculas" select="$maiusculas"/>
						</xsl:call-template>
					</fo:block>
				</xsl:if>
				<xsl:if test="$nome != ''">
					<fo:block>
						<xsl:if test="$nomeEmNegrito">
							<xsl:attribute name="font-weight">bold</xsl:attribute>
						</xsl:if>
						<xsl:call-template name="caixa">
							<xsl:with-param name="texto" select="$nome"/>
							<xsl:with-param name="maiusculas" select="$maiusculas"/>
						</xsl:call-template>
					</fo:block>
				</xsl:if>
			</fo:block>
		</xsl:if>
	</xsl:template>

	<xsl:template name="caixa">
		<xsl:param name="texto"/>
		<xsl:param name="maiusculas"/>
		<xsl:choose>
			<xsl:when test="$maiusculas">
				<xsl:call-template name="maiusculas">
					<xsl:with-param name="texto" select="$texto"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise><xsl:value-of select="$texto"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- XSLT 1.0 não tem upper-case(): alfabeto e letras acentuadas do português. -->
	<xsl:variable name="minusculas" select="'abcdefghijklmnopqrstuvwxyzáàâãäéèêëíìîïóòôõöúùûüçñ'"/>
	<xsl:variable name="maiusculasTabela" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZÁÀÂÃÄÉÈÊËÍÌÎÏÓÒÔÕÖÚÙÛÜÇÑ'"/>

	<xsl:template name="maiusculas">
		<xsl:param name="texto"/>
		<xsl:value-of select="translate($texto, $minusculas, $maiusculasTabela)"/>
	</xsl:template>

	<!-- Artigo: o rótulo do artigo é impresso no mesmo bloco do texto do caput. -->
	<xsl:template match="lx:Artigo" mode="dispositivo">
		<xsl:apply-templates select="lx:Caput" mode="dispositivo">
			<xsl:with-param name="rotulo" select="lx:Rotulo"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="*[not(self::lx:Rotulo or self::lx:Caput or self::lx:TituloDispositivo)]"
			mode="dispositivo"/>
	</xsl:template>

	<xsl:template match="lx:Caput" mode="dispositivo">
		<xsl:param name="rotulo" select="/.."/>
		<xsl:call-template name="bloco-dispositivo">
			<xsl:with-param name="rotulo" select="$rotulo"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="lx:Paragrafo | lx:Inciso | lx:Alinea | lx:Item" mode="dispositivo">
		<xsl:call-template name="bloco-dispositivo">
			<xsl:with-param name="rotulo" select="lx:Rotulo"/>
		</xsl:call-template>
	</xsl:template>

	<!--
	  Bloco de um dispositivo: rótulo em negrito (emenda: citacao2html troca Rotulo por strong), espaço e
	  o primeiro p; os p seguintes em blocos próprios; depois os dispositivos subordinados, na ordem do
	  documento. Os espaços do início do p são colapsados pelo XSL-FO.
	-->
	<xsl:template name="bloco-dispositivo">
		<xsl:param name="rotulo"/>
		<xsl:variable name="textoRotulo" select="normalize-space($rotulo)"/>
		<xsl:variable name="paragrafos" select="lx:p"/>
		<xsl:if test="$textoRotulo != '' or normalize-space($paragrafos[1]) != ''">
			<fo:block>
				<xsl:if test="$textoRotulo != ''">
					<fo:inline font-weight="bold"><xsl:value-of select="$textoRotulo"/></fo:inline>
					<xsl:text> </xsl:text>
				</xsl:if>
				<xsl:apply-templates select="$paragrafos[1]/node()" mode="inline"/>
			</fo:block>
		</xsl:if>
		<xsl:for-each select="$paragrafos[position() &gt; 1][normalize-space(.) != '']">
			<fo:block><xsl:apply-templates mode="inline"/></fo:block>
		</xsl:for-each>
		<xsl:apply-templates select="*[not(self::lx:Rotulo or self::lx:p)]" mode="dispositivo"/>
	</xsl:template>

	<!-- Blocos de alteração de norma vigente e omissis: issue #72, parte 4 -->
	<xsl:template match="lx:Alteracao | lx:Omissis" mode="dispositivo"/>
	<!-- Pena e título de dispositivo: issue #72, parte 6 -->
	<xsl:template match="lx:Pena | lx:TituloDispositivo" mode="dispositivo"/>
	<!-- Demais elementos: não impressos -->
	<xsl:template match="*" mode="dispositivo" priority="-1"/>

	<xsl:template match="lx:ParteInicial">
		<xsl:apply-templates select="lx:Epigrafe | lx:Ementa | lx:Preambulo"/>
	</xsl:template>

	<!--
	  Epígrafe. Emenda: bloco da epígrafe (text-align="center", font-weight="bold",
	  font-size="$maxTamanhoFonte", espaços trocados por &#160;). O bloco do complemento não é usado.
	-->
	<xsl:template match="lx:Epigrafe">
		<xsl:variable name="texto" select="normalize-space(.)"/>
		<xsl:if test="$texto != ''">
			<fo:block text-align="center" font-weight="bold" font-size="{$maxTamanhoFonte}">
				<xsl:value-of select="translate($texto, ' ', '&#160;')"/>
			</fo:block>
		</xsl:if>
	</xsl:template>

	<!--
	  Ementa. Recuo: emenda, html2foTextoLivre (estilo-ementa: margin-left 6.5cm, text-indent 0) e
	  citacao2html (margin-left 40% da área útil de 16,4cm). Justificação e entrelinha: bloco do comando
	  de emenda, onde a citação é impressa. Espaço de 48pt após a epígrafe: issue #72.
	  Os espaços são colapsados pelo próprio XSL-FO (white-space-collapse e linefeed-treatment padrão).
	-->
	<xsl:template match="lx:Ementa">
		<xsl:if test="normalize-space(.) != ''">
			<fo:block space-before="48pt" margin-left="6.5cm" text-indent="0" text-align="justify"
				line-height="{$lineHeight}">
				<xsl:apply-templates mode="inline"/>
			</fo:block>
		</xsl:if>
	</xsl:template>

	<xsl:template match="lx:b" mode="inline">
		<fo:inline font-weight="bold"><xsl:apply-templates mode="inline"/></fo:inline>
	</xsl:template>

	<xsl:template match="lx:i" mode="inline">
		<fo:inline font-style="italic"><xsl:apply-templates mode="inline"/></fo:inline>
	</xsl:template>

	<!-- span (referência a norma) e demais elementos inline: só o texto. Links: issue #72, parte 5. -->
	<xsl:template match="*" mode="inline">
		<xsl:apply-templates mode="inline"/>
	</xsl:template>

	<!--
	  Preâmbulo, no lugar do comando de emenda. Emenda: bloco "Comando de emenda"
	  (line-height="$lineHeight", text-align="justify", text-indent="2.5cm") com parágrafos de
	  margin-bottom="$pMarginBottom". Espaço de 72pt após a ementa: issue #72.
	  Somente texto: a formatação inline é descartada.
	-->
	<xsl:template match="lx:Preambulo">
		<xsl:variable name="paragrafos" select="lx:p[normalize-space(.) != '']"/>
		<xsl:if test="normalize-space(.) != ''">
			<fo:block space-before="72pt" line-height="{$lineHeight}" text-align="justify" text-indent="2.5cm">
				<xsl:choose>
					<xsl:when test="$paragrafos">
						<xsl:for-each select="$paragrafos">
							<fo:block margin-bottom="{$pMarginBottom}"><xsl:value-of select="normalize-space(.)"/></fo:block>
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<fo:block margin-bottom="{$pMarginBottom}"><xsl:value-of select="normalize-space(.)"/></fo:block>
					</xsl:otherwise>
				</xsl:choose>
			</fo:block>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>
