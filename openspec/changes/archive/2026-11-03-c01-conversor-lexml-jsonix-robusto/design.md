# Design

## Context

Estado atual:

- **`ConversorLexmlJsonixImpl`** (`lexmljsonix/conversor`) monta uma linha de comando em string. Ela roda pelo `CliUtils`: `cmd.exe /c` no Windows e `bash -c` no Linux. Há dois modos:
  - **Entrada e saída padrão** (`-`), que é o padrão: `usarEntradaSaidaPadraoDuranteConversao = true`;
  - **Arquivo** em `Files.createTempDirectory("lexml")`, com remoção opcional do diretório.
- **`CliUtils`**:
  - não lê o código de saída;
  - captura `Exception` e só registra no log;
  - no Linux usa `inheritIO()`. Com entrada e saída padrão, o `inheritIO` redireciona o stdout para o console, e a leitura de `getInputStream()` volta vazia;
  - no Windows chama `redirectError()` sem argumento, o que não altera nada, e não consome o stderr.
- **`ConversorDocumentoArticuladoCli`** (`etaservices/documentoarticulado/conversor`) já faz a conversão de forma robusta para o `jsonix-lexml` 2.x:
  - `ProcessBuilder` com lista de argumentos;
  - stdout e stderr gravados em arquivos;
  - tempo limite;
  - código de saída diferente de 0, saída ausente e saída vazia tratados como falha (`EtaBackendException`);
  - remoção do diretório com novas tentativas.

  É coberto por `ConversorDocumentoArticuladoCliTest`, com o `JsonixLexmlSimulado` no lugar do executável.
- **Hosts:** o lexeditweb usa o modo por arquivo e o `jsonix-lexml` 2.3.0.

## Goals / Non-Goals

**Goals:**
- Fazer a falha de conversão aparecer como exceção, com a mensagem do `jsonix-lexml`.
- Ter um único caminho de execução do `jsonix-lexml` na biblioteca.
- Manter o contrato `ConversorLexmlJsonix` (`xmlToJson`/`jsonToXml`) e a configuração existente aceita.

**Non-Goals:**
- Mudar o `LexmlJsonixService`: obtenção do texto e regras de `preferirSubstitutivo`.
- Mudar o `ConversorDocumentoArticuladoCli`.

## Decisions

- **Delegar ao `ConversorDocumentoArticuladoCli`.** O `ConversorLexmlJsonixImpl` cria, no construtor, um `ConversorDocumentoArticuladoCli(Paths.get(cli), timeout)`:
  - `xmlToJson` chama `xmlParaJson`;
  - `jsonToXml` chama `jsonParaXml`.

  Alternativa descartada: corrigir o `CliUtils`. Daria dois caminhos para a mesma execução, e o modo por entrada e saída padrão da 2.x não está documentado nem testado.
- **Sempre por arquivo.** As propriedades `usarEntradaSaidaPadraoDuranteConversao` e `removerDiretorioAposConversao`:
  - continuam em `LexmlJsonixProperties`, com `@Deprecated` e Javadoc dizendo que não têm efeito;
  - geram aviso no log, ao criar o conversor, se `usarEntradaSaidaPadraoDuranteConversao=true` ou `removerDiretorioAposConversao=false`.

  O padrão de `usarEntradaSaidaPadraoDuranteConversao` continua `true`, para não mudar o binding. Por isso, para não registrar o aviso em toda aplicação que não informa a propriedade, o aviso só sai quando ela foi informada explicitamente. Para saber se foi informada, a propriedade passa a ser `Boolean`, com `null` como padrão: `getUsarEntradaSaidaPadraoDuranteConversao()` devolve `null` quando não foi informada.

  Os getters antigos (`isUsarEntradaSaidaPadraoDuranteConversao()` e `isRemoverDiretorioAposConversao()`) são removidos, porque ninguém fora do conversor os usa (verificar na task 1). Se algum consumidor externo os usar, manter `boolean` e aceitar o aviso só para `false` em `removerDiretorioAposConversao`.
- **Tempo limite**: nova propriedade `Duration timeout = Duration.ofSeconds(60)` (`lexml-jsonix.timeout`). O binding de `Duration` do Spring aceita `60s`, `2m` e similares.
- **Exceções**:
  - as do delegado (`EtaBackendException`, que é `RuntimeException`) são relançadas como `RuntimeException`, com o prefixo atual ("Erro ao converter de xml para jsonix: " / "Erro ao converter de json para xml: ") e a causa original;
  - as mensagens e o tipo visto pelos hosts continuam os mesmos de hoje, sem o caminho silencioso.
- **Verificação do executável**: mantém `checarExistenciaArquivo` e a exceção do perfil `test`.
- **Caminho relativo**: o `ProcessBuilder` com lista resolve `./jsonix-lexml-linux` em relação ao diretório de trabalho do processo, como o `bash -c` fazia. Não precisa de `bash`/`cmd`, e caminhos com espaço deixam de quebrar.
- **Remoção do `CliUtils` e do `DadosInOut`**, se nenhum outro código os usar (busca na task 1).
- **Testes**: o `ConversorLexmlJsonixImpl` ganha um construtor de pacote que recebe o `ConversorDocumentoArticulado` delegado. Com ele, os testes cobrem:
  - a delegação nas duas direções;
  - o embrulho da exceção, com prefixo e causa;
  - a falha de criação com executável inexistente;
  - o aviso das propriedades obsoletas, com captura do log pelo appender do Logback;
  - o tempo limite padrão.

  Os cenários de stderr, código de saída, saída vazia e tempo limite estourado já estão cobertos por `ConversorDocumentoArticuladoCliTest`, que exercita o mesmo código.

## Levantamento (task 1.1)

Busca em `eta-backend-services` (código principal e testes) e nos hosts locais `lexeditweb`, `lexeditweb2` e `eta/editor-emendas` (Java, `.properties` e `.yaml`):

- **`CliUtils` e `DadosInOut`**: usados só pelo `ConversorLexmlJsonixImpl`. Podem ser removidos.
- **`isUsarEntradaSaidaPadraoDuranteConversao()` e `isRemoverDiretorioAposConversao()`**: chamados só pelo `ConversorLexmlJsonixImpl`; nenhum host os usa em código. A decisão de trocar para `Boolean` se mantém.
- **Configuração dos hosts**: os três informam `lexml-jsonix.usar-entrada-saida-padrao-durante-conversao=false` (o comportamento novo, sem aviso) e nenhum informa `remover-diretorio-apos-conversao`. O `lexml-jsonix.cli` é relativo (`jsonix-lexml-win.exe`, `./jsonix-lexml-linux`, `./jsonix-lexml-macos`), resolvido pelo `ProcessBuilder` no diretório de trabalho, como antes.

## Risks / Trade-offs

- [Consumidor que usava o modo por entrada e saída padrão] → passa ao modo por arquivo sem mudar configuração. Custo: E/S em disco temporário, desprezível para o tamanho das proposições.
- [Falha que antes devolvia texto vazio passa a ser HTTP 500 no host] → é o objetivo. A mensagem do `jsonix-lexml` chega ao log do host. Registrar em `CHANGELOG`/README, se o projeto mantiver, para quem consome a biblioteca.
- [Mudança de `boolean` para `Boolean` nas propriedades] → o binding por `application.properties` continua igual. Só quebra quem chama os getters antigos em código Java, o que a task 1 verifica.

## Open Questions

Nenhuma.
