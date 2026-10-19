# Tasks

## 1. Template e testes

- [x] 1.1 Conforme design D1: no `template-velocity-documento-articulado.xml`, o ramo sem data do fecho passa a ser `$local,` (sem espaços para preencher nem ponto final), com o comentário do bloco atualizado. Verificar pelos testes de 1.2
- [x] 1.2 Testes JUnit 5 (sem executável): no `DocumentoArticuladoTemplateProcessorTest`, fecho "Sala das Sessões," com data vazia, ausente e inválida (o caso com data continua igual); no `DocumentoArticuladoPdfGeneratorTest`, a justificação longa com o fecho "Sala das Sessões," seguido da assinatura, sem "de". Verificar com `mvn test -Dtest="DocumentoArticuladoTemplateProcessorTest,DocumentoArticuladoPdfGeneratorTest"`

## 2. Documentação

- [x] 2.1 Atualizar o `CLAUDE.md` (fecho sem data) e, no `pdf-documento-articulado/historico.md`, a anotação da alternativa do `ParteFinal` (a diferença da data vazia deixa de existir). Verificar pela revisão do diff

## 3. Verificação integrada

- [x] 3.1 Rodar `mvn clean install` e verificar que todos os testes passam e que nenhum arquivo de emenda, parecer ou `lexmljsonix` foi alterado; descartar a regravação do `emenda.pdf` (`git checkout -- emenda.pdf`)
- [x] 3.2 **Depois do 3.1**, gerar com `DocumentoArticuladoJson2PDF` e o `jsonix-lexml` real os PDFs **v13.1** de todos os documentos de teste em `pdf-documento-articulado/pdfs-gerados/v13.1/` e atualizar o `README.md` da pasta. Verificar com PDFBox o fecho "Sala das Sessões," na justificação longa e que os demais não mudaram em relação à v13; validar no veraPDF (PDF/A-3B). Registrar o resultado na conclusão da task
