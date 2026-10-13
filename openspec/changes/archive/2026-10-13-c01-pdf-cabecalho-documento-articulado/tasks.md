# Tasks

## 1. Fixtures

- [x] 1.1 Conforme design D4: no `documento-articulado-exemplo`, `textoCabecalho="Gabinete do Senador Davi Alcolumbre"`; no `documento-com-justificacao-longa`, `imprimirBrasao="false"` e `textoCabecalho="Comissão de Desenvolvimento Regional e Turismo"`; regenerar os pares com o `jsonix-lexml` (JSON-first) e conferir que, fora das opções de impressão, nada mudou. Verificar com `mvn test -Dtest=ConversorDocumentoArticuladoFakeTest`

## 2. Cabeçalho

- [x] 2.1 No `DocumentoArticuladoTemplateProcessor`, conforme design D1 e D2: ler `opcoesImpressao` (padrões do editor quando ausente), a casa pela autoridade da URN e o brasão de `/static/img/brasao.jpg` (base64, carregado uma vez, `EtaBackendException` se faltar); pôr no contexto `imprimirBrasao`, `brasao`, `casaLegislativa`, `textoCabecalho` (escapado) e `temCabecalho`; retirar `documento`. Verificar pelos testes de 2.3
- [x] 2.2 No `template-velocity-documento-articulado.xml`, conforme design D3: região do cabeçalho no `firstPage` (4cm/3cm com cabeçalho, 1cm sem) e `fo:static-content` `header-principal` com brasão, casa (ITC Stone Sans 11pt) e texto, no lugar do bloco comentado da emenda; atualizar o comentário de cabeçalho do template. Verificar pelos testes de 2.3
- [x] 2.3 Estender o `DocumentoArticuladoTemplateProcessorTest` (JUnit 5, sem executável): região e margem com e sem cabeçalho, brasão em data URI, casa para `senado.federal`, `camara.deputados`, `congresso.nacional` e `federal`, texto escapado (`&`, `<`), só texto sem brasão, padrões com `OpcoesImpressao` ausente. Verificar com `mvn test -Dtest=DocumentoArticuladoTemplateProcessorTest`

## 3. PDF e documentação

- [x] 3.1 Atualizar o `DocumentoArticuladoPdfGeneratorTest` (JUnit 5, PDFBox, sem executável): brasão desenhado só na primeira página, centralizado e com ~1,9 cm de largura; "SENADO FEDERAL" em ITC Stone Sans acima da epígrafe; texto do cabeçalho no exemplo; cabeçalho só com texto na longa; MPV só com brasão; deslocamento do corpo com cabeçalho; ajustar os testes existentes afetados pelo deslocamento. Confirmar que o `DocumentoArticuladoJsonExtractorTest` continua passando. Verificar com `mvn test -Dtest="DocumentoArticulado*Test"`
- [x] 3.2 Atualizar o `CLAUDE.md` (seção `etaservices.documentoarticulado` e Testes): cabeçalho, opções lidas, casa pela URN, variáveis do contexto do Velocity. Verificar pela revisão do diff

## 4. Verificação integrada

- [x] 4.1 Rodar `mvn clean install` e verificar que todos os testes passam e que nenhum arquivo de emenda, parecer ou `lexmljsonix` foi alterado; descartar a regravação do `emenda.pdf` (`git checkout -- emenda.pdf`)
- [x] 4.2 **Depois do 4.1**, gerar com `DocumentoArticuladoJson2PDF` e o `jsonix-lexml` real os PDFs **v9** de todos os documentos de teste em `pdf-documento-articulado/pdfs-gerados/v9/` e atualizar o `README.md` da pasta. Verificar com PDFBox o cabeçalho (brasão, casa, texto) e o JSON recuperado; comparar visualmente com o cabeçalho da emenda; validar no veraPDF (PDF/A-3B), com atenção à imagem do brasão e à fonte ITC Stone Sans. Registrar o resultado na conclusão da task

  **Resultado (13/10/2026):** PDFs v9 de todos os documentos de teste em `pdf-documento-articulado/pdfs-gerados/v9/`. PDFBox: brasão de 53,6pt centralizado só na página 1 (igual ao medido numa emenda real), casa em ITC Stone Sans 11pt, texto do cabeçalho acima da epígrafe, corpo 3cm mais abaixo; exemplo com brasão + SENADO FEDERAL + texto, justificação longa só com texto, MPV só com brasão. JSON recuperado igual em 5 documentos; no exemplo e no catálogo difere só nos espaços entre elementos inline (limitação conhecida). Inspeção visual conferida com o cabeçalho da emenda. veraPDF: os sete conformes PDF/A-3B (validado pelo usuário).
