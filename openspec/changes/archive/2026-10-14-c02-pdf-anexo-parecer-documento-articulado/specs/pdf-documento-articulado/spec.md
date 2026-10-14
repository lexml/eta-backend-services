# Spec Delta

## ADDED Requirements

### Requirement: Anexo de parecer
Quando o `lexedit:Metadado` do documento tiver `anexoParecer` verdadeiro, o PDF MUST NOT imprimir a justificação (título "JUSTIFICAÇÃO", corpo e as notas de rodapé dela), o local e data e as assinaturas, mesmo que o documento os tenha, como na emenda. O cabeçalho, os alertas e pendências, a parte inicial e a articulação SHALL continuar impressos como nos demais documentos. Com o atributo ausente, falso ou com valor que não seja verdadeiro, o PDF SHALL ser o mesmo de um documento sem o atributo.

#### Scenario: Anexo de parecer sem justificação, fecho e assinaturas
- **WHEN** o documento tem `anexoParecer="true"`, articulação e justificação com nota de rodapé
- **THEN** o PDF termina no último dispositivo da articulação, sem o título "JUSTIFICAÇÃO", sem o texto e as notas da justificação, sem local e data e sem assinaturas

#### Scenario: Local e autoria preenchidos
- **WHEN** o documento tem `anexoParecer="true"` e também `local`, `data` e autoria de parlamentares
- **THEN** nem o local e data nem as assinaturas são impressos

#### Scenario: Demais partes mantidas
- **WHEN** o documento tem `anexoParecer="true"`, cabeçalho e pendências
- **THEN** o cabeçalho, as caixas de alerta e pendências, a epígrafe, a ementa, o preâmbulo e a articulação são impressos como em um documento sem o atributo

#### Scenario: Atributo ausente ou falso
- **WHEN** o documento não tem `anexoParecer` ou tem `anexoParecer="false"`
- **THEN** a justificação, o local e data e as assinaturas são impressos normalmente
