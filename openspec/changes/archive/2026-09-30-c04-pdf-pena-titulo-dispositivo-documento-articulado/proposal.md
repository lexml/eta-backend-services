# Proposal

## Why

Depois da parte 3 da issue [#72](https://github.com/lexml/eta-backend-services/issues/72) (change `2026-09-30-c03-pdf-agrupadores-documento-articulado`), o PDF ainda omite a **pena** e o **título de dispositivo**. No documento de teste de crimes, cinco penas ("Pena – reclusão, de 4 (quatro) a 12 (doze) anos, e multa.") e três títulos ("Desvio ou apropriação de recursos e insumos da saúde") não aparecem, e um projeto penal sai sem as penas. Esta é a **parte 6** das 6 em que a #72 foi dividida. É feita antes da parte 4 porque é menor e independente, enquanto a dúvida da parte 4 sobre o "(NR)" é esclarecida.

## What Changes

- **Pena:** impressa onde aparece no documento (no fim do caput ou do parágrafo), com a mesma formatação dos dispositivos de artigo (recuo de primeira linha de 2,5cm, justificado, sem espaço extra), mas com o **rótulo "Pena –" sem negrito**.
- **Título de dispositivo:** impresso **antes** do dispositivo a que pertence (em geral o artigo), com a mesma formatação dos dispositivos, **sem rótulo e todo em negrito**. Não é centralizado, diferente dos agrupadores. É mantido na mesma página do dispositivo que o segue.
- Pena ou título vazios não geram bloco.
- Continuam sem impressão: alteração de norma e omissis (parte 4), links (parte 5), além de justificação, local e data e assinaturas.

## Capabilities

### New Capabilities
<!-- Nenhuma: evolui a capability existente. -->

### Modified Capabilities
- `pdf-documento-articulado`:
  - "Conteúdo impresso" deixa de excluir pena e título de dispositivo;
  - "Dispositivos da articulação" passa a incluir a pena;
  - "Formatação dos dispositivos" passa a admitir a exceção do rótulo da pena;
  - novo requisito "Pena e título de dispositivo".

## Impact

- **Código:** só a XSLT `src/main/resources/documentoarticulado/documento-articulado-conteudo.xsl`: parâmetro `rotuloNegrito` no `bloco-dispositivo` e templates de `Pena` e `TituloDispositivo`, hoje vazios. O Java, o template Velocity e a API não mudam.
- **Testes:** novos casos no `DocumentoArticuladoConteudoTransformerTest` e no `DocumentoArticuladoPdfGeneratorTest` com o documento de teste da pena. Os testes das partes 2 e 3 que exigem a ausência de "Pena –" e do título precisam ser invertidos.
- **Nenhuma alteração** em emenda, parecer ou `lexmljsonix`; nenhuma dependência nova.
- **PDFs de referência:** nova versão **v4** em `pdf-documento-articulado/pdfs-gerados/` (pasta local, não versionada), revalidada no veraPDF.
