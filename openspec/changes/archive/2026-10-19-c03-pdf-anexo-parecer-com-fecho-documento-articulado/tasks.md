# Tasks

## 1. Fecho e testes

- [x] 1.1 Conforme design D1: `FechoDocumentoArticulado.de` deixa de esvaziar o fecho em anexo de parecer (javadoc atualizado); `anexoParecer()` continua para a justificação. Verificar pelos testes de 1.2
- [x] 1.2 Testes JUnit 5 (sem executável): no `FechoDocumentoArticuladoTest`, com `anexoParecer` true e local, data e autoria preenchidos, o fecho e as assinaturas vêm preenchidos (e `anexoParecer()` continua verdadeiro); no `DocumentoArticuladoPdfGeneratorTest`, o exemplo como anexo de parecer sem justificação e com o fecho e as cinco assinaturas logo depois da articulação. Verificar com `mvn test -Dtest="FechoDocumentoArticuladoTest,DocumentoArticuladoPdfGeneratorTest"`

## 2. Fixture e documentação

- [x] 2.1 Conforme design D2: `documento-anexo-parecer` com local, data e um senador (`ParteFinal` coerente), regenerada pelo procedimento JSON-first; no `DocumentoArticuladoPdfGeneratorTest`, a fixture termina com o fecho e a assinatura, sem justificação, e uma variação sem local nem autoria termina no último artigo; `CLAUDE.md` atualizado. Verificar com `mvn test -Dtest="DocumentoArticulado*Test,ConversorDocumentoArticuladoFake*Test,FechoDocumentoArticuladoTest"`

## 3. Verificação integrada

- [x] 3.1 Rodar `mvn clean install` e verificar que todos os testes passam e que nenhum arquivo de emenda, parecer ou `lexmljsonix` foi alterado; descartar a regravação do `emenda.pdf` (`git checkout -- emenda.pdf`)
- [x] 3.2 **Depois do 3.1**, gerar com `DocumentoArticuladoJson2PDF` e o `jsonix-lexml` real os PDFs **v13.3** de todos os documentos de teste em `pdf-documento-articulado/pdfs-gerados/v13.3/` e atualizar o `README.md` da pasta. Verificar com PDFBox o anexo de parecer (fecho e assinatura depois da articulação, sem justificação) e que os demais não mudaram em relação à v13.2; validar no veraPDF (PDF/A-3B). Registrar o resultado na conclusão da task
