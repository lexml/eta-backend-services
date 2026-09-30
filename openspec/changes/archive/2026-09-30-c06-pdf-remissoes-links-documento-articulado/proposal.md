# Proposal

## Why

Com as partes 1, 2, 3, 4 e 6 da issue [#72](https://github.com/lexml/eta-backend-services/issues/72) concluídas, o PDF imprime todo o texto da proposição, mas as referências a normas e a outros dispositivos saem como texto simples. A #72 pede que as remissões tenham link: interno para as remissões internas e para o portal normas.leg.br nas remissões externas, em cinza escuro. Esta é a **parte 5**, a última das 6 em que a #72 foi dividida.

## What Changes

- **Remissões externas** (`span` ou `Remissao` com `xlink:href` começando por `urn:`): link para `https://normas.leg.br/?urn=<URN>`, com a URN do documento sem codificação. Exemplo: `https://normas.leg.br/?urn=urn:lex:br:federal:lei:1996-12-20;9394`.
- **Remissões internas** (`xlink:href` com o id de um elemento do próprio documento): link interno no PDF para o dispositivo referido. Quando o alvo **não existe** ou não é impresso (ex.: remissão a um artigo excluído, registrada pelo editor como inválida), a remissão sai como **texto simples, sem link e sem marca visual**.
- **Aparência dos links:** cinza (`#808080`), **sem sublinhado**, destacando-se do texto preto (a #72 pede "cinza escuro"; tons mais escuros foram testados e se confundiam com o preto, ver design D4).
- Os links valem onde o texto preserva a formatação inline: ementa, texto dos dispositivos (inclusive dentro de blocos de alteração) e título de dispositivo. O preâmbulo e o nome dos agrupadores continuam só como texto, pelas regras das partes 1 e 3.
- Os dispositivos que são **alvo** de alguma remissão interna recebem um identificador de destino no PDF (no rótulo do artigo, ou no bloco dos demais dispositivos e dos títulos de agrupador). Os que não são alvo continuam sem identificador.

## Capabilities

### New Capabilities
<!-- Nenhuma: evolui a capability existente. -->

### Modified Capabilities
- `pdf-documento-articulado`:
  - "Ementa" e "Formatação dos dispositivos" deixam de exigir que as referências saiam como texto simples;
  - novo requisito "Remissões e links".

## Impact

- **Código:** só a XSLT `src/main/resources/documentoarticulado/documento-articulado-conteudo.xsl`:
  - templates de `span` e `Remissao` no modo `inline`;
  - chaves para localizar alvos de remissões internas;
  - identificadores de destino nos dispositivos que são alvo.

  O Java, o template Velocity e a API não mudam.
- **Testes:**
  - novos casos no `DocumentoArticuladoConteudoTransformerTest`: links externos da #72, remissão interna válida em XML mínimo e remissão inválida do exemplo da #71;
  - novos casos no `DocumentoArticuladoPdfGeneratorTest`, com as anotações de link do PDF lidas pelo PDFBox;
  - ajuste dos testes que exigem "sem link".
- **PDF/A:** é a primeira vez que o PDF tem anotações de link. PDF/A-3B admite links e ações de URI, e a validação no veraPDF da versão **v6** é o ponto de atenção.
- **Nenhuma alteração** em emenda, parecer ou `lexmljsonix`; nenhuma dependência nova.
