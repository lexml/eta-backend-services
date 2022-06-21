<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:java="http://saxon.sf.net/java-type"
     xmlns:sets="http://exslt.org/sets"
     xsi:schemaLocation="http://www.w3.org/1999/XSL/Format ../xsd/fop.xsd"
     exclude-result-prefixes="java fo">

    <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="no" xml:space="default"
        encoding="utf-8"/>

    <xsl:param name="formato" />
    <xsl:variable name="isPdf" select="$formato = 'pdf'"/>
    <xsl:variable name="isRtf" select="$formato = 'rtf'"/>

    <xsl:strip-space elements="*" />
    <xsl:decimal-format name="brasil" decimal-separator="," grouping-separator="." NaN="vazio" />

    <xsl:variable name="text-indent-default">2.5cm</xsl:variable>
    <xsl:variable name="text-indent-alteracao">1cm</xsl:variable>
    <xsl:variable name="margin-left-alteracao">3cm</xsl:variable>
    <xsl:variable name="margin-left-ementa">7cm</xsl:variable>

    <xsl:template match="EmendaProposicao">
        <fo:root language="pt-BR" font-family="Gentium Basic">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="simpleA4-1Col-right"
                    page-height="29.7cm" page-width="21cm"
                    margin-top="2.5cm" margin-bottom="2cm" margin-left="2.5cm" margin-right="2cm">
                    <fo:region-body margin-bottom="1cm"/>
                    <fo:region-after extent="1cm"/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:declarations> 
            	@@xmpmeta@@                  
            </fo:declarations>
            <fo:page-sequence master-reference="simpleA4-1Col-right"
                font-size="14pt" text-align="left"
                line-stacking-strategy="font-height" line-height="140%" >
				<fo:static-content flow-name="xsl-region-after">
					<xsl:call-template name="Rodape" />
				</fo:static-content>            
                <fo:flow flow-name="xsl-region-body">
                    <xsl:call-template name="TrataEmenda"/>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
    
    <xsl:template name="TrataEmenda">

	    <xsl:variable name="genero">
	        <xsl:choose>
	            <xsl:when test="Emenda/Proposicao/@genero">
	                <xsl:value-of select="Emenda/Proposicao/@genero"/>
	            </xsl:when>
	            <!--
	            Para emendas antigas consideramos o gênero feminino apenas para PEC
	             -->
	            <xsl:otherwise>
	                <xsl:choose>
	                    <xsl:when test="contains(Emenda/Proposicao, 'PEC')">f</xsl:when>
	                    <xsl:otherwise>m</xsl:otherwise>
	                </xsl:choose>
	            </xsl:otherwise>
	        </xsl:choose>
	    </xsl:variable>
	    
	    <xsl:variable name="sufixoEpigrafe">
	        <xsl:choose>
	            <xsl:when test="Emenda/Epigrafe/Destino[@tipo='Comissão']">
	                - <xsl:value-of select="Emenda/Epigrafe/Destino/text()"/>
	            </xsl:when>
	        </xsl:choose>
	    </xsl:variable>
	    
	    <xsl:variable name="substitutivo">
	        <xsl:choose>
	            <xsl:when test="Emenda/Proposicao/@substitutivo">
	                <xsl:value-of select="Emenda/Proposicao/@substitutivo"/>
	            </xsl:when>
	            <!--
	            Para emendas antigas consideramos que não é substitutivo
	             -->
	            <xsl:otherwise>n</xsl:otherwise>
	        </xsl:choose>
	    </xsl:variable>

        <fo:block font-weight="bold" font-size="16pt" text-align="center">
            EMENDA Nº ________ <xsl:value-of select="$sufixoEpigrafe"/>  
        </fo:block>
        <fo:block text-align="center">
        	<xsl:variable name="sub">
        		<xsl:choose>
        			<xsl:when test="$substitutivo = 's'">ao Substitutivo </xsl:when>
        			<xsl:otherwise></xsl:otherwise>
        		</xsl:choose>
        	</xsl:variable>
            <xsl:variable name="preposicao">
                <xsl:choose>
                    <xsl:when test="$genero = 'f'">à</xsl:when>
                    <xsl:otherwise>ao</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            (<xsl:value-of select="concat($sub, $preposicao, ' ', Emenda/Proposicao)" />)
        </fo:block>

        <fo:block margin-top="2cm" />

        <xsl:for-each select="Emenda">
            <fo:block>
                <xsl:apply-templates select="ComandoEmenda" />
            </fo:block>
        </xsl:for-each>
        
 		@@fecho@@

    </xsl:template>

    <!-- template do comando de emenda -->
    <xsl:template match="ComandoEmenda">
        <xsl:apply-templates mode="ComandoEmenda"/>
    </xsl:template>

    <xsl:template match="p" mode="ComandoEmenda">
        <fo:block text-indent="{$text-indent-default}" text-align="justify">
            <xsl:choose>
                <xsl:when test="name(following-sibling::*[1]) = 'p'">
                  <xsl:attribute name="margin-bottom">10mm</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:attribute name="margin-bottom">5mm</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="Citacao" mode="ComandoEmenda">
        <fo:block margin-bottom="1cm">
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="Aspas">
        <xsl:apply-templates mode="aspas"/>
    </xsl:template>

    <xsl:template match="*[@refid]" mode="aspas">
        <xsl:param name="rotulo"/>
        <xsl:param name="abreAspas"/>
        <xsl:param name="fechaAspas"/>
        <xsl:param name="alteracao"/>

        <xsl:variable name="ident">
            <xsl:value-of select="@refid" />
        </xsl:variable>

        <xsl:variable name="rotuloAnterior" select="preceding-sibling::*[1][name() = 'Rotulo']"/>
        <xsl:variable name="primeiro" select="
            sets:has-same-node((ancestor::Aspas[1]//Omissis[1] | ancestor::Aspas[1]//*[@refid][1])[1], .)
        "/>
        <xsl:variable name="primeiroNaAlteracao" select="
            sets:has-same-node((ancestor::Alteracao[1]//*[@refid][1])[1], .)
        "/>
        <xsl:variable name="ultimoNaAlteracaoOuNoParagrafo" select="
            sets:has-same-node((ancestor::Alteracao[1]//*[@refid])[last()], .) or
            exists(ancestor::Alteracao) and name(following::*[1]) = 'Artigo'
        "/>
<!--
[<xsl:value-of select="name()"/>, Primeiro: <xsl:value-of select="$primeiro"/>, Alteracao: <xsl:value-of select="$alteracao"/>]
 -->

        <xsl:apply-templates select="/EmendaProposicao/Emenda/Marcadores//*[@id=$ident]" mode="aspas">
            <xsl:with-param name="rotulo">
                <xsl:choose>
                    <xsl:when test="$rotulo">
                        <xsl:value-of select="$rotulo"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$rotuloAnterior"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:with-param>
            <xsl:with-param name="abreAspas" select="$primeiro or (name() = 'Caput' and not($alteracao))"/>
            <xsl:with-param name="fechaAspas" select="
                sets:has-same-node((ancestor::Aspas[1]//Omissis | ancestor::Aspas[1]//*[@refid])[last()], .)
            "/>
            <xsl:with-param name="alteracao" select="$alteracao or name() = 'Alteracao'"/>
            <xsl:with-param name="abreAspasSimples" select="$primeiroNaAlteracao or (name() = 'Caput' and $alteracao)"/>
            <xsl:with-param name="fechaAspasSimples" select="$ultimoNaAlteracaoOuNoParagrafo"/>
        </xsl:apply-templates>
        <xsl:apply-templates mode="aspas">
            <xsl:with-param name="rotulo">
                <xsl:if test="name() = 'Artigo'">
                    <xsl:value-of select="/EmendaProposicao/Emenda/Marcadores//*[@id=$ident]//Rotulo[1]"/>
                </xsl:if>
            </xsl:with-param>
            <xsl:with-param name="abreAspas" select="$primeiro"/>
            <xsl:with-param name="alteracao" select="$alteracao or name() = 'Alteracao'"/>
        </xsl:apply-templates>
    </xsl:template>

    <!--
    Aspas duplas &#x201C; e &#x201D;
    Aspas simples &#x2018; e &#x2019;
     -->

    <xsl:template match="Ementa|Artigo|Caput|Inciso|Paragrafo|Item|Alinea|Alteracao" mode="aspas">
        <xsl:param name="rotulo"/>
        <xsl:param name="abreAspas"/>
        <xsl:param name="fechaAspas"/>
        <xsl:param name="alteracao"/>
        <xsl:param name="abreAspasSimples"/>
        <xsl:param name="fechaAspasSimples"/>
<!--
[<xsl:value-of select="name()"/>, <xsl:value-of select="$fechaAspasSimples"/>]
 -->
        <xsl:if test="@textoOmitido = 's'">
            <xsl:call-template name="Omissis">
                <xsl:with-param name="rotuloDeOmissis" select="$rotulo"/>
                <xsl:with-param name="abreAspas" select="$abreAspas"/>
                <xsl:with-param name="fechaAspas" select="$fechaAspas"/>
                <xsl:with-param name="alteracao" select="$alteracao"/>
                <xsl:with-param name="abreAspasSimples" select="$abreAspasSimples"/>
                <xsl:with-param name="fechaAspasSimples" select="$fechaAspasSimples"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:apply-templates mode="aspas">
            <xsl:with-param name="rotulo" select="$rotulo"/>
            <xsl:with-param name="abreAspas" select="$abreAspas"/>
            <xsl:with-param name="fechaAspas" select="$fechaAspas"/>
            <xsl:with-param name="dispositivo" select="name()"/>
            <xsl:with-param name="alteracao" select="$alteracao"/>
            <xsl:with-param name="abreAspasSimples" select="$abreAspasSimples"/>
            <xsl:with-param name="fechaAspasSimples" select="$fechaAspasSimples"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="p" mode="aspas">
        <xsl:param name="rotulo"/>
        <xsl:param name="abreAspas"/>
        <xsl:param name="fechaAspas"/>
        <xsl:param name="dispositivo"/>
        <xsl:param name="alteracao"/>
        <xsl:param name="abreAspasSimples"/>
        <xsl:param name="fechaAspasSimples"/>
        <fo:block text-align="justify">
            <xsl:choose>
                <xsl:when test="$dispositivo = 'Ementa'">
                    <xsl:attribute name="margin-left" select="$margin-left-ementa"/>
                </xsl:when>
                <xsl:when test="$alteracao">
                    <xsl:attribute name="margin-left" select="$margin-left-alteracao"/>
                    <xsl:attribute name="text-indent" select="$text-indent-alteracao"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="text-indent" select="$text-indent-default"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="$abreAspas">&#x201C;</xsl:if><xsl:if test="$abreAspasSimples">&#x2018;</xsl:if><xsl:choose>
                <xsl:when test="preceding-sibling::*[1][name() = 'Rotulo']">
                    <xsl:for-each select="preceding-sibling::*[1]">
                        <xsl:call-template name="RotuloEmAspas"/>
                    </xsl:for-each>
                </xsl:when>
                <xsl:when test="$rotulo">
                    <xsl:for-each select="$rotulo">
                        <xsl:call-template name="RotuloEmAspas"/>
                    </xsl:for-each>
                </xsl:when>
            </xsl:choose>
            <xsl:apply-templates mode="aspas"
            /><xsl:if test="$fechaAspasSimples">&#x2019; </xsl:if
            ><xsl:if test="$fechaAspas">&#x201D;</xsl:if>
        </fo:block>
    </xsl:template>

    <xsl:template name="Omissis" match="Omissis" mode="aspas">
        <xsl:param name="rotuloDeOmissis"/>
        <xsl:param name="abreAspas"/>
        <xsl:param name="fechaAspas"/>
        <xsl:param name="alteracao"/>
        <xsl:param name="abreAspasSimples"/>
        <xsl:param name="fechaAspasSimples"/>
        <xsl:element name="fo:block">
            <xsl:choose>
                <xsl:when test="$alteracao">
                    <xsl:attribute name="margin-left" select="$margin-left-alteracao"/>
                    <xsl:attribute name="text-indent" select="$text-indent-alteracao"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="text-indent" select="$text-indent-default"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:attribute name="text-align-last">justify</xsl:attribute>
<!--
[<xsl:value-of select="name()"/>]
[<xsl:value-of select="$abreAspas"/>]
[<xsl:value-of select="exists(ancestor::*[@refid] | preceding-sibling::*[@refid])"/>]
[<xsl:value-of select="sets:has-same-node(ancestor::Aspas[1]//Caput[1][not(@refid)]/Omissis[1], .)"/>]
[<xsl:value-of select="sets:has-same-node(preceding::*[1][name() = 'Rotulo'], ancestor::Aspas[1]/Artigo[1][not(@refid)]/Rotulo)"/>]
[<xsl:value-of select="..[name() = 'Caput'][@refid]"/>]
 -->
            <xsl:if test="(($abreAspas or sets:has-same-node(ancestor::Aspas[1]//Caput[1][not(@refid)]/Omissis[1], .))
                     and not(exists(ancestor::*[@refid] | preceding-sibling::*[@refid])))
                or (sets:has-same-node(preceding::*[1][name() = 'Rotulo'], ancestor::Aspas[1]/Artigo[1][not(@refid)]/Rotulo)
                    and not(..[name() = 'Caput'][@refid]))">&#x201C;</xsl:if>
            <xsl:if test="$abreAspasSimples">&#x2018;</xsl:if><xsl:choose>

                <!--
                Apresentação do rótulo da Omissis ou do rótulo do Artigo quando seguido de Omissis
                 -->
                <xsl:when test="preceding-sibling::*[1][name() = 'Rotulo'] | Rotulo">
                    <xsl:for-each select="preceding-sibling::*[1][name() = 'Rotulo'] | Rotulo">
                        <xsl:call-template name="RotuloEmAspas"/>
                    </xsl:for-each>
                </xsl:when>

                <!--
                Apresentação de rótulo de artigo quando a primeira Omissis está no início do Caput não alterado (sem refid)
                 -->
                <xsl:when test="exists(parent::*[1][name() = 'Caput'][empty(@refid)]) and empty(preceding-sibling::*)">
                    <xsl:for-each select="ancestor::Artigo/Rotulo[1]">
                        <xsl:call-template name="RotuloEmAspas"/>
                    </xsl:for-each>
                </xsl:when>

                <!--
                Rótulo passado por parâmetro
                 -->
                <xsl:when test="$rotuloDeOmissis">
                    <xsl:for-each select="$rotuloDeOmissis">
                        <xsl:call-template name="RotuloEmAspas"/>
                    </xsl:for-each>
                </xsl:when>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="$isPdf">
                    <fo:leader leader-pattern="dots" />
                </xsl:when>
                <xsl:when test="$isRtf">
                    <xsl:text>.........................................</xsl:text>
                </xsl:when>
            </xsl:choose><xsl:if test="$fechaAspasSimples">&#x2019; </xsl:if>
            <xsl:if test="$fechaAspas or sets:has-same-node((ancestor::Aspas[1]//Omissis | ancestor::Aspas[1]//*[@refid])[last()], .)">&#x201D;</xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template name="RotuloEmAspas">
        <fo:inline font-weight="bold" padding-right="5px">
            <xsl:value-of select="normalize-space(.)"/>
            <xsl:if test="$isRtf"><xsl:value-of select="' '"/></xsl:if>
        </fo:inline>
    </xsl:template>

    <xsl:template match="Rotulo" mode="aspas">
    </xsl:template>

    <xsl:template match="Modificacao|Adicao" mode="aspas">
        <xsl:param name="rotulo"/>
        <xsl:param name="abreAspas"/>
        <xsl:param name="fechaAspas"/>
        <xsl:param name="alteracao"/>
        <xsl:param name="abreAspasSimples"/>
        <xsl:param name="fechaAspasSimples"/>
        <xsl:apply-templates mode="aspas">
            <xsl:with-param name="rotulo" select="$rotulo"/>
            <xsl:with-param name="abreAspas" select="$abreAspas"/>
            <xsl:with-param name="fechaAspas" select="$fechaAspas"/>
            <xsl:with-param name="alteracao" select="$alteracao"/>
            <xsl:with-param name="abreAspasSimples" select="$abreAspasSimples"/>
            <xsl:with-param name="fechaAspasSimples" select="$fechaAspasSimples"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="Supressao" mode="aspas">
        <xsl:param name="abreAspas"/>
        <xsl:param name="fechaAspas"/>
        <xsl:param name="alteracao"/>
        <xsl:param name="abreAspasSimples"/>
        <xsl:param name="fechaAspasSimples"/>
        <fo:block text-indent="{$text-indent-default}">
            <xsl:if test="$abreAspas">&#x201C;</xsl:if><xsl:if test="$abreAspasSimples">&#x2018;</xsl:if><xsl:for-each select=".//Rotulo">
                <xsl:call-template name="RotuloEmAspas"/>
            </xsl:for-each>
            <xsl:choose>
                <xsl:when test="not(empty(descendant::Omissis))">
                    <xsl:text>(Omissis suprimido)</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>(Suprimido).</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="$fechaAspasSimples">&#x2019; </xsl:if
            ><xsl:if test="$fechaAspas">&#x201D;</xsl:if>
        </fo:block>
    </xsl:template>

    <xsl:template match="Parte|Livro|Titulo|Capitulo|Secao|Subsecao" mode="aspas">
        <xsl:apply-templates mode="aspasAgrupador" />
    </xsl:template>

    <xsl:template match="Rotulo" mode="aspasAgrupador">
        <fo:inline text-align="center">
            <xsl:apply-templates mode="aspas" />
        </fo:inline>
    </xsl:template>

    <xsl:template match="*" mode="aspas">
        <fo:inline color="red">
            ERRO
            <xsl:value-of select="name()" />
        </fo:inline>
    </xsl:template>

    <!-- templates que tratam da parte html -->

    <xsl:template match="p">
        <fo:block space-after=".35cm">
            <xsl:call-template name="Trata-style"/>
            <xsl:apply-templates />
        </fo:block>
    </xsl:template>
    <xsl:template match="span" mode="#all">
        <fo:inline>
            <xsl:call-template name="Trata-style"/>
            <xsl:apply-templates />
        </fo:inline>
    </xsl:template>
    <xsl:template match="b" mode="#all">
        <fo:inline font-weight="bold">
            <xsl:apply-templates />
        </fo:inline>
    </xsl:template>
    <xsl:template match="i" mode="#all">
        <fo:inline font-style="italic">
            <xsl:apply-templates />
        </fo:inline>
    </xsl:template>
    <xsl:template match="u" mode="#all">
        <fo:inline text-decoration="underline">
            <xsl:apply-templates />
        </fo:inline>
    </xsl:template>
    <xsl:template match="sup" mode="#all">
        <fo:inline vertical-align="super">
            <xsl:if test="$isPdf">
                <xsl:attribute name="font-size">80%</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates />
        </fo:inline>
    </xsl:template>
    <xsl:template match="sub" mode="#all">
        <fo:inline vertical-align="sub">
            <xsl:if test="$isPdf">
                <xsl:attribute name="font-size">80%</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates />
        </fo:inline>
    </xsl:template>

    <!-- Transoforma atributos CSS do atributo style em atributos XSL-FO -->
    <xsl:template name="Trata-style">

        <xsl:for-each select="tokenize(@style, ';')">

            <xsl:variable name="atributo" select="."/>

            <xsl:if test="contains($atributo, ':')">
                <xsl:variable name="nome" select="normalize-space(substring-before($atributo, ':'))"/>
                <xsl:variable name="valor" select="normalize-space(substring-after($atributo, ':'))"/>
                <xsl:if test="$nome = 'text-align' or $nome = 'font-size'">
                    <xsl:attribute name="{$nome}" select="$valor"/>

                    <xsl:if test="$nome = 'text-align' and $valor = 'justify'">
                        <xsl:attribute name="text-indent" select="$text-indent-default"/>
                    </xsl:if>

                </xsl:if>
            </xsl:if>

        </xsl:for-each>

    </xsl:template>
    
    <xsl:template name="Rodape">
    
    	<xsl:if test="Emenda/Proposicao/@identificacaoTexto">
    		<fo:block font-size="10pt" border-top="solid 1px black" line-height="12pt" padding-before="2pt">
		        <xsl:value-of select="Emenda/Proposicao/@identificacaoTexto"/>
    		</fo:block>
    	</xsl:if>
    
    </xsl:template>

</xsl:stylesheet>
