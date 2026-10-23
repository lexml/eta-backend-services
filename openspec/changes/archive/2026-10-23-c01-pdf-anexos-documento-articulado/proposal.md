# Proposal

## Why

O PDF do documento articulado (proposição do `lexml-eta`) não aceita anexos. A emenda e o parecer já permitem juntar documentos ao PDF, e o fluxo da proposição precisa do mesmo recurso: arquivos adicionados ao final do PDF e guardados dentro dele. O `PdfGeneratorParecer` é a referência de comportamento: a lista de anexos vai separada do modelo, todos os anexos são embutidos no PDF/A e só os marcados para impressão são concatenados às páginas. No documento articulado o JSON é o próprio LexML e não tem campo para anexos, o que também leva à lista separada.

## What Changes

- `DocumentoArticuladoPdfGenerator` ganha a sobrecarga `generate(String json, List<AnexoPDFA> anexos, OutputStream outputStream)`. O `generate(json, outputStream)` atual continua igual e passa a delegar com lista vazia; o construtor não muda.
- Todos os anexos informados são embutidos no PDF/A-3B como arquivos anexos com relação `Data`, ao lado do `documento-articulado.xml`.
- Os anexos com `imprimir = true` têm as páginas concatenadas, na ordem recebida, ao final do PDF do documento articulado.
- O hash de verificação (MD5) continua sendo calculado sobre o PDF final, já com as páginas dos anexos.
- Um anexo chamado `documento-articulado.xml` é rejeitado com `EtaBackendException`, pois conflitaria com o anexo do modelo usado na recuperação do JSON.
- Qualquer falha com os anexos (PDF ilegível entre os impressos, nome reservado) lança `EtaBackendException` sem escrever nada no destino.
- Sem anexos (lista ausente, vazia ou só com itens nulos) o PDF gerado é o mesmo de hoje.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `pdf-documento-articulado`: novos requisitos para os anexos do PDF (embutir, concatenar, nome reservado, falhas, compatibilidade sem anexos).

## Impact

- `DocumentoArticuladoPdfGenerator` (nova sobrecarga, leitura dos streams para memória) e `DocumentoArticuladoFopProcessor` (anexos no `PDFA` e concatenação com `PDFMergerUtility`).
- Reutiliza `br.gov.lexml.eta.etaservices.parecer.AnexoPDFA` (decisão: a dependência do pacote `parecer` é aceita em troca de um tipo único para o consumidor).
- Sem mudança em `DocumentoArticuladoJsonExtractor`, no conversor `jsonix-lexml`, no template Velocity, na XSLT, na emenda, no parecer ou em `lexmljsonix`. Sem novas dependências (PDFBox e `pdfa-helper` já estão no `pom.xml`).
- Testes JUnit com `ConversorDocumentoArticuladoFake` e PDFs de `src/test/resources`; `CLAUDE.md` atualizado.
