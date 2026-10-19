# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver a spec delta.

Medidas (PDFBox, linha de base a linha de base, fonte 14 sem "reduzir"), PDFs de emenda em `pdf-documento-articulado/PDF-COMPARACAO/` e a fixture `documento-com-justificacao` (v13.1):

| Espaço | Emenda | Documento articulado |
|---|---|---|
| entre linhas e dispositivos | 21,0 | 21,0 |
| preâmbulo -> primeiro artigo | 29,4 | 29,4 |
| último dispositivo -> "JUSTIFICAÇÃO" | 55,0 | 46,6 |
| "JUSTIFICAÇÃO" -> corpo; entre parágrafos | 33,6; 29,4 | 33,6; 29,4 |
| justificação -> fecho; fecho -> assinaturas | 52,9; 46,8 | 52,9; 46,8 |

Na emenda, a citação termina com o espaço após o parágrafo (0,6em = 8,4pt), que soma ao espaço antes do título. No documento articulado, o bloco da articulação (`<xsl:template match="lx:Articulacao">`) não tem espaço depois.

## Goals / Non-Goals

**Goals:**
- O mesmo espaço da emenda entre o fim da articulação e o que vem depois.

**Non-Goals:**
- Mudar a entrelinha (a diferença percebida vem da opção "reduzir" do exemplo) ou qualquer outro espaço.

## Decisions

### D1. `padding-bottom` no bloco da articulação

O bloco da articulação ganha `padding-bottom="{$pMarginBottom}"`, o mesmo parâmetro do espaço após o parágrafo (0.6em; 0.45em com "reduzir"), relativo à fonte do documento. Por estar no bloco externo, não altera o espaço entre dispositivos. Padding, e não margem: no FOP, `margin-bottom` vira `space-after` e se funde com o `space-before` do bloco seguinte (vale o maior, 26pt antes do título), sem somar; verificado no PDF (46,6pt com margem, em vez de 55pt). O padding sempre soma. Vale também quando não há justificação: o fecho fica com o mesmo espaço adicional, como na sequência citação -> local e data.

## Risks / Trade-offs

- [Documento sem justificação nem fecho] -> O espaço fica no fim da página, sem efeito visível.

## Migration Plan

Não se aplica: biblioteca sem estado; vale para os próximos PDFs.
