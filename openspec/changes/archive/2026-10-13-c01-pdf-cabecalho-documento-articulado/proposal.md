# Proposal

## Why

A issue [#74](https://github.com/lexml/eta-backend-services/issues/74) pede o cabeçalho no PDF da proposição, "respeitando as configurações de impressão (deixar para configurar tamanho de letra e espaçamento por último)". Hoje o PDF do documento articulado não tem cabeçalho: o bloco da emenda está comentado no template ("NÃO RETIRAR") e as opções de impressão do documento (`lexedit:OpcoesImpressao`, especificação 02 do lexml-eta) não são lidas.

A #74 foi dividida em duas partes. Esta é a **parte A**: o cabeçalho (brasão, nome da casa legislativa e texto do cabeçalho), conforme `imprimirBrasao` e `textoCabecalho`. A **parte B** (tamanho da fonte e espaçamento entre linhas: `tamanhoFonte` e `reduzirEspacoEntreLinhas`) fica para uma change seguinte, como a própria issue sugere.

## What Changes

- O PDF passa a ter, **na primeira página**, o cabeçalho da emenda: o Brasão da República centralizado com o nome da casa legislativa abaixo (em ITC Stone Sans 11pt) quando `imprimirBrasao` é verdadeiro, e o `textoCabecalho` centralizado quando informado. Sem brasão e sem texto, não há cabeçalho e o corpo começa como hoje.
- O **nome da casa** vem da autoridade da URN do documento: `senado.federal` -> SENADO FEDERAL, `camara.deputados` -> CÂMARA DOS DEPUTADOS, `congresso.nacional` -> CONGRESSO NACIONAL; outra autoridade -> só o brasão.
- **Opções ausentes** valem o padrão do editor lexml-eta (`imprimirBrasao = true`, `textoCabecalho` vazio), como define a especificação 02 ("a ausência de uma opção significa o comportamento padrão da aplicação").
- **Exemplos**: o `documento-articulado-exemplo` passa a ter texto de cabeçalho (cobre brasão + casa + texto) e uma fixture passa a ter o brasão desligado com texto (cabeçalho só com texto).

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `pdf-documento-articulado`: novo requisito "Cabeçalho".

## Impact

- `DocumentoArticuladoTemplateProcessor`: lê `OpcoesImpressao` e a autoridade da URN do JSON e coloca no contexto do Velocity o brasão (base64 de `static/img/brasao.jpg`), o nome da casa e o texto do cabeçalho.
- `template-velocity-documento-articulado.xml`: região do cabeçalho na primeira página e `fo:static-content` com brasão, casa e texto, no lugar do bloco comentado da emenda.
- Testes (`DocumentoArticuladoTemplateProcessorTest`, `DocumentoArticuladoPdfGeneratorTest`) e fixtures `documento-articulado-exemplo` e `documento-com-justificacao-longa`.
- `CLAUDE.md`.
- Sem mudança na XSLT do conteúdo, na API pública ou em dependências; `static/img/` já é compartilhado. Sem impacto em emenda, parecer ou `lexmljsonix`.
