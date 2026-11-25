# Design

## Context

O módulo `br.gov.lexml.eta.etaservices.documentoarticulado.pdf` gera o XSL-FO completo da proposição em `DocumentoArticuladoFoGerador` (validação JSON → `jsonix-lexml` → `documento-articulado-conteudo.xsl` → `template-velocity-documento-articulado.xml`). Esse XSL-FO já encapsula toda a lógica legislativa e de impressão (epígrafe, `vao-do-numero`, agrupadores, aspas/notas de alteração, `anexoParecer`, pendências, fecho, assinaturas, classes do Quill, remoção de `<del>`, opções de impressão).

## Goals / Non-Goals

**Goals:**
- Exportar `.docx` válido (OOXML) diretamente a partir do XSL-FO já produzido por `DocumentoArticuladoFoGerador`.
- Zero novas dependências no `pom.xml` (reaproveitando `dom4j`, `ImageIO` e `java.util.zip`).
- Superar as limitações do RTF: cabeçalho real só na 1ª página (`w:titlePg`), caixa de pendências contínua, notas de rodapé nativas com numeração automática, tabelas centralizadas com `gridSpan`/`vMerge`, imagens PNG/JPEG nativas e omissis editável com tab leader pontilhado.
- Manter escrita atômica no `OutputStream` e nenhuma interferência no PDF ou RTF.

**Non-Goals:**
- Não converter RTF → DOCX (não existe conversor Java open-source puro adequado e o RTF do FOP já perdeu estrutura e qualidade).
- Não adicionar Apache POI (`~12–15 MB` de dependências transitivas e risco de conflito em apps Spring Boot consumidoras) nem `docx4j`.
- Não embutir `documento-articulado.xml` nem anexos no DOCX.
- Não alterar a XSLT nem o template Velocity do PDF.

## Decisions

1. **Origem: XSL-FO (`DocumentoArticuladoFoGerador` + `DocumentoArticuladoFoHelper`)**
   - Fonte única de verdade com vocabulário fechado (`fo:static-content`, `fo:flow`, `fo:block`, `fo:block-container`, `fo:inline`, `fo:basic-link`, `fo:leader`, `fo:footnote`, `fo:list-block`, `fo:table`, `fo:external-graphic`).
   - Evoluções futuras na XSLT/Velocity refletem automaticamente no DOCX sem duplicação de regras.

2. **Tecnologia: OOXML direto (`dom4j` + `ZipOutputStream`)**
   - `MedidasFo`: conversão determinística de unidades FO (`mm`, `cm`, `pt`, `in`, `px`, `em`, `%`) para twips, half-points, EMUs e entrelinha Word (`240 * percentual / 100 / 1.15`).
   - `PacoteDocx`: empacotamento OPC (`[Content_Types].xml`, `_rels/.rels`, `word/styles.xml`, `word/settings.xml`, `word/document.xml`, partes opcionais `word/header1.xml`, `word/footnotes.xml`, `word/media/*` e seus `.rels`).
   - `DocumentoArticuladoFoDocx`: caminhamento da árvore FO com pilha de propriedades herdadas (`EstiloFo`).

3. **Mapeamento de construções críticas**
   - **Cabeçalho da 1ª página:** `fo:static-content[@flow-name='header-principal']` → `word/header1.xml` referenciado por `<w:headerReference w:type="first"/>` com `<w:titlePg/>` no `<w:sectPr>`.
   - **Caixa de pendências (`background-color="yellow"`):** renderizada como tabela `1×1` sem borda com `<w:shd w:val="clear" w:fill="FFFF00"/>` e margens internas (`<w:tcMar>`), garantindo fundo contínuo sem filetes brancos entre título e lista no Word e no LibreOffice.
   - **Notas de rodapé (`fo:footnote`):** gravadas em `word/footnotes.xml` com `<w:footnoteRef/>`; o número manual emitido pela XSLT no início do `fo:footnote-body` é removido para evitar numeração duplicada (`1 1 Texto`).
   - **Omissis (`fo:leader leader-pattern="dots"`):** tab stop à direita na margem útil com `w:leader="dot"` + `<w:r><w:tab/></w:r>`.
   - **Tabelas centralizadas:** a tabela externa de 3 colunas usada no FO só para centralizar é desembrulhada; a interna recebe `<w:jc w:val="center"/>` e a largura da coluna central. `number-columns-spanned` → `w:gridSpan`; `number-rows-spanned` → `w:vMerge w:val="restart"` + células de continuação `w:vMerge/` nas linhas seguintes.
   - **Imagens (`fo:external-graphic`):** `data:image/*;base64,...` legível por `ImageIO` é embutida em `word/media/` no formato original (PNG/JPEG/GIF/BMP) com dimensões em EMUs preservando proporção; imagens externas ou ilegíveis viram aviso em vermelho (`[Imagem omitida: não foi possível incluí-la no DOCX.]`).

## Risks / Trade-offs

- **Conformidade estrita no Microsoft Word:** o Word é rigoroso com a ordem dos elementos filhos em `w:pPr`, `w:rPr`, `w:tblPr`, `w:tcPr` e `w:sectPr`. Mitigação: seguir rigorosamente a ordem do XSD OOXML (ECMA-376), validar com testes unitários estruturais, testar leitura via `LeitorArquivoProposicao.lerDocx` e inspecionar manualmente no Word/LibreOffice via `Json2DOCX`.
- **Blocos aninhados em XSL-FO:** `fo:block` pode misturar texto/inline e `fo:block` filhos. Mitigação: emissão incremental de parágrafos na travessia da árvore mantendo a pilha de estilos herdados.
