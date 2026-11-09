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

### 4. Adaptar o XSL-FO ao renderer RTF no Java, sem variante na XSLT
O renderer RTF do FOP é legado e não entende algumas construções aceitas pelo PDF. A PoC (ver "Resultado da PoC") achou três que precisam de ajuste: imagem que não é JPEG (o renderer joga a exceção e o base64 como texto do documento), `external-destination="url('...')"` (copiado com o `url('')`) e colunas de tabela proporcionais ou em porcentagem (largura zero no RTF). Em vez de um parâmetro de saída na XSLT/Velocity, a árvore do XSL-FO é ajustada em Java antes da renderização (`DocumentoArticuladoFoRtf`, chamado só por `gerarRtf`): imagens `data:` que não sejam JPEG são trocadas por um aviso em vermelho, o `url('...')` dos links externos é removido, a tabela externa de centralização da justificação é removida (no RTF viraria tabela aninhada; perde-se a centralização) e as colunas ganham largura absoluta calculada sobre a largura útil da página (164 mm, A4 menos as margens do template). Assim a XSLT, o template Velocity e o PDF ficam intocados.

Dois ajustes são feitos no próprio texto RTF já renderizado (`ajustarRtf`), porque o renderer não converte `line-height` e escreve a fonte sem família: a entrelinha vira `\sl<n>\slmult1` (150% do corpo no PDF equivale a 1,5 / 1,15 de linha simples no Word, ou 120% com "reduzir"), inserida após `\sectd` e após cada `\pard` (que reinicia as propriedades de parágrafo); e a fonte `Gentium Basic` ganha `\froman` e `\falt Georgia`, para o Word cair em uma serifada, e não em Arial, quando ela não estiver instalada. O fator 1,15 é uma aproximação e pode ser calibrado após a conferência no Word.

Notas de rodapé: o FOP escreve `\footnote \ftnalt` (nota de fim: iria para o fim do documento), e o `\ftnalt` é removido para a nota ir ao rodapé da página; e o `font-size` 0.7em dos sobrescritos e subscritos é retirado do FO, porque o Word já reduz o texto sobrescrito e o número da nota ficaria minúsculo.

Cabeçalho: o conteúdo do `static-content` `header-principal` (brasão JPEG, casa legislativa e `textoCabecalho`) é copiado para o início do `fo:flow`, antes da caixa de pendências, e a região original fica no FO (o FOP exige um bloco nela). Como o PDF só gera esse conteúdo quando `imprimirBrasao` ou `textoCabecalho` o pedem, o RTF herda a mesma condição. Alternativa descartada: variante na XSLT (obrigaria a manter dois caminhos de saída no XSL e testar o PDF contra regressão a cada ajuste). A omissão do PNG é provisória: pode virar conversão para JPEG depois.

### 5. Critério de aceitação por conteúdo, não por layout
A spec exige presença e ordem do texto (epígrafe, ementa, preâmbulo, articulação, justificação, fecho, assinaturas, pendências) e exclusão de texto revisado como excluído. Cabeçalho com brasão, omissis pontilhado, recuos e notas de rodapé são "melhor esforço", registrados como limitações conhecidas depois da PoC. O RTF cita a fonte pelo nome (Gentium Basic) sem embuti-la: se a fonte não existir na máquina do usuário, o Word usa substituta (limitação aceita).

### 6. Testes sem Word
Testes JUnit 5 com `ConversorDocumentoArticuladoFake` e as fixtures existentes: o resultado começa com `{\rtf`, tem chaves balanceadas e contém os trechos de texto esperados. Como o RTF escapa acentos (`\'e7`, `\uN`), as asserções comparam o texto extraído do RTF por um decodificador simples dentro do teste (ou palavras sem acento); um RTF-para-texto completo não é necessário. A verificação visual no Word é um checklist manual com um utilitário `main` nos moldes do `Json2PDF`.

## Resultado da PoC (task 2.2)

RTF gerado com o FO atual, sem variante, para os 8 documentos de teste (`exemplo`, `documento-com-articulacao-e-alteracao-de-norma`, `documento-com-capitulo-e-secao`, `documento-com-pena-e-titulo-de-dispositivo`, `documento-com-remissoes-internas`, `documento-com-justificacao`, `documento-com-justificacao-longa`, `documento-anexo-parecer`): nenhum lançou exceção. Conferência feita sobre o texto extraído do RTF e sobre as marcações RTF; a conferência visual no Word fica para a task 4.1.

| Construção | Resultado |
|---|---|
| Pendências, epígrafe, ementa, preâmbulo, agrupadores, articulação, rótulos, aspas e "(NR)" | Preservados, na ordem do PDF; a caixa de pendências sai antes da epígrafe, em negrito vermelho com fundo |
| Texto excluído em revisão (`del`) | Ausente, como no PDF |
| Justificação, local e data, assinaturas | Presentes; assinaturas em tabela |
| Anexo de parecer | Sem justificação; local e data presentes |
| Omissis (linha pontilhada) | Preservado (tabulação com `\tldot`) |
| Notas de rodapé | Preservadas como notas reais (`\footnote`) |
| Listas | Texto preservado; marcadores e numeração a conferir no Word |
| **Tabelas (justificação e assinaturas)** | **O texto sai, mas todas as células ficam com largura zero (`\cellx0`), porque o renderer não resolve `proportional-column-width` nem porcentagem; o Word não mostra a tabela (tratado na decisão 4). Achado depois da PoC inicial, com um documento real com imagem e tabela** |
| Imagem JPEG (`data:`) | Preservada (`\pict`) |
| **Imagem PNG (`data:`)** | **Falha no renderer: o texto da exceção e o base64 inteiro viram texto do documento (tratado na decisão 4: aviso em vermelho no lugar)** |
| Imagem externa não carregada | Ignorada, como no PDF |
| Cabeçalho da primeira página (brasão, casa legislativa, `textoCabecalho`) | Ausente na PoC: a região `before` não é renderizada (tratado na decisão 4: o conteúdo vai para o início do corpo, e só existe quando `imprimirBrasao` ou `textoCabecalho` o pedem) |
| Links externos | Texto preservado; o destino saía com o `url('...')` (tratado na decisão 4) |
| Links internos | Saem como `HYPERLINK "#id"` com os marcadores (`bkmkstart`) presentes; funcionamento no Word a conferir na task 4.1 |
| Fonte | Citada pelo nome (Gentium Basic), sem embutir |

Conclusão: o conteúdo exigido pela spec é atendido; o PNG, o destino dos links externos, a largura das tabelas e o cabeçalho são corrigidos na decisão 4.

## Risks / Trade-offs

- [O renderer RTF do FOP perde ou deforma construções do FO atual] → PoC com os documentos da #72/#75 antes de fixar a spec; variante de FO (decisão 4) só se necessário; limitações documentadas.
- [Conteúdo exigido pela spec some no RTF (ex.: pendências no cabeçalho/região, assinaturas em tabela)] → testes de conteúdo por fixture; se faltar, tratar na variante de FO.
- [Exceções do renderer RTF com FO fora do subconjunto suportado] → capturar e relançar como `EtaBackendException`; nenhuma escrita parcial.
- [Refatoração do passo compartilhado altera o PDF por engano] → manter os testes atuais do PDF sem edição e rodá-los como critério de conclusão.
- [Usuário espera DOCX] → fora de escopo; o RTF abre no Word e pode ser salvo como DOCX; decisão sobre DOCX nativo fica para depois da avaliação.
- [Dependência de um renderer legado, não mantido] → isolado em um método; troca por outra solução (ex.: gerador DOCX) não afeta a API pública se o contrato de entrada e saída for mantido.
