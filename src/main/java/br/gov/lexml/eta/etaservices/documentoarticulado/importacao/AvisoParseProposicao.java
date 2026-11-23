package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import br.gov.lexml.parser.pl.errors.ParseProblem;
import scala.jdk.javaapi.CollectionConverters;

/**
 * Aviso de técnica legislativa ou validação de esquema emitido durante o parseamento da proposição.
 */
public final class AvisoParseProposicao {

    private final int codigo;
    private final String tipo;
    private final String categoria;
    private final String mensagem;
    private final List<String> posicoes;

    public AvisoParseProposicao(int codigo, String tipo, String categoria, String mensagem, List<String> posicoes) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.categoria = categoria;
        this.mensagem = Objects.requireNonNull(mensagem, "mensagem");
        this.posicoes = posicoes == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(posicoes));
    }

    static AvisoParseProposicao de(ParseProblem problem) {
        List<String> pos = new ArrayList<>();
        for (String p : CollectionConverters.asJava(problem.pos())) {
            if (p != null && !p.trim().isEmpty()) {
                pos.add(p.trim());
            }
        }
        return new AvisoParseProposicao(
                problem.problemType().code(),
                problem.problemType().description(),
                problem.problemType().category().description(),
                problem.message(),
                pos);
    }

    public int getCodigo() {
        return codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getMensagem() {
        return mensagem;
    }

    public List<String> getPosicoes() {
        return posicoes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AvisoParseProposicao)) {
            return false;
        }
        AvisoParseProposicao that = (AvisoParseProposicao) o;
        return codigo == that.codigo
                && Objects.equals(tipo, that.tipo)
                && Objects.equals(categoria, that.categoria)
                && Objects.equals(mensagem, that.mensagem)
                && Objects.equals(posicoes, that.posicoes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo, tipo, categoria, mensagem, posicoes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (tipo != null && !tipo.isEmpty()) {
            sb.append(tipo);
            if (!tipo.equals(mensagem)) {
                sb.append(": ").append(mensagem);
            }
        } else {
            sb.append(mensagem);
        }
        if (!posicoes.isEmpty()) {
            sb.append(" (").append(String.join(", ", posicoes)).append(")");
        }
        return sb.toString();
    }

}

