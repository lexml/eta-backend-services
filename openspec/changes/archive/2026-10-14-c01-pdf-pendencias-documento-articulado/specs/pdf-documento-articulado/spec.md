# Spec Delta

## ADDED Requirements

### Requirement: Alertas e pendências
O PDF SHALL apresentar, na primeira página, depois do cabeçalho e antes da epígrafe, os alertas da emenda conforme os dados do documento (`lexedit:Metadado`), nesta ordem e cada um em uma caixa de fundo amarelo, texto vermelho em negrito e 12pt (independente do tamanho da fonte do documento): (1) "Este documento contém marcas de revisão e não deve ser protocolado até que estas sejam removidas.", centralizado, quando houver ao menos uma revisão de texto ou da articulação; (2) "Este documento contém comentários e não deve ser protocolado até que sejam removidos.", centralizado, quando houver ao menos um comentário; (3) "Este documento contém pendências de preenchimento e não deve ser protocolado até que estas sejam resolvidas:", alinhado à esquerda e seguido de uma lista com marcadores ("•", sem negrito) com cada pendência do documento, na ordem e com o texto em que foi registrada, quando houver ao menos uma pendência. A lista MUST NOT ser filtrada nem reescrita, mesmo que um item repita o aviso de outra caixa. Sem nenhum desses dados, nenhuma caixa MUST ser impressa.

#### Scenario: Marcas de revisão
- **WHEN** o documento tem revisões de texto ou revisões da articulação registradas
- **THEN** a primeira página apresenta, antes da epígrafe, a caixa "Este documento contém marcas de revisão e não deve ser protocolado até que estas sejam removidas."

#### Scenario: Comentários
- **WHEN** o documento tem ao menos um comentário
- **THEN** a primeira página apresenta a caixa "Este documento contém comentários e não deve ser protocolado até que sejam removidos.", depois da caixa de revisão quando ela existir

#### Scenario: Pendências como registradas
- **WHEN** o documento tem as pendências "Não foi informado um texto de justificação." e "Remover comentários."
- **THEN** a caixa de pendências de preenchimento lista os dois itens, nessa ordem e com esse texto, mesmo que a caixa de comentários também seja impressa

#### Scenario: Só pendências
- **WHEN** o documento tem pendências, mas nenhuma revisão nem comentário
- **THEN** só a caixa de pendências de preenchimento é impressa

#### Scenario: Sem alertas
- **WHEN** o documento não tem revisões, comentários nem pendências (ex.: os documentos da issue #72)
- **THEN** nenhuma caixa é impressa e a primeira página começa pela epígrafe, na mesma posição de hoje

#### Scenario: Tamanho fixo
- **WHEN** o documento tem `tamanhoFonte="18"`
- **THEN** o texto das caixas continua em 12pt
