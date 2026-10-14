# Spec Delta

## MODIFIED Requirements

### Requirement: Conteúdo impresso
O PDF SHALL imprimir, a partir do `documento-articulado.xml` gerado para o documento, a parte inicial (epígrafe, ementa e preâmbulo), a articulação e a justificação, nessa ordem, seguidas do local e data e das assinaturas, montados a partir dos dados estruturados do documento (`lexedit:Metadado`). A representação textual do local, data e assinaturas no LexML (`ParteFinal`) MUST NOT ser impressa, para não duplicar esse conteúdo.

#### Scenario: Ordem do conteúdo
- **WHEN** um documento com epígrafe, ementa, preâmbulo, articulação, justificação, local e data e autoria de parlamentares é convertido em PDF
- **THEN** o texto extraído do PDF contém a epígrafe, a ementa, o preâmbulo, os artigos, a justificação, o local e data e as assinaturas, nessa ordem

#### Scenario: Elemento ausente
- **WHEN** o documento não possui um dos elementos da parte inicial (ex.: sem epígrafe) ou não possui justificação
- **THEN** o PDF é gerado com os demais elementos, sem bloco vazio nem título no lugar do ausente

#### Scenario: Elementos ainda não impressos
- **WHEN** o documento tem local, data e autoria de parlamentares e também a representação textual deles no LexML (`ParteFinal`)
- **THEN** a representação textual não é impressa: o local e data e cada assinatura aparecem uma única vez no texto extraído do PDF, montados a partir dos dados estruturados

## ADDED Requirements

### Requirement: Local e data
Quando o documento tiver `local` preenchido, o PDF SHALL imprimir, depois da justificação (ou da articulação, se não houver justificação), o fecho "`<local>`, `<data>`." com recuo de primeira linha de 2,5cm, mantido na mesma página das assinaturas, com 34pt de espaço antes (26pt com "reduzir espaço entre linhas"), como na emenda. A data MUST sair por extenso em português (ex.: "24 de abril de 2026"). Quando a data estiver vazia, ausente ou inválida, o fecho MUST sair com espaços para preenchimento manual do dia, do mês e do ano ("`<local>`, ___ de ___ de ___."), como na emenda. Sem `local`, o fecho MUST NOT ser impresso.

#### Scenario: Local e data preenchidos
- **WHEN** o documento tem `local="Sala das Sessões"` e `data="2026-04-24"`
- **THEN** o PDF contém "Sala das Sessões, 24 de abril de 2026." depois da justificação, com recuo de 2,5cm

#### Scenario: Data vazia
- **WHEN** o documento tem `local="Sala das Sessões"` e `data` vazia ou ausente
- **THEN** o PDF contém "Sala das Sessões," seguido de espaços em branco para o dia, "de", espaços para o mês, "de", espaços para o ano e ponto final

#### Scenario: Espaço antes conforme as opções de impressão
- **WHEN** o documento tem `reduzirEspacoEntreLinhas="true"`
- **THEN** o espaço antes do fecho é de 26pt (34pt sem a opção)

#### Scenario: Sem local
- **WHEN** o documento não tem `local` nem autoria (ex.: os documentos da issue #72)
- **THEN** o PDF termina no último elemento do conteúdo, sem fecho nem assinaturas

### Requirement: Assinaturas
Quando a autoria do documento for de parlamentares (`tipo="Parlamentar"`) com ao menos um `Parlamentar`, o PDF SHALL imprimir, depois do local e data, uma assinatura por parlamentar, na ordem do documento, em duas colunas de mesma largura, centralizadas, em negrito, com 30pt de espaço antes de cada assinatura, como na emenda; com número ímpar de parlamentares, a última assinatura MUST ocupar as duas colunas. Cada assinatura MUST ter: o tratamento seguido do nome ("Senador"/"Senadora" quando a casa do parlamentar não for a Câmara dos Deputados, "Deputado"/"Deputada" quando for, conforme o sexo, como na emenda); "(`<partido>` - `<UF>`)" quando `imprimirPartidoUF` for verdadeiro; e o cargo, quando preenchido. Assinaturas adicionais (linhas em branco para assinatura manual) e assinatura de comissão MUST NOT ser impressas.

#### Scenario: Senadores e deputados
- **WHEN** o documento tem como autores um senador, uma senadora, um deputado e uma deputada
- **THEN** o PDF contém "Senador <nome>", "Senadora <nome>", "Deputado <nome>" e "Deputada <nome>", nessa ordem, em negrito e centralizados

#### Scenario: Duas colunas e número ímpar
- **WHEN** o documento tem três parlamentares
- **THEN** os dois primeiros ficam lado a lado, cada um centralizado na sua metade da largura do texto, e o terceiro fica centralizado na largura toda, abaixo deles

#### Scenario: Partido e UF
- **WHEN** `imprimirPartidoUF` é verdadeiro
- **THEN** abaixo do nome aparece "(UNIÃO - AP)" (partido e UF do parlamentar); quando é falso, nenhuma assinatura tem partido e UF

#### Scenario: Cargo
- **WHEN** um parlamentar tem `cargo="Presidente do Senado Federal"` e outro tem cargo vazio
- **THEN** o cargo aparece abaixo do primeiro, e o segundo não tem linha de cargo

#### Scenario: Mantidas com o fecho
- **WHEN** o fecho e as assinaturas não cabem no fim de uma página
- **THEN** o fecho não fica sozinho no fim da página: vai para a página seguinte junto com a primeira linha de assinaturas

#### Scenario: Autoria por comissão
- **WHEN** a autoria do documento é de uma comissão (`tipo="Comissão"`)
- **THEN** nenhuma assinatura é impressa; o fecho é impresso se houver local
