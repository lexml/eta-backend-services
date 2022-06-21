package br.gov.lexml.eta.etaservices.printing;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record Emenda(
  Instant dataUltimaModificacao,
  String aplicacao,
  String versaoAplicacao,
  ModoEdicaoEmenda modoEdicao,
  Map<String, Object> metadados,
  RefProposicaoEmendada proposicao,
  ColegiadoApreciador colegiado,
  Epigrafe epigrafe,
  List<ComponenteEmendado> componentes,
  ComandoEmenda comandoEmenda,
  String justificativa,
  String local,
  LocalDate data,
  Autoria autoria,
  OpcoesImpressao opcoesImpressao) {
}
