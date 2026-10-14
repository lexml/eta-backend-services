# Tasks

## 1. Leitura do atributo e fecho

- [x] 1.1 Conforme design D1 e D2: `FechoDocumentoArticulado.anexoParecer(documento)` (só booleano `true`) e fecho vazio em anexo de parecer. Verificar pelos testes de 1.2
- [x] 1.2 Estender o `FechoDocumentoArticuladoTest` (JUnit 5, sem executável): `anexoParecer` true, false, ausente e de outro tipo (texto "true"); com true, local, data e assinaturas vazios mesmo preenchidos no documento; com false/ausente, como antes. Verificar com `mvn test -Dtest=FechoDocumentoArticuladoTest`

## 2. XSLT e gerador

- [x] 2.1 Conforme design D3: `xsl:param imprimirJustificacao` na `documento-articulado-conteudo.xsl` (padrão `'true'`), condicionando a chamada da justificação; terceiro argumento no `DocumentoArticuladoConteudoTransformer.transformar` (sobrecarga de dois argumentos = `true`); o `DocumentoArticuladoPdfGenerator` passa `!anexoParecer`. Verificar pelos testes de 2.2
- [x] 2.2 Estender o `DocumentoArticuladoConteudoTransformerTest`: com `imprimirJustificacao` falso, o FO não tem o título "JUSTIFICAÇÃO", o corpo nem `fo:footnote` da justificação, e mantém parte inicial e articulação; com verdadeiro, como hoje. Verificar com `mvn test -Dtest=DocumentoArticuladoConteudoTransformerTest`

## 3. Fixture, PDF e documentação

- [x] 3.1 Conforme design D4: criar pelo procedimento JSON-first o par `documento-anexo-parecer` (`anexoParecer="true"`, articulação, justificação com nota, sem local/data/autoria/`ParteFinal`, uma pendência), registrá-lo no `ConversorDocumentoArticuladoFake` (`ANEXO_PARECER`) e atualizar o `ConversorDocumentoArticuladoFakeTest` se ele enumerar as fixtures. Estender o `DocumentoArticuladoPdfGeneratorTest` (JUnit 5, PDFBox): na fixture, o PDF termina no último artigo, sem "JUSTIFICAÇÃO", sem o texto e a nota da justificação, sem fecho nem assinaturas, com cabeçalho e caixa de pendências; variações do exemplo com `anexoParecer` true (local e autoria preenchidos não impressos), false e ausente (tudo impresso); JSON recuperado igual ao original. Verificar com `mvn test -Dtest="DocumentoArticulado*Test,ConversorDocumentoArticuladoFake*Test,FechoDocumentoArticuladoTest"`
- [x] 3.2 Atualizar o `CLAUDE.md` (seção `etaservices.documentoarticulado` e Testes: anexo de parecer, a fixture nova). Verificar pela revisão do diff

## 4. Verificação integrada

- [x] 4.1 Rodar `mvn clean install` e verificar que todos os testes passam e que nenhum arquivo de emenda, parecer ou `lexmljsonix` foi alterado; descartar a regravação do `emenda.pdf` (`git checkout -- emenda.pdf`)
- [x] 4.2 **Depois do 4.1**, gerar com `DocumentoArticuladoJson2PDF` e o `jsonix-lexml` real os PDFs **v13** de todos os documentos de teste (inclusive o `documento-anexo-parecer`) em `pdf-documento-articulado/pdfs-gerados/v13/` e atualizar o `README.md` da pasta. Verificar com PDFBox que o anexo de parecer termina na articulação e que os demais não mudaram em relação à v12, e o JSON recuperado; inspeção visual do anexo de parecer; validar no veraPDF (PDF/A-3B). Registrar o resultado na conclusão da task
