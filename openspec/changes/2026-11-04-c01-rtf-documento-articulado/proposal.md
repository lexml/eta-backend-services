# Proposal

## Why

A proposição do ETA (documento articulado) só pode ser exportada em PDF/A. Quem precisa continuar editando o texto fora do editor não tem um formato que o Word abra. O RTF é a saída editável que o FOP 2.7 já oferece (`MimeConstants.MIME_RTF`), sem dependência nova, e permite avaliar a viabilidade de um formato editável antes de decidir se o DOCX é obrigatório.

## What Changes

- Novo gerador público `DocumentoArticuladoRtfGenerator`, construído com o mesmo `ConversorDocumentoArticulado` do gerador de PDF, que recebe o `documento-articulado.json` e escreve um RTF no destino, sob demanda (não é embutido no PDF).
- O RTF é renderizado pelo FOP a partir do mesmo XSL-FO do PDF (mesma conversão JSON -> XML, mesma XSLT de conteúdo, mesma página Velocity), sem PDF/A, XMP, hash, anexos nem concatenação.
- Extração, no `DocumentoArticuladoPdfGenerator`, do passo "JSON -> XSL-FO + XML" para um ponto package-private compartilhado, sem alterar o comportamento nem a API do PDF.
- Novo método no `DocumentoArticuladoFopProcessor` para renderizar XSL-FO em RTF com a fábrica FOP já existente.
- Nada é recuperável a partir do RTF: ele não carrega o modelo, então não há extração de JSON.
- O RTF garante o **conteúdo** (textos e ordem), não a fidelidade visual ao PDF; imagens que não sejam JPEG são omitidas, com aviso em vermelho no lugar (provisório, pode virar conversão para JPEG), e os links externos mantêm o endereço. As demais perdas de formatação ficam registradas como limitações conhecidas.
- Fora de escopo: DOCX, emenda e parecer, anexos, PDF/A.

## Capabilities

### New Capabilities
- `rtf-documento-articulado`: gerar, sob demanda, o RTF do documento articulado a partir do `documento-articulado.json`, com o conteúdo da proposição, as mesmas validações de entrada do PDF e as limitações de formatação conhecidas.

### Modified Capabilities

(nenhuma; a spec `pdf-documento-articulado` não muda de comportamento)

## Impact

- Código: `br.gov.lexml.eta.etaservices.documentoarticulado.pdf` (nova classe pública, refatoração interna do `DocumentoArticuladoPdfGenerator`, novo método no `DocumentoArticuladoFopProcessor`). Possível parâmetro de saída na XSLT/Velocity apenas se a PoC mostrar que o FO atual quebra o renderer RTF.
- API pública: nova classe, sem alteração nas existentes. A aplicação consumidora a monta manualmente, como as demais.
- Dependências: nenhuma (o FOP 2.7 já traz o renderer RTF).
- Testes: novos testes JUnit 5 com o `ConversorDocumentoArticuladoFake`, sem rede nem `jsonix-lexml`; inspeção visual no Word via utilitário manual.
- Documentação: `CLAUDE.md` (arquitetura de `documentoarticulado`).
