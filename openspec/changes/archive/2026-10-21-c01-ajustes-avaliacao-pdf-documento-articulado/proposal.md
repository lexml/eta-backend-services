# Proposal

## Why

A chefia avaliou a versão final dos PDFs do documento articulado e pediu quatro ajustes pequenos de apresentação. Hoje:

- os avisos de revisão e de comentários repetem o que a lista de pendências já diz;
- o rótulo dos dispositivos ("Art. 15.") se abre na justificação do parágrafo;
- os alertas não acompanham o tamanho da fonte;
- a epígrafe de projeto não numerado perde o espaço do número ("PROJETO DE LEI Nº , DE").

## What Changes

- **Alertas (ajuste 1):** só a caixa de pendências é impressa.
  - Saem as caixas "marcas de revisão" e "comentários"; o que elas avisavam já vem na lista de pendências registrada pelo editor.
  - O cabeçalho da lista passa a ser "Este documento contém pendências e não deve ser protocolado até que sejam resolvidas:".
- **Rótulo como uma palavra só (ajuste 2):** os espaços internos do rótulo dos dispositivos ("Art. 15.", "§ 1º") deixam de ser esticados pela justificação.
  - O espaço entre o rótulo e o texto continua ajustável.
  - Em alguns parágrafos com rótulo, uma ou duas palavras podem trocar de linha (efeito aceito; ver design D2). O texto e o número de páginas não mudam.
- **Alertas proporcionais à fonte (ajuste 3):** a caixa de pendências passa a usar 2pt a menos que o texto.
  - Fica 12pt com fonte 14 (igual a hoje), 14pt com 16 e 16pt com 18.
- **Epígrafe sem número (ajuste 4):**
  - a epígrafe passa a ser aparada só nas pontas, preservando os espaços internos;
  - quando entre "Nº" e a vírgula houver só espaços, o vão sai com os espaços gravados pelo editor, com no mínimo 8, como em "PROJETO DE LEI Nº        , DE".
- **Nova versão para avaliação:** três PDFs do `documento-articulado-exemplo`, sem "reduzir espaço entre linhas", com as fontes 14, 16 e 18.

## Capabilities

### New Capabilities

(nenhuma)

### Modified Capabilities

- `pdf-documento-articulado`:
  - "Alertas e pendências" removido e substituído pelo novo "Caixa de pendências": só a caixa de pendências, com novo texto e tamanho proporcional;
  - "Formatação dos dispositivos": rótulo sem espaço interno esticado;
  - "Epígrafe": trim só nas pontas e vão do número;
  - "Parâmetros de impressão": tamanho dos alertas.

## Impact

- `documentoarticulado/template-velocity-documento-articulado.xml`: bloco dos alertas.
- `documentoarticulado/documento-articulado-conteudo.xsl`: templates `bloco-dispositivo` e `lx:Epigrafe`.
- `AlertasDocumentoArticulado`: remoção de `possuiRevisoes` e `possuiComentarios`.
- `DocumentoArticuladoTemplateProcessor`: contexto do Velocity.
- `ParametrosImpressaoDocumentoArticulado`: novo tamanho dos alertas.
- Testes:
  - `AlertasDocumentoArticuladoTest`, `ParametrosImpressaoDocumentoArticuladoTest`;
  - `DocumentoArticuladoTemplateProcessorTest`, `DocumentoArticuladoConteudoTransformerTest`, `DocumentoArticuladoPdfGeneratorTest`.
- `CLAUDE.md`: descrição dos alertas, do rótulo e da epígrafe.
- Nenhuma mudança de API, dependência ou formato do anexo embutido no PDF.
