package br.gov.lexml.eta.etaservices.parecer;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.gov.lexml.eta.etaservices.emenda.Sexo;
import br.gov.lexml.eta.etaservices.printing.json.ColegiadoApreciadorPojo;
import br.gov.lexml.eta.etaservices.printing.json.NotaRodapePojo;
import br.gov.lexml.eta.etaservices.printing.json.OpcoesImpressaoPojo;
import br.gov.lexml.eta.etaservices.printing.json.ParlamentarPojo;
import br.gov.lexml.eta.etaservices.printing.json.RefProposicaoEmendadaPojo;
import br.gov.lexml.eta.etaservices.printing.pdf.TipoDocumento;

public class ParecerMapper {

    public static Parecer from(ParecerJsonDTO dto) {
        Objects.requireNonNull(dto, "dto é obrigatório");

        Parecer p = new Parecer();

        p.setDataUltimaModificacao(dto.dataUltimaModificacao);
        p.setMetadados(dto.metadados);
        p.setPendenciasPreenchimento(dto.pendenciasPreenchimento);
        p.setLocal(dto.local);
        p.setData(dto.data);

        if (dto.colegiadoApreciador != null) {
            ColegiadoApreciadorPojo c = new ColegiadoApreciadorPojo();
            c.setSiglaCasaLegislativa(dto.colegiadoApreciador.siglaCasaLegislativa);
            c.setTipoColegiado(dto.colegiadoApreciador.tipoColegiado);
            p.setColegiadoApreciador(c);
        }

        p.setEpigrafe(dto.epigrafe);

        if (dto.materia != null) {
            RefProposicaoEmendadaPojo r = new RefProposicaoEmendadaPojo();
            r.setUrn(dto.materia.urn);
            r.setSigla(dto.materia.sigla);
            r.setNumero(dto.materia.numero);
            r.setAno(dto.materia.ano);
            r.setEmenta(dto.materia.ementa != null ? dto.materia.ementa : dto.ementa);
            p.setProposicao(r);
        }

        // autoria
        if (dto.autoria != null) {
            AutoriaParecer a = new AutoriaParecer();
            if (dto.autoria.relator != null) {
                a.setRelator(toParlamentar(dto.autoria.relator));
            }
            if (dto.autoria.presidente != null) {
                a.setPresidente(toParlamentar(dto.autoria.presidente));
            }
            p.setAutoria(a);
        }

        // opções de impressão
        OpcoesImpressaoPojo oi = new OpcoesImpressaoPojo();
        if (dto.opcoesImpressao != null) {
            if (dto.opcoesImpressao.imprimirBrasao != null) {
                oi.setImprimirBrasao(dto.opcoesImpressao.imprimirBrasao);
            }
            oi.setTextoCabecalho(dto.opcoesImpressao.textoCabecalho);
            oi.setReduzirEspacoEntreLinhas(dto.opcoesImpressao.reduzirEspacoEntreLinhas);
            oi.setTamanhoFonte(dto.opcoesImpressao.tamanhoFonte);
        } else {
            oi.setImprimirBrasao(false);
        }
        p.setOpcoesImpressao(oi);

        // revisões (se precisar mapear para RevisaoPojo, faça aqui)
        // if (dto.revisoes != null) {
        // List<RevisaoPojo> lista = new ArrayList<>();
        // for (ParecerJsonDTO.RevisaoDTO r : dto.revisoes) {
        // RevisaoPojo rp = new RevisaoPojo();
        // rp.setId(r.id);
        // rp.setAutor(r.autor);
        // rp.setComentario(r.comentario);
        // rp.setTrecho(r.trecho);
        // lista.add(rp);
        // }
        // p.setRevisoes(lista);
        // }

        // notas de rodapé
        if (dto.notasRodape != null) {
            List<NotaRodapePojo> lista = new ArrayList<>();
            for (ParecerJsonDTO.NotaRodapeDTO n : dto.notasRodape) {
                NotaRodapePojo nr = new NotaRodapePojo();
                nr.setId(n.id);
                nr.setNumero(n.numero);
                nr.setTexto(n.texto);
                lista.add(nr);
            }
            p.setNotasRodape(lista);
        }



        if (dto.voto != null && dto.voto.itensVoto != null && !dto.voto.itensVoto.isEmpty()) {
            Voto voto = new Voto();
            List<ItemVoto> itens = new ArrayList<>();

            for (ParecerJsonDTO.ItemVotoDTO it : dto.voto.itensVoto) {
                ItemVoto item = new ItemVoto();
                item.setTexto(it.texto);
                item.setPosicao(it.posicao);

                if (it.documento != null) {
                    DocumentoVoto documentoVoto = new DocumentoVoto();
                    documentoVoto.setNomeArquivo(it.documento.nomeArquivo);
                    documentoVoto.setBase64(it.documento.base64);
                    documentoVoto.setTipo(TipoDocumento.parse(it.documento.tipo));
                    item.setDocumentoVoto(documentoVoto);
                }

                itens.add(item);
            }

            voto.setItensVoto(itens);
            p.setVoto(voto);
        }

        p.setEmenta(dto.ementa);
        p.setRelatorio(dto.relatorio);
        p.setAnalise(dto.analise);
        // p.setJustificativa(); //
        // p.setJustificativaAntesRevisao(); //
        // convenção

        return p;
    }

    private static ParlamentarPojo toParlamentar(ParecerJsonDTO.ParlamentarDTO src) {
        ParlamentarPojo parlamenta = new ParlamentarPojo();
        parlamenta.setIdentificacao(src.identificacao);
        parlamenta.setNome(src.nome);
        parlamenta.setSexo(Sexo.valueOf(src.sexo));
        parlamenta.setSiglaPartido(src.siglaPartido);
        parlamenta.setSiglaUF(src.siglaUF);
        parlamenta.setCargo(src.cargo);
        return parlamenta;
    }
}
