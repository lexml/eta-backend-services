# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver `specs/pdf-documento-articulado/spec.md`.

Estado atual (resultado da #71):

- `DocumentoArticuladoPdfGenerator.generate(json, out)` valida o JSON, obtém o `documento-articulado.xml` pelo conversor, aplica o template Velocity (`DocumentoArticuladoTemplateProcessor`), renderiza com o FOP próprio e grava o hash. O `xml` hoje só é embutido como anexo.
- O template `documentoarticulado/template-velocity-documento-articulado.xml` tem um `<fo:block/>` vazio no `fo:flow` e guarda a impressão da emenda em blocos `#* NÃO RETIRAR ... *#`.

Como a emenda imprime a citação de dispositivos, observado no código:

- O **front** monta a citação como um "quase HTML" (`<p class="agrupador">`, `<Rotulo>`, aspas já no texto).
- O back faz trocas de texto (`VelocityExtension.citacao2html`) e converte HTML em FO com `HTML2FOConverter` (htmlcleaner) e a XSLT genérica `xhtml2fo.xsl` (740 linhas).
- O back da emenda **não lê LexML**.

O documento articulado chega como LexML de verdade (JSON jsonix e o XML equivalente). O classpath tem o **Xalan 2.7.2** (dependência transitiva), que se registra como `TransformerFactory` padrão. Esse Xalan trata o namespace XSL-FO (`http://www.w3.org/1999/XSL/Format`) como XSL e rejeita atributos de `fo:*` em folhas `version="1.0"`. O `xhtml2fo.xsl` da emenda contorna isso declarando `version="2.0"` (modo tolerante). Correção feita durante a implementação: a premissa inicial era que não havia Xalan no classpath.

Restrição mantida: fluxo **isolado** de emenda e parecer. Nada é reutilizado; valores de formatação são copiados.

## Goals / Non-Goals

**Goals:**
- Estabelecer a abordagem de geração do texto que as partes 2 a 6 da #72 vão estender.
- Imprimir epígrafe, ementa e preâmbulo com a formatação da emenda.
- Deixar tamanhos de fonte e espaçamentos parametrizados, com os valores padrão.

**Non-Goals:**
- Articulação e dispositivos (parte 2), agrupadores (parte 3), alteração de norma e omissis (parte 4), remissões e links (parte 5), pena e título de dispositivo (parte 6).
- Ler as opções de impressão do documento (`lexedit:OpcoesImpressao`): os parâmetros ficam fixos nos valores padrão.
- Cabeçalho com brasão, justificação, local, data e assinaturas: continuam comentados.

## Decisions

### D1. Velocity para a página, XSLT para o conteúdo (abordagem híbrida)

```
generate(json, out)
 |-- validar(json) -> JsonNode
 |-- conversor.jsonParaXml(json) -> xml                         (já existe)
 |-- parametros = ParametrosImpressaoDocumentoArticulado.padrao()   [novo]
 |-- conteudoFo = DocumentoArticuladoConteudoTransformer            [novo]
 |                  .transformar(xml, parametros)
 |                  (documentoarticulado/documento-articulado-conteudo.xsl)
 |-- xslFo = templateProcessor.processar(documento, parametros, conteudoFo)
 |-- FOP + PDF/A + anexo xml, hash                               (sem mudança)
```

- O **template Velocity** continua sendo o "modelo xsl-fo derivado da emenda": página, XMP, `font-size` do fluxo e os blocos comentados. O `<fo:block/>` do `fo:flow` é substituído por `$conteudoFo`.
- A **XSLT** percorre o LexML e gera o fragmento FO do conteúdo. As partes seguintes da #72 são novos templates na mesma XSLT.
- O `xml` já produzido para o anexo é reaproveitado como entrada: não há conversão extra nem nova chamada ao `jsonix-lexml`.

*Alternativas descartadas:*
- **Só Velocity sobre o JSON:** o conteúdo misto do jsonix (strings e objetos `{name, value}`) e a recursão da hierarquia ficam frágeis no Velocity 1.7, que tem escopo global de variáveis em macros recursivas.
- **Velocity com um gerador Java** (`JsonNode` → FO): viável e sem XSLT, mas mais verboso para a hierarquia, os omissis e os links das partes 2 a 5.
- **Caminho da emenda** (LexML → "quase HTML" → `xhtml2fo.xsl`): exigiria copiar o `HTML2FOConverter` e o `xhtml2fo.xsl`, faria duas conversões e perderia informação (`xlink:href`) necessária na parte 5.

### D2. Transformação XSLT

`DocumentoArticuladoConteudoTransformer` (package-private, em `documentoarticulado.pdf`):

- Compila `documento-articulado-conteudo.xsl` uma única vez (`Templates` em holder estático, thread-safe) com `TransformerFactory.newDefaultInstance()`, o **processador XSLT do próprio JDK**, e `FEATURE_SECURE_PROCESSING` ativo, sem acesso a DTD ou entidades externas. Não usa `newInstance()`, que pegaria o Xalan do classpath (ver Context) ou outro processador da aplicação que usa a biblioteca. O resultado não depende do classpath e dispensa o truque `version="2.0"` da emenda.
- Para cada geração, cria um `Transformer`, define os `xsl:param` a partir dos parâmetros (D5) e transforma a string XML em string FO.
- Em caso de erro de transformação, lança `EtaBackendException`.

A XSLT:

- Declara `lx` = `http://www.lexml.gov.br/1.0` (namespace padrão do LexML) e `xlink`, com `exclude-result-prefixes`.
- Produz **um único elemento raiz** `<fo:block>` envolvendo os blocos da parte inicial. O fragmento é XML bem formado e fácil de testar isoladamente.
- Ignora `lx:Metadado` (inclusive `lexedit:*`) e, nesta etapa, `lx:Articulacao`, `lx:ParteFinal` e `lx:Justificacao` (templates vazios).
- Elemento ausente: nenhum template é aplicado, então nenhum bloco vazio é gerado.

O fragmento entra no template como valor de referência Velocity (`$conteudoFo`). O Velocity **não reinterpreta** o valor, então `$` e `#` do texto do documento saem literalmente. Diferente da emenda, que usa `VelocityExtensionUtils.render` sobre o FO gerado.

### D3. Espaços em branco

- **Epígrafe:** `normalize-space(.)` e troca dos espaços por U+00A0 (`translate`), como na emenda (`epigrafe.texto.replace(' ', '&#160;')`), para ficar em uma linha.
- **Preâmbulo:** um `fo:block` por `lx:p`, com `normalize-space(.)` de cada parágrafo. A formatação inline é descartada porque só o valor textual é usado.
- **Ementa (conteúdo misto):** os nós de texto são copiados como estão, e o FOP colapsa espaços e quebras de linha pelo comportamento padrão do XSL-FO (`white-space-collapse="true"`, `linefeed-treatment="treat-as-space"`). Isso evita o `normalize-space` por nó de texto, que removeria os espaços ao redor de `b`, `i` e `span` (XSLT 1.0 não tem regex).
- **Inline na ementa:** `lx:b` → `fo:inline font-weight="bold"`; `lx:i` → `fo:inline font-style="italic"`; `lx:span` → apenas o conteúdo (link na parte 5); outros inline → apenas o conteúdo.

### D4. Formatação copiada da emenda

| Elemento | FO gerado | Origem (emenda) |
|---|---|---|
| Epígrafe | `text-align="center" font-weight="bold" font-size="{maxTamanhoFonte}"`, espaços não quebráveis | `template-velocity-emenda.xml`, bloco da epígrafe (sem o bloco do complemento) |
| Ementa | `space-before="48pt" margin-left="6.5cm" text-indent="0" text-align="justify" line-height="{lineHeight}"` | recuo: `html2foTextoLivre` (`estilo-ementa`: 6.5cm) e `citacao2html` (`margin-left: 40%` da área útil de 16,4cm ≈ 6,56cm); `text-align`/`line-height`: bloco do comando, que contém a citação. 48pt: #72 |
| Preâmbulo | `space-before="72pt" text-indent="2.5cm" text-align="justify" line-height="{lineHeight}"`; cada parágrafo com `margin-bottom="{pMarginBottom}"` | bloco "Comando de emenda" do template da emenda. 72pt: #72 |
| Fluxo | `font-size="{tamanhoFonte}pt"` no `fo:flow` | `fo:flow` do template da emenda |

Os valores ficam na XSLT (e o do fluxo no template), com comentário indicando a origem. Nenhum arquivo da emenda é importado ou referenciado.

### D5. Parâmetros de impressão fixos nesta etapa

`ParametrosImpressaoDocumentoArticulado` (package-private, imutável), com as mesmas regras de cálculo do template da emenda:

| Parâmetro | Regra (emenda) | Padrão |
|---|---|---|
| `tamanhoFonte` | opção do documento | 14 |
| `maxTamanhoFonte` | 18pt se `tamanhoFonte` = 18, senão 16pt | 16pt |
| `lineHeight` | 120% se reduzir espaço entre linhas, senão 150% | 150% |
| `pMarginBottom` | 0.45em se reduzir espaço entre linhas, senão 0.6em | 0.6em |

Nesta etapa só existe `padrao()`. O nome evita confusão com o `lexedit:OpcoesImpressao` do documento. Uma fábrica que lê as opções do `JsonNode` fica para a etapa das opções de impressão, sem mudar a XSLT nem o template.

### D6. Ajustes no template Velocity

- `fo:flow`: `font-size="${tamanhoFonte}pt"` e `$conteudoFo` no lugar do `<fo:block/>`.
- Os blocos `#* NÃO RETIRAR ... *#` continuam como estão. Um comentário no template registra que epígrafe e comando passaram a ser gerados pela XSLT, mas os blocos comentados continuam como referência para as próximas partes (justificação, local e data, assinaturas).
- O `DocumentoArticuladoTemplateProcessor.processar` passa a receber os parâmetros e o fragmento FO. Título, aplicação e data continuam iguais.

### D7. Fixtures e testes

- **Fixtures novas** em `src/test/resources/documentoarticulado/`, a partir dos três documentos anexados à #72 (articulação e alteração de norma; capítulo e seção; pena e título de dispositivo):
  - `.json` gerado com `jsonix-lexml tojson` do XML anexado;
  - `.xml` gerado com `jsonix-lexml toxml` desse JSON, para manter o par consistente com o que o conversor produz, como na #71.
- `ConversorDocumentoArticuladoFake` passa a oferecer esses pares.
- **Testes da XSLT** (`DocumentoArticuladoConteudoTransformerTest`): XML → fragmento FO, verificando ordem, atributos (48pt, 72pt, 6.5cm, 2.5cm, tamanhos), espaços não quebráveis na epígrafe, `b` removido do preâmbulo, texto do `span` presente sem link, `b`/`i` preservados na ementa e ausência de bloco para elemento ausente.
- **Testes do PDF:** texto extraído com PDFBox na ordem epígrafe → ementa → preâmbulo e sem texto de dispositivos.
- **Verificação manual:** `DocumentoArticuladoJson2PDF` com os três documentos, comparação com as imagens da #72 e revalidação PDF/A-3B no veraPDF, agora que há texto e fontes embutidas.

## Risks / Trade-offs

- [XSLT é uma segunda linguagem no fluxo] → escopo pequeno e isolado, com testes XML → FO para cada template. O projeto já usa XSLT (`xhtml2fo.xsl`).
- [Processador XSLT do JDK (XSLTC) só suporta XSLT 1.0] → sem regex nem funções 2.0. D3 trata os espaços sem regex.
- [Xalan do classpath trata `fo:*` como XSL] → evitado com `newDefaultInstance()` (D2).
- [Epígrafe com espaços não quebráveis pode ultrapassar a margem se for muito longa] → mesmo comportamento da emenda; epígrafes são curtas.
- [PDF/A com texto passa a embutir as fontes Gentium (normal, negrito, itálico)] → as fontes já estão configuradas; revalidar no veraPDF (D7).
- [Testes da #71 que esperam página sem texto] → ajustar para o novo comportamento: a articulação continua ausente, mas a parte inicial passa a aparecer.
- [Documento sem `ParteInicial`] → a XSLT não gera blocos; o PDF sai com o fluxo vazio. É preciso um bloco mínimo para o FOP: a raiz `<fo:block>` do fragmento sempre existe.

## Migration Plan

Mudança interna da biblioteca, sem alteração de API. Os PDFs gerados a partir desta versão passam a ter a parte inicial impressa; os PDFs já gerados continuam sendo abertos normalmente, porque a extração do JSON não muda. Rollback: reverter a change.
