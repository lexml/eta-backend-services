# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver a spec delta.

Estado atual:
- O conteúdo (parte inicial, articulação, justificação) vem pronto da XSLT em `$conteudoFo`; a XSLT ignora o `lx:ParteFinal` (`<xsl:template match="lx:ParteFinal"/>`).
- O `template-velocity-documento-articulado.xml` guarda, comentado como "NÃO RETIRAR", o trecho da emenda com "Local e data", "Assinaturas" (signatários em pares) e as assinaturas adicionais de senadores e deputados.
- O `DocumentoArticuladoTemplateProcessor` põe no contexto do Velocity só valores simples, já escapados (o JSON não vai inteiro para o template); as opções de impressão são lidas por `OpcoesImpressaoDocumentoArticulado.de(JsonNode)`.
- No JSON (jsonix), os dados ficam em `value.metadado.metadadoProprietario[*].any[*].value`: `local`, `data` (`aaaa-mm-dd`, opcional/vazia) e `autoria` (`tipo`, `imprimirPartidoUF`, `parlamentares.parlamentar[*]` com `nome`, `sexo`, `siglaPartido`, `siglaUF`, `siglaCasaLegislativa`, `cargo`; ou `colegiadoAutor` na comissão).
- O LexML também traz a representação textual (`ParteFinal` com `LocalDataFecho` e `AssinaturaTexto`), gerada pelo editor a partir desses dados.

Referência (emenda, `template-velocity-emenda.xml`, e `ParlamentarPojo`/`Emenda`):

| | Emenda |
|---|---|
| Fecho | bloco `role="fecho"`, `space-before="$spacing4"` (34pt; 26pt com "reduzir"), `text-indent="2.5cm"`, `text-align="justify"`, `keep-with-next.within-page="always"`, `keep-with-previous="10"`; "`$local`, `$dataFormatada`." ou, sem data, espaços não quebráveis com "de" (dia, mês, ano) e ponto |
| Data | `DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)` em minúsculas (depende da localidade padrão da JVM) |
| Assinaturas | bloco em negrito; `fo:table role="Signatários"` com duas colunas de 50%; pares (`getParlamentaresPair`), a última sozinha com `number-columns-spanned="2"`; células `text-align="center"`; nome com `margin-top="30pt"`; partido/UF e cargo com `keep-with-previous="always"` |
| Tratamento | `CD` -> Deputado/Deputada, senão Senador/Senadora; `M` -> masculino, senão feminino |
| Adicionais | tabelas em branco por `quantidadeAssinaturasAdicionais*` (fora, issue #76) |

## Goals / Non-Goals

**Goals:**
- Fecho e assinaturas com a estrutura e os valores da emenda, a partir dos dados estruturados.
- Exemplo cobrindo todas as combinações possíveis em um documento; o restante em fixtures e variações nos testes.

**Non-Goals:**
- Assinaturas adicionais e assinatura de comissão.
- Imprimir o `ParteFinal` textual (registrado como alternativa para a atividade de formatação, D5).
- Ajustes finos de estilo além da emenda (ex.: só o nome em negrito, como no `AssinaturaTexto` do editor): atividade de formatação.

## Decisions

### D1. Fonte dos dados: `lexedit:Metadado`, como a emenda (decisão do explore)

Os dados vêm do JSON, não do `ParteFinal`. Alternativa considerada: imprimir o `LocalDataFecho`/`AssinaturaTexto` pela XSLT (texto já pronto, sem reimplementar tratamento e data). Rejeitada nesta etapa porque o padrão a seguir é o da emenda (data vazia com espaços para preencher, que o texto do editor não tem); fica registrada para a atividade de formatação (D5).

### D2. `FechoDocumentoArticulado`: leitura do local, data e assinaturas

Classe package-private em `pdf/`, imutável, `static de(JsonNode documento)`, no estilo do `OpcoesImpressaoDocumentoArticulado` (percorre `metadadoProprietario[*].any[*].value` até achar o nó com os dados):
- `getLocal()`: texto aparado; vazio quando ausente.
- `getData()`: por extenso com localidade explícita `pt-BR` (`d 'de' MMMM 'de' yyyy`, ex.: "24 de abril de 2026", "1 de maio de 2026"), e não pela localidade padrão da JVM como a emenda, para o resultado não depender do servidor; vazio quando ausente, vazia ou fora de `aaaa-mm-dd`.
- `getAssinaturas()`: só com `tipo="Parlamentar"`; uma por `parlamentar`, na ordem, com tratamento (regra da emenda: `siglaCasaLegislativa` `CD` -> Deputado/Deputada, senão Senador/Senadora; `sexo` `M` -> masculino, senão feminino), nome, "(PARTIDO - UF)" quando `imprimirPartidoUF` e cargo aparado (vazio = sem linha). Comissão ou sem autoria: lista vazia.

Valores para o Velocity: o `DocumentoArticuladoTemplateProcessor` põe no contexto `local`, `dataPorExtenso` (strings escapadas para XML) e `assinaturas` como **lista de pares** (`List<List<Map<String, String>>>`, com chaves `nome`, `partidoUF` e `cargo` já escapadas; o tratamento vai junto do nome). Mapas e listas porque o Velocity 1.7 só chama métodos públicos de classes públicas e as classes de `pdf/` são package-private (decisão de manter a API mínima).

### D3. Template: blocos da emenda depois do `$conteudoFo`

O trecho "Local e data" e "Assinaturas" sai do comentário "NÃO RETIRAR" e passa a ser ativo logo depois de `$conteudoFo`, com as adaptações:
- `$emenda.local`/`$emenda.dataFormatada`/`$ve.isEmpty(...)` -> `$local`/`$dataPorExtenso`; bloco do fecho só `#if($local != "")`;
- `$spacing4` -> `$espacoAntesFecho`, novo valor de `ParametrosImpressaoDocumentoArticulado` (`getEspacoAntesFecho()`: 34pt; 26pt com "reduzir");
- pares de `$emenda.autoria.parlamentaresPair` -> `$assinaturas`; `#if` de partido/UF e cargo pela string vazia;
- tabela só `#if(!$assinaturas.isEmpty())`.

As tabelas de assinaturas adicionais e o restante do trecho continuam comentados ("NÃO RETIRAR"). O conteúdo da XSLT não muda (o `ParteFinal` continua ignorado).

### D4. Fixtures (JSON-first) e cobertura

- `documento-articulado-exemplo`: cinco autores — Senador Davi Alcolumbre (com cargo), Senadora Soraya Thronicke (sem cargo), um deputado e uma deputada fictícios (um com cargo) e um quinto parlamentar fictício (número ímpar: última assinatura nas duas colunas); `imprimirPartidoUF="true"`, data preenchida. O `ParteFinal` da fonte é atualizado para o texto que o editor geraria, para o par continuar coerente. O exemplo passa a ter `tamanhoFonte="14"` (com "reduzir"), ajuste feito pelo usuário depois da change c02 e mantido; os testes de fonte 18 usam variações em memória.
- `documento-com-justificacao-longa`: `imprimirPartidoUF="false"` e `data` vazia (fecho com espaços para preencher, sem partido/UF).
- `documento-com-justificacao`: mantido (uma senadora: célula única).
- Variações em memória nos testes (como na change c02, registradas no `ConversorDocumentoArticuladoFake`): autoria por comissão, sem `data`, sem `local`.
- Documentos da #72: sem local nem autoria (nada impresso).

### D5. Alternativa textual registrada

No `historico.md` da pasta de trabalho, entre os itens da atividade de formatação: imprimir o `ParteFinal` (`LocalDataFecho`/`AssinaturaTexto`) em vez de montar pelos metadados, e só o nome em negrito, como no texto do editor.

## Risks / Trade-offs

- [Texto do `ParteFinal` diferente do impresso, ex.: data vazia] -> O PDF segue a emenda (D1); a diferença é registrada (D5).
- [Fecho e assinaturas que não cabem na página] -> `keep-with-next`/`keep-with-previous` da emenda; verificação no exemplo (6 páginas com fonte 18) e na justificação longa.
- [Data por extenso dependente da JVM] -> Localidade explícita `pt-BR` (D2).
- [Nome com caracteres especiais (`&`, `<`)] -> Valores escapados para XML antes do Velocity.

## Migration Plan

Não se aplica: biblioteca sem estado; vale para os próximos PDFs.
