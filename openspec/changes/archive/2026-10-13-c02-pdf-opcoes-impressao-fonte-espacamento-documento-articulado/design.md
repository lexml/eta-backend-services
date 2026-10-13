# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver a spec delta.

Estado atual:
- `ParametrosImpressaoDocumentoArticulado` já tem as regras da emenda no construtor privado (`tamanhoFonte`, `reduzirEspacoEntreLinhas` -> `maxTamanhoFonte`, `lineHeight`, `pMarginBottom`), mas só expõe `padrao()`; o `DocumentoArticuladoPdfGenerator` sempre o usa.
- Os parâmetros vão para a XSLT (`tamanhoFonte`, `maxTamanhoFonte`, `lineHeight`, `pMarginBottom`, pelo `DocumentoArticuladoConteudoTransformer`) e para o Velocity (`tamanhoFonte` do `fo:flow`).
- A XSLT tem fixos 26pt (antes do título "JUSTIFICAÇÃO") e 14pt (antes do corpo), que na emenda são `$spacing3` e `$spacing1` e variam com "reduzir"; e 48pt/72pt (ementa/preâmbulo), que vieram do texto da #72.
- A leitura do nó `opcoesImpressao` do JSON (`value.metadado.metadadoProprietario[*].any[*].value.opcoesImpressao`) está no `DocumentoArticuladoTemplateProcessor` (parte A).

Referência (emenda, `template-velocity-emenda.xml`):

| | padrão | `reduzirEspacoEntreLinhas` |
|---|---|---|
| `$lineHeight` | 150% | 120% |
| `$pMarginBottom` | 0.6em | 0.45em |
| `$spacing1` (antes do corpo da justificação) | 14pt | 10pt |
| `$spacing3` (antes do título "JUSTIFICAÇÃO") | 26pt | 22pt |
| `$maxTamanhoFonte` | 18pt se `tamanhoFonte` = 18, senão 16pt | idem |

Editor (lexml-eta): `tamanhoFonte` só 14, 16 ou 18 (padrão 14); `reduzirEspacoEntreLinhas` padrão `false`.

## Goals / Non-Goals

**Goals:**
- Parâmetros construídos a partir das opções do documento, com os valores da emenda.
- Uma única leitura das opções de impressão, usada pelos parâmetros e pelo cabeçalho.

**Non-Goals:**
- Variar os espaços de 48pt e 72pt (vieram da #72) e o cabeçalho (fixo na emenda).
- `$spacing2` e `$spacing4` da emenda (depois do complemento da epígrafe e antes do local e data): não usados no documento articulado.

## Decisions

### D1. `OpcoesImpressaoDocumentoArticulado`: leitura única das opções

Classe package-private em `pdf/`, imutável, criada a partir do `JsonNode` do documento (`de(JsonNode)`), com os padrões do editor: `imprimirBrasao` (true), `textoCabecalho` (""), `reduzirEspacoEntreLinhas` (false), `tamanhoFonte` (14). O `DocumentoArticuladoTemplateProcessor` passa a usá-la para o cabeçalho (sem mudar o comportamento da parte A) e o `ParametrosImpressaoDocumentoArticulado` para os parâmetros.

- `tamanhoFonte`: aceito só 14, 16 ou 18; outro valor (inclusive não inteiro) -> 14. O esquema aceita qualquer inteiro positivo, mas o editor só gera esses três, e tamanhos fora disso não foram previstos no layout (decisão do explore).
- `reduzirEspacoEntreLinhas`: só booleano; outro tipo -> false.

### D2. `ParametrosImpressaoDocumentoArticulado.de(OpcoesImpressaoDocumentoArticulado)`

Novo construtor de fábrica com as regras que já existem, mais dois valores:

| Getter novo | padrão | reduzir | Origem |
|---|---|---|---|
| `getEspacoAntesTituloJustificacao()` | `"26pt"` | `"22pt"` | `$spacing3` |
| `getEspacoAntesCorpoJustificacao()` | `"14pt"` | `"10pt"` | `$spacing1` |

`padrao()` continua existindo (= `de` das opções padrão) para os testes e para quem não tem documento. O `DocumentoArticuladoPdfGenerator` passa a chamar `de(OpcoesImpressaoDocumentoArticulado.de(documento))`.

### D3. XSLT e transformer

Dois `xsl:param` a mais (`espacoAntesTituloJustificacao`, `espacoAntesCorpoJustificacao`, com os padrões 26pt e 14pt), usados nos blocos da justificação no lugar dos valores fixos; o `DocumentoArticuladoConteudoTransformer` passa os dois. 48pt e 72pt continuam fixos, com comentário dizendo que não variam com "reduzir" (vêm da #72).

Nada mais muda na XSLT: tudo o que depende do tamanho da fonte já é relativo (`0.7em` das notas e sobrescritos, `80%` das células) ou vem dos parâmetros.

### D4. Fixture e testes

- `documento-articulado-exemplo`: `tamanhoFonte="18"` e `reduzirEspacoEntreLinhas="true"` (decisão do explore: o exemplo exercita as opções; o padrão fica com as outras fixtures). Regenerar o par (JSON-first). Os testes que medem posições ou tamanhos no exemplo são ajustados (ex.: posição da epígrafe com o cabeçalho, que passa a ter 18pt).
- Variações 16 e 14+reduzir nos testes do PDF registrando no `ConversorDocumentoArticuladoFake` o JSON alterado em memória com o XML da fixture (o XML só é usado para o conteúdo; os parâmetros vêm do JSON).

## Risks / Trade-offs

- [Fonte 18 com entrelinha de 120% em tabelas, listas e notas] -> Tudo é relativo; verificação visual nos PDFs v10.
- [Testes de posição do exemplo] -> Ajustados na task de testes do PDF; os demais testes usam fixtures com o padrão.
- [Documento com `tamanhoFonte` fora do editor] -> 14, registrado no requisito.

## Migration Plan

Não se aplica: biblioteca sem estado; vale para os próximos PDFs.
