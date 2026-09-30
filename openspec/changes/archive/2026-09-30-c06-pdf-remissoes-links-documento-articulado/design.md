# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver `specs/pdf-documento-articulado/spec.md`.

Estado atual (partes 1 a 4 e 6):

- Na XSLT `documentoarticulado/documento-articulado-conteudo.xsl`, o texto da ementa, dos dispositivos (inclusive em blocos de alteração) e dos títulos de dispositivo passa pelo modo `inline`. Nele, `b` e `i` são formatados, qualquer outro elemento (inclusive `span` e `Remissao`) vira só o texto, e o `text()` apara o fim do último texto antes de aspas de fechamento.
- O preâmbulo e o nome dos agrupadores usam `normalize-space` (somente texto).
- Nenhum bloco tem `id` de destino.

Referências observadas nos quatro documentos de teste:

| Tipo | Elemento | `xlink:href` | Onde | Quantidade |
|---|---|---|---|---|
| Externa | `span` | URN completa (`urn:lex:br:federal:lei:1996-12-20;9394`, `...;8069!art58`) | ementa, dispositivos e blocos de alteração | 22 nos documentos da #72 |
| Interna | `Remissao` | id do documento (`art4`) | caput do Art. 2º do exemplo da #71 | 1, com o **alvo ausente** (artigo excluído, registrado em `lexedit:RemissoesInternasInvalidas`) |

- Todas as URNs externas são absolutas, inclusive dentro dos blocos de alteração. O `xml:base` do bloco não é necessário.
- No exemplo da #71 existe um `Artigo` (`_art4-exc1`) dentro dos metadados `lexedit:RevisoesArticulacao`, no namespace do LexML. Ele **não é impresso** e não pode ser tratado como alvo.
- Não há nenhuma remissão interna válida nas fixtures. Por isso é preciso uma fixture nova (D5).

## Goals / Non-Goals

**Goals:**
- Links externos para o normas.leg.br e internos para dispositivos impressos, em cinza, sem sublinhado.
- Remissão interna com alvo ausente sem link e sem erro no FOP.
- PDF continuando PDF/A-3B válido com as anotações de link.

**Non-Goals:**
- Links no preâmbulo e no nome dos agrupadores (continuam só texto).
- Marcação visual de remissões internas inválidas.
- Codificação da URN na URL; resolução de URNs relativas ao `xml:base` (não ocorrem nos dados).

## Decisions

### D1. Chaves XSLT para alvos e destinos

```xml
<!-- dispositivos impressos que podem ser destino de link interno (só dentro da articulação) -->
<xsl:key name="dispositivo-impresso"
         match="lx:Articulacao//*[self::lx:Artigo or self::lx:Caput or self::lx:Paragrafo or self::lx:Inciso
                or self::lx:Alinea or self::lx:Item or self::lx:Pena or self::lx:Parte or self::lx:Livro
                or self::lx:Titulo or self::lx:Capitulo or self::lx:Secao or self::lx:Subsecao]"
         use="@id"/>
<!-- remissões internas, indexadas pelo id do alvo -->
<xsl:key name="remissao-interna"
         match="lx:Articulacao//*[self::lx:Remissao or self::lx:span][@xlink:href][not(starts-with(@xlink:href, 'urn:'))]
                | lx:Ementa//*[self::lx:Remissao or self::lx:span][@xlink:href][not(starts-with(@xlink:href, 'urn:'))]"
         use="@xlink:href"/>
```

- Restringir `dispositivo-impresso` a `lx:Articulacao//` exclui o `Artigo` dos metadados `lexedit`, que não é impresso.
- Alternativa descartada: um `id` em todos os blocos. Isso encheria o FO de ids sem uso, e um id repetido derrubaria a geração.

### D2. Links no modo `inline`

Um template para `lx:span[@xlink:href] | lx:Remissao[@xlink:href]` no modo `inline`:

- `href` começa com `urn:` → `<fo:basic-link external-destination="url('https://normas.leg.br/?urn={href}')" color="#808080">` com o conteúdo no modo `inline`;
- senão, se `key('dispositivo-impresso', href)` existe → `<fo:basic-link internal-destination="{href}" color="#808080">`;
- senão → só o conteúdo no modo `inline`, como hoje: texto simples, na cor do texto.

O conteúdo continua no modo `inline`, então `b`, `i` e o aparo antes das aspas de fechamento funcionam dentro dos links.

### D3. Identificadores de destino só nos alvos

Um dispositivo recebe `id="{@id}"` apenas se `key('remissao-interna', @id)` não for vazio:

- **Artigo:** o id vai no `fo:inline` do rótulo ("Art. 1º"), dentro do bloco do caput, porque o artigo não tem bloco próprio. O `Artigo` repassa o próprio id ao `Caput` (parâmetro `idRotulo`), como já repassa o rótulo e o `abreAspas`.
- **Caput, Parágrafo, Inciso, Alínea, Item, Pena:** o id vai no `fo:block` do dispositivo, no bloco do rótulo e do primeiro `p`.
- **Agrupadores:** o id vai no bloco do título.

Os blocos que não são alvo continuam sem atributos. Por isso os testes das partes anteriores que exigem blocos sem atributos continuam válidos.

### D4. Aparência

`color="#808080"` no `fo:basic-link`, sem `text-decoration`. É a "cinza escuro, diferenciando levemente do texto" da #72: o texto é preto, e o cinza a 50% se destaca sem prejudicar a leitura (o #404040 da primeira versão e o #666666 da segunda ficaram pouco distinguíveis do preto na validação visual). O rótulo em negrito e os trechos em itálico dentro do link mantêm a formatação.

### D5. Fixture com remissões internas válidas

Nova fixture `documento-com-remissoes-internas`: um LexML mínimo escrito à mão, convertido com `jsonix-lexml tojson` e depois `toxml`, para manter o par consistente como nas outras. Ela tem:

- epígrafe, ementa com uma remissão externa e preâmbulo com uma remissão externa, que deve sair sem link;
- um capítulo, o Art. 1º com § 1º e o Art. 2º;
- no Art. 2º, uma remissão ao **Art. 1º** (`art1`), outra ao **§ 1º do Art. 1º** (`art1_par1`), uma ao **capítulo** e uma **inválida** (`art9`).

Ela é registrada no `ConversorDocumentoArticuladoFake.comDocumentosDeTeste()` e usada nos testes e nos PDFs v6.

### D6. Ajuste dos testes das partes anteriores

- `DocumentoArticuladoConteudoTransformerTest.ementaComRecuoESpanComoTextoSimples` e `artigoComRotuloEmNegritoNoMesmoBlocoDoCaput` exigem "sem `basic-link`": passam a exigir o link externo.
- Testes que contam os elementos inline da ementa ou dos dispositivos da #72 passam a considerar o `fo:basic-link`.

## Risks / Trade-offs

- [PDF/A-3B com anotações de link] → PDF/A-3 admite anotações de link e ações de URI, e o FOP as gera no modo PDF/A com o marcador de impressão. Validar a v6 no veraPDF, que é o ponto de atenção desta parte.
- [URN com caracteres especiais na URL] → `:`, `;` e `!` são aceitos na query. A URL fica igual ao exemplo da #72. Se o portal exigir codificação no futuro, basta ajustar o template.
- [Remissão para um alvo não coberto pela chave (ex.: `Omissis`, `p`)] → sai como texto simples, sem erro. A regra é conservadora.
- [Id do documento repetido] → só os alvos recebem id. Os ids do LexML são únicos no documento, e a restrição a `lx:Articulacao//` evita o conflito com os metadados.

## Migration Plan

Só a XSLT e as fixtures de teste mudam. PDFs gerados a partir desta versão passam a ter links. Rollback: reverter a change.
