# Spec Delta

## MODIFIED Requirements

### Requirement: Local e data
Quando o documento tiver `local` preenchido, o PDF SHALL imprimir, depois da justificação (ou da articulação, se não houver justificação), o fecho "`<local>`, `<data>`." com recuo de primeira linha de 2,5cm, mantido na mesma página das assinaturas, com 34pt de espaço antes (26pt com "reduzir espaço entre linhas"), como na emenda. A data MUST sair por extenso em português (ex.: "24 de abril de 2026"). Quando a data estiver vazia, ausente ou inválida, o fecho MUST ser apenas o local seguido de vírgula ("`<local>`,"), sem espaços para preenchimento e sem ponto final (issue #81, especificação 03 do lexml-eta). Sem `local`, o fecho MUST NOT ser impresso.

#### Scenario: Local e data preenchidos
- **WHEN** o documento tem `local="Sala das Sessões"` e `data="2026-04-24"`
- **THEN** o PDF contém "Sala das Sessões, 24 de abril de 2026." depois da justificação, com recuo de 2,5cm

#### Scenario: Data vazia
- **WHEN** o documento tem `local="Sala das Sessões"` e `data` vazia, ausente ou inválida
- **THEN** o fecho é "Sala das Sessões,", sem espaços para preencher, "de" nem ponto final

#### Scenario: Espaço antes conforme as opções de impressão
- **WHEN** o documento tem `reduzirEspacoEntreLinhas="true"`
- **THEN** o espaço antes do fecho é de 26pt (34pt sem a opção)

#### Scenario: Sem local
- **WHEN** o documento não tem `local` nem autoria (ex.: os documentos da issue #72)
- **THEN** o PDF termina no último elemento do conteúdo, sem fecho nem assinaturas
