# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver a spec delta.

Estado atual:
- A justificação (com as notas de rodapé dela) é impressa pela XSLT `documento-articulado-conteudo.xsl` (template `justificacao`, chamado na raiz depois da `Norma`), com parâmetros vindos do `DocumentoArticuladoConteudoTransformer`.
- O local e data e as assinaturas são impressos pelo template Velocity a partir do `FechoDocumentoArticulado`, que lê `local`, `data` e `autoria` do valor do `lexedit:Metadado` no JSON; o bloco só aparece com `local` ou assinaturas.
- O `DocumentoArticuladoPdfGenerator` lê o JSON uma vez e chama o transformer (`transformar(xml, parametros)`) e o template processor (`processar(documento, parametros, conteudoFo)`).
- No JSON, o atributo vem como booleano no valor do `lexedit:Metadado` (`"anexoParecer": true`); o `jsonix-lexml` 2.0.0 o preserva nas duas direções (verificado com o exemplo).

Referência (emenda, `template-velocity-emenda.xml`): `#if (!$emenda.anexoParecer && $emenda.justificativa)` na justificação, `#if (!$emenda.anexoParecer)` no bloco de local e data e assinaturas e no autor do XMP (`dc:creator`, que o template do documento articulado não preenche). O restante (cabeçalho, alertas, conteúdo) não depende do atributo.

## Goals / Non-Goals

**Goals:**
- Não imprimir justificação, local e data e assinaturas em anexo de parecer, a partir de uma única leitura do atributo.

**Non-Goals:**
- Mudar cabeçalho, alertas e pendências ou o conteúdo da norma em anexo de parecer (a emenda não muda).
- Remover dados do documento ou do `documento-articulado.xml` embutido: o anexo do PDF continua sendo o documento convertido, sem alteração.

## Decisions

### D1. Leitura do atributo

`static boolean anexoParecer(JsonNode documento)` package-private no `FechoDocumentoArticulado` (onde já está a localização do `lexedit:Metadado`): verdadeiro só quando o valor for o booleano `true`; ausente, `false` ou outro tipo -> falso (a ausência indica `false`, pela issue).

### D2. Local e data e assinaturas

`FechoDocumentoArticulado.de(documento)` devolve fecho vazio (sem local, sem data e sem assinaturas) quando `anexoParecer(documento)` for verdadeiro. O template não muda: o bloco já não é impresso sem local e sem assinaturas. Alternativa considerada: uma variável `$anexoParecer` no template, como na emenda; rejeitada por duplicar a condição que o fecho vazio já expressa.

### D3. Justificação

Novo `xsl:param name="imprimirJustificacao"` (padrão `'true'`) na XSLT; a chamada do template `justificacao` na raiz fica dentro de `xsl:if test="$imprimirJustificacao = 'true'"`. As notas de rodapé da justificação estão dentro dela e deixam de ser geradas junto. O `DocumentoArticuladoConteudoTransformer.transformar` ganha um terceiro argumento (`boolean imprimirJustificacao`), passado pelo gerador como `!anexoParecer(documento)`; a sobrecarga de dois argumentos continua existindo para os testes atuais (equivale a `true`).

Alternativa considerada: a XSLT ler `@anexoParecer` diretamente do `documento-articulado.xml`. Rejeitada para ter uma só leitura do atributo (no JSON, como o fecho), evitando duas regras para o mesmo dado (ex.: `xsd:boolean` aceita `1` no XML).

### D4. Fixture e cobertura

- `documento-anexo-parecer` (JSON-first, fonte LexML no scratchpad): projeto de lei do Senado com `anexoParecer="true"`, epígrafe, ementa, preâmbulo, alguns artigos e justificação com uma nota de rodapé; sem `local`, `data` e `lexedit:Autoria` e sem `ParteFinal`, como o editor grava no modo anexo de parecer; com uma pendência, para mostrar que os alertas continuam. Registrada no `ConversorDocumentoArticuladoFake` (`ANEXO_PARECER`, em `comDocumentosDeTeste()`).
- Variações em memória (como nas changes anteriores): o exemplo com `anexoParecer` `true` (com local, data e autoria preenchidos), `false` e ausente.

## Risks / Trade-offs

- [Pendência "Não foi informado um texto de justificação." em anexo de parecer] -> Impressa como registrada (decisão da #79); o editor decide o que registrar.
- [Outro tipo no atributo (ex.: texto "true")] -> Tratado como falso, como as demais opções lidas do JSON (só booleano).

## Migration Plan

Não se aplica: biblioteca sem estado; vale para os próximos PDFs.
