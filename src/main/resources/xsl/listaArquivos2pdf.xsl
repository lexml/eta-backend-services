<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:java="http://saxon.sf.net/java-type"
     xmlns:sdf="java:java.text.SimpleDateFormat"
     xmlns:date="java:java.util.Date"
     xsi:schemaLocation="http://www.w3.org/1999/XSL/Format ../xsd/fop.xsd"
     exclude-result-prefixes="java fo">

    <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="no" xml:space="default"
        encoding="utf-8"/>

    <xsl:param name="imagem" select="." />
	<xsl:param name="nomeTipoDocumento" select="." />
    
    <xsl:strip-space elements="*" />

    <xsl:template match="ListaArquivos">
        <fo:root language="pt-BR">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="simpleA4-1Col-right" page-height="21cm" page-width="29.7cm" margin-top="10mm" margin-bottom="10mm"
                    margin-left="18mm" margin-right="12mm">
                    <fo:region-body margin-top="25mm" margin-bottom="10mm" />
                    <fo:region-before extent="2cm" region-name="Cabecalho" />
                    <fo:region-after extent="5mm" region-name="rodape" />
                </fo:simple-page-master>
                <fo:page-sequence-master master-name="master-simpleA4-1Col">
                    <fo:repeatable-page-master-alternatives>
                        <fo:conditional-page-master-reference master-reference="simpleA4-1Col-right" />
                    </fo:repeatable-page-master-alternatives>
                </fo:page-sequence-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="master-simpleA4-1Col" font-family="Times Roman" text-align="justify">
                <fo:static-content flow-name="Cabecalho">
                    <fo:block>
                        <fo:table table-layout="fixed" width="100%">
                            <fo:table-column column-width="20mm" />
                            <fo:table-column column-width="210mm" />
                            <fo:table-column column-width="30mm" />
                            <fo:table-body text-align="start" vertical-align="top">
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block>
                                            <fo:external-graphic src="file:///{$imagem}" width="15mm" height="15mm" content-width="scale-to-fit" />
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block font-size="12pt" font-weight="bold">
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell>
                                        <fo:block font-size="11pt">
                                            Data:
                                            <xsl:value-of select="sdf:format(sdf:new('dd/MM/yyyy'), date:new())"/>
                                        </fo:block>
                                        <fo:block font-size="11pt">
                                            Hora:
                                            <xsl:value-of
                                                select="sdf:format(sdf:new('HH:mm'), date:new())" />
                                        </fo:block>
                                        <fo:block font-size="11pt">
                                            Pág.:
                                            <fo:page-number />
                                            de
                                            <fo:page-number-citation ref-id="last-page" />
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-body>
                        </fo:table>
                        <fo:table table-layout="fixed" width="100%">
                            <fo:table-body>
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block border-top-width="1pt" border-top-style="solid" space-before="3pt"
                                            font-size="12" text-align="start">
                                            Lista de <xsl:value-of select="$nomeTipoDocumento"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                    <fo:block border-bottom-width="1pt" border-bottom-style="solid" space-before="3pt" />
                </fo:static-content>
                <fo:static-content flow-name="rodape">
                    <fo:block border-top-width="1pt" border-top-style="solid" space-before="2pt" font-size="10" text-align="center" />
                </fo:static-content>
                <fo:flow flow-name="xsl-region-body" font-size="9pt">
                    <xsl:call-template name="TrataArquivos" />
                    <fo:block id="last-page" />
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
    <xsl:template name="TrataArquivos">
        <fo:block>
            <fo:table table-layout="fixed" width="100%">
                <fo:table-column column-width="25%" />
                <fo:table-column column-width="25%" />
                <fo:table-column column-width="25%" />
                <fo:table-column column-width="25%" />
                <fo:table-body text-align="start" vertical-align="top">
                    <fo:table-row>
                        <fo:table-cell>
                            <fo:block font-size="12pt" font-weight="bold">Nome</fo:block>
                        </fo:table-cell>
                        <fo:table-cell>
                            <fo:block font-size="12pt" font-weight="bold">Data</fo:block>
                        </fo:table-cell>
                        <fo:table-cell>
                            <fo:block font-size="12pt" font-weight="bold">Descrição</fo:block>
                        </fo:table-cell>
                        <fo:table-cell>
                            <fo:block font-size="12pt" font-weight="bold">Elaborador</fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>
        <xsl:for-each select="Arquivo">
            <fo:block>
                <fo:table table-layout="fixed" width="100%">
                    <fo:table-column column-width="25%" />
                    <fo:table-column column-width="25%" />
                    <fo:table-column column-width="25%" />
                    <fo:table-column column-width="25%" />
                    <fo:table-body text-align="start" vertical-align="top">
                        <fo:table-row>
                            <fo:table-cell>
                                <fo:block>
                                    <xsl:value-of select="Nome" />
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block>
                                    <xsl:value-of select="Data" />
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block>
                                    <xsl:value-of select="Descricao" />
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell>
                                <fo:block>
                                    <xsl:value-of select="Elaborador" />
                                </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </fo:table-body>
                </fo:table>
            </fo:block>
        </xsl:for-each>
    </xsl:template>
    <!-- grupos de atributos -->
    <xsl:attribute-set name="bordaPrincipal">
        <xsl:attribute name="padding">3mm</xsl:attribute>
        <xsl:attribute name="border-width">1pt</xsl:attribute>
        <xsl:attribute name="border-style">solid</xsl:attribute>
        <xsl:attribute name="space-before">3mm</xsl:attribute>
        <xsl:attribute name="margin-left">4mm</xsl:attribute>
        <xsl:attribute name="margin-right">2mm</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="titulo0">
        <xsl:attribute name="font-size">16pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="tituloQuadro">
        <xsl:attribute name="font-size">11pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="color">navy</xsl:attribute>
        <xsl:attribute name="text-indent">-2mm</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="titulo2">
        <xsl:attribute name="font-size">12pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="texto">
        <xsl:attribute name="font-size">11pt</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="textoBold">
        <xsl:attribute name="font-size">11pt</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:attribute-set>
</xsl:stylesheet>
