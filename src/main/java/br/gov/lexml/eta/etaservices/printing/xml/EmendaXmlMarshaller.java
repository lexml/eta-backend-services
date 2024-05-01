package br.gov.lexml.eta.etaservices.printing.xml;

import static br.gov.lexml.eta.etaservices.emenda.TipoColegiado.COMISSAO;

import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.text.StringEscapeUtils;

import br.gov.lexml.eta.etaservices.emenda.Anexo;
import br.gov.lexml.eta.etaservices.emenda.Autoria;
import br.gov.lexml.eta.etaservices.emenda.ColegiadoApreciador;
import br.gov.lexml.eta.etaservices.emenda.ColegiadoAutor;
import br.gov.lexml.eta.etaservices.emenda.ComandoEmenda;
import br.gov.lexml.eta.etaservices.emenda.ComandoEmendaTextoLivre;
import br.gov.lexml.eta.etaservices.emenda.ComponenteEmendado;
import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaAdicionado;
import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaModificado;
import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaSuprimido;
import br.gov.lexml.eta.etaservices.emenda.DispositivosEmenda;
import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.emenda.Epigrafe;
import br.gov.lexml.eta.etaservices.emenda.ItemComandoEmenda;
import br.gov.lexml.eta.etaservices.emenda.NotaRodape;
import br.gov.lexml.eta.etaservices.emenda.OpcoesImpressao;
import br.gov.lexml.eta.etaservices.emenda.Parlamentar;
import br.gov.lexml.eta.etaservices.emenda.RefProposicaoEmendada;
import br.gov.lexml.eta.etaservices.emenda.Revisao;
import br.gov.lexml.eta.etaservices.emenda.SubstituicaoTermo;
import br.gov.lexml.eta.etaservices.printing.json.NotaRodapePojo;
import br.gov.lexml.eta.etaservices.printing.json.RevisaoElementoPojo;
import br.gov.lexml.eta.etaservices.printing.json.RevisaoJustificativaPojo;
import br.gov.lexml.eta.etaservices.printing.json.RevisaoTextoLivrePojo;

public class EmendaXmlMarshaller {
	
    public static final String FECHA_TAG_SEM_CONTEUDO = "/>\n";

    public String toXml(Emenda emenda) {
    	try {    		
	        final StringBuilder sb = new StringBuilder();
	        geraCabecalhoEmenda(emenda, sb);
	        geraMetadados(emenda, sb);
	        geraProposicao(emenda.getProposicao(), sb);
	        geraColegiado(emenda.getColegiadoApreciador(), sb);
	        geraEpigrafe(emenda.getEpigrafe(), sb);
	        geraComponentes(emenda.getComponentes(), sb);
	        geraComandoEmendaTextoLivre(emenda.getComandoEmendaTextoLivre(), sb);
	        geraComandoEmenda(emenda.getComandoEmenda(), sb);
            geraSubstituicaoTermo(emenda.getSubstituicaoTermo(), sb);
	        geraAnexos(emenda.getAnexos(), sb);
	        geraJustificativa(emenda.getJustificativa(), sb);
	        geraJustificativaAntesRevisao(emenda.getJustificativaAntesRevisao(), sb);
	        geraAutoria(emenda.getAutoria(), sb);
	        geraOpcoesImpressao(emenda.getOpcoesImpressao(), sb);
	        geraRevisoes(emenda.getRevisoes(), sb);
	        geraNotasRodape(emenda.getNotasRodape(), sb);
            geraEmendarTextoSubstitutivo(emenda.getEmendarTextoSubstitutivo(), sb);

			sb.append("</Emenda>");
			return sb.toString();
		}
    	catch(Exception e) {
    		throw new RuntimeException("Falha ao gerar XML", e);
    	}
    }

	private void geraCabecalhoEmenda(Emenda emenda, StringBuilder sb) {
        sb.append("<Emenda versaoFormatoArquivo=\"1.0\" local=\"").append(emenda.getLocal()).append("\"");

        if (emenda.getData() != null) {
            sb.append(" data=\"").append(DateTimeFormatter.ISO_DATE.format(emenda.getData()))
                    .append("\"");
        }
        sb.append(">\n");
    }

    /**
     * Generates the metadata section of the emenda XML file
     *
     * @param emenda the emenda object
     * @param sb     StringBuilder
     */
    private void geraMetadados(Emenda emenda, StringBuilder sb) {
        sb.append("  <Metadados>\n")
                .append("    <DataUltimaModificacao>")
                .append(emenda.getDataUltimaModificacao())
                .append("</DataUltimaModificacao>\n")
                .append("    <Aplicacao>")
                .append(emenda.getAplicacao())
                .append("</Aplicacao>\n")
                .append("    <VersaoAplicacao>")
                .append(emenda.getVersaoAplicacao())
                .append("</VersaoAplicacao>\n")
                .append("    <ModoEdicao>")
                .append(emenda.getModoEdicao())
                .append("</ModoEdicao>\n");

        emenda.getMetadados().forEach((k, v) ->
                sb.append("    <")
                        .append(k)
                        .append(">")
                        .append(v)
                        .append("</")
                        .append(k)
                        .append(">\n"));

        sb.append("  </Metadados>\n");
    }

    private void geraProposicao(RefProposicaoEmendada proposicao, StringBuilder sb) {
        sb.append("  <Proposicao ")
                .append("urn=\"")
                .append(proposicao.getUrn())
                .append("\" ")
                .append("sigla=\"")
                .append(proposicao.getSigla())
                .append("\" ")
                .append("numero=\"")
                .append(proposicao.getNumero())
                .append("\" ")
                .append("ano=\"")
                .append(proposicao.getAno())
                .append("\" ")
                .append("ementa=\"")
                .append(StringEscapeUtils.escapeXml10(htmlAttribute2txt(proposicao.getEmenta())))
                .append("\" ")
                .append("identificacaoTexto=\"")
                .append(StringEscapeUtils.escapeXml10(proposicao.getIdentificacaoTexto()))
                .append("\" ")
                .append(FECHA_TAG_SEM_CONTEUDO);
    }

	private void geraColegiado(ColegiadoApreciador colegiado, StringBuilder sb) {
        sb.append("  <ColegiadoApreciador ")
                .append("siglaCasaLegislativa=\"")
                .append(colegiado
                        .getSiglaCasaLegislativa())
                .append("\" ")
                .append("tipoColegiado=\"")
                .append(colegiado.getTipoColegiado().getDescricao())
                .append("\"");

        if (colegiado.getTipoColegiado() == COMISSAO) {
            sb.append(" siglaComissao=\"")
                    .append(colegiado.getSiglaComissao())
                    .append("\"");
        }

        sb.append(" />\n");
    }

    private void geraSubstituicaoTermo(SubstituicaoTermo substituicaoTermo, StringBuilder sb) {
        if (substituicaoTermo != null) {
            sb.append("  <SubstituicaoTermo ")
                    .append("tipo=\"")
                    .append(substituicaoTermo.getTipo().getDescricao())
                    .append("\" ")
                    .append("termo=\"")
                    .append(StringEscapeUtils.escapeXml10(substituicaoTermo.getTermo()))
                    .append("\" ")
                    .append("novoTermo=\"")
                    .append(StringEscapeUtils.escapeXml10(substituicaoTermo.getNovoTermo()))
                    .append("\" ")
                    .append("flexaoGenero=\"")
                    .append(substituicaoTermo.isFlexaoGenero())
                    .append("\" ")
                    .append("flexaoNumero=\"")
                    .append(substituicaoTermo.isFlexaoNumero())
                    .append("\" ")
                    .append(FECHA_TAG_SEM_CONTEUDO);
        }
    }

    private void geraEpigrafe(Epigrafe epigrafe, StringBuilder sb) {
        sb.append("  <Epigrafe ")
                .append("texto=\"")
                .append(StringEscapeUtils.escapeXml10(epigrafe.getTexto()).trim())
                .append("\" ");

        if (epigrafe.getComplemento() != null) {
            sb.append("complemento=\"")
                    .append(StringEscapeUtils.escapeXml10(epigrafe.getComplemento()))
                    .append("\" ");
        }

        sb.append("/>\n");
    }

    private void geraComponentes(List<? extends ComponenteEmendado> componentes, StringBuilder sb) {
        componentes.forEach(componente -> {
            sb.append("  <Componente ")
                    .append("urn=\"")
                    .append(componente.getUrn())
                    .append("\" ")
                    .append("articulado=\"")
                    .append(componente.isArticulado())
                    .append("\" ");

            if (componente.getTituloAnexo() != null) {
                sb.append("tituloAnexo=\"")
                        .append(StringEscapeUtils.escapeXml10(componente.getTituloAnexo()))
                        .append("\" ");
            }

            if (componente.getRotuloAnexo() != null) {
                sb.append("rotuloAnexo=\"")
                        .append(StringEscapeUtils.escapeXml10(componente.getRotuloAnexo()))
                        .append("\" ");
            }

            sb.append(">\n");
            geraDispositivos(componente.getDispositivos(), sb);
            sb.append("  </Componente>\n");
        });
    }

    private void geraAnexos(List<? extends Anexo> anexos, StringBuilder sb) {
    	if(anexos != null) {    		
    		anexos.forEach(anexo -> {
    			sb.append("  <Anexo ")
    			.append("nomeArquivo=\"")
    			.append(anexo.getNomeArquivo())
    			.append("\" ")
    			.append("base64=\"")
    			.append(anexo.getBase64())
    			.append("\" ")
    			.append(FECHA_TAG_SEM_CONTEUDO);
    		});
    	}
    }

    private void geraDispositivos(DispositivosEmenda dispositivos, StringBuilder sb) {
        sb.append("    <Dispositivos>\n");
        dispositivos.getDispositivosSuprimidos().forEach(suprimido -> geraDispositivosSuprimidos(suprimido, sb));
        dispositivos.getDispositivosModificados().forEach(modificado -> geraDispositivosModificados(modificado, sb));
        dispositivos.getDispositivosAdicionados().forEach(adicionado -> geraDispositivosAdicionados(adicionado, sb));
        sb.append("    </Dispositivos>\n");
    }

    private void geraDispositivosSuprimidos(DispositivoEmendaSuprimido suprimido, StringBuilder sb) {
        sb.append("      <DispositivoSuprimido ")
                .append("tipo=\"")
                .append(suprimido.getTipo())
                .append("\" ")
                .append("id=\"")
                .append(suprimido.getId())
                .append("\" ")
                .append("rotulo=\"")
                .append(suprimido.getRotulo())
                .append("\" ");
        
        if (suprimido.getUrnNormaAlterada() != null) {
            sb.append("xml:base=\"")
                    .append(suprimido.getUrnNormaAlterada())
                    .append("\" ");
        }
        
        sb.append(FECHA_TAG_SEM_CONTEUDO);
    }

    private void geraDispositivosModificados(DispositivoEmendaModificado modificado, StringBuilder sb) {
        sb.append("      <DispositivoModificado ")
                .append("tipo=\"")
                .append(modificado.getTipo())
                .append("\" ")
                .append("id=\"")
                .append(modificado.getId())
                .append("\" ")
                .append("rotulo=\"")
                .append(modificado.getRotulo())
                .append("\" ");
        if (modificado.isTextoOmitido() != null && modificado.isTextoOmitido()) {
            sb.append("textoOmitido=\"")
                    .append(modificado.isTextoOmitido())
                    .append("\" ");
        }
        if (modificado.isAbreAspas() != null && modificado.isAbreAspas()) {
            sb.append("abreAspas=\"")
                    .append(modificado.isAbreAspas())
                    .append("\" ");
        }
        if (modificado.isFechaAspas() != null && modificado.isFechaAspas()) {
            sb.append("fechaAspas=\"")
                    .append(modificado.isFechaAspas())
                    .append("\" ");
        }
        if (modificado.getNotaAlteracao() != null) {
            sb.append("notaAlteracao=\"")
                    .append(modificado.getNotaAlteracao())
                    .append("\" ");
        }
        
        if (modificado.getUrnNormaAlterada() != null) {
            sb.append("xml:base=\"")
                    .append(modificado.getUrnNormaAlterada())
                    .append("\" ");
        }
        
        sb.append(">\n");
        sb.append("        <Texto>")
                .append(modificado.getTexto().trim())
                .append("</Texto>\n");
        sb.append("      </DispositivoModificado> ");

    }

    private void geraDispositivosAdicionados(DispositivoEmendaAdicionado adicionado, StringBuilder sb) {
        sb.append("      <DispositivoAdicionado ");

        if (adicionado.isOndeCouber() != null) {
            sb.append("ondeCouber=\"")
                    .append(adicionado.isOndeCouber())
                    .append("\" ");
        }

        if (adicionado.getIdPai() != null) {
            sb.append("idPai=\"")
                    .append(adicionado.getIdPai())
                    .append("\" ");
        }

        if (adicionado.getIdIrmaoAnterior() != null) {
            sb.append("idIrmaoAnterior=\"")
                    .append(adicionado.getIdIrmaoAnterior())
                    .append("\" ");
        }

        if (adicionado.getIdPosicaoAgrupador() != null) {
            sb.append("idPosicaoAgrupador=\"")
                    .append(adicionado.getIdPosicaoAgrupador())
                    .append("\" ");
        }

        sb.append(">\n");

        geraFilhosDispositivosAdicionados(adicionado, sb);

        sb.append("      </DispositivoAdicionado>\n");

    }

    private void geraFilhosDispositivosAdicionados(DispositivoEmendaAdicionado filho, StringBuilder sb) {
        sb.append("        <");
        sb.append(filho.getTipo());
        
        sb.append(" id=\"")
        	.append(filho.getId())
        	.append("\" ");

        if (filho.getUuid2() != null) {
            sb.append("uuid2=\"")
                    .append(filho.getUuid2())
                    .append("\" ");
        }

        if (filho.getUrnNormaAlterada() != null) {
            sb.append("xml:base=\"")
                    .append(filho.getUrnNormaAlterada())
                    .append("\" ");
        }

        // TODO - Avaliar uso do true/false ou s/n por estar na estrutura lexml (não é atributo lexml)
        if (filho.isExisteNaNormaAlterada() != null) {
            sb.append("existeNaNormaAlterada=\"")
                    .append(filho.isExisteNaNormaAlterada())
                    .append("\" ");
        }
        
        if (filho.isTextoOmitido() != null && filho.isTextoOmitido()) {
            sb.append("textoOmitido=\"s\" ");
        }
        if (filho.isAbreAspas() != null && filho.isAbreAspas()) {
            sb.append("abreAspas=\"s\" ");
        }
        if (filho.isFechaAspas() != null && filho.isFechaAspas()) {
            sb.append("fechaAspas=\"s\" ");
        }
        if (filho.getNotaAlteracao() != null) {
            sb.append("notaAlteracao=\"")
                    .append(filho.getNotaAlteracao())
                    .append("\" ");
        }
        
        if (filho.getRotulo() == null && (filho.getTexto() == null || filho.getTipo().equals("Omissis")) &&
                (filho.getFilhos() == null || filho.getFilhos().isEmpty())) {
            sb.append(FECHA_TAG_SEM_CONTEUDO);
        } else {
            sb.append(">\n");
            if (filho.getRotulo() != null) {
                sb.append("          <Rotulo>")
                        .append(filho.getRotulo())
                        .append("</Rotulo>\n");
            }
            if (filho.getTexto() != null) {
                sb.append("          <p>")
                        .append(filho.getTexto())
                        .append("</p>\n");
            }

            filho.getFilhos().forEach(filhoFilho -> geraFilhosDispositivosAdicionados(filhoFilho, sb));

            sb.append("        </");
            sb.append(filho.getTipo());
            sb.append(">\n");
        }
    }


    private void geraComandoEmenda(ComandoEmenda comandoEmenda, StringBuilder sb) {
        sb.append("  <ComandoEmenda>\n");

        if (comandoEmenda.getCabecalhoComum() != null) {
            sb.append("    <CabecalhoComum>")
                    .append(comandoEmenda.getCabecalhoComum())
                    .append("</CabecalhoComum>\n");
        }

        comandoEmenda.getComandos().forEach(comando -> geraComando(comando, sb));
        sb.append("  </ComandoEmenda>\n");
    }

    private void geraComandoEmendaTextoLivre(ComandoEmendaTextoLivre comandoEmendaTextoLivre, StringBuilder sb) {
    	if(comandoEmendaTextoLivre != null) {
    		sb.append("  <ComandoEmendaTextoLivre");
    		
    		if(comandoEmendaTextoLivre.getMotivo() != null) {
    			sb.append(" motivo=\"")
	    			.append(comandoEmendaTextoLivre.getMotivo())
	    			.append("\" ");    			
    		}
    		
    		sb.append(">");
    		sb.append(comandoEmendaTextoLivre.getTexto() != null ? StringEscapeUtils.escapeXml10(comandoEmendaTextoLivre.getTexto()) : "");
    		sb.append("  </ComandoEmendaTextoLivre>\n");
    		
    		if(comandoEmendaTextoLivre.getTextoAntesRevisao() != null) {
    			sb.append("  <ComandoEmendaTextoLivreAntesRevisao>");
        		sb.append(StringEscapeUtils.escapeXml10(comandoEmendaTextoLivre.getTextoAntesRevisao()));
        		sb.append("  </ComandoEmendaTextoLivreAntesRevisao>\n");	
    		}
    		
    	}
    }

    private void geraComando(ItemComandoEmenda comando, StringBuilder sb) {
        sb.append("    <ItemComandoEmenda>\n");

        if (comando.getRotulo() != null) {
            sb.append("      <Rotulo>")
                    .append(comando.getRotulo())
                    .append("</Rotulo>\n");
        }

        sb.append("      <Cabecalho>")
                .append(comando.getCabecalho())
                .append("</Cabecalho>\n");

        if (comando.getCitacao() != null) {
            sb.append("      <Citacao>")
                    .append(comando.getCitacao())
                    .append("</Citacao>\n");
        }

        if (comando.getComplemento() != null) {
            sb.append("      <Complemento>")
                    .append(comando.getComplemento())
                    .append("</Complemento>\n");
        }
        
        sb.append("    </ItemComandoEmenda>\n");
    }

    private void geraJustificativa(String justificativa, StringBuilder sb) {
    	if(justificativa != null) {
			sb.append("  <Justificativa>")
	    		.append(StringEscapeUtils.escapeXml10(justificativa))
	    		.append("</Justificativa>\n");    		
    	}
    }
    
    private void geraJustificativaAntesRevisao(String justificativa, StringBuilder sb) {
    	if(justificativa != null) {
    		sb.append("  <JustificativaAntesRevisao>")
	    		.append(StringEscapeUtils.escapeXml10(justificativa))
	    		.append("</JustificativaAntesRevisao>\n");    		
    	}
    }

    private void geraAutoria(Autoria autoria, StringBuilder sb) {
        sb.append("  <Autoria ")
                .append(" tipo=\"")
                .append(autoria.getTipo().getDescricao())
                .append("\" imprimirPartidoUF=\"")
                .append(autoria.isImprimirPartidoUF())
                .append("\" quantidadeAssinaturasAdicionaisDeputados=\"")
                .append(autoria.getQuantidadeAssinaturasAdicionaisDeputados())
                .append("\" quantidadeAssinaturasAdicionaisSenadores=\"")
                .append(autoria.getQuantidadeAssinaturasAdicionaisSenadores())
                .append("\" >\n");

        autoria.getParlamentares().forEach(autor -> geraParlamentar(autor, sb));

        if (autoria.getColegiado() != null) {
            geraColegiadoAutor(autoria.getColegiado(), sb);
        }

        sb.append("  </Autoria>\n");


    }

    private void geraParlamentar(Parlamentar autor, StringBuilder sb) {
        sb.append("    <Parlamentar ")
                .append(" identificacao=\"")
                .append(autor.getIdentificacao())
                .append("\" ")
                .append("nome=\"")
                .append(StringEscapeUtils.escapeXml10(autor.getNome()))
                .append("\" ")
                .append("tratamento=\"")
                .append(autor.getTratamento())
                .append("\" ")
                .append("siglaPartido=\"")
                .append(autor.getSiglaPartido())
                .append("\" ")
                .append("siglaUF=\"")
                .append(autor.getSiglaUF())
                .append("\" ")
                .append("siglaCasaLegislativa=\"")
                .append(autor.getSiglaCasaLegislativa())
                .append("\" ")
                .append("sexo=\"")
                .append(autor.getSexo())
                .append("\" ");

        if (autor.getCargo() != null) {
            sb.append("cargo=\"")
                    .append(StringEscapeUtils.escapeXml10(autor.getCargo()))
                    .append("\" ");
        }
        sb.append(FECHA_TAG_SEM_CONTEUDO);
    }

    private void geraColegiadoAutor(ColegiadoAutor colegiado, StringBuilder sb) {
        sb.append("    <Colegiado ")
                .append("identificacao=\"")
                .append(colegiado.getIdentificacao())
                .append("\" ")
                .append("nome=\"")
                .append(StringEscapeUtils.escapeXml10(colegiado.getNome()))
                .append("\" ")
                .append("sigla=\"")
                .append(colegiado.getSigla())
                .append("\" ")
                .append(FECHA_TAG_SEM_CONTEUDO);
    }

    private void geraOpcoesImpressao(OpcoesImpressao opcoesImpressao, StringBuilder sb) {
        sb.append("  <OpcoesImpressao ")
                .append(" imprimirBrasao=\"")
                .append(opcoesImpressao.isImprimirBrasao())
                .append("\" ");

        if (opcoesImpressao.getTextoCabecalho() != null) {
            sb.append(" textoCabecalho=\"")
                    .append(StringEscapeUtils.escapeXml10(opcoesImpressao.getTextoCabecalho()))
                    .append("\" ");
        }
        
        sb.append(" tamanhoFonte=\"")
        .append(opcoesImpressao.getTamanhoFonte())
        .append("\" ");
        
        sb.append(" reduzirEspacoEntreLinhas=\"")
                .append(opcoesImpressao.isReduzirEspacoEntreLinhas())
                .append("\" ")
                .append(FECHA_TAG_SEM_CONTEUDO);


    }

    private static String htmlAttribute2txt(String html) {
    	if (html == null) {
    		return null;
    	}
		return html.replaceAll("(?i)<br/?>", " ")
				.replaceAll("<.+?>", "")
				.replaceAll("\\s{2,}", " ")
				.trim();
	}

    private void geraRevisoes(List<? extends Revisao> revisoes, StringBuilder sb) throws Exception {
    	
    	if(revisoes == null || revisoes.isEmpty()) {
    		return;
    	}
    	
    	JAXBContext jcRevisaoJustificativa = JAXBContext.newInstance(RevisaoJustificativaPojo.class);    	
    	Marshaller jmRevisaoJustificativa = jcRevisaoJustificativa.createMarshaller();
    	jmRevisaoJustificativa.setProperty(Marshaller.JAXB_FRAGMENT, true);
    	jmRevisaoJustificativa.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    	JAXBContext jcRevisaoTextoLivre = JAXBContext.newInstance(RevisaoTextoLivrePojo.class);    	
    	Marshaller jmRevisaoTextoLivre = jcRevisaoTextoLivre.createMarshaller();
    	jmRevisaoTextoLivre.setProperty(Marshaller.JAXB_FRAGMENT, true);
    	jmRevisaoTextoLivre.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    	JAXBContext jcRevisaoElemento = JAXBContext.newInstance(RevisaoElementoPojo.class);    	
    	Marshaller jmRevisaoElemento = jcRevisaoElemento.createMarshaller();
    	jmRevisaoElemento.setProperty(Marshaller.JAXB_FRAGMENT, true);
    	jmRevisaoElemento.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    	
    	sb.append("<Revisoes>\n");

    	StringBuilderWriter sw = new StringBuilderWriter(sb);
    	for(Revisao r: revisoes) { 
    		if (r instanceof RevisaoElementoPojo) {    			
    			jmRevisaoElemento.marshal(r, sw);
    		}
    		else if (r instanceof RevisaoJustificativaPojo) {
    			jmRevisaoJustificativa.marshal(r, sw);
    		}
    		else if (r instanceof RevisaoTextoLivrePojo) {
    			jmRevisaoTextoLivre.marshal(r, sw);
    		}
        	sb.append("\n");
    	}
    	
    	sb.append("</Revisoes>\n");
	}
    
    private void geraNotasRodape(List<? extends NotaRodape> notasRodape, StringBuilder sb) throws Exception {
    	if(notasRodape == null || notasRodape.isEmpty()) {
    		return;
    	}
    	
    	JAXBContext jcNotaRodape = JAXBContext.newInstance(NotaRodapePojo.class);    	
    	Marshaller jmNotaRodape = jcNotaRodape.createMarshaller();
    	jmNotaRodape.setProperty(Marshaller.JAXB_FRAGMENT, true);
    	jmNotaRodape.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    	sb.append("<NotasRodape>\n");

    	StringBuilderWriter sw = new StringBuilderWriter(sb);
    	for(NotaRodape nr: notasRodape) {
    		jmNotaRodape.marshal(nr, sw);
        	sb.append("\n");
    	}
    	
    	sb.append("</NotasRodape>\n");    	
    }

    private void geraEmendarTextoSubstitutivo(Boolean emendarTextoSubstitutivo, StringBuilder sb) {
        if(emendarTextoSubstitutivo != null) {
            sb.append("  <EmendarTextoSubstitutivo valor=\"")
                    .append(emendarTextoSubstitutivo ? 's' : 'n')
                    .append("\"></EmendarTextoSubstitutivo>\n");
        }
    }

//    public static void main(String[] args) {
//		System.out.println(html2txt("teste  <a href=\"...\">link</a> <br>Nova linha."));
//	}

}
