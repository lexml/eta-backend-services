# Proposal

## Why

Depois da parte 2 da issue [#72](https://github.com/lexml/eta-backend-services/issues/72) (change `2026-09-30-c02-pdf-articulacao-basica-documento-articulado`), o PDF imprime os artigos que estão dentro de agrupadores, mas não o rótulo e o nome desses agrupadores. Na MPV de teste, por exemplo, "CAPÍTULO I – DISPOSIÇÕES PRELIMINARES" e "Seção I – Dos beneficiários" não aparecem, e a divisão do texto se perde. Esta é a **parte 3** das 6 em que a #72 foi dividida: imprimir os **agrupadores de artigos**.

## What Changes

- O PDF passa a imprimir, antes dos artigos de cada agrupador, o **rótulo** e o **nome**, cada um em uma linha, centralizados e sem o recuo de primeira linha dos dispositivos:
  - **Parte, Livro, Título e Capítulo:** rótulo e nome em maiúsculas; rótulo em negrito, nome em fonte regular;
  - **Seção e Subseção:** rótulo e nome em negrito, com a capitalização do documento.
- **Sem espaço extra** antes ou depois dos agrupadores, como na emenda e na imagem de exemplo da #72. O título do agrupador **não fica sozinho no fim da página**: é mantido na mesma página do conteúdo seguinte.
- O nome é impresso só como texto: a formatação inline interna (ex.: `<b>` no nome da seção) é descartada, porque a linha inteira já tem formatação definida.
- O agrupador genérico `Agrupamento` do lexml-eta, fora da lista da #72, continua sendo apenas atravessado.
- Continuam sem impressão: alteração de norma e omissis (parte 4), pena e título de dispositivo (parte 6), além de justificação, local e data e assinaturas.

## Capabilities

### New Capabilities
<!-- Nenhuma: evolui a capability existente. -->

### Modified Capabilities
- `pdf-documento-articulado`: o rótulo e o nome dos agrupadores passam a ser impressos. Os requisitos "Conteúdo impresso" e "Dispositivos da articulação" deixam de excluí-los, e entra o requisito "Agrupadores".

## Impact

- **Código:** só a XSLT `src/main/resources/documentoarticulado/documento-articulado-conteudo.xsl` (template dos agrupadores). O Java, o template Velocity e a API não mudam.
- **Testes:** novos casos no `DocumentoArticuladoConteudoTransformerTest` (com a MPV de teste e XML mínimo para Parte, Livro, Título e Subseção, que não existem nas fixtures) e no `DocumentoArticuladoPdfGeneratorTest`. Os testes da parte 2 que exigem a ausência de "CAPÍTULO" e dos nomes precisam ser ajustados.
- **Nenhuma alteração** em emenda, parecer ou `lexmljsonix`; nenhuma dependência nova.
- **PDFs de referência:** nova versão **v3** em `pdf-documento-articulado/pdfs-gerados/` (pasta local, não versionada), revalidada no veraPDF.
