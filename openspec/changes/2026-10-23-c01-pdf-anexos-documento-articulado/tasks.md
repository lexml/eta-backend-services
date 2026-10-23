# Tasks

## 1. Embutir e concatenar anexos no processador FOP

- [x] 1.1 Estender `DocumentoArticuladoFopProcessor.gerarPdf` para receber os anexos a embutir e os a imprimir (a versão atual delega com listas vazias), embutindo cada anexo com `PDFAttachmentFile` de relação `Data` e tipo MIME por extensão; verificar com teste que o PDF contém os anexos com o conteúdo original ao lado do `documento-articulado.xml`
- [x] 1.2 Concatenar com `PDFMergerUtility` as páginas dos anexos impressos, na ordem recebida, depois do PDF/A em memória; verificar com teste que o número de páginas é a soma e que a ordem é a esperada
- [x] 1.3 Converter falha de leitura de anexo impresso em `EtaBackendException` com o nome do anexo; verificar com teste de anexo impresso que não é PDF
- [x] 1.4 Testes JUnit 5 do processador (sem rede e sem `jsonix-lexml`): sem anexos igual ao atual, um impresso, vários com mistura de impressos e não impressos, `.docx` só embutido, e o XMP e o modelo preservados após o merge; rodar `mvn test -Dtest=DocumentoArticuladoFopProcessorTest`

## 2. API do gerador de PDF

- [x] 2.1 Adicionar `DocumentoArticuladoPdfGenerator.generate(String, List<AnexoPDFA>, OutputStream)`, ignorando itens nulos ou sem stream, lendo cada stream uma só vez para `byte[]` e fazendo o `generate(json, out)` atual delegar com lista vazia; verificar que os testes existentes de `DocumentoArticuladoPdfGeneratorTest` continuam passando sem alteração
- [x] 2.2 Rejeitar anexo chamado `documento-articulado.xml` (sem diferenciar maiúsculas) com `EtaBackendException` antes de converter e sem escrever no destino; verificar com teste
- [x] 2.3 Manter o hash sobre os bytes finais; verificar com teste que, com anexos impressos, o placeholder some e o MD5 gravado é o do arquivo
- [x] 2.4 Testes JUnit 5 do gerador usando `ConversorDocumentoArticuladoFake` e PDFs de anexo em `src/test/resources`: geração com anexos, nada escrito em caso de falha, e ida e volta do JSON com `DocumentoArticuladoJsonExtractor` em PDF com anexos; rodar `mvn test -Dtest=DocumentoArticuladoPdfGeneratorAnexosTest` (classe nova, para não inchar `DocumentoArticuladoPdfGeneratorTest`)

## 3. Documentação e verificação geral

- [ ] 3.1 Atualizar o `CLAUDE.md` (seção de `documentoarticulado`) com a nova sobrecarga, a semântica de embutir e imprimir, o nome reservado e o exemplo de montagem; verificar a revisão do texto
- [ ] 3.2 Rodar `mvn test` completo e confirmar que não há regressão em emenda, parecer e documento articulado
- [ ] 3.3 Rodar `openspec validate 2026-10-23-c01-pdf-anexos-documento-articulado --strict` e verificar que não há erros

## Workflow follow-up

- Arquivar a change depois da revisão (`/opsx:archive`), mesclando o delta em `openspec/specs/pdf-documento-articulado/spec.md`.
