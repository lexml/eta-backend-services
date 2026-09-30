# Proposal

## Why

O PDF da proposição gerado pela change `2026-09-24-c01-salvar-abrir-pdf-documento-articulado` (issue #71) sai com a página em branco. A issue [#72](https://github.com/lexml/eta-backend-services/issues/72) pede a apresentação de epígrafe, ementa, preâmbulo e articulação, com formatação derivada da emenda. Por ser uma tarefa grande, a #72 foi dividida em 6 partes. Esta é a **parte 1**: define como o texto do documento articulado é gerado e imprime a **parte inicial** (epígrafe, ementa e preâmbulo), que é a base para as partes seguintes.

## What Changes

- O PDF passa a imprimir a **parte inicial** do documento articulado:
  - **Epígrafe:** centralizada e em negrito, no tamanho de fonte de destaque e sem quebra de linha, igual à emenda, mas **sem o complemento**.
  - **Ementa:** 48pt abaixo da epígrafe, com o recuo à esquerda da ementa na emenda (6,5cm), justificada. Mantém negrito e itálico; o texto de links (`span`) sai como texto simples, e os links ficam para a parte 5.
  - **Preâmbulo:** 72pt abaixo da ementa, na posição e formatação do comando de emenda. **Somente texto**: formatação inline é descartada, e cada parágrafo vira um bloco.
- A **articulação continua sem impressão** (parte 2). Cabeçalho, justificação, local, data e assinaturas continuam comentados no template.
- Nova abordagem de geração do texto, **isolada da emenda**:
  - uma **XSLT própria** transforma o `documento-articulado.xml`, que já é gerado no fluxo, em um fragmento XSL-FO;
  - o **template Velocity** continua responsável pela página (layout, XMP, blocos comentados) e recebe esse fragmento.
- **Tamanhos de fonte e espaçamentos parametrizados**, com os valores padrão da emenda (14pt, destaque 16pt, entrelinha 150%, espaço entre parágrafos 0.6em). A leitura das opções de impressão do documento fica para uma etapa posterior.
- **BREAKING (comportamento)**: o requisito atual "PDF sem impressão do texto nesta etapa" deixa de valer. O PDF passa a ter texto, o que muda o conteúdo visível, mas não a API da biblioteca.

## Capabilities

### New Capabilities
<!-- Nenhuma: evolui a capability existente. -->

### Modified Capabilities
- `pdf-documento-articulado`: o PDF passa a imprimir epígrafe, ementa e preâmbulo, substituindo o requisito "PDF sem impressão do texto nesta etapa". Os metadados XMP, o título e o hash de verificação continuam valendo.

## Impact

- **Código** (pacote `br.gov.lexml.eta.etaservices.documentoarticulado.pdf`):
  - classe nova de transformação XSLT do conteúdo;
  - classe nova de parâmetros de impressão;
  - ajustes no `DocumentoArticuladoTemplateProcessor` e no `DocumentoArticuladoPdfGenerator`.
- **Recursos** (`src/main/resources/documentoarticulado/`):
  - `documento-articulado-conteudo.xsl` (novo);
  - ajuste no `template-velocity-documento-articulado.xml`.
- **Testes:** novas fixtures a partir dos três documentos de teste anexados à #72 (articulação com alteração de norma, capítulo e seção, pena e título de dispositivo). Os testes de PDF da #71 que exigem página sem texto precisam ser ajustados.
- **Nenhuma alteração** em emenda, parecer ou `lexmljsonix`; nenhuma dependência nova (XSLT 1.0 do JDK). A API pública (`generate(json, out)` e `extractJsonFromPdf`) não muda.
- Nenhum impacto no lexeditweb nem no lexml-eta.
