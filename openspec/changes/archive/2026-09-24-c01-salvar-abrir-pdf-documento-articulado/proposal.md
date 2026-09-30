# Proposal

## Why

O editor de proposições (`lexml-eta`) já salva e abre o `documento-articulado.json` (LexML em formato jsonix, com os metadados `lexedit:Metadado` no formato do `jsonix-lexml` 2.0.0), mas só como arquivo JSON. A próxima etapa é permitir salvar a proposição em um PDF/A que carregue o texto em LexML (`documento-articulado.xml`) embutido e reabrir esse PDF no editor, como já é feito para emenda (`emenda.xml`) e parecer (`parecer.json`). A formatação visual do texto no PDF não interessa neste momento; o objetivo é o ciclo JSON -> PDF com XML -> JSON.

## What Changes

- Novo fluxo **isolado** para o documento articulado do ETA, sem reutilizar nenhuma classe, template ou configuração usados por emenda e parecer (`FOPProcessor`, `FOHelper`, `VelocityTemplateProcessor*`, `VelocityExtension`, `fop.xconf`, `ConversorLexmlJsonix`, `CliUtils`, `LexmlJsonixProperties`). Compartilham-se apenas recursos binários genéricos (`pdfa-fonts/`, `static/img/`) e os utilitários genéricos de `util/` (`EtaBackendException`, `BytesUtil`).
- **Gerar PDF**: recebe o `documento-articulado.json`, converte para LexML XML pelo executável `jsonix-lexml` (2.0.0) e gera um PDF/A-3B com o XML embutido como anexo `documento-articulado.xml`, com o hash de verificação no XMP, como nos demais documentos.
- **Template próprio** derivado do XSL-FO da emenda, com toda a parte de impressão do texto comentada (preservada para aproveitamento posterior); o PDF sai com a estrutura de página e os metadados XMP, sem texto.
- **Extrair JSON**: recebe um PDF, recupera o anexo `documento-articulado.xml` e devolve o `documento-articulado.json` convertido pelo `jsonix-lexml`. PDF sem esse anexo é rejeitado.
- **Conversor próprio JSON <-> XML** via executável `jsonix-lexml`, que trata como erro a saída vazia (o executável 2.0.0 sai com código 0 mesmo em erro, reportando apenas no stderr), usa UTF-8 explícito e tem tempo limite.
- Sem classes de domínio: o documento trafega como texto JSON/XML.
- Sem auto-configuration nem propriedade nova: as classes são POJOs montados pela aplicação consumidora, que informa o caminho do executável.

## Capabilities

### New Capabilities
- `pdf-documento-articulado`: gerar um PDF/A a partir do `documento-articulado.json` com o `documento-articulado.xml` (LexML) embutido, recuperar o JSON a partir desse PDF e a conversão JSON <-> XML pelo executável `jsonix-lexml` que sustenta os dois sentidos.

### Modified Capabilities
<!-- Nenhuma: o projeto ainda não possui specs e os fluxos de emenda/parecer não mudam. -->

## Impact

- **Código novo** em `br.gov.lexml.eta.etaservices.documentoarticulado` (subpacotes `conversor`, `pdf`, `extracao`) e recursos novos em `src/main/resources/documentoarticulado/` (template Velocity e configuração FOP próprios).
- **Nenhuma alteração** em classes, templates ou testes de emenda, parecer ou `lexmljsonix`; nenhuma dependência Maven nova (usa FOP, PDFBox, `pdfa-helper`, Velocity e Jackson já presentes).
- **API pública nova** da biblioteca (aditiva, não quebra consumidores): `ConversorDocumentoArticulado` / `ConversorDocumentoArticuladoCli`, `DocumentoArticuladoPdfGenerator`, `DocumentoArticuladoJsonExtractor`.
- **Aplicações consumidoras** (fora do escopo desta change): o `lexeditweb` precisa expor os endpoints e registrar os beans. Os executáveis `jsonix-lexml` 2.0.0 (`-linux`, `-macos`, `-win.exe`) já foram atualizados em `lexeditweb-editor/` (SHA-256 conferidos com o release em 24/09/2026), faltando apenas commitá-los. O front `lexml-eta` precisa das ações de salvar/abrir PDF.
