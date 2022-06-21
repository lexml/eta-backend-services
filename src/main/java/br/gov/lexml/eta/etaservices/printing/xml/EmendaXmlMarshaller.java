package br.gov.lexml.eta.etaservices.printing.xml;

import br.gov.lexml.eta.etaservices.printing.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static br.gov.lexml.eta.etaservices.printing.TipoColegiado.COMISSAO;

public class EmendaXmlMarshaller {

    public static final String FECHA_TAG_SEM_CONTEUDO = "\" />\n";

    public String toXml(Emenda emenda) {
        final var sb = new StringBuilder();
        geraCabecalhoEmenda(emenda, sb);
        geraMetadados(emenda, sb);
        geraProposicao(emenda.proposicao(), sb);
        geraColegiado(emenda.colegiado(), sb);
        geraEpigrafe(emenda.epigrafe(), sb);
        geraComponentes(emenda.componentes(), sb);
        geraComandoEmenda(emenda.comandoEmenda(), sb);
        geraJustificativa(emenda.justificativa(), sb);
        geraAutoria(emenda.autoria(), sb);
        geraOpcoesImpressao(emenda.opcoesImpressao(), sb);

        sb.append("</Emenda>");
        return sb.toString();


    }

    private void geraCabecalhoEmenda(Emenda emenda, StringBuilder sb) {
        sb.append("<Emenda versaoFormatoArquivo=\"1.0\" local=\"").append(emenda.local()).append("\"");

        if (emenda.data() != null) {
            sb.append(" data=\"").append(DateTimeFormatter.ISO_DATE.format(emenda.data()))
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
                .append(emenda.dataUltimaModificacao())
                .append("</DataUltimaModificacao>\n")
                .append("    <Aplicacao>")
                .append(emenda.aplicacao())
                .append("</Aplicacao>\n")
                .append("    <VersaoAplicacao>")
                .append(emenda.versaoAplicacao())
                .append("</VersaoAplicacao>\n")
                .append("    <ModoEdicao>")
                .append(emenda.modoEdicao())
                .append("</ModoEdicao>\n");

        emenda.metadados().forEach((k, v) ->
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
                .append(proposicao.urn())
                .append("\" ")
                .append("sigla=\"")
                .append(proposicao.sigla())
                .append("\" ")
                .append("numero=\"")
                .append(proposicao.numero())
                .append("\" ")
                .append("ano=\"")
                .append(proposicao.ano())
                .append("\" ")
                .append("ementa=\"")
                .append(proposicao.ementa())
                .append("\" ")
                .append("identificacaoTexto=\"")
                .append(proposicao.identificacaoTexto())
                .append(FECHA_TAG_SEM_CONTEUDO);
    }

    private void geraColegiado(ColegiadoApreciador colegiado, StringBuilder sb) {
        sb.append("  <ColegiadoApreciador ")
                .append("siglaCasaLegislativa=\"")
                .append(colegiado
                        .siglaCasaLegislativa())
                .append("\" ")
                .append("tipoColegiado=\"")
                .append(colegiado.tipoColegiado())
                .append("\"");

        if (colegiado.tipoColegiado() == COMISSAO) {
            sb.append(" siglaComissao=\"")
                    .append(colegiado.siglaComissao())
                    .append("\"");
        }

        sb.append(" />\n");
    }

    private void geraEpigrafe(Epigrafe epigrafe, StringBuilder sb) {
        sb.append("  <Epigrafe ")
                .append("texto=\"")
                .append(epigrafe.texto())
                .append("\" ");

        if (epigrafe.complemento() != null) {
            sb.append("complemento=\"")
                    .append(epigrafe.complemento())
                    .append("\" ");
        }

        sb.append("/>\n");
    }

    private void geraComponentes(List<ComponenteEmendado> componentes, StringBuilder sb) {
        componentes.forEach(componente -> {
            sb.append("  <Componente ")
                    .append("urn=\"")
                    .append(componente.urn())
                    .append("\" ")
                    .append("artigo=\"")
                    .append(componente.articulado())
                    .append("\" ");

            if (componente.tituloAnexo() != null) {
                sb.append("tituloAnexo=\"")
                        .append(componente.tituloAnexo())
                        .append("\" ");
            }

            if (componente.rotuloAnexo() != null) {
                sb.append("rotuloAnexo=\"")
                        .append(componente.rotuloAnexo())
                        .append("\" ");
            }

            sb.append(">\n");
            geraDispositivos(componente.dispositivos(), sb);
            sb.append("  </Componente>\n");
        });
    }

    private void geraDispositivos(DispositivosEmenda dispositivos, StringBuilder sb) {
        sb.append("    <Dispositivos>\n");
        dispositivos.dispositivosSuprimidos().forEach(suprimido -> geraDispositivosSuprimidos(suprimido, sb));
        dispositivos.dispositivosModificados().forEach(modificado -> geraDispositivosModificados(modificado, sb));
        dispositivos.dispositivosAdicionados().forEach(adicionado -> geraDispositivosAdicionados(adicionado, sb));
        sb.append("    </Dispositivos>\n");
    }

    private void geraDispositivosSuprimidos(DispositivoEmendaSuprimido suprimido, StringBuilder sb) {
        sb.append("      <DispositivoSuprimido ")
                .append("tipo=\"")
                .append(suprimido.tipo())
                .append("\" ")
                .append("id=\"")
                .append(suprimido.id())
                .append("\" ")
                .append("rotulo=\"")
                .append(suprimido.rotulo())
                .append(FECHA_TAG_SEM_CONTEUDO);
    }

    private void geraDispositivosModificados(DispositivoEmendaModificado modificado, StringBuilder sb) {
        sb.append("      <DispositivoModificado ")
                .append("tipo=\"")
                .append(modificado.tipo())
                .append("\" ")
                .append("id=\"")
                .append(modificado.id())
                .append("\" ")
                .append("rotulo=\"")
                .append(modificado.rotulo())
                .append("\" ");
        if (modificado.textoOmitido() != null) {
            sb.append("textoOmitido=\"")
                    .append(modificado.textoOmitido())
                    .append("\" ");
        }
        if (modificado.abreAspas() != null) {
            sb.append("abreAspas=\"")
                    .append(modificado.abreAspas())
                    .append("\" ");
        }
        if (modificado.fechaAspas() != null) {
            sb.append("fechaAspas=\"")
                    .append(modificado.fechaAspas())
                    .append("\" ");
        }
        if (modificado.notaAlteracao() != null) {
            sb.append("notaAlteracao=\"")
                    .append(modificado.notaAlteracao())
                    .append("\" ");
        }
        sb.append(">\n");
        sb.append("        <Texto>")
                .append(modificado.texto())
                .append("\n        </Texto>\n");

    }

    private void geraDispositivosAdicionados(DispositivoEmendaAdicionado adicionado, StringBuilder sb) {
        sb.append("      <DispositivoAdicionado ");

        if (adicionado.ondeCouber() != null) {
            sb.append("ondeCouber=\"")
                    .append(adicionado.ondeCouber())
                    .append("\" ");
        }

        if (adicionado.idPai() != null) {
            sb.append("idPai=\"")
                    .append(adicionado.idPai())
                    .append("\" ");
        }

        if (adicionado.idIrmaoAnterior() != null) {
            sb.append("idIrmaoAnterior=\"")
                    .append(adicionado.idIrmaoAnterior())
                    .append("\" ");
        }

        if (adicionado.urnNormaAlterada() != null) {
            sb.append("urnNormaAlterada=\"")
                    .append(adicionado.urnNormaAlterada())
                    .append("\" ");
        }

        if (adicionado.existeNaNormaAlterada() != null) {
            sb.append("existeNaNormaAlterada=\"")
                    .append(adicionado.existeNaNormaAlterada())
                    .append("\" ");
        }

        sb.append(">\n");

        adicionado.filhos().forEach(filho -> geraDispositivosAdicionados(filho, sb));

        sb.append("\n      </DispositivoAdicionado>\n");


    }

    private void geraComandoEmenda(ComandoEmenda comandoEmenda, StringBuilder sb) {
        sb.append("  <ComandoEmenda>\n");

        if (comandoEmenda.cabecalhoComum() != null) {
            sb.append("    <CabecalhoComum>")
                    .append(comandoEmenda.cabecalhoComum())
                    .append("</CabecalhoComum>\n");
        } else {
            sb.append("    <CabecalhoComum/>\n");
        }

        comandoEmenda.comandos().forEach(comando -> geraComando(comando, sb));
        sb.append("  </ComandoEmenda>\n");
    }

    private void geraComando(ItemComandoEmenda comando, StringBuilder sb) {
        sb.append("    <ItemComandoEmenda>\n");

        if (comando.rotulo() != null) {
            sb.append("      <rotulo>")
                    .append(comando.rotulo())
                    .append("</rotulo>\n");
        } else {
            sb.append("      <rotulo/>\n");
        }

        sb.append("      <cabecalho>")
                .append(comando.cabecalho())
                .append("</cabecalho>\n");

        if (comando.citacao() != null) {
            sb.append("      <citacao>")
                    .append(comando.citacao())
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
                .append(autoria.tipo())
                .append("\" imprimirPartidoUF=\"")
                .append(autoria.imprimirPartidoUF())
                .append("\" quantidadeAssinaturasAdicionaisDeputados=\"")
                .append(autoria.quantidadeAssinaturasAdicionaisDeputados())
                .append("\" quantidadeAssinaturasAdicionaisSenadores=\"")
                .append(autoria.quantidadeAssinaturasAdicionaisSenadores())
                .append("\" >\n");

        autoria.parlamentares().forEach(autor -> geraParlamentar(autor, sb));

        if (autoria.colegiado() != null) {
            geraColegiadoAutor(autoria.colegiado(), sb);
        }

        sb.append("  </Autoria>\n");


    }

    private void geraParlamentar(Parlamentar autor, StringBuilder sb) {
        sb.append("    <Parlamentar ")
                .append(" identificacao=\"")
                .append(autor.identificacao())
                .append("\" ")
                .append("nome=\"")
                .append(autor.nome())
                .append("\" ")
                .append("tratamento=\"")
                .append(autor.tratamento())
                .append("\" ")
                .append("siglaPartido=\"")
                .append(autor.siglaPartido())
                .append("\" ")
                .append("siglaUF=\"")
                .append(autor.siglaUf())
                .append("\" ")
                .append("siglaCasaLegislativa=\"")
                .append(autor.siglaCasaLegislativa())
                .append(FECHA_TAG_SEM_CONTEUDO);
    }

    private void geraColegiadoAutor(ColegiadoAutor colegiado, StringBuilder sb) {
        sb.append("    <Colegiado ")
                .append("identificacao=\"")
                .append(colegiado.identificacao())
                .append("\" ")
                .append("nome=\"")
                .append(colegiado.nome())
                .append("\" ")
                .append("sigla=\"")
                .append(colegiado.sigla())
                .append(FECHA_TAG_SEM_CONTEUDO);
    }

    private void geraOpcoesImpressao(OpcoesImpressao opcoesImpressao, StringBuilder sb) {
        sb.append("  <OpcoesImpressao ")
                .append(" imprimirBrasao=\"")
                .append(opcoesImpressao.imprimirBrasao())
                .append("\" ");

        if (opcoesImpressao.textoCabecalho() != null) {
            sb.append(" textoCabecalho=\"")
                    .append(opcoesImpressao.textoCabecalho())
                    .append("\" ");
        }

        sb.append(" reduzirEspacoEntreLinhas=\"")
                .append(opcoesImpressao.reduzirEspacoEntreLinhas())
                .append(FECHA_TAG_SEM_CONTEUDO);


    }

}
