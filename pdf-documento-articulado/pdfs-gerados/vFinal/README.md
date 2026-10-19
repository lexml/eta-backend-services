# Versão final dos PDFs do documento articulado

PDFs/A da proposição gerados pelo `eta-backend-services` (fluxo `documentoarticulado`) a partir dos documentos de
teste do projeto, em todas as combinações das opções de impressão que mudam o layout: tamanho da fonte (14, 16 e
18, os valores do editor) com e sem "reduzir espaço entre linhas".

```
vFinal/
  espaco-entre-linhas-reduzido/   reduzirEspacoEntreLinhas = true  (entrelinha de 120%)
    14/  16/  18/                 tamanhoFonte
  espaco-entre-linhas-normal/     reduzirEspacoEntreLinhas = false (entrelinha de 150%)
    14/  16/  18/
```

Em cada pasta, para cada documento:

- `<documento>.json` — o documento-articulado.json usado na geração (o documento de teste com as duas opções
  trocadas; nos documentos da issue #72, que não têm `lexedit:Metadado`, foi incluído um mínimo só com as
  `OpcoesImpressao`, com brasão e sem texto de cabeçalho, os padrões do editor);
- `<documento>.pdf` — o PDF/A-3B gerado, com o `documento-articulado.xml` embutido;
- `<documento>.recuperado.json` — o JSON extraído de volta do próprio PDF.

## Documentos e o que cada um mostra

| Documento | Recursos |
|---|---|
| `documento-articulado-exemplo` | Exemplo completo: cabeçalho (brasão, casa e texto), as três caixas de alerta e as cinco pendências, epígrafe, ementa com link, preâmbulo, agrupadores (Título, Capítulo, Seção, Subseção), pena e título de dispositivo, alteração de norma com aspas e omissis, remissões internas e externas, justificação (formatação inline, estilos de parágrafo, revisões, comentário, notas de rodapé, listas, tabelas e imagens), local e data e cinco assinaturas de senadores e deputados (com e sem cargo, a última sozinha). |
| `documento-com-articulacao-e-alteracao-de-norma` | Articulação com alteração de norma vigente (issue #72). |
| `documento-com-capitulo-e-secao` | Medida provisória com capítulos e seções; cabeçalho só com o brasão (autoridade federal). |
| `documento-com-pena-e-titulo-de-dispositivo` | Pena e título de dispositivo. |
| `documento-com-remissoes-internas` | Remissões internas válidas e inválidas. |
| `documento-com-justificacao` | Catálogo da justificação: formatação inline, estilos do editor, revisões, comentário, notas (inclusive em trecho excluído), listas aninhadas, tabelas com mesclagem, imagens PNG/JPEG/externa; alertas de revisão e comentários; uma assinatura. |
| `documento-com-justificacao-longa` | Justificação de várias páginas com notas em páginas diferentes; cabeçalho só com texto; fecho sem data ("Sala das Sessões,"); assinatura sem partido/UF. |
| `documento-anexo-parecer` | Anexo de parecer (`anexoParecer="true"`): sem a justificação; local, data e assinatura logo depois da articulação. |

## Verificação

- Tamanho do texto e entrelinha medidos com PDFBox em todos os 48 PDFs: 14/16/18pt com 16,8/19,2/21,6pt entre
  linhas (reduzido) e 21/24/27pt (normal). Epígrafe e título "JUSTIFICAÇÃO" em 16pt (18pt com fonte 18); notas de
  rodapé em 0,7 e células de tabela em 0,8 do tamanho do texto; caixas de alerta em 12pt e cabeçalho em 11pt, fixos.
- JSON recuperado igual ao usado em 36 dos 48; nos 12 do exemplo e da justificação, a diferença é só nos espaços
  em branco entre elementos inline, que o conversor `jsonix-lexml` descarta (limitação conhecida).
- Conformidade PDF/A-3B: os 48 PDFs validados no veraPDF, todos conformes.

Gerado em 19/10/2026 com `DocumentoArticuladoJson2PDF` e o `jsonix-lexml` 2.0.0, a partir da branch
`feature/PDF-ETA` (commit `76d5ccc`).
