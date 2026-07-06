package br.gov.lexml.eta.etaservices.printing.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

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
import br.gov.lexml.eta.etaservices.emenda.TipoColegiado;
import br.gov.lexml.eta.etaservices.printing.json.NotaRodapePojo;
import br.gov.lexml.eta.etaservices.printing.json.RevisaoElementoPojo;
import br.gov.lexml.eta.etaservices.printing.json.RevisaoJustificativaPojo;
import br.gov.lexml.eta.etaservices.printing.json.RevisaoTextoLivrePojo;
import br.gov.lexml.eta.etaservices.util.XMLUtil;

public class EmendaXmlMarshaller {

    public String toXml(Emenda emenda) {
        try {
            Document document = DocumentHelper.createDocument();
            Element element = geraCabecalhoEmenda(emenda, document);
            geraMetadados(emenda, element);
            geraProposicao(emenda.getProposicao(), element);
            geraColegiado(emenda.getColegiadoApreciador(), element);
            geraEpigrafe(emenda.getEpigrafe(), element);
            geraComponentes(emenda.getComponentes(), element);
            geraComandoEmendaTextoLivre(emenda.getComandoEmendaTextoLivre(), element);
            geraComandoEmenda(emenda.getComandoEmenda(), element);
            geraSubstituicaoTermo(emenda.getSubstituicaoTermo(), element);
            geraAnexos(emenda.getAnexos(), element);
            geraJustificativa(emenda.getJustificativa(), element);
            geraJustificativaAntesRevisao(emenda.getJustificativaAntesRevisao(), element);
            geraAutoria(emenda.getAutoria(), element);
            geraOpcoesImpressao(emenda.getOpcoesImpressao(), element);
            geraRevisoes(emenda.getRevisoes(), element);
            geraNotasRodape(emenda.getNotasRodape(), element);
            geraPendenciasPreenchimento(emenda.getPendenciasPreenchimento(), element);

            return toString(element);
        } catch(Exception e) {
            throw new RuntimeException("Falha ao gerar XML", e);
        }
    }

    protected Element geraCabecalhoEmenda(Emenda emenda, Document document) {
        Element element = document.addElement("Emenda");

        element.addAttribute("versaoFormatoArquivo", "1.0");
        element.addAttribute("local", emenda.getLocal());

        if (emenda.getData() != null) {
            element.addAttribute("data", DateTimeFormatter.ISO_DATE.format(emenda.getData()));
        }

        return element;
    }

    protected void geraMetadados(Emenda emenda, Element emendaElement) {
        Element element = emendaElement.addElement("Metadados");

        element.addElement("DataUltimaModificacao").setText(emenda.getDataUltimaModificacao().toString());
        element.addElement("Aplicacao").setText(emenda.getAplicacao());
        element.addElement("VersaoAplicacao").setText(emenda.getVersaoAplicacao());
        element.addElement("ModoEdicao").setText(emenda.getModoEdicao().toString());

        emenda.getMetadados().forEach((k, v) ->
                element.addElement(k).setText(v.toString())
        );
    }

    protected void geraProposicao(RefProposicaoEmendada proposicao, Element emendaElement) {
        Element element = emendaElement.addElement("Proposicao");

        element.addAttribute("urn", proposicao.getUrn());
        element.addAttribute("sigla", proposicao.getSigla());
        element.addAttribute("numero", proposicao.getNumero());
        element.addAttribute("ano", String.valueOf(proposicao.getAno()));
        element.addAttribute("ementa", html2txt(XMLUtil.sanitize(proposicao.getEmenta())));
        element.addAttribute("identificacaoTexto", proposicao.getIdentificacaoTexto());
        element.addAttribute("emendarTextoSubstitutivo", String.valueOf(proposicao.isEmendarTextoSubstitutivo()));
    }

    protected void geraColegiado(ColegiadoApreciador colegiado, Element emendaElement) {
        Element element = emendaElement.addElement("ColegiadoApreciador");

        element.addAttribute("siglaCasaLegislativa", colegiado.getSiglaCasaLegislativa().name());
        element.addAttribute("tipoColegiado", colegiado.getTipoColegiado().getDescricao());

        if (colegiado.getTipoColegiado() == TipoColegiado.COMISSAO ||
                colegiado.getTipoColegiado() == TipoColegiado.PLENARIO_VIA_COMISSAO) {
            element.addAttribute("siglaComissao", colegiado.getSiglaComissao());
        }
    }

    protected void geraSubstituicaoTermo(SubstituicaoTermo substituicaoTermo, Element emendaElement) {
        if (substituicaoTermo != null) {
            Element element = emendaElement.addElement("SubstituicaoTermo");

            element.addAttribute("tipo", substituicaoTermo.getTipo().getDescricao());
            element.addAttribute("termo", XMLUtil.sanitize(substituicaoTermo.getTermo()));
            element.addAttribute("novoTermo", XMLUtil.sanitize(substituicaoTermo.getNovoTermo()));
            element.addAttribute("flexaoGenero", String.valueOf(substituicaoTermo.isFlexaoGenero()));
            element.addAttribute("flexaoNumero", String.valueOf(substituicaoTermo.isFlexaoNumero()));
        }
    }

    protected void geraEpigrafe(Epigrafe epigrafe, Element emendaElement) {
        if (epigrafe != null) {
            Element element = emendaElement.addElement("Epigrafe");

            element.addAttribute("texto", epigrafe.getTexto().trim());

            if (epigrafe.getComplemento() != null) {
                element.addAttribute("complemento", epigrafe.getComplemento());
            }
        }
    }

    protected void geraComponentes(List<? extends ComponenteEmendado> componentes, Element emendaElement) {
        Element dispositivoElement;
        for (ComponenteEmendado componente : componentes) {
            Element componenteElement = emendaElement.addElement("Componente");

            componenteElement.addAttribute("urn", componente.getUrn());
            componenteElement.addAttribute("articulado", String.valueOf(componente.isArticulado()));

            if (componente.getTituloAnexo() != null) {
                componenteElement.addAttribute("tituloAnexo", componente.getTituloAnexo());
            }

            if (componente.getRotuloAnexo() != null) {
                componenteElement.addAttribute("rotuloAnexo", componente.getRotuloAnexo());
            }

            geraDispositivos(componente.getDispositivos(), componenteElement);
        }
    }

    protected void geraAnexos(List<? extends Anexo> anexos, Element emendaElement) {
        if (anexos != null) {
            Element anexosElement = emendaElement.addElement("Anexos");

            anexos.forEach(anexo -> {
                anexosElement.addElement("Anexo")
                        .addAttribute("nomeArquivo", anexo.getNomeArquivo())
                        .addAttribute("base64", anexo.getBase64());
            });
        }
    }

    protected void geraDispositivos(DispositivosEmenda dispositivos, Element componenteElement) {
        Element dispositivosElement = componenteElement.addElement("Dispositivos");
        dispositivos.getDispositivosSuprimidos().forEach(adicionado -> geraDispositivosSuprimidos(adicionado, dispositivosElement));
        dispositivos.getDispositivosModificados().forEach(adicionado -> geraDispositivosModificados(adicionado, dispositivosElement));
        dispositivos.getDispositivosAdicionados().forEach(adicionado -> geraDispositivosAdicionados(adicionado, dispositivosElement));
    }

    private void geraDispositivosSuprimidos(DispositivoEmendaSuprimido suprimido, Element dispositivosElement) {
        Element suprimidoElement = dispositivosElement.addElement("DispositivoSuprimido");

        suprimidoElement.addAttribute("tipo", suprimido.getTipo());
        suprimidoElement.addAttribute("id", suprimido.getId());
        suprimidoElement.addAttribute("rotulo", suprimido.getRotulo());

        if (suprimido.getUrnNormaAlterada() != null) {
            suprimidoElement.addAttribute("xml:base", suprimido.getUrnNormaAlterada());
        }

        suprimidoElement.addText("");
    }

    private void geraDispositivosModificados(DispositivoEmendaModificado modificado, Element dispositivosElement) {
        Element modificadoElement = dispositivosElement.addElement("DispositivoModificado");

        modificadoElement.addAttribute("tipo", modificado.getTipo());
        modificadoElement.addAttribute("id", modificado.getId());
        modificadoElement.addAttribute("rotulo", modificado.getRotulo());

        if (modificado.isTextoOmitido() != null && modificado.isTextoOmitido()) {
            modificadoElement.addAttribute("textoOmitido", String.valueOf(modificado.isTextoOmitido()));
        }

        if (modificado.isAbreAspas() != null && modificado.isAbreAspas()) {
            modificadoElement.addAttribute("abreAspas", String.valueOf(modificado.isAbreAspas()));
        }

        if (modificado.isFechaAspas() != null && modificado.isFechaAspas()) {
            modificadoElement.addAttribute("fechaAspas", String.valueOf(modificado.isFechaAspas()));
        }

        if (modificado.getNotaAlteracao() != null) {
            modificadoElement.addAttribute("notaAlteracao", modificado.getNotaAlteracao().toString());
        }

        if (modificado.getUrnNormaAlterada() != null) {
            modificadoElement.addAttribute("xml:base", modificado.getUrnNormaAlterada());
        }

        Element textoElement = modificadoElement.addElement("Texto");
        XMLUtil.setElementContentFromXmlFragment(textoElement, modificado.getTexto());
    }

    private void geraDispositivosAdicionados(DispositivoEmendaAdicionado adicionado, Element dispositivosElement) {
        Element dispositivoAdicionadoElement = dispositivosElement.addElement("DispositivoAdicionado");

        if (adicionado.isOndeCouber() != null) {
            dispositivoAdicionadoElement.addAttribute("ondeCouber", String.valueOf(adicionado.isOndeCouber()));
        }

        if (adicionado.getIdPai() != null) {
            dispositivoAdicionadoElement.addAttribute("idPai", adicionado.getIdPai());
        }

        if (adicionado.getIdIrmaoAnterior() != null) {
            dispositivoAdicionadoElement.addAttribute("idIrmaoAnterior", adicionado.getIdIrmaoAnterior());
        }

        if (adicionado.getIdPosicaoAgrupador() != null) {
            dispositivoAdicionadoElement.addAttribute("idPosicaoAgrupador", adicionado.getIdPosicaoAgrupador());
        }

        geraFilhosDispositivosAdicionados(adicionado, dispositivoAdicionadoElement);
    }

    private void geraFilhosDispositivosAdicionados(DispositivoEmendaAdicionado filho, Element emendaElement) {
        Element filhoElement = emendaElement.addElement(filho.getTipo());

        filhoElement.addAttribute("id", filho.getId());

        if (filho.getUuid2() != null) {
            filhoElement.addAttribute("uuid2", filho.getUuid2());
        }

        if (filho.getUrnNormaAlterada() != null) {
            filhoElement.addAttribute("xml:base", filho.getUrnNormaAlterada());
        }

        if (filho.isExisteNaNormaAlterada() != null) {
            filhoElement.addAttribute("existeNaNormaAlterada", String.valueOf(filho.isExisteNaNormaAlterada()));
        }

        if (filho.isTextoOmitido() != null && filho.isTextoOmitido()) {
            filhoElement.addAttribute("textoOmitido", "s");
        }

        if (filho.isAbreAspas() != null && filho.isAbreAspas()) {
            filhoElement.addAttribute("abreAspas", "s");
        }

        if (filho.isFechaAspas() != null && filho.isFechaAspas()) {
            filhoElement.addAttribute("fechaAspas", "s");
        }

        if (filho.getNotaAlteracao() != null) {
            filhoElement.addAttribute("notaAlteracao", filho.getNotaAlteracao().toString());
        }

        if (filho.getRotulo() != null || 
                (filho.getTexto() != null && !"Omissis".equals(filho.getTipo())) || 
                (filho.getFilhos() != null && !filho.getFilhos().isEmpty())) {
            if (filho.getRotulo() != null) {
                Element rotuloElement = filhoElement.addElement("Rotulo");
                rotuloElement.setText(filho.getRotulo());
            }

            if (filho.getTexto() != null) {
                Element textoElement = filhoElement.addElement("p");
                XMLUtil.setElementContentFromXmlFragment(textoElement, filho.getTexto());
            }

            if (filho.getFilhos() != null) {
                for (DispositivoEmendaAdicionado neto : filho.getFilhos()) {
                    geraFilhosDispositivosAdicionados(neto, filhoElement);
                }
            }
        }
    }

    protected void geraComandoEmenda(ComandoEmenda comandoEmenda, Element emendaElement) {
        Element element = emendaElement.addElement("ComandoEmenda");

        if (comandoEmenda.getCabecalhoComum() != null) {
            Element cabecalhoComumElement = element.addElement("CabecalhoComum");
            cabecalhoComumElement.addText(comandoEmenda.getCabecalhoComum());
        }

        comandoEmenda.getComandos().forEach(comando -> geraComando(comando, element));
    }

    private void geraComando(ItemComandoEmenda comando, Element emendaElement) {
        Element comandoElement = emendaElement.addElement("ItemComandoEmenda");

        if (comando.getRotulo() != null) {
            Element rotuloElement = comandoElement.addElement("Rotulo");
            rotuloElement.addText(comando.getRotulo());
        }

        Element cabecalhoElement = comandoElement.addElement("Cabecalho");
        XMLUtil.setElementContentFromXmlFragment(cabecalhoElement, comando.getCabecalho());

        if (comando.getCitacao() != null) {
            Element citacaoElement = comandoElement.addElement("Citacao");
            XMLUtil.setElementContentFromXmlFragment(citacaoElement, comando.getCitacao());
        }

        if (comando.getComplemento() != null) {
            Element complementoElement = comandoElement.addElement("Complemento");
            complementoElement.addText(comando.getComplemento());
        }
    }

    protected void geraComandoEmendaTextoLivre(ComandoEmendaTextoLivre comandoEmendaTextoLivre, Element emendaElement) {
        if (comandoEmendaTextoLivre != null) {
            Element comandoElement = emendaElement.addElement("ComandoEmendaTextoLivre");

            if (comandoEmendaTextoLivre.getMotivo() != null) {
                comandoElement.addAttribute("motivo", comandoEmendaTextoLivre.getMotivo());
            }

                comandoElement.addText(comandoEmendaTextoLivre.getTexto() != null ? 
                        XMLUtil.sanitize(comandoEmendaTextoLivre.getTexto()) : "");

            if (comandoEmendaTextoLivre.getTextoAntesRevisao() != null) {
                Element antesRevisaoElement = emendaElement.addElement("ComandoEmendaTextoLivreAntesRevisao");
                antesRevisaoElement.addText(XMLUtil.sanitize(comandoEmendaTextoLivre.getTextoAntesRevisao()));
            }
        }
    }

    protected void geraJustificativa(String justificativa, Element emendaElement) {
        if (justificativa != null) {
            Element justificativaElement = emendaElement.addElement("Justificativa");
            justificativaElement.addText(XMLUtil.sanitize(justificativa));
        }
    }

    protected void geraJustificativaAntesRevisao(String justificativa, Element emendaElement) {
        if (justificativa != null) {
            Element justificativaElement = emendaElement.addElement("JustificativaAntesRevisao");
            justificativaElement.addText(XMLUtil.sanitize(justificativa));
        }
    }

    protected void geraAutoria(Autoria autoria, Element emendaElement) {
        Element element = emendaElement.addElement("Autoria");
        element.addAttribute("tipo", autoria.getTipo().getDescricao());
        element.addAttribute("imprimirPartidoUF", String.valueOf(autoria.isImprimirPartidoUF()));
        element.addAttribute("quantidadeAssinaturasAdicionaisDeputados", String.valueOf(autoria.getQuantidadeAssinaturasAdicionaisDeputados()));
        element.addAttribute("quantidadeAssinaturasAdicionaisSenadores", String.valueOf(autoria.getQuantidadeAssinaturasAdicionaisSenadores()));

        for (Parlamentar autor : autoria.getParlamentares()) {
            geraParlamentar(autor, element);
        }

        if (autoria.getColegiado() != null) {
            geraColegiadoAutor(autoria.getColegiado(), element);
        }
    }

    private void geraParlamentar(Parlamentar autor, Element emendaElement) {
        Element parlamentarElement = emendaElement.addElement("Parlamentar");

        parlamentarElement.addAttribute("identificacao", autor.getIdentificacao());
        parlamentarElement.addAttribute("nome", autor.getNome());
        parlamentarElement.addAttribute("tratamento", autor.getTratamento());
        parlamentarElement.addAttribute("siglaPartido", autor.getSiglaPartido());
        parlamentarElement.addAttribute("siglaUF", autor.getSiglaUF());
        parlamentarElement.addAttribute("siglaCasaLegislativa", autor.getSiglaCasaLegislativa().name());
        parlamentarElement.addAttribute("sexo", autor.getSexo().name());

        if (autor.getCargo() != null) {
            parlamentarElement.addAttribute("cargo", autor.getCargo());
        }
    }

    private void geraColegiadoAutor(ColegiadoAutor colegiado, Element emendaElement) {
        Element colegiadoElement = emendaElement.addElement("Colegiado");

        colegiadoElement.addAttribute("identificacao", colegiado.getIdentificacao());
        colegiadoElement.addAttribute("nome", colegiado.getNome());
        colegiadoElement.addAttribute("sigla", colegiado.getSigla());
    }

    protected void geraOpcoesImpressao(OpcoesImpressao opcoesImpressao, Element emendaElement) {
        Element opcoesElement = emendaElement.addElement("OpcoesImpressao");

        opcoesElement.addAttribute("imprimirBrasao", String.valueOf(opcoesImpressao.isImprimirBrasao()));

        if (opcoesImpressao.getTextoCabecalho() != null) {
            opcoesElement.addAttribute("textoCabecalho", XMLUtil.sanitize(opcoesImpressao.getTextoCabecalho()));
        }

        opcoesElement.addAttribute("tamanhoFonte", String.valueOf(opcoesImpressao.getTamanhoFonte()));
        opcoesElement.addAttribute("reduzirEspacoEntreLinhas", String.valueOf(opcoesImpressao.isReduzirEspacoEntreLinhas()));
    }

    private static String html2txt(String html) {
    	if (html == null) {
    		return null;
    	}
		return html.replaceAll("(?i)<br/?>", " ")
				.replaceAll("<.+?>", "")
				.replaceAll("\\s{2,}", " ")
				.trim();
	}

    protected void geraRevisoes(List<? extends Revisao> revisoes, Element emendaElement) throws Exception {
        if (revisoes == null || revisoes.isEmpty()) {
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

        Element revisoesElement = emendaElement.addElement("Revisoes");

        for (Revisao r : revisoes) {
            StringWriter sw = new StringWriter();
            if (r instanceof RevisaoElementoPojo) {
                jmRevisaoElemento.marshal(r, sw);
            } else if (r instanceof RevisaoJustificativaPojo) {
                jmRevisaoJustificativa.marshal(r, sw);
            } else if (r instanceof RevisaoTextoLivrePojo) {
                jmRevisaoTextoLivre.marshal(r, sw);
            }

            String xmlString = sw.toString();
            Document document = DocumentHelper.parseText(xmlString);
            Element element = document.getRootElement();

            revisoesElement.add(element);
        }
    }

    protected void geraNotasRodape(List<? extends NotaRodape> notasRodape, Element emendaElement) {
        if (notasRodape == null || notasRodape.isEmpty()) {
            return;
        }

        Element notasRodapeElement = emendaElement.addElement("NotasRodape");

        for (NotaRodape nr : notasRodape) {
            NotaRodapePojo notaRodapePojo = (NotaRodapePojo) nr;
            Element notaRodapeElement = notasRodapeElement.addElement("NotaRodape");

            if (notaRodapePojo.getId() != null) {
                notaRodapeElement.addAttribute("id", notaRodapePojo.getId());
            }
            if (notaRodapePojo.getNumero() != null) {
                notaRodapeElement.addAttribute("numero", String.valueOf(notaRodapePojo.getNumero()));
            }
            if (notaRodapePojo.getTexto() != null) {
                notaRodapeElement.addAttribute("texto", XMLUtil.sanitize(notaRodapePojo.getTexto()));
            }
        }
    }

    protected void geraPendenciasPreenchimento(List<String> pendenciasPreenchimento, Element emendaElement) {
        if (pendenciasPreenchimento == null || pendenciasPreenchimento.isEmpty()) {
            return;
        }

        Element pendenciasElement = emendaElement.addElement("PendenciasPreenchimento");

        for (String pendencia : pendenciasPreenchimento) {
            Element pendenciaElement = pendenciasElement.addElement("PendenciaPreenchimento");

            pendenciaElement.addText(pendencia);
        }
    }

    public String toString(Element element) throws IOException {
        StringWriter stringWriter = new StringWriter();

        // Não utilizar OutputFormat para evitar erros de formatação de alguns textos
        XMLWriter writer = new XMLWriter(stringWriter);
        writer.write(element);
        writer.close();

        return stringWriter.toString();
    }
}
