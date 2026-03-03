package br.gov.lexml.eta.etaservices.parecer;

import java.io.InputStream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnexoPDFA {
    private String nomeArquivo;
    private InputStream file;
    private boolean imprimir;
}
