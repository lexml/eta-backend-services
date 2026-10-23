# Design

## Context

Ver proposal.md (Why). Hoje `DocumentoArticuladoPdfGenerator.generate(json, out)` valida o JSON, converte para XML, gera o conteúdo FO, monta o XSL-FO e chama `DocumentoArticuladoFopProcessor.gerarPdf(xslFo, xml)`, que devolve os bytes do PDF/A-3B com o `documento-articulado.xml` embutido. Depois o gerador grava o MD5 no placeholder do XMP e escreve no destino.

O parecer é a referência: `PdfGeneratorParecer` lê cada `AnexoPDFA` em `byte[]`, separa o grupo a embutir (todos) do grupo a imprimir (`imprimir = true`) e passa ao `FOPProcessor`, que embute com `PDFA.addAttachments` (relação `Data`) e concatena com `PDFMergerUtility`. O fluxo do documento articulado é isolado do `FOPProcessor` (ver CLAUDE.md), então a lógica é reproduzida no `DocumentoArticuladoFopProcessor`, não chamada.

## Goals / Non-Goals

**Goals:**
- Manter o comportamento atual sem anexos, sem mudar o construtor nem a API existente.
- Embutir todos os anexos e concatenar os impressos, com o hash sobre o PDF final.

**Non-Goals:**
- Converter anexos não PDF (ex.: `.docx`) em páginas: só PDF pode ser impresso.
- Mudar a extração do JSON, o conversor, o template ou a XSLT.
- Garantir que o PDF final com anexos de terceiros continue em conformidade PDF/A (igual ao parecer).
- Limitar tamanho ou quantidade de anexos (responsabilidade da aplicação consumidora).

## Decisions

**1. Sobrecarga de `generate`, não do construtor.** `generate(json, out)` delega para `generate(json, null, out)`; o novo método é `generate(String json, List<AnexoPDFA> anexos, OutputStream out)`. Anexos mudam a cada chamada, e o gerador hoje não tem estado por requisição (só o conversor, fixo). *Alternativa:* construtor com anexos, rejeitada porque obrigaria uma instância por requisição.

**2. Reutilizar `parecer.AnexoPDFA`.** Decisão do usuário. Aceita-se a dependência de `documentoarticulado.pdf` para `etaservices.parecer` em troca de um tipo único para o consumidor. *Alternativa:* tipo próprio no pacote, descartada por duplicar 3 campos.

**3. Embutir todos e concatenar só os impressos (como o parecer).** O campo `imprimir` mantém a semântica existente e o front pode guardar um anexo sem imprimi-lo. *Alternativa:* só concatenar, descartada pelo pedido de embutir.

**4. Concatenação dentro de `DocumentoArticuladoFopProcessor`.** O método passa a receber os anexos (`gerarPdf(xslFo, xml, anexosEmbutidos, anexosImpressos)`; a versão de dois argumentos delega com listas vazias). Após `stamper.close()`, se houver impressos, o PDF/A em memória e os anexos vão para um `PDFMergerUtility` (PDFBox), com o PDF/A como primeira fonte, e o resultado volta como `byte[]`. Assim o gerador continua inserindo o hash sobre os bytes finais, como já faz. A detecção do tipo MIME por extensão (`.pdf`, `.docx`, demais) é reproduzida como método privado, já que o `guessMimeType` do `FOPProcessor` é privado e o fluxo é isolado.

**5. Leitura dos streams uma única vez, no gerador.** Cada `InputStream` é lido para `byte[]` (como no parecer); os mesmos bytes alimentam o embutimento e a concatenação, de modo que os dois usos não dependem de reposicionar o stream. Itens nulos ou sem stream são ignorados antes de qualquer validação.

**6. Validações antes de escrever.** O nome reservado (`documento-articulado.xml`, comparação sem diferenciar maiúsculas) é rejeitado logo depois da validação do JSON e antes da conversão. Falhas do merge viram `EtaBackendException` com o nome do anexo; como o merge ocorre em memória e o `write` é a última etapa, a regra "nada é escrito se falhar" se mantém.

## Risks / Trade-offs

- [O merge do PDFBox pode reescrever o XMP ou o catálogo e perder o placeholder do hash ou o anexo do modelo] → Testes de aceitação: PDF com anexo impresso tem hash preenchido, `documento-articulado.xml` presente e ida e volta do JSON funcionando. Se o placeholder não sobreviver, ajustar a ordem (ver Open Questions).
- [PDF final com anexo de terceiros deixa de ser PDF/A estrito] → Mesmo comportamento do parecer; aceito e documentado no CLAUDE.md.
- [Anexos grandes em memória] → Mesmo modelo do parecer; limites ficam com a aplicação consumidora.
- [Anexo `.docx` com `imprimir = true` falha no merge] → Falha clara com o nome do anexo (`EtaBackendException`); `imprimir` só vale para PDF.
- [Dependência `documentoarticulado` -> `parecer`] → Restrita ao tipo `AnexoPDFA`; documentada.

## Open Questions

- Se o PDFBox não preservar o placeholder do hash após o merge, calcular o hash antes ou depois? Responde-se no teste de aceitação da primeira tarefa de implementação; não muda os requisitos (o hash é sobre o PDF final), só a mecânica.
