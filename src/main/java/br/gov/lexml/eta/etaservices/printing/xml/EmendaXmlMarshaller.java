package br.gov.lexml.eta.etaservices.printing.xml;

import br.gov.lexml.eta.etaservices.printing.Autoria;
import br.gov.lexml.eta.etaservices.printing.ColegiadoApreciador;
import br.gov.lexml.eta.etaservices.printing.ColegiadoAutor;
import br.gov.lexml.eta.etaservices.printing.ComandoEmenda;
import br.gov.lexml.eta.etaservices.printing.ComponenteEmendado;
import br.gov.lexml.eta.etaservices.printing.DispositivoEmendaAdicionado;
import br.gov.lexml.eta.etaservices.printing.DispositivoEmendaModificado;
import br.gov.lexml.eta.etaservices.printing.DispositivoEmendaSuprimido;
import br.gov.lexml.eta.etaservices.printing.DispositivosEmenda;
import br.gov.lexml.eta.etaservices.printing.Emenda;
import br.gov.lexml.eta.etaservices.printing.Epigrafe;
import br.gov.lexml.eta.etaservices.printing.ItemComandoEmenda;
import br.gov.lexml.eta.etaservices.printing.OpcoesImpressao;
import br.gov.lexml.eta.etaservices.printing.Parlamentar;
import br.gov.lexml.eta.etaservices.printing.RefProposicaoEmendada;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static br.gov.lexml.eta.etaservices.printing.TipoColegiado.COMISSAO;

public class EmendaXmlMarshaller {

    public static final String FECHA_TAG_SEM_CONTEUDO = "\" />\n";

    public String toXml(Emenda emenda) {
        final StringBuilder sb = new StringBuilder();
        geraCabecalhoEmenda(emenda, sb);
        geraMetadados(emenda, sb);
        geraProposicao(emenda.getProposicao(), sb);
        geraColegiado(emenda.getColegiadoApreciador(), sb);
        geraEpigrafe(emenda.getEpigrafe(), sb);
        geraComponentes(emenda.getComponentes(), sb);
        geraComandoEmenda(emenda.getComandoEmenda(), sb);
        geraJustificativa(emenda.getJustificativa(), sb);
        geraAutoria(emenda.getAutoria(), sb);
        geraOpcoesImpressao(emenda.getOpcoesImpressao(), sb);

        sb.append("</Emenda>");
        return sb.toString();


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
                .append(proposicao.getEmenta())
                .append("\" ")
                .append("identificacaoTexto=\"")
                .append(proposicao.getIdentificacaoTexto())
                .append(FECHA_TAG_SEM_CONTEUDO);
    }

    private void geraColegiado(ColegiadoApreciador colegiado, StringBuilder sb) {
        sb.append("  <ColegiadoApreciador ")
                .append("siglaCasaLegislativa=\"")
                .append(colegiado
                        .getSiglaCasaLegislativa())
                .append("\" ")
                .append("tipoColegiado=\"")
                .append(colegiado.getTipoColegiado())
                .append("\"");

        if (colegiado.getTipoColegiado() == COMISSAO) {
            sb.append(" siglaComissao=\"")
                    .append(colegiado.getSiglaComissao())
                    .append("\"");
        }

        sb.append(" />\n");
    }

    private void geraEpigrafe(Epigrafe epigrafe, StringBuilder sb) {
        sb.append("  <Epigrafe ")
                .append("texto=\"")
                .append(epigrafe.getTexto())
                .append("\" ");

        if (epigrafe.getComplemento() != null) {
            sb.append("complemento=\"")
                    .append(epigrafe.getComplemento())
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
                    .append("artigo=\"")
                    .append(componente.isArticulado())
                    .append("\" ");

            if (componente.getTituloAnexo() != null) {
                sb.append("tituloAnexo=\"")
                        .append(componente.getTituloAnexo())
                        .append("\" ");
            }

            if (componente.getRotuloAnexo() != null) {
                sb.append("rotuloAnexo=\"")
                        .append(componente.getRotuloAnexo())
                        .append("\" ");
            }

            sb.append(">\n");
            geraDispositivos(componente.getDispositivos(), sb);
            sb.append("  </Componente>\n");
        });
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
                .append(FECHA_TAG_SEM_CONTEUDO);
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
        if (modificado.isTextoOmitido() != null) {
            sb.append("textoOmitido=\"")
                    .append(modificado.isTextoOmitido())
                    .append("\" ");
        }
        if (modificado.isAbreAspas() != null) {
            sb.append("abreAspas=\"")
                    .append(modificado.isAbreAspas())
                    .append("\" ");
        }
        if (modificado.isFechaAspas() != null) {
            sb.append("fechaAspas=\"")
                    .append(modificado.isFechaAspas())
                    .append("\" ");
        }
        if (modificado.getNotaAlteracao() != null) {
            sb.append("notaAlteracao=\"")
                    .append(modificado.getNotaAlteracao())
                    .append("\" ");
        }
        sb.append(">\n");
        sb.append("        <Texto>")
                .append(modificado.getTexto())
                .append("\n        </Texto>\n");
        sb.append("      </DispositivoModificado> ";

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

        if (adicionado.getUrnNormaAlterada() != null) {
            sb.append("urnNormaAlterada=\"")
                    .append(adicionado.getUrnNormaAlterada())
                    .append("\" ");
        }

        if (adicionado.isExisteNaNormaAlterada() != null) {
            sb.append("existeNaNormaAlterada=\"")
                    .append(adicionado.isExisteNaNormaAlterada())
                    .append("\" ");
        }

        sb.append(">\n");

        adicionado.getFilhos().forEach(filho -> geraFilhosDispositivosAdicionados(filho, sb));

        sb.append("      </DispositivoAdicionado>\n");

    }

    private void geraFilhosDispositivosAdicionados(DispositivoEmendaAdicionado filho, StringBuilder sb) {
        sb.append("        <");
        sb.append(filho.getTipo());

        if (filho.getRotulo() == null && filho.getTexto() == null &&
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
                sb.append("          <Texto>")
                        .append(filho.getTexto())
                        .append("</Texto>\n");
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
        } else {
            sb.append("    <CabecalhoComum/>\n");
        }

        comandoEmenda.getComandos().forEach(comando -> geraComando(comando, sb));
        sb.append("  </ComandoEmenda>\n");
    }

    private void geraComando(ItemComandoEmenda comando, StringBuilder sb) {
        sb.append("    <ItemComandoEmenda>\n");

        if (comando.getRotulo() != null) {
            sb.append("      <rotulo>")
                    .append(comando.getRotulo())
                    .append("</rotulo>\n");
        } else {
            sb.append("      <rotulo/>\n");
        }

        sb.append("      <cabecalho>")
                .append(comando.getCabecalho())
                .append("</cabecalho>\n");

        if (comando.getCitacao() != null) {
            sb.append("      <citacao>")
                    .append(comando.getCitacao())
                    .append("</citacao>\n");
        } else {
            sb.append("      <citacao/>\n");
        }

        sb.append("    </ItemComandoEmenda>\n");
    }

    private void geraJustificativa(String justificativa, StringBuilder sb) {
        sb.append("  <Justificativa>")
                .append(justificativa)
                .append("</Justificativa>\n");
    }

    private void geraAutoria(Autoria autoria, StringBuilder sb) {
        sb.append("  <Autoria ")
                .append(" tipo=\"")
                .append(autoria.getTipo())
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
                .append(autor.getNome())
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
                .append(FECHA_TAG_SEM_CONTEUDO);
    }

    private void geraColegiadoAutor(ColegiadoAutor colegiado, StringBuilder sb) {
        sb.append("    <Colegiado ")
                .append("identificacao=\"")
                .append(colegiado.getIdentificacao())
                .append("\" ")
                .append("nome=\"")
                .append(colegiado.getNome())
                .append("\" ")
                .append("sigla=\"")
                .append(colegiado.getSigla())
                .append(FECHA_TAG_SEM_CONTEUDO);
    }

    private void geraOpcoesImpressao(OpcoesImpressao opcoesImpressao, StringBuilder sb) {
        sb.append("  <OpcoesImpressao ")
                .append(" imprimirBrasao=\"")
                .append(opcoesImpressao.isImprimirBrasao())
                .append("\" ");

        if (opcoesImpressao.getTextoCabecalho() != null) {
            sb.append(" textoCabecalho=\"")
                    .append(opcoesImpressao.getTextoCabecalho())
                    .append("\" ");
        }

        sb.append(" reduzirEspacoEntreLinhas=\"")
                .append(opcoesImpressao.isReduzirEspacoEntreLinhas())
                .append(FECHA_TAG_SEM_CONTEUDO);


    }

}
