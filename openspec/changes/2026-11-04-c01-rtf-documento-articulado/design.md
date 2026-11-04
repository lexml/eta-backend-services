# Design

## Context

Motivação em `proposal.md` (Why); requisitos em `specs/rtf-documento-articulado/spec.md`.

Hoje `DocumentoArticuladoPdfGenerator.generate` faz, em sequência: validar o JSON, converter para XML, calcular parâmetros e opções de impressão, aplicar a XSLT de conteúdo, aplicar o template Velocity de página (gerando o `xslFo`) e chamar `DocumentoArticuladoFopProcessor.gerarPdf`, que exige PDF/A e XMP, carimba anexos, concatena PDFs e devolve os bytes; por fim o gerador insere o hash. Só os dois últimos passos são específicos do PDF.

O `DocumentoArticuladoFoHelper` já separa o `x:xmpmeta` do FO (`getFoSemXmpmeta()`), então o mesmo FO pode ser entregue a outro renderer. O FOP 2.7 traz o renderer RTF em `fop-core` (`MimeConstants.MIME_RTF`), mas não tem renderer DOCX. O fluxo de documento articulado é isolado do `printing.pdf.FOPProcessor` (emenda/parecer) e não deve passar a depender dele.

Referência externa: o `FOPProcessor` do projeto madoc gera RTF chamando o mesmo método com `MIME_RTF` e escrevendo direto no destino, sem o pós-processamento de PDF/A.

## Goals / Non-Goals

**Goals:**
- Gerar RTF sob demanda reaproveitando validação, conversão, XSLT e página do PDF, sem duplicar essa lógica.
- Manter o comportamento e a API do gerador de PDF inalterados.
- Registrar, por meio de uma PoC, o que o renderer RTF do FOP preserva e perde neste documento.

**Non-Goals:**
- DOCX (decisão posterior, conforme a necessidade).
- Fidelidade visual ao PDF; extração do JSON a partir do RTF; anexos; PDF/A; emenda e parecer.
- Embutir o RTF no PDF como anexo `ALTERNATIVE` (como faz o madoc).

## Decisions

### 1. Classe pública irmã `DocumentoArticuladoRtfGenerator`
`generateRtf`-no-gerador-de-PDF foi descartado: o nome `DocumentoArticuladoPdfGenerator` ficaria enganoso. Uma classe irmã, construída com o mesmo `ConversorDocumentoArticulado`, é simétrica com `DocumentoArticuladoJsonExtractor` e deixa o gerador de PDF intocado em API. A aplicação consumidora a monta manualmente, como as demais. Assinatura: `generate(String json, OutputStream out)`, com os mesmos contratos de erro do PDF (`EtaBackendException`, nada escrito em caso de falha, `IOException` na escrita).

### 2. Extrair o passo "JSON -> xslFo + xml" para um ponto package-private compartilhado
O trecho de `generate` entre `validar` e `gerarPdf` passa para uma classe/método package-private que devolve `xslFo` e `xml`. Os dois geradores o usam. Alternativa descartada: duplicar o encadeamento no gerador de RTF (divergiria a cada nova regra de impressão). A extração não pode mudar o PDF; a rede de proteção são os testes atuais do PDF, que devem continuar passando sem alteração.

### 3. Novo método no `DocumentoArticuladoFopProcessor`: renderizar RTF
`gerarRtf(String xslFo)` usa a fábrica FOP existente com `newFop(MIME_RTF, userAgent, out)` e a mesma transformação identidade sobre `getFoSemXmpmeta()`, sem PDF/A, XMP, anexos, hash nem merge. O `FOUserAgent` não recebe `pdf-a-mode`, acessibilidade nem data de criação. Os bytes só vão ao destino depois de renderizados por completo (buffer em memória), para garantir "nada escrito em caso de falha". Alternativa descartada: parametrizar `gerarPdf` com o MIME, como no madoc, porque aqui o método exige PDF/A e XMP logo na entrada e misturaria dois fluxos num só.

### 4. Mesmo XSL-FO; variante só se a PoC exigir
Primeiro tentar o FO atual. O renderer RTF do FOP é legado e pode ignorar ou rejeitar construções usadas aqui (`page-sequence-master` com páginas condicionais, `fo:leader`, tabela dentro de tabela, `fo:footnote`, `fo:static-content`, região `before`). Se a PoC mostrar falha ou perda de conteúdo exigido pela spec, introduzir um parâmetro de saída (por exemplo `formato`) na XSLT de conteúdo e no template Velocity que simplifique só essas construções, mantendo o PDF idêntico. Não se antecipa essa variante sem evidência.

### 5. Critério de aceitação por conteúdo, não por layout
A spec exige presença e ordem do texto (epígrafe, ementa, preâmbulo, articulação, justificação, fecho, assinaturas, pendências) e exclusão de texto revisado como excluído. Cabeçalho com brasão, omissis pontilhado, recuos e notas de rodapé são "melhor esforço", registrados como limitações conhecidas depois da PoC. O RTF cita a fonte pelo nome (Gentium Basic) sem embuti-la: se a fonte não existir na máquina do usuário, o Word usa substituta (limitação aceita).

### 6. Testes sem Word
Testes JUnit 5 com `ConversorDocumentoArticuladoFake` e as fixtures existentes: o resultado começa com `{\rtf`, tem chaves balanceadas e contém os trechos de texto esperados. Como o RTF escapa acentos (`\'e7`, `\uN`), as asserções comparam o texto extraído do RTF por um decodificador simples dentro do teste (ou palavras sem acento); um RTF-para-texto completo não é necessário. A verificação visual no Word é um checklist manual com um utilitário `main` nos moldes do `Json2PDF`.

## Risks / Trade-offs

- [O renderer RTF do FOP perde ou deforma construções do FO atual] → PoC com os documentos da #72/#75 antes de fixar a spec; variante de FO (decisão 4) só se necessário; limitações documentadas.
- [Conteúdo exigido pela spec some no RTF (ex.: pendências no cabeçalho/região, assinaturas em tabela)] → testes de conteúdo por fixture; se faltar, tratar na variante de FO.
- [Exceções do renderer RTF com FO fora do subconjunto suportado] → capturar e relançar como `EtaBackendException`; nenhuma escrita parcial.
- [Refatoração do passo compartilhado altera o PDF por engano] → manter os testes atuais do PDF sem edição e rodá-los como critério de conclusão.
- [Usuário espera DOCX] → fora de escopo; o RTF abre no Word e pode ser salvo como DOCX; decisão sobre DOCX nativo fica para depois da avaliação.
- [Dependência de um renderer legado, não mantido] → isolado em um método; troca por outra solução (ex.: gerador DOCX) não afeta a API pública se o contrato de entrada e saída for mantido.
