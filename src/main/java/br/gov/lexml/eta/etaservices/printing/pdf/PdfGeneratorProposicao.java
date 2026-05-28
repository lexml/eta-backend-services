package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.lexml.eta.etaservices.proposicao.Proposicao;
import br.gov.lexml.eta.etaservices.util.BytesUtil;

public class PdfGeneratorProposicao {
    private final VelocityTemplateProcessorFactory templateProcessorFactory;

    public PdfGeneratorProposicao(VelocityTemplateProcessorFactory templateProcessorFactory) {
        this.templateProcessorFactory = templateProcessorFactory;
    }

    public void generate(Proposicao proposicao, OutputStream outputStream) throws Exception {
        final String templateResult = templateProcessorFactory.get().getTemplateResult(proposicao);
        final String proposicaoInJson = toProposicaoJson(proposicao);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        new FOPProcessor().processFOP(baos, templateResult, proposicaoInJson, TipoDocumento.PROPOSICAO);

        // Insere hash de verificação
        byte[] bytearr = baos.toByteArray();
        int i = BytesUtil.lastIndexOf(bytearr,
                "<check:hash>00000000000000000000000000000000".getBytes(StandardCharsets.UTF_8));
        if (i >= 0) {
            byte[] md5bytes = md5Hex(bytearr).getBytes(StandardCharsets.UTF_8);
            int openTagLen = "<check:hash>".getBytes(StandardCharsets.UTF_8).length;
            System.arraycopy(md5bytes, 0, bytearr, i + openTagLen, md5bytes.length);
        }
        outputStream.write(bytearr);
        outputStream.flush();
    }

    private String md5Hex(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytes);
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

    private String toProposicaoJson(Proposicao proposicao) {
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return om.writeValueAsString(proposicao);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar Proposição para JSON", e);
        }
    }
}
