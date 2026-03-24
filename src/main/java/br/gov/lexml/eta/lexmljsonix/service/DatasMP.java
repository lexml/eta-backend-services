package br.gov.lexml.eta.lexmljsonix.service;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasMP {

	private Integer idProcesso;
	private LocalDate dataPublicacao;
	private LocalDate dataLimiteRecebimentoEmendas;
	
}
