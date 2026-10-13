# Tasks

## 1. Opções e parâmetros

- [x] 1.1 Conforme design D1 e D2: criar `OpcoesImpressaoDocumentoArticulado` (leitura única, padrões do editor, `tamanhoFonte` só 14/16/18) e `ParametrosImpressaoDocumentoArticulado.de(...)` com `getEspacoAntesTituloJustificacao()` e `getEspacoAntesCorpoJustificacao()`; o `DocumentoArticuladoTemplateProcessor` passa a usar `OpcoesImpressaoDocumentoArticulado` para o cabeçalho e o `DocumentoArticuladoPdfGenerator` deixa de usar `padrao()`. Verificar pelos testes de 1.2
- [x] 1.2 Testes JUnit 5 (sem executável): `ParametrosImpressaoDocumentoArticuladoTest` (14, 16, 18, valor fora, ausente, reduzir, combinações) e um teste de `OpcoesImpressaoDocumentoArticulado` (padrões, tipos inválidos); confirmar que o `DocumentoArticuladoTemplateProcessorTest` continua passando sem mudança de comportamento no cabeçalho. Verificar com `mvn test -Dtest="ParametrosImpressao*Test,OpcoesImpressao*Test,DocumentoArticuladoTemplateProcessorTest"`

## 2. XSLT

- [x] 2.1 Conforme design D3: `xsl:param` `espacoAntesTituloJustificacao` e `espacoAntesCorpoJustificacao` na `documento-articulado-conteudo.xsl`, no lugar de 26pt e 14pt; passá-los no `DocumentoArticuladoConteudoTransformer`; comentário nos 48pt/72pt fixos. Verificar pelos testes de 2.2
- [x] 2.2 Estender o `DocumentoArticuladoConteudoTransformerTest`: com parâmetros de "reduzir" e fonte 18, o FO tem `line-height` 120%, `margin-bottom` 0.45em, título com 22pt antes e corpo com 10pt antes, epígrafe em 18pt; 48pt e 72pt inalterados. Verificar com `mvn test -Dtest=DocumentoArticuladoConteudoTransformerTest`

## 3. Fixture, PDF e documentação

- [x] 3.1 Conforme design D4: `tamanhoFonte="18"` e `reduzirEspacoEntreLinhas="true"` no `documento-articulado-exemplo` (regenerar o par pelo procedimento JSON-first e conferir que só as opções mudaram); atualizar o `DocumentoArticuladoPdfGeneratorTest` (JUnit 5, PDFBox): texto em 18pt e epígrafe em 18pt no exemplo, entrelinha de 120% (distância entre linhas = 1,2 x fonte), variação com fonte 16 e com 14 + reduzir registradas no `ConversorDocumentoArticuladoFake`, notas e células proporcionais, cabeçalho e 48pt/72pt inalterados; ajustar os testes existentes que medem posições no exemplo. Verificar com `mvn test -Dtest="DocumentoArticulado*Test,ConversorDocumentoArticuladoFakeTest"`
- [x] 3.2 Atualizar o `CLAUDE.md` (seção `etaservices.documentoarticulado` e Testes): opções de impressão lidas, valores, o que é fixo, exemplo com 18 + reduzir. Verificar pela revisão do diff

## 4. Verificação integrada

- [x] 4.1 Rodar `mvn clean install` e verificar que todos os testes passam e que nenhum arquivo de emenda, parecer ou `lexmljsonix` foi alterado; descartar a regravação do `emenda.pdf` (`git checkout -- emenda.pdf`)
- [x] 4.2 **Depois do 4.1**, gerar com `DocumentoArticuladoJson2PDF` e o `jsonix-lexml` real os PDFs **v10** de todos os documentos de teste em `pdf-documento-articulado/pdfs-gerados/v10/` e atualizar o `README.md` da pasta. Verificar com PDFBox os tamanhos e entrelinhas do exemplo (18 + reduzir) e dos demais (padrão), e o JSON recuperado; inspeção visual do exemplo (tabelas, listas, notas e imagens em 18pt); validar no veraPDF (PDF/A-3B). Registrar o resultado na conclusão da task
