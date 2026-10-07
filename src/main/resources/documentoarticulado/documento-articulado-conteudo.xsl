<?xml version="1.0" encoding="UTF-8"?>
<!--
  Conteúdo impresso no PDF do documento articulado: transforma o documento-articulado.xml (LexML) em um
  fragmento XSL-FO, inserido no fo:flow do template-velocity-documento-articulado.xml.

  Fluxo isolado da emenda: esta XSLT não importa nem referencia xhtml2fo.xsl ou outros recursos da emenda.
  Os valores de formatação foram copiados da emenda e têm a origem indicada em comentário.

  Imprime a parte inicial (epígrafe, ementa e preâmbulo; issue #72, parte 1) e a articulação básica
  (artigo, caput, parágrafo, inciso, alínea e item; parte 2), com os agrupadores (parte 3), a pena e o
  título de dispositivo (parte 6), os blocos de alteração de norma vigente (parte 4) e os links das
  remissões (parte 5). Depois da articulação, a justificação (issue #75, parte A: parágrafos, formatação
  inline, estilos de parágrafo do editor, marcas de revisão e notas de rodapé); listas, tabelas e imagens da
  justificação, local e data e assinaturas ainda não são impressos.
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
	<xsl:param name="tamanhoFonte" select="'14pt'"/>
	<xsl:param name="maxTamanhoFonte" select="'16pt'"/>
	<xsl:param name="lineHeight" select="'150%'"/>
	<xsl:param name="pMarginBottom" select="'0.6em'"/>

	<!--
	  Remissões (issue #72, parte 5). Dispositivos impressos que podem ser destino de link interno: só os
	  da articulação (os metadados lexedit também têm dispositivos, que não são impressos).
	-->
	<xsl:key name="dispositivo-impresso"
		match="lx:Articulacao//*[self::lx:Artigo or self::lx:Caput or self::lx:Paragrafo or self::lx:Inciso
			or self::lx:Alinea or self::lx:Item or self::lx:Pena or self::lx:Parte or self::lx:Livro
			or self::lx:Titulo or self::lx:Capitulo or self::lx:Secao or self::lx:Subsecao]"
		use="@id"/>
	<!-- Remissões internas (href que não é URN), indexadas pelo id do alvo. -->
	<xsl:key name="remissao-interna"
		match="lx:Remissao[@xlink:href][not(starts-with(@xlink:href, 'urn:'))]
			| lx:span[@xlink:href][not(starts-with(@xlink:href, 'urn:'))]"
		use="@xlink:href"/>

	<!-- Links em cinza (#808080, mais claro que o #404040 e o #666666 testados antes, que se confundiam com o preto), diferenciando-se do texto, sem sublinhado (issue #72). -->
	<xsl:variable name="corLink" select="'#808080'"/>
	<xsl:variable name="urlNormas" select="'https://normas.leg.br/?urn='"/>

	<!-- Raiz única do fragmento: o fo:flow sempre recebe ao menos um bloco. -->
	<xsl:template match="/">
		<fo:block>
			<xsl:apply-templates select="lx:LexML/lx:ProjetoNorma/lx:Norma"/>
			<xsl:call-template name="justificacao">
				<xsl:with-param name="partes" select="lx:LexML/lx:ProjetoNorma/lx:Justificacao/lx:PartePrincipal"/>
			</xsl:call-template>
		</fo:block>
	</xsl:template>

	<!--
	  Justificação (issue #75), depois da articulação. Emenda: bloco "JUSTIFICAÇÃO" (text-align="center",
	  font-weight="bold", font-size="$maxTamanhoFonte", space-before="$spacing3" = 26pt) seguido do bloco
	  role="Justificativa" (space-before="$spacing1" = 14pt, line-height="$lineHeight", text-indent="2.5cm", sem
	  text-align: alinhado à esquerda, como no editor). Só é impressa se houver texto fora de exclusões (em
	  parágrafo, item de lista ou célula de tabela); havendo mais de uma Justificacao, um único título e os
	  conteúdos na ordem do documento.
	-->
	<xsl:template name="justificacao">
		<xsl:param name="partes"/>
		<xsl:if test="$partes//text()[not(ancestor::lx:del)][normalize-space()]
			or $partes//lx:img[starts-with(normalize-space(@src), 'data:image/')][not(ancestor::lx:del)]">
			<fo:block text-align="center" font-weight="bold" font-size="{$maxTamanhoFonte}" space-before="26pt"
				keep-with-next.within-page="always">JUSTIFICAÇÃO</fo:block>
			<!--
			  line-height-shift-adjustment="disregard-shifts" (diferença em relação à emenda, decidida na parte B):
			  sobrescrito e subscrito (número de nota de rodapé, m², H₂O) não aumentam a altura da linha. Sem isso, a
			  linha com o número da nota fica mais baixa que as vizinhas e, numa célula, desalinha da célula ao lado.
			-->
			<fo:block space-before="14pt" line-height="{$lineHeight}" text-indent="2.5cm"
				line-height-shift-adjustment="disregard-shifts">
				<xsl:apply-templates select="$partes/*" mode="justificacao"/>
			</fo:block>
		</xsl:if>
	</xsl:template>

	<!--
	  Parágrafo da justificação. Emenda: html2foTextoLivre põe margin-bottom: $pMarginBottom em cada p. Um
	  parágrafo sem texto é uma linha em branco do editor (na emenda, <p><br></p>): sai como linha em branco.
	-->
	<xsl:template match="lx:p" mode="justificacao">
		<fo:block margin-bottom="{$pMarginBottom}">
			<xsl:call-template name="estilo-paragrafo"/>
			<!--
			  Parágrafo que começa com imagem: centralizado e sem recuo. Emenda: html2foTextoLivre troca <p...><img
			  por <p... class="align-center"><img; aqui vale para todo parágrafo cujo primeiro conteúdo é imagem.
			-->
			<!-- not(self::text()) e não self::*: no XSLT do JDK, [self::* or normalize-space(.)] dá falso para elemento sem texto (ex.: img). -->
			<xsl:if test="node()[not(self::text()) or normalize-space(.)][1][self::lx:img and starts-with(normalize-space(@src), 'data:image/')]">
				<xsl:attribute name="text-align">center</xsl:attribute>
				<xsl:attribute name="text-indent">0</xsl:attribute>
			</xsl:if>
			<xsl:choose>
				<xsl:when test=".//text()[not(ancestor::lx:del)][normalize-space()]
					or .//lx:img[starts-with(normalize-space(@src), 'data:image/')][not(ancestor::lx:del)]">
					<xsl:apply-templates mode="inline"/>
				</xsl:when>
				<xsl:otherwise>&#160;</xsl:otherwise>
			</xsl:choose>
		</fo:block>
	</xsl:template>

	<!--
	  Listas da justificação (issue #75, parte B). Emenda: xhtml2fo.xsl, templates ol, ol/li, ul e ul/li
	  (list-block com margin-left 2.5cm e text-indent 0; provisional-label-separation 1em;
	  provisional-distance-between-starts de (algarismos do nº de itens) * 0.9 + 0.6 em na numerada e 1em na de
	  marcadores; rótulo "N." ou "•" alinhado à direita) e html2foTextoLivre (margin-bottom: $pMarginBottom em
	  ol e ul). Uma lista dentro de um item é impressa pelo mesmo template, recuada a partir do corpo do item. O
	  recuo plano do editor (li class="indent-N") é ignorado, como na emenda.
	-->
	<xsl:template match="lx:ol | lx:ul" mode="justificacao">
		<xsl:call-template name="lista"/>
	</xsl:template>

	<xsl:template name="lista">
		<fo:list-block margin-left="2.5cm" text-indent="0" margin-bottom="{$pMarginBottom}"
			provisional-label-separation="1em">
			<xsl:attribute name="provisional-distance-between-starts">
				<xsl:choose>
					<xsl:when test="self::lx:ol">
						<xsl:value-of select="string-length(string(count(lx:li))) * 0.9 + 0.6"/>
						<xsl:text>em</xsl:text>
					</xsl:when>
					<xsl:otherwise>1em</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:apply-templates select="lx:li" mode="lista"/>
		</fo:list-block>
	</xsl:template>

	<!--
	  Item de lista: rótulo e corpo. No corpo, cada trecho de texto e elementos inline vira um bloco, cada p um
	  bloco próprio e cada ol/ul uma lista aninhada, na ordem do item.
	-->
	<xsl:template match="lx:li" mode="lista">
		<fo:list-item>
			<fo:list-item-label end-indent="label-end()">
				<fo:block text-align="end">
					<xsl:choose>
						<xsl:when test="parent::lx:ol"><xsl:number count="lx:li"/>.</xsl:when>
						<xsl:otherwise>•</xsl:otherwise>
					</xsl:choose>
				</fo:block>
			</fo:list-item-label>
			<fo:list-item-body start-indent="body-start()">
				<xsl:apply-templates select="node()" mode="item-de-lista"/>
				<xsl:if test="not(node())">
					<fo:block/>
				</xsl:if>
			</fo:list-item-body>
		</fo:list-item>
	</xsl:template>

	<xsl:template match="lx:p" mode="item-de-lista">
		<fo:block>
			<xsl:apply-templates mode="inline"/>
		</fo:block>
	</xsl:template>

	<xsl:template match="lx:ol | lx:ul" mode="item-de-lista">
		<xsl:call-template name="lista"/>
	</xsl:template>

	<!--
	  Texto e elementos inline do item: o primeiro nó de cada trecho (sem bloco antes dele) abre um bloco com ele
	  e os irmãos inline seguintes do mesmo trecho; os demais nós do trecho já foram impressos.
	-->
	<xsl:template match="node()" mode="item-de-lista">
		<xsl:variable name="blocosAntes" select="count(preceding-sibling::*[self::lx:p or self::lx:ol or self::lx:ul])"/>
		<xsl:variable name="anterior" select="preceding-sibling::node()[1]"/>
		<xsl:if test="not($anterior) or $anterior[self::lx:p or self::lx:ol or self::lx:ul]">
			<xsl:variable name="trecho" select=". | following-sibling::node()[not(self::lx:p or self::lx:ol or self::lx:ul)]
				[count(preceding-sibling::*[self::lx:p or self::lx:ol or self::lx:ul]) = $blocosAntes]"/>
			<!-- not(self::text()) e não self::*: no XSLT do JDK, [self::* or normalize-space(.)] dá falso para elemento sem texto (ex.: img). -->
			<xsl:if test="$trecho[not(self::text()) or normalize-space(.)]">
				<fo:block>
					<xsl:apply-templates select="$trecho" mode="inline"/>
				</fo:block>
			</xsl:if>
		</xsl:if>
	</xsl:template>

	<!--
	  Tabelas da justificação (issue #75, parte B). Emenda: xhtml2fo.xsl, templates table (tabela externa de três
	  colunas só para centralizar; a do meio tem a largura da tabela, ou 100%), table-common-atts (borda 1pt
	  solid, ou nenhuma com border="0"; border-collapse collapse; table-layout fixed), column-width (colunas
	  proporcionais), td e th (padding .2em; borda na célula só com border diferente de 0; bloco com font-size 80%
	  e line-height 140%; th em negrito e centralizado na vertical). O width inteiro do LexML é tomado como
	  porcentagem, a unidade do editor. O LexML não guarda a largura das células: colunas de mesma largura.
	-->
	<xsl:template match="lx:table" mode="justificacao">
		<fo:table table-layout="fixed" width="100%">
			<fo:table-column column-width="proportional-column-width(1)"/>
			<fo:table-column>
				<xsl:attribute name="column-width">
					<xsl:call-template name="porcentagem">
						<xsl:with-param name="valor" select="@width"/>
					</xsl:call-template>
				</xsl:attribute>
			</fo:table-column>
			<fo:table-column column-width="proportional-column-width(1)"/>
			<fo:table-body>
				<fo:table-row>
					<fo:table-cell column-number="2">
						<fo:block text-indent="0">
							<fo:table width="100%" table-layout="fixed" border-collapse="collapse">
								<xsl:choose>
									<xsl:when test="@border = 0">
										<xsl:attribute name="border-style">none</xsl:attribute>
									</xsl:when>
									<xsl:otherwise>
										<xsl:attribute name="border">1pt solid</xsl:attribute>
									</xsl:otherwise>
								</xsl:choose>
								<xsl:call-template name="colunas">
									<xsl:with-param name="quantidade">
										<xsl:call-template name="quantidade-de-colunas"/>
									</xsl:with-param>
								</xsl:call-template>
								<fo:table-body>
									<xsl:apply-templates select="lx:tr" mode="tabela"/>
								</fo:table-body>
							</fo:table>
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
	</xsl:template>

	<!-- Maior número de colunas entre as linhas, contando os colspan. -->
	<xsl:template name="quantidade-de-colunas">
		<xsl:for-each select="lx:tr">
			<xsl:sort select="count(lx:td | lx:th) - count((lx:td | lx:th)[@colspan]) + sum((lx:td | lx:th)/@colspan)"
				data-type="number" order="descending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="count(lx:td | lx:th) - count((lx:td | lx:th)[@colspan]) + sum((lx:td | lx:th)/@colspan)"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="colunas">
		<xsl:param name="quantidade"/>
		<xsl:if test="$quantidade &gt; 0">
			<fo:table-column column-width="proportional-column-width(1)"/>
			<xsl:call-template name="colunas">
				<xsl:with-param name="quantidade" select="$quantidade - 1"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template match="lx:tr" mode="tabela">
		<fo:table-row>
			<xsl:apply-templates select="lx:td | lx:th" mode="tabela"/>
		</fo:table-row>
	</xsl:template>

	<xsl:template match="lx:td | lx:th" mode="tabela">
		<fo:table-cell padding=".2em">
			<xsl:if test="self::lx:th">
				<xsl:attribute name="font-weight">bold</xsl:attribute>
				<xsl:attribute name="display-align">center</xsl:attribute>
			</xsl:if>
			<xsl:if test="ancestor::lx:table[1][@border and @border != 0]">
				<xsl:attribute name="border">1pt solid</xsl:attribute>
			</xsl:if>
			<xsl:if test="@colspan &gt; 1">
				<xsl:attribute name="number-columns-spanned"><xsl:value-of select="@colspan"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@rowspan &gt; 1">
				<xsl:attribute name="number-rows-spanned"><xsl:value-of select="@rowspan"/></xsl:attribute>
			</xsl:if>
			<fo:block font-size="80%" line-height="140%">
				<xsl:apply-templates mode="inline"/>
			</fo:block>
		</fo:table-cell>
	</xsl:template>

	<!-- Largura em porcentagem a partir do inteiro do LexML (1 a 100); sem valor válido, 100%. -->
	<xsl:template name="porcentagem">
		<xsl:param name="valor"/>
		<xsl:choose>
			<xsl:when test="number($valor) &gt; 0 and number($valor) &lt;= 100">
				<xsl:value-of select="number($valor)"/>
				<xsl:text>%</xsl:text>
			</xsl:when>
			<xsl:otherwise>100%</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Demais blocos da justificação (ex.: div, Bloco): não impressos. -->
	<xsl:template match="*" mode="justificacao" priority="-1"/>

	<!--
	  Estilos de parágrafo do editor (classes do Quill), lidos por token. Valores da emenda:
	  html2foTextoLivre (estilo-ementa: margin-left 6.5cm, text-indent 0; estilo-norma-alterada: margin-left
	  3cm, text-indent 1.5cm; ql-text-indent-0px; ql-margin-bottom-0px) e
	  HTML2FOConverter.trataAlinhamentoDePragrafo (ql-align-*: text-align, e text-indent 0 em center e right).
	  Ordem: estilos, alinhamento e, por último,
	  as escolhas explícitas de recuo e espaço; um atributo repetido substitui o anterior. Classes
	  desconhecidas são ignoradas.
	-->
	<xsl:template name="estilo-paragrafo">
		<xsl:variable name="classes" select="concat(' ', normalize-space(@class), ' ')"/>
		<xsl:if test="contains($classes, ' estilo-ementa ')">
			<xsl:attribute name="margin-left">6.5cm</xsl:attribute>
			<xsl:attribute name="text-indent">0</xsl:attribute>
		</xsl:if>
		<xsl:if test="contains($classes, ' estilo-norma-alterada ')">
			<xsl:attribute name="margin-left">3cm</xsl:attribute>
			<xsl:attribute name="text-indent">1.5cm</xsl:attribute>
		</xsl:if>
		<!-- Alinhamento com e sem o prefixo ql-: o editor grava sem ele (ajustaHtmlFromEditor do lexml-eta). -->
		<xsl:choose>
			<xsl:when test="contains($classes, ' ql-align-center ') or contains($classes, ' align-center ')">
				<xsl:attribute name="text-align">center</xsl:attribute>
				<xsl:attribute name="text-indent">0</xsl:attribute>
			</xsl:when>
			<xsl:when test="contains($classes, ' ql-align-right ') or contains($classes, ' align-right ')">
				<xsl:attribute name="text-align">right</xsl:attribute>
				<xsl:attribute name="text-indent">0</xsl:attribute>
			</xsl:when>
			<xsl:when test="contains($classes, ' ql-align-justify ') or contains($classes, ' align-justify ')">
				<xsl:attribute name="text-align">justify</xsl:attribute>
			</xsl:when>
		</xsl:choose>
		<xsl:if test="contains($classes, ' ql-text-indent-0px ')">
			<xsl:attribute name="text-indent">0</xsl:attribute>
		</xsl:if>
		<xsl:if test="contains($classes, ' ql-margin-bottom-0px ')">
			<xsl:attribute name="margin-bottom">0</xsl:attribute>
		</xsl:if>
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
				<xsl:call-template name="id-destino"/>
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

	<!--
	  Artigo: o título de dispositivo, se houver, vem antes; o rótulo do artigo é impresso no mesmo bloco
	  do texto do caput.
	-->
	<xsl:template match="lx:Artigo" mode="dispositivo">
		<xsl:apply-templates select="lx:TituloDispositivo" mode="dispositivo"/>
		<!-- Em bloco de alteração, as aspas de abertura do artigo saem com o rótulo, no bloco do caput. -->
		<!-- O artigo não tem bloco próprio: se for alvo de remissão, o destino fica no rótulo. -->
		<xsl:apply-templates select="lx:Caput" mode="dispositivo">
			<xsl:with-param name="rotulo" select="lx:Rotulo"/>
			<xsl:with-param name="abreAspas" select="@abreAspas = 's'"/>
			<xsl:with-param name="idRotulo">
				<xsl:if test="key('remissao-interna', @id)"><xsl:value-of select="@id"/></xsl:if>
			</xsl:with-param>
		</xsl:apply-templates>
		<xsl:apply-templates select="*[not(self::lx:Rotulo or self::lx:Caput or self::lx:TituloDispositivo)]"
			mode="dispositivo"/>
	</xsl:template>

	<xsl:template match="lx:Caput" mode="dispositivo">
		<xsl:param name="rotulo" select="/.."/>
		<xsl:param name="abreAspas" select="@abreAspas = 's'"/>
		<xsl:param name="idRotulo" select="''"/>
		<xsl:call-template name="bloco-dispositivo">
			<xsl:with-param name="rotulo" select="$rotulo"/>
			<xsl:with-param name="abreAspas" select="$abreAspas"/>
			<xsl:with-param name="idRotulo" select="$idRotulo"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="lx:Paragrafo | lx:Inciso | lx:Alinea | lx:Item" mode="dispositivo">
		<xsl:call-template name="bloco-dispositivo">
			<xsl:with-param name="rotulo" select="lx:Rotulo"/>
		</xsl:call-template>
	</xsl:template>

	<!--
	  Pena (issue #72, parte 6): mesma formatação dos dispositivos de artigo, sem negrito no rótulo.
	  Impressa na posição em que aparece (em geral no fim do caput ou do parágrafo).
	-->
	<xsl:template match="lx:Pena" mode="dispositivo">
		<xsl:call-template name="bloco-dispositivo">
			<xsl:with-param name="rotulo" select="lx:Rotulo"/>
			<xsl:with-param name="rotuloNegrito" select="false()"/>
		</xsl:call-template>
	</xsl:template>

	<!--
	  Título de dispositivo (issue #72, parte 6): mesma formatação dos dispositivos de artigo (herda recuo,
	  justificação e entrelinha da articulação; não é centralizado), sem rótulo e todo em negrito.
	  keep-with-next evita o título sozinho no fim da página, como no título dos agrupadores.
	-->
	<xsl:template match="lx:TituloDispositivo" mode="dispositivo">
		<xsl:if test="normalize-space(.) != ''">
			<fo:block font-weight="bold" keep-with-next.within-page="always">
				<xsl:apply-templates mode="inline"/>
			</fo:block>
		</xsl:if>
	</xsl:template>

	<!--
	  Bloco de um dispositivo: título de dispositivo do próprio dispositivo (se houver), aspas de abertura
	  (abreAspas), rótulo (em negrito, exceto na pena; emenda: citacao2html troca Rotulo por strong),
	  espaço e o primeiro p, ou a linha pontilhada quando textoOmitido="s"; os p seguintes em blocos
	  próprios; o fechamento das aspas e a nota de alteração (fechaAspas, notaAlteracao) no último bloco de
	  texto; depois os dispositivos subordinados, na ordem do documento. Os espaços do início do p são
	  colapsados pelo XSL-FO.
	-->
	<xsl:template name="bloco-dispositivo">
		<xsl:param name="rotulo"/>
		<xsl:param name="rotuloNegrito" select="true()"/>
		<xsl:param name="abreAspas" select="@abreAspas = 's'"/>
		<xsl:param name="idRotulo" select="''"/>
		<xsl:variable name="textoRotulo" select="normalize-space($rotulo)"/>
		<xsl:variable name="textoOmitido" select="@textoOmitido = 's'"/>
		<xsl:variable name="fechaAspas" select="@fechaAspas = 's'"/>
		<xsl:variable name="nota" select="normalize-space(@notaAlteracao)"/>
		<xsl:variable name="paragrafos" select="lx:p"/>
		<xsl:variable name="demaisParagrafos" select="$paragrafos[position() &gt; 1][normalize-space(.) != '']"/>
		<xsl:apply-templates select="lx:TituloDispositivo" mode="dispositivo"/>
		<xsl:if test="$textoRotulo != '' or normalize-space($paragrafos[1]) != '' or $textoOmitido or $abreAspas">
			<fo:block>
				<xsl:call-template name="id-destino"/>
				<xsl:if test="$abreAspas">
					<xsl:call-template name="abre-aspas"/>
				</xsl:if>
				<xsl:if test="$textoRotulo != ''">
					<fo:inline>
						<xsl:if test="$idRotulo != ''">
							<xsl:attribute name="id"><xsl:value-of select="$idRotulo"/></xsl:attribute>
						</xsl:if>
						<xsl:if test="$rotuloNegrito">
							<xsl:attribute name="font-weight">bold</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="$textoRotulo"/>
					</fo:inline>
					<xsl:text> </xsl:text>
				</xsl:if>
				<xsl:choose>
					<xsl:when test="$textoOmitido">
						<xsl:call-template name="linha-pontilhada"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates select="$paragrafos[1]/node()" mode="inline"/>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:if test="$fechaAspas and not($demaisParagrafos)">
					<xsl:call-template name="fecha-aspas">
						<xsl:with-param name="nota" select="$nota"/>
					</xsl:call-template>
				</xsl:if>
			</fo:block>
		</xsl:if>
		<xsl:for-each select="$demaisParagrafos">
			<fo:block>
				<xsl:apply-templates mode="inline"/>
				<xsl:if test="$fechaAspas and position() = last()">
					<xsl:call-template name="fecha-aspas">
						<xsl:with-param name="nota" select="$nota"/>
					</xsl:call-template>
				</xsl:if>
			</fo:block>
		</xsl:for-each>
		<xsl:apply-templates select="*[not(self::lx:Rotulo or self::lx:p or self::lx:TituloDispositivo)]" mode="dispositivo"/>
	</xsl:template>

	<!--
	  Bloco de alteração de norma vigente (issue #72, parte 4). Emenda: citacao2html troca <Alteracao> por
	  margin-left="3cm" text-indent="1.5cm" (os mesmos valores de estilo-norma-alterada); os dispositivos
	  de dentro herdam o recuo e usam os templates da articulação. Sem espaço extra. As aspas e a nota de
	  alteração vêm dos atributos abreAspas, fechaAspas e notaAlteracao; nenhuma aspa é gerada por conta
	  própria (as aspas da citação da emenda não existem aqui).
	-->
	<xsl:template match="lx:Alteracao" mode="dispositivo">
		<fo:block margin-left="3cm" text-indent="1.5cm">
			<xsl:apply-templates select="*" mode="dispositivo"/>
		</fo:block>
	</xsl:template>

	<!-- Omissis: linha pontilhada inteira, com aspas conforme os atributos. -->
	<xsl:template match="lx:Omissis" mode="dispositivo">
		<fo:block>
			<xsl:if test="@abreAspas = 's'">
				<xsl:call-template name="abre-aspas"/>
			</xsl:if>
			<xsl:call-template name="linha-pontilhada"/>
			<xsl:if test="@fechaAspas = 's'">
				<xsl:call-template name="fecha-aspas">
					<xsl:with-param name="nota" select="normalize-space(@notaAlteracao)"/>
				</xsl:call-template>
			</xsl:if>
		</fo:block>
	</xsl:template>

	<!--
	  Linha pontilhada até a margem direita. Emenda: xhtml2fo.xsl (span class="omissis"). O comprimento
	  mínimo padrão (0) deixa a linha encolher para caber o fechamento das aspas no fim da mesma linha.
	-->
	<xsl:template name="linha-pontilhada">
		<fo:leader leader-pattern="dots" leader-length.optimum="100%"/>
	</xsl:template>

	<!--
	  Texto no modo inline. O último texto do último p de um dispositivo com fechaAspas perde os espaços do
	  fim, para as aspas de fechamento ficarem coladas ao texto ("pública.”" e não "pública. ”").
	-->
	<xsl:template match="text()" mode="inline">
		<xsl:variable name="p" select="ancestor::lx:p[1]"/>
		<xsl:choose>
			<xsl:when test="$p/parent::*[@fechaAspas = 's'] and not($p/following-sibling::lx:p[normalize-space(.) != ''])
					and not(following::text()[generate-id(ancestor::lx:p[1]) = generate-id($p)])">
				<xsl:call-template name="aparar-fim">
					<xsl:with-param name="texto" select="."/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Remove espaços, tabulações e quebras de linha do fim do texto (XSLT 1.0 não tem regex). -->
	<xsl:template name="aparar-fim">
		<xsl:param name="texto"/>
		<xsl:variable name="ultimo" select="substring($texto, string-length($texto))"/>
		<xsl:choose>
			<xsl:when test="string-length($texto) &gt; 0 and contains(' &#9;&#10;&#13;', $ultimo)">
				<xsl:call-template name="aparar-fim">
					<xsl:with-param name="texto" select="substring($texto, 1, string-length($texto) - 1)"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$texto"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Aspas fora do negrito do rótulo, como na emenda. -->
	<xsl:template name="abre-aspas">
		<xsl:text>“</xsl:text>
	</xsl:template>

	<!-- Aspas de fechamento e, se houver, a nota de alteração com espaço antes: ” (NR). Nada depois. -->
	<xsl:template name="fecha-aspas">
		<xsl:param name="nota"/>
		<xsl:text>”</xsl:text>
		<xsl:if test="$nota != ''">
			<xsl:text> (</xsl:text>
			<xsl:value-of select="$nota"/>
			<xsl:text>)</xsl:text>
		</xsl:if>
	</xsl:template>
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

	<!-- Sublinhado, subscrito e sobrescrito (issue #75). Emenda: xhtml2fo.xsl (u; sub e sup com 0.7em). -->
	<xsl:template match="lx:u" mode="inline">
		<fo:inline text-decoration="underline"><xsl:apply-templates mode="inline"/></fo:inline>
	</xsl:template>

	<xsl:template match="lx:sub" mode="inline">
		<fo:inline baseline-shift="sub" font-size="0.7em"><xsl:apply-templates mode="inline"/></fo:inline>
	</xsl:template>

	<xsl:template match="lx:sup" mode="inline">
		<fo:inline baseline-shift="super" font-size="0.7em"><xsl:apply-templates mode="inline"/></fo:inline>
	</xsl:template>

	<!--
	  Link do editor (issue #75): com endereço web em xlink:href, link externo no estilo das remissões; sem
	  endereço, só o texto.
	-->
	<xsl:template match="lx:a" mode="inline">
		<xsl:variable name="href" select="normalize-space(@xlink:href)"/>
		<xsl:choose>
			<xsl:when test="starts-with($href, 'http://') or starts-with($href, 'https://')">
				<fo:basic-link external-destination="url('{$href}')" color="{$corLink}">
					<xsl:apply-templates mode="inline"/>
				</fo:basic-link>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="inline"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
	  Imagem (issue #75, parte B). Emenda: xhtml2fo.xsl, template img (bloco próprio com fo:external-graphic,
	  content-width="scale-to-fit", scaling="uniform", width do img ou 100%). Diferenças: o data URI vai direto
	  para o FOP, sem a gravação em arquivo temporário e a conversão para JPEG de VelocityExtension.trataImagens;
	  só src "data:image/..." é usado (outros endereços fariam o FOP acessar rede ou disco); o bloco tem
	  text-indent 0, para a imagem em largura total não passar da margem direita; o width inteiro do LexML é
	  tomado como porcentagem. Imagem que o FOP não consegue ler: ele registra o erro e segue sem ela.
	-->
	<xsl:template match="lx:img" mode="inline">
		<xsl:variable name="src" select="normalize-space(@src)"/>
		<xsl:if test="starts-with($src, 'data:image/')">
			<fo:block text-indent="0">
				<fo:external-graphic src="url('{$src}')" content-width="scale-to-fit" scaling="uniform">
					<xsl:attribute name="width">
						<xsl:call-template name="porcentagem">
							<xsl:with-param name="valor" select="@width"/>
						</xsl:call-template>
					</xsl:attribute>
				</fo:external-graphic>
			</fo:block>
		</xsl:if>
	</xsl:template>

	<!--
	  Marcas de revisão (issue #75): o PDF representa a versão revisada. Exclusões não são impressas;
	  inclusões saem como o restante do texto (regra geral abaixo), sem destaque.
	-->
	<xsl:template match="lx:del" mode="inline"/>

	<!--
	  Nota de rodapé (issue #75; especificação 07 do lexml-eta: texto inline, numeração calculada na
	  impressão). Emenda: xhtml2fo.xsl, template nota-rodape (número sobrescrito em 0.7em no texto; corpo com
	  font-size="$tamanhoFonte" e bloco interno de 0.7em e line-height 1.5em) e VelocityTemplateProcessor (número
	  e espaço antes do texto da nota). A numeração conta só as notas impressas: as de dentro de exclusões (del)
	  nem chegam aqui. O corpo herda as propriedades do parágrafo onde a nota está; as herdáveis que mudariam a
	  nota (recuos, alinhamento, peso, estilo, decoração) são zeradas.
	-->
	<xsl:template match="lx:NotaDeRodape" mode="inline">
		<xsl:variable name="numero">
			<xsl:number level="any" count="lx:NotaDeRodape[not(ancestor::lx:del)]" from="lx:ProjetoNorma"/>
		</xsl:variable>
		<fo:footnote>
			<fo:inline baseline-shift="super" font-size="0.7em"><xsl:value-of select="$numero"/></fo:inline>
			<fo:footnote-body>
				<fo:block font-size="{$tamanhoFonte}" text-indent="0" start-indent="0" end-indent="0" text-align="start"
					font-weight="normal" font-style="normal" text-decoration="none">
					<fo:block font-size="0.7em" line-height="1.5em">
						<xsl:value-of select="$numero"/>
						<xsl:text> </xsl:text>
						<xsl:apply-templates mode="inline"/>
					</fo:block>
				</fo:block>
			</fo:footnote-body>
		</fo:footnote>
	</xsl:template>

	<!--
	  Remissões (issue #72, parte 5): href com URN -> link para o portal normas.leg.br (URN sem codificação);
	  href com o id de um dispositivo impresso -> link interno; senão (alvo ausente, ex.: artigo excluído)
	  só o texto, sem link e sem marca visual.
	-->
	<xsl:template match="lx:span[@xlink:href] | lx:Remissao[@xlink:href]" mode="inline">
		<xsl:variable name="href" select="normalize-space(@xlink:href)"/>
		<xsl:choose>
			<xsl:when test="starts-with($href, 'urn:')">
				<fo:basic-link external-destination="url('{$urlNormas}{$href}')" color="{$corLink}">
					<xsl:apply-templates mode="inline"/>
				</fo:basic-link>
			</xsl:when>
			<xsl:when test="$href != '' and key('dispositivo-impresso', $href)">
				<fo:basic-link internal-destination="{$href}" color="{$corLink}">
					<xsl:apply-templates mode="inline"/>
				</fo:basic-link>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="inline"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Demais elementos inline: só o texto. -->
	<xsl:template match="*" mode="inline">
		<xsl:apply-templates mode="inline"/>
	</xsl:template>

	<!--
	  Identificador de destino de link interno, só nos elementos que são alvo de alguma remissão interna
	  (ids repetidos derrubariam o FO; os demais blocos ficam sem atributos).
	-->
	<xsl:template name="id-destino">
		<xsl:if test="@id and key('remissao-interna', @id)">
			<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
		</xsl:if>
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
