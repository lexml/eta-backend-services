package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Parâmetros opcionais que controlam o modo e o perfil de parseamento da proposição.
 */
public final class OpcoesParseProposicao {

    private final ModoParseProposicao modo;
    private final String autoridade;
    private final String tipoNorma;
    private final Integer numero;
    private final Integer ano;
    private final Boolean usarLinker;
    private final boolean validarSchema;
    private final boolean falharComAvisos;
    private final List<String> regexEpigrafeAdicionais;
    private final List<String> regexEpigrafeContinuacaoAdicionais;
    private final List<String> regexPosEpigrafeAdicionais;

    private OpcoesParseProposicao(Builder builder) {
        this.modo = builder.modo;
        this.autoridade = normalizar(builder.autoridade);
        this.tipoNorma = normalizar(builder.tipoNorma);
        this.numero = builder.numero;
        this.ano = builder.ano;
        this.usarLinker = builder.usarLinker;
        this.validarSchema = builder.validarSchema != null && builder.validarSchema;
        this.falharComAvisos = builder.falharComAvisos;
        this.regexEpigrafeAdicionais = normalizarEValidarRegexes(
                builder.regexEpigrafeAdicionais, "epígrafe");
        this.regexEpigrafeContinuacaoAdicionais = normalizarEValidarRegexes(
                builder.regexEpigrafeContinuacaoAdicionais, "continuação de epígrafe");
        this.regexPosEpigrafeAdicionais = normalizarEValidarRegexes(
                builder.regexPosEpigrafeAdicionais, "pós-epígrafe");
    }

    /** Opções padrão para o Cenário 2 (aproveitamento de ementa e articulação no {@code lexml-eta}). */
    public static OpcoesParseProposicao aproveitamento() {
        return builder().modo(ModoParseProposicao.APROVEITAMENTO).build();
    }

    /** Opções padrão para o Cenário 1 (parseamento completo com perfil padrão de Projeto de Lei). */
    public static OpcoesParseProposicao completo() {
        return builder().modo(ModoParseProposicao.COMPLETO).build();
    }

    /**
     * Opções para o Cenário 1 (parseamento completo) informando autoridade e tipo de norma na sintaxe
     * de URN do LexML (ex.: {@code "senado.federal"}, {@code "projeto.lei;pls"}).
     */
    public static OpcoesParseProposicao completo(String autoridade, String tipoNorma) {
        return builder()
                .modo(ModoParseProposicao.COMPLETO)
                .autoridade(autoridade)
                .tipoNorma(tipoNorma)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public ModoParseProposicao getModo() {
        return modo;
    }

    public String getAutoridade() {
        return autoridade;
    }

    public String getTipoNorma() {
        return tipoNorma;
    }

    public Integer getNumero() {
        return numero;
    }

    public Integer getAno() {
        return ano;
    }

    public Boolean getUsarLinker() {
        return usarLinker;
    }

    public boolean isValidarSchema() {
        return validarSchema;
    }

    public boolean isFalharComAvisos() {
        return falharComAvisos;
    }

    /** Expressões regulares adicionais para reconhecer a linha inicial da epígrafe (somadas às padrão). */
    public List<String> getRegexEpigrafeAdicionais() {
        return regexEpigrafeAdicionais;
    }

    /** Expressões regulares adicionais para linhas de continuação de epígrafes multilinhas (somadas às padrão). */
    public List<String> getRegexEpigrafeContinuacaoAdicionais() {
        return regexEpigrafeContinuacaoAdicionais;
    }

    /** Expressões regulares adicionais para linhas pós-epígrafe ignoradas antes da ementa (somadas às padrão). */
    public List<String> getRegexPosEpigrafeAdicionais() {
        return regexPosEpigrafeAdicionais;
    }

    private static String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.trim();
        return limpo.isEmpty() ? null : limpo;
    }

    private static List<String> normalizarEValidarRegexes(List<String> regexes, String tipo) {
        if (regexes.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> resultado = new ArrayList<>(regexes.size());
        for (String padrao : regexes) {
            String limpo = normalizar(padrao);
            if (limpo == null) {
                continue;
            }
            try {
                Pattern.compile(ParserBlocosProposicao.normalizarRegexParaTextoNormalizado(limpo));
            } catch (PatternSyntaxException e) {
                throw new EtaBackendException(
                        "Expressão regular de " + tipo + " inválida (\"" + limpo + "\"): " + e.getDescription(), e);
            }
            resultado.add(limpo);
        }
        return resultado.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(resultado);
    }

    public static final class Builder {

        private ModoParseProposicao modo = ModoParseProposicao.APROVEITAMENTO;
        private String autoridade;
        private String tipoNorma;
        private Integer numero;
        private Integer ano;
        private Boolean usarLinker;
        private Boolean validarSchema;
        private boolean falharComAvisos;
        private final List<String> regexEpigrafeAdicionais = new ArrayList<>();
        private final List<String> regexEpigrafeContinuacaoAdicionais = new ArrayList<>();
        private final List<String> regexPosEpigrafeAdicionais = new ArrayList<>();

        private Builder() {
        }

        public Builder modo(ModoParseProposicao modo) {
            this.modo = Objects.requireNonNull(modo, "modo");
            return this;
        }

        public Builder autoridade(String autoridade) {
            this.autoridade = autoridade;
            return this;
        }

        public Builder tipoNorma(String tipoNorma) {
            this.tipoNorma = tipoNorma;
            return this;
        }

        public Builder numero(Integer numero) {
            this.numero = numero;
            return this;
        }

        public Builder ano(Integer ano) {
            this.ano = ano;
            return this;
        }

        public Builder usarLinker(Boolean usarLinker) {
            this.usarLinker = usarLinker;
            return this;
        }

        public Builder validarSchema(boolean validarSchema) {
            this.validarSchema = validarSchema;
            return this;
        }

        public Builder falharComAvisos(boolean falharComAvisos) {
            this.falharComAvisos = falharComAvisos;
            return this;
        }

        public Builder regexEpigrafeAdicionais(Collection<String> regexes) {
            this.regexEpigrafeAdicionais.clear();
            if (regexes != null) {
                this.regexEpigrafeAdicionais.addAll(regexes);
            }
            return this;
        }

        public Builder adicionarRegexEpigrafe(String... regexes) {
            if (regexes != null) {
                Collections.addAll(this.regexEpigrafeAdicionais, regexes);
            }
            return this;
        }

        public Builder regexEpigrafeContinuacaoAdicionais(Collection<String> regexes) {
            this.regexEpigrafeContinuacaoAdicionais.clear();
            if (regexes != null) {
                this.regexEpigrafeContinuacaoAdicionais.addAll(regexes);
            }
            return this;
        }

        public Builder adicionarRegexEpigrafeContinuacao(String... regexes) {
            if (regexes != null) {
                Collections.addAll(this.regexEpigrafeContinuacaoAdicionais, regexes);
            }
            return this;
        }

        public Builder regexPosEpigrafeAdicionais(Collection<String> regexes) {
            this.regexPosEpigrafeAdicionais.clear();
            if (regexes != null) {
                this.regexPosEpigrafeAdicionais.addAll(regexes);
            }
            return this;
        }

        public Builder adicionarRegexPosEpigrafe(String... regexes) {
            if (regexes != null) {
                Collections.addAll(this.regexPosEpigrafeAdicionais, regexes);
            }
            return this;
        }

        public OpcoesParseProposicao build() {
            return new OpcoesParseProposicao(this);
        }

    }

}

