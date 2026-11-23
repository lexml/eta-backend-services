package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;

class TipoArquivoProposicaoTest {

    @Test
    void deveResolverPorContentTypeComESemParametros() {
        assertThat(TipoArquivoProposicao.porContentType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .isEqualTo(TipoArquivoProposicao.DOCX);
        assertThat(TipoArquivoProposicao.porContentType("text/plain")).isEqualTo(TipoArquivoProposicao.TXT);
        assertThat(TipoArquivoProposicao.porContentType("TEXT/PLAIN; charset=utf-8"))
                .isEqualTo(TipoArquivoProposicao.TXT);
        assertThat(TipoArquivoProposicao.porContentType("application/xml")).isEqualTo(TipoArquivoProposicao.XML);
        assertThat(TipoArquivoProposicao.porContentType("text/xml; charset=UTF-8"))
                .isEqualTo(TipoArquivoProposicao.XML);
        assertThat(TipoArquivoProposicao.porContentType("application/pdf")).isEqualTo(TipoArquivoProposicao.PDF);
    }

    @Test
    void deveRejeitarContentTypeNuloVazioOuNaoSuportado() {
        assertThatThrownBy(() -> TipoArquivoProposicao.porContentType(null))
                .isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> TipoArquivoProposicao.porContentType("   "))
                .isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> TipoArquivoProposicao.porContentType("; charset=utf-8"))
                .isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> TipoArquivoProposicao.porContentType("application/json"))
                .isInstanceOf(EtaBackendException.class)
                .hasMessageContaining("application/json");
    }

    @Test
    void deveResolverPorExtensaoENomeDeArquivo() {
        assertThat(TipoArquivoProposicao.porExtensao("proposicao.docx")).isEqualTo(TipoArquivoProposicao.DOCX);
        assertThat(TipoArquivoProposicao.porExtensao(".DOCX")).isEqualTo(TipoArquivoProposicao.DOCX);
        assertThat(TipoArquivoProposicao.porExtensao("txt")).isEqualTo(TipoArquivoProposicao.TXT);
        assertThat(TipoArquivoProposicao.porExtensao("minuta.lei.xml")).isEqualTo(TipoArquivoProposicao.XML);
        assertThat(TipoArquivoProposicao.porExtensao("documento.PDF")).isEqualTo(TipoArquivoProposicao.PDF);
    }

    @Test
    void deveRejeitarExtensaoNulaVaziaOuNaoSuportada() {
        assertThatThrownBy(() -> TipoArquivoProposicao.porExtensao(null))
                .isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> TipoArquivoProposicao.porExtensao("   "))
                .isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> TipoArquivoProposicao.porExtensao("arquivo."))
                .isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> TipoArquivoProposicao.porExtensao("proposicao.odt"))
                .isInstanceOf(EtaBackendException.class)
                .hasMessageContaining("odt");
    }

    @Test
    void deveExporContentTypeEExtensaoEsperados() {
        assertThat(TipoArquivoProposicao.DOCX.getExtensao()).isEqualTo("docx");
        assertThat(TipoArquivoProposicao.DOCX.getContentType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        assertThat(TipoArquivoProposicao.TXT.getExtensao()).isEqualTo("txt");
        assertThat(TipoArquivoProposicao.TXT.getContentType()).isEqualTo("text/plain");
        assertThat(TipoArquivoProposicao.XML.getExtensao()).isEqualTo("xml");
        assertThat(TipoArquivoProposicao.XML.getContentType()).isEqualTo("application/xml");
        assertThat(TipoArquivoProposicao.PDF.getExtensao()).isEqualTo("pdf");
        assertThat(TipoArquivoProposicao.PDF.getContentType()).isEqualTo("application/pdf");
    }

}

