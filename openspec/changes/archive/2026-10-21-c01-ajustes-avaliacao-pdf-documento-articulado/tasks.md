# Tasks

## 1. Linha de base para a comparação

- [x] 1.1 Com o código atual, gerar no scratchpad os PDFs de referência: `documento-articulado-exemplo` (fontes 14, 16 e 18, sem "reduzir") e os demais documentos de teste. Verificar que os arquivos existem e abrem.
- [x] 1.2 Escrever no scratchpad o script de comparação com PDFBox: lista cada linha de cada página com o texto e a posição (x e y da linha de base) e mostra as diferenças entre dois PDFs. Verificar que, rodado na referência contra ela mesma, não mostra diferença.

## 2. Epígrafe sem número (ajuste 4)

- [x] 2.1 No template `lx:Epigrafe` de `documento-articulado-conteudo.xsl`:
  - trocar quebras de linha e tabulações por espaço;
  - aparar as pontas (`aparar-inicio` novo e `aparar-fim` existente);
  - manter o vão só de espaços entre "Nº"/"N°" e a vírgula com no mínimo 8 espaços (menor vira 8; maior fica como gravado, opção B);
  - manter a troca dos espaços por `&#160;`.

  Verificar com `mvn test -Dtest=DocumentoArticuladoConteudoTransformerTest`.
- [x] 2.2 Em `DocumentoArticuladoConteudoTransformerTest`, cobrir:
  - vãos de 0, 1, 5 e 8 espaços, que dão 8 `&#160;`, e vãos de 12 e 15, que ficam como gravados;
  - quebras de linha e tabulações dentro do vão, que contam como espaço;
  - epígrafe com número, sem mudança;
  - espaços internos duplos preservados;
  - espaços e quebras de linha nas pontas removidos;
  - "N°" com o sinal de grau;
  - texto só com espaços, que não imprime nada.

  Verificar que os testes passam.
- [x] 2.3 Em `DocumentoArticuladoPdfGeneratorTest`, verificar no PDF do exemplo que a linha da epígrafe tem o vão (texto extraído com 8 espaços entre "Nº" e ","). Comparar com a referência da task 1.1: só a linha da epígrafe pode mudar. Mostrar o resultado da comparação.
- [x] 2.4 Atualizar a descrição da epígrafe no `CLAUDE.md` (trim nas pontas e vão do número) e verificar que o texto bate com o requisito "Epígrafe" da spec delta.

## 3. Rótulo como uma palavra só (ajuste 2)

- [x] 3.1 Testar a opção D2 (a), `word-spacing` fixo no `fo:inline` do rótulo, medindo com PDFBox a distância entre "Art." e o número em linhas com folgas diferentes no exemplo. Se não for constante, implementar D2 (b): `fo:leader` de comprimento fixo e `keep-together.within-line`. Registrar no design qual opção foi adotada e o resultado da medição.
- [x] 3.2 Em `DocumentoArticuladoConteudoTransformerTest`, verificar o FO gerado para o rótulo de artigo, de parágrafo ("§ 1º"), da pena (sem negrito) e dentro de alteração (com aspas). Verificar que os testes passam.
- [x] 3.3 Em `DocumentoArticuladoPdfGeneratorTest`, cobrir o cenário "Rótulo como uma palavra só": a mesma distância entre "Art." e o número em dois artigos cujas linhas têm folgas diferentes. Verificar que o teste passa.
- [x] 3.4 Comparar com a referência da task 1.1 (exemplo nas 3 fontes e os demais documentos de teste): o texto de cada parágrafo e o número de páginas são os mesmos. Quebras de linha diferentes em parágrafos com rótulo são aceitas (decisão do usuário na task 3.1, ver design D2). Mostrar o resultado.
- [x] 3.5 Atualizar o `CLAUDE.md` (rótulo como uma palavra só e por que `&#160;` não serve no FOP) e verificar a coerência com o requisito "Formatação dos dispositivos".

## 4. Alertas: só pendências, proporcionais à fonte (ajustes 1 e 3)

- [x] 4.1 Remover `possuiRevisoes` e `possuiComentarios` de `AlertasDocumentoArticulado` e do contexto do `DocumentoArticuladoTemplateProcessor`. Atualizar `AlertasDocumentoArticuladoTest`, mantendo os casos de pendências, e verificar que ele passa.
- [x] 4.2 Adicionar `getTamanhoFonteAlerta()` (`tamanhoFonte − 2`) a `ParametrosImpressaoDocumentoArticulado` e `tamanhoFonteAlerta` ao contexto. Cobrir 14→12pt, 16→14pt, 18→16pt e o padrão em `ParametrosImpressaoDocumentoArticuladoTest`, e verificar que ele passa.
- [x] 4.3 No template: remover as caixas de revisão e de comentários, trocar o cabeçalho para "Este documento contém pendências e não deve ser protocolado até que sejam resolvidas:" e usar `${tamanhoFonteAlerta}` no lugar de `12pt`. Atualizar os comentários do template. Verificar com `DocumentoArticuladoTemplateProcessorTest`, ajustado para:
  - não haver os textos antigos;
  - o novo cabeçalho;
  - o tamanho por fonte.
- [x] 4.4 Em `DocumentoArticuladoPdfGeneratorTest`, cobrir:
  - o exemplo (revisões, comentários e pendências) com só a caixa de pendências;
  - um documento com revisões e comentários, mas sem pendências, sem caixa;
  - o tamanho da caixa medido em 12, 14 e 16pt nas fontes 14, 16 e 18.

  Verificar que `mvn test` passa por completo, sem rede nem `jsonix-lexml`.
- [x] 4.5 Atualizar a descrição dos alertas no `CLAUDE.md` (só pendências, novo texto, `tamanhoFonte − 2`, divergência da emenda) e verificar a coerência com os requisitos "Alertas e pendências" e "Parâmetros de impressão".

## 5. Entrega para a nova avaliação

- [x] 5.1 Gerar os 3 PDFs do `documento-articulado-exemplo` sem "reduzir espaço entre linhas", nas fontes 14, 16 e 18, com os JSON usados, numa pasta indicada pelo usuário (sugestão: `notas-pdf-documento-articulado/v14`). Verificar que o JSON recuperado de cada PDF é igual ao usado, fora a limitação conhecida dos espaços inline.
- [x] 5.2 Pedir ao usuário a validação dos 3 PDFs no veraPDF e a conferência visual dos quatro ajustes. Registrar o resultado no `historico.md`.
