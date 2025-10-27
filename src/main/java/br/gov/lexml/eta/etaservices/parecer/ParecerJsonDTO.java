package br.gov.lexml.eta.etaservices.parecer;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.gov.lexml.eta.etaservices.emenda.SiglaCasaLegislativa;
import br.gov.lexml.eta.etaservices.emenda.TipoColegiado;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParecerJsonDTO {
    public Instant dataUltimaModificacao;
    public String aplicacao;
    public String versaoAplicacao;
    public Map<String, Object> metadados;
    public List<String> pendenciasPreenchimento;

    public String ano;
    public String ementa;

    public ColegiadoApreciadorDTO colegiadoApreciador;
    public MateriaDTO materia;
    public String epigrafe;

    public String relatorio;
    public String analise;
    public VotoDTO voto;

    public String local;
    public LocalDate data;
    public AutoriaDTO autoria;

    public OpcoesImpressaoDTO opcoesImpressao;
    public List<RevisaoDTO> revisoes;
    public List<NotaRodapeDTO> notasRodape;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ColegiadoApreciadorDTO {
        public SiglaCasaLegislativa siglaCasaLegislativa;
        public TipoColegiado tipoColegiado;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MateriaDTO {
        public String urn;
        public String sigla;
        public String numero;
        public String ano;
        public String ementa;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VotoDTO {
        public List<ItemVotoDTO> itensVoto;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemVotoDTO {
        public String texto;
        public DocumentoDTO documento;
        public Integer posicao;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DocumentoDTO {
        public String tipo;
        public String nomeArquivo;
        public String base64;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AutoriaDTO {
        public ParlamentarDTO relator;
        public ParlamentarDTO presidente;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParlamentarDTO {
        public String identificacao;
        public String nome;
        public String sexo;
        public String siglaPartido;
        public String siglaUF;
        public String cargo;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpcoesImpressaoDTO {
        public Boolean imprimirBrasao;
        public String textoCabecalho;
        public Boolean reduzirEspacoEntreLinhas;
        public Integer tamanhoFonte;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RevisaoDTO {
        public String id;
        public String autor;
        public String comentario;
        public String trecho;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NotaRodapeDTO {
        public String id;
        public Integer numero;
        public String texto;
    }
}
