# Proposal

## Why

Na comparação entre o PDF do documento articulado e PDFs de emenda gerados pelo editor (`pdf-documento-articulado/PDF-COMPARACAO/`), todos os espaços verticais coincidem no mesmo modo de impressão, exceto um: entre o último dispositivo da articulação e o título "JUSTIFICAÇÃO" o documento articulado tem 46,6pt, e a emenda, 55,0pt. A diferença (8,4pt = 0,6em da fonte 14) é o espaço após o parágrafo que a emenda aplica no fim da citação dos dispositivos e que a articulação do documento articulado não tem. A outra diferença percebida, a entrelinha, vem da opção "reduzir espaço entre linhas" ligada no exemplo e desligada nas emendas comparadas, e não exige mudança.

## What Changes

- A articulação passa a terminar com o espaço após o parágrafo dos parâmetros de impressão (0.6em; 0.45em com "reduzir espaço entre linhas"), antes do que vier a seguir (justificação ou, sem ela, o local e data).
- Com isso, o espaço entre o último dispositivo e o título "JUSTIFICAÇÃO" passa de 46,6pt para 55,0pt (fonte 14, sem "reduzir").
- Nenhum outro espaço muda: entre dispositivos continua sem espaço adicional.
- **Exemplo**: o `documento-articulado-exemplo` volta a ter `reduzirEspacoEntreLinhas="false"` (pedido do usuário), no mesmo modo das emendas de comparação; o modo com "reduzir" continua coberto por variações nos testes.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `pdf-documento-articulado`: o requisito "Formatação dos dispositivos" passa a prever o espaço após a articulação.

## Impact

- `documento-articulado-conteudo.xsl` (bloco da articulação).
- Fixture `documento-articulado-exemplo` (regenerada JSON-first, só a opção muda), testes `DocumentoArticuladoConteudoTransformerTest` e `DocumentoArticuladoPdfGeneratorTest` e `CLAUDE.md`.
- Sem mudança de classes Java, API pública ou dependências; sem impacto em emenda, parecer ou `lexmljsonix`.
