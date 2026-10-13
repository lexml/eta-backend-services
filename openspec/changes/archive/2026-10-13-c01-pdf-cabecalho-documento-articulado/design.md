# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver a spec delta.

Estado atual:
- `template-velocity-documento-articulado.xml` tem o `firstPage` com `region-body margin-top="1cm"` e, comentados ("NÃO RETIRAR"), o `region-before` `header-principal` e o `fo:static-content` com brasão, casa e texto da emenda.
- `DocumentoArticuladoTemplateProcessor.processar` põe no contexto `documento` (JSON como Map, não usado no template), `titulo`, `aplicacao`, `dataIso`, `tamanhoFonte` e `conteudoFo`.
- `fop-documento-articulado.xconf` já declara a fonte `ITC-StoneSansStd`, servida pelo `ResolverFontes`.

Referência (emenda): `template-velocity-emenda.xml` (cabeçalho com `region-before extent="3cm"` e `region-body margin-top="4cm"` na primeira página quando há brasão ou texto; brasão `fo:external-graphic` com `data:image/jpeg;base64`; casa em `ITC-StoneSansStd` 11pt com `margin-top="3pt"`; bloco do brasão com `margin-bottom="2pt"`; texto do cabeçalho centralizado e escapado) e `VelocityTemplateProcessor` (lê `/static/img/brasao.jpg` com `EtaFileUtil.readFromImageAsBase64String`). Medido num PDF de emenda com brasão: 1,9 x 2,0 cm, centralizado.

Opções de impressão (especificação 02 do lexml-eta; `OpcoesImpressao` do editor): `imprimirBrasao` (padrão `true`), `textoCabecalho` (padrão `""`), `reduzirEspacoEntreLinhas` e `tamanhoFonte` (parte B). No JSON jsonix: `value.metadado.metadadoProprietario[*].any[*].value.opcoesImpressao`.

## Goals / Non-Goals

**Goals:**
- Cabeçalho idêntico ao da emenda, no template Velocity (camada da página), com os dados lidos do JSON pelo `DocumentoArticuladoTemplateProcessor`.

**Non-Goals:**
- `tamanhoFonte` e `reduzirEspacoEntreLinhas` (parte B).
- Cabeçalho das demais páginas (`header-assinaturas` da emenda continua comentado).
- Brasões específicos por casa (só existe `brasao.jpg`, o Brasão da República, também usado pela emenda).

## Decisions

### D1. Cabeçalho no Velocity, dados preparados no Java

O cabeçalho é layout de página (região `region-before` do `firstPage`), por isso fica no template Velocity, como na emenda; a XSLT continua só com o conteúdo. O `DocumentoArticuladoTemplateProcessor` prepara variáveis simples, sem lógica no template além de `#if`:

| Variável | Valor | Origem |
|---|---|---|
| `$imprimirBrasao` | boolean | `opcoesImpressao.imprimirBrasao`; ausente ou não booleano -> `true` |
| `$brasao` | base64 de `/static/img/brasao.jpg` | só carregado quando `$imprimirBrasao` |
| `$casaLegislativa` | "SENADO FEDERAL", "CÂMARA DOS DEPUTADOS", "CONGRESSO NACIONAL" ou vazio | autoridade da URN (D2) |
| `$textoCabecalho` | texto escapado para XML | `opcoesImpressao.textoCabecalho`; ausente -> vazio |
| `$temCabecalho` | `$imprimirBrasao` ou texto não vazio | calculado |

O `$documento` (Map do JSON, nunca usado pelo template) é retirado do contexto.

O brasão é lido do classpath (`/static/img/`, compartilhado com a emenda por decisão da #71) com `IOUtils` e `Base64` diretamente no processador, sem usar o `EtaFileUtil` (que é de `util/`, compartilhável, mas registra erro e devolve vazio; aqui a ausência do recurso é falha de empacotamento e vira `EtaBackendException`). O base64 é calculado uma vez (constante carregada sob demanda), não a cada PDF.

### D2. Casa legislativa pela autoridade da URN

`urn:lex:br:<autoridade>:<tipo>:...`: terceiro segmento separado por `:`. `senado.federal` -> SENADO FEDERAL; `camara.deputados` -> CÂMARA DOS DEPUTADOS; `congresso.nacional` -> CONGRESSO NACIONAL; outros (ex.: `federal` da medida provisória) -> vazio (só o brasão). Sempre disponível, inclusive com a URN provisória (especificação 01). Alternativa descartada: `lexedit:Parlamentar siglaCasaLegislativa` (opcional, pode não existir e pode misturar casas).

### D3. Template (valores da emenda)

```xml
<fo:simple-page-master master-name="firstPage" ...>
  #if ($temCabecalho)
    <fo:region-body margin-top="4cm"/>
    <fo:region-before extent="3cm" region-name="header-principal"/>
  #else
    <fo:region-body margin-top="1cm"/>
  #end
...
<fo:page-sequence master-reference="document">
  #if ($temCabecalho)
  <fo:static-content flow-name="header-principal">
    #if ($imprimirBrasao)
    <fo:block text-align="center" margin-bottom="2pt">
      <fo:external-graphic src="url('data:image/jpeg;base64,$brasao')"/>
      #if ($casaLegislativa != '')
      <fo:block font-size="11pt" font-family="ITC-StoneSansStd" margin-top="3pt">$casaLegislativa</fo:block>
      #end
    </fo:block>
    #end
    #if ($textoCabecalho != '')
    <fo:block text-align="center">$textoCabecalho</fo:block>
    #end
  </fo:static-content>
  #end
```

- Na emenda o `#if` da casa fica dentro do bloco de 11pt, que é emitido mesmo sem casa; aqui o bloco só sai com casa (sem bloco vazio).
- O texto do cabeçalho herda a fonte padrão do `fo:root` (Gentium Basic) no tamanho padrão do FOP (12pt), como na emenda (o `static-content` não herda o `font-size` do `fo:flow`).
- O bloco comentado da emenda ("NÃO RETIRAR") do cabeçalho principal é substituído por este; o `header-assinaturas` continua comentado.

### D4. Fixtures

- `documento-articulado-exemplo`: `textoCabecalho="Gabinete do Senador Davi Alcolumbre"` (brasão + casa + texto), mantendo o resto; regenerar o par (JSON-first, procedimento das partes anteriores).
- `documento-com-justificacao-longa`: `imprimirBrasao="false"` e `textoCabecalho` não vazio (cabeçalho só com texto).
- Fixtures sem `OpcoesImpressao` (as da #72) passam a sair com brasão e casa (padrão); a de capítulo e seção (MPV, autoridade `federal`) cobre "só o brasão".
- O caso "sem cabeçalho" (brasão falso e texto vazio) e a URN da Câmara são cobertos em teste unitário alterando o JSON em memória.

### D5. Testes

- `DocumentoArticuladoTemplateProcessorTest`: presença/ausência do `region-before` e `margin-top` 4cm/1cm, brasão em base64, casa por autoridade (Senado, Câmara, Congresso, outra), texto escapado, padrões com opções ausentes.
- `DocumentoArticuladoPdfGeneratorTest`: brasão desenhado só na primeira página, centralizado e com o tamanho da emenda (~1,9 cm de largura); "SENADO FEDERAL" em ITC Stone Sans; texto do cabeçalho; cabeçalho só com texto; corpo começando ~3cm mais abaixo com cabeçalho. Testes existentes que dependem de posições ou da página de notas são ajustados ao novo deslocamento.

## Risks / Trade-offs

- [Primeira página com ~3cm a menos de conteúdo] -> Muda a paginação dos documentos; testes de "nota na página N" usam a página da referência, não números fixos, mas são revistos.
- [Autoridade da URN fora das três casas] -> Só o brasão (documentado no requisito).
- [Fontes do cabeçalho] -> ITC Stone Sans já está no xconf e no `ResolverFontes`; o PDF/A exige fontes embutidas, verificado no veraPDF.

## Migration Plan

Não se aplica: biblioteca sem estado; vale para os próximos PDFs gerados.
