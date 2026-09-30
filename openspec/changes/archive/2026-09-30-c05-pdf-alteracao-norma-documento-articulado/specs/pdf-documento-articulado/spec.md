# Spec Delta

## MODIFIED Requirements

### Requirement: Conteúdo impresso
O PDF SHALL imprimir, a partir do `documento-articulado.xml` gerado para o documento, a parte inicial (epígrafe, ementa e preâmbulo) seguida da articulação, na ordem do documento. Nesta etapa MUST NOT ser impressos: a justificação, o local e data e as assinaturas.

#### Scenario: Ordem do conteúdo
- **WHEN** um documento com epígrafe, ementa, preâmbulo e articulação é convertido em PDF
- **THEN** o texto extraído do PDF contém a epígrafe, a ementa, o preâmbulo e os artigos, nessa ordem

#### Scenario: Elemento ausente
- **WHEN** o documento não possui um dos elementos da parte inicial (ex.: sem epígrafe)
- **THEN** o PDF é gerado com os demais elementos, sem bloco vazio no lugar do ausente

#### Scenario: Elementos ainda não impressos
- **WHEN** o documento contém justificação (ex.: "Esta proposição promove a ...")
- **THEN** o texto extraído do PDF não contém o texto da justificação

## ADDED Requirements

### Requirement: Alteração de norma vigente
O bloco de alteração de norma vigente SHALL ser impresso logo após o dispositivo que o introduz, com margem esquerda de 3cm e recuo de primeira linha de 1,5cm, sem espaço adicional antes ou depois. Os dispositivos do bloco MUST ser impressos com a mesma formatação dos dispositivos da articulação. As aspas e a nota de alteração MUST ser impressas somente conforme os atributos do documento: aspas de abertura (“) antes do rótulo do dispositivo com `abreAspas="s"`; aspas de fechamento (”) depois do texto, ou da linha pontilhada, do dispositivo com `fechaAspas="s"`; e, quando houver `notaAlteracao`, um espaço e a nota entre parênteses depois das aspas (ex.: `” (NR)`). As aspas MUST NOT ser impressas em negrito. Nada MUST ser impresso depois da nota de alteração. O omissis MUST ser impresso como uma linha pontilhada até a margem direita. O dispositivo com `textoOmitido="s"` MUST ser impresso com o rótulo seguido de uma linha pontilhada, no lugar do texto.

#### Scenario: Bloco de alteração com aspas e nota
- **WHEN** o Art. 2º altera a Lei nº 9.394 com um bloco cujo artigo "Art. 12-A." tem `abreAspas="s"` e cujo último parágrafo tem `fechaAspas="s"` e `notaAlteracao="NR"`
- **THEN** o PDF apresenta, logo após o caput do Art. 2º e com o recuo do bloco de alteração, "“Art. 12-A. ..." e, no fim do último parágrafo, "...” (NR)", sem nenhum caractere depois do "(NR)"

#### Scenario: Dispositivo com texto omitido e omissis
- **WHEN** o bloco altera o art. 327 com o caput com `textoOmitido="s"`, um omissis e o § 3º com `fechaAspas="s"` e `notaAlteracao="NR"`
- **THEN** o PDF apresenta "“Art. 327." seguido de linha pontilhada, uma linha pontilhada inteira e "§ 3º A pena será aumentada da metade ... pública.” (NR)"

#### Scenario: Aspas de fechamento no omissis
- **WHEN** o último elemento do bloco é um omissis com `fechaAspas="s"` e `notaAlteracao="NR"`
- **THEN** o PDF apresenta a linha pontilhada seguida de "” (NR)" no fim da mesma linha

#### Scenario: Omissis dentro de um dispositivo
- **WHEN** um inciso com `textoOmitido="s"` contém um omissis seguido das alíneas "k)" e "l)"
- **THEN** o PDF apresenta o rótulo do inciso seguido de linha pontilhada, uma linha pontilhada inteira e em seguida as alíneas "k)" e "l)" com seus textos

#### Scenario: Sem aspas fora dos atributos
- **WHEN** um dispositivo dentro do bloco de alteração não tem `abreAspas` nem `fechaAspas`
- **THEN** o PDF não apresenta aspas nesse dispositivo
