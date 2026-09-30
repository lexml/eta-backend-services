# Proposal

## Why

Com as partes 1, 2, 3 e 6 da issue [#72](https://github.com/lexml/eta-backend-services/issues/72) concluídas, o PDF imprime toda a articulação, exceto os **blocos de alteração de norma vigente**. O artigo que altera outra lei aparece só com "passa a vigorar acrescida do seguinte art. 12-A:", e o texto da alteração não sai. Os três documentos de teste da #72 têm esses blocos. Esta é a **parte 4** das 6 em que a #72 foi dividida.

## What Changes

- O PDF passa a imprimir o **bloco de alteração de norma vigente** (`Alteracao`) logo após o dispositivo que o introduz, **recuado** como na emenda (margem esquerda de 3cm e recuo de primeira linha de 1,5cm). Os dispositivos de dentro (artigo, caput, parágrafo, inciso, alínea, item, pena, agrupadores) usam a mesma formatação das partes anteriores.
- **Aspas e "(NR)" vêm dos atributos LexML** dos dispositivos, sem nenhuma aspa gerada por conta própria:
  - `abreAspas="s"` → “ antes do rótulo do dispositivo;
  - `fechaAspas="s"` → ” depois do texto (ou da linha pontilhada) do dispositivo;
  - `notaAlteracao="NR"` → " (NR)" depois das aspas, **com espaço** (`” (NR)`).
- **Omissis** (`Omissis`): linha pontilhada até a margem direita.
- **Dispositivo com texto omitido** (`textoOmitido="s"`): rótulo seguido de linha pontilhada, no lugar do texto.
- Nada é impresso depois do "(NR)". A dúvida sobre o X vermelho da imagem de exemplo da #72 foi resolvida assim: ele marca o fechamento da citação da emenda, que não existe neste documento.
- Continuam sem impressão: justificação, local e data e assinaturas. Os links das remissões continuam como texto simples (parte 5).

## Capabilities

### New Capabilities
<!-- Nenhuma: evolui a capability existente. -->

### Modified Capabilities
- `pdf-documento-articulado`: "Conteúdo impresso" deixa de excluir os blocos de alteração e os omissis, e entra o requisito "Alteração de norma vigente".

## Impact

- **Código:** só a XSLT `src/main/resources/documentoarticulado/documento-articulado-conteudo.xsl`:
  - bloco de alteração;
  - omissis e texto omitido;
  - aspas e "(NR)" no `bloco-dispositivo`, com o `abreAspas` do artigo repassado ao caput.

  O Java, o template Velocity e a API não mudam.
- **Testes:** novos casos no `DocumentoArticuladoConteudoTransformerTest` e no `DocumentoArticuladoPdfGeneratorTest` com os três documentos da #72. Os testes das partes anteriores que exigem a ausência dos trechos das alterações ("Art. 12-A.", "k) pessoas físicas beneficiárias", "A pena será aumentada da metade") precisam ser invertidos.
- **Nenhuma alteração** em emenda, parecer ou `lexmljsonix`; nenhuma dependência nova.
- **PDFs de referência:** nova versão **v5** em `pdf-documento-articulado/pdfs-gerados/` (pasta local, não versionada), revalidada no veraPDF.
