# Proposal

## Why

O `ConversorLexmlJsonixImpl` (bean `ConversorLexmlJsonix`) é usado pelos hosts para converter o texto das proposições, por exemplo o texto da proposição na emenda e o `parser/jsonix` do lexeditweb. Ele executa o `jsonix-lexml` pelo `CliUtils`, que tem estes problemas:

- **Código de saída e exceções:** não olha o código de saída e engole as exceções, só registrando no log.
- **stderr:** no Linux vai para o console do servidor (`inheritIO`); no Windows não é consumido (`redirectError()` sem argumento não faz nada).
- **Tempo limite:** não há.

Com o `jsonix-lexml` 2.x, que sai com código 0 mesmo quando falha, uma conversão com erro pode virar texto vazio sem nenhum aviso, principalmente no modo por entrada e saída padrão, que é o padrão da biblioteca. O lexeditweb atualizou os executáveis para a 2.3.0 (change `2026-10-26-c01-atualizar-jsonix-lexml-2-3-0` de lá). A mesma biblioteca já tem um conversor que trata esses casos: o `ConversorDocumentoArticuladoCli`, do PDF do documento articulado.

## What Changes

- O `ConversorLexmlJsonixImpl` passa a delegar a conversão ao `ConversorDocumentoArticuladoCli`. Com isso ganha:
  - diretório temporário próprio;
  - stderr capturado e incluído na mensagem de erro;
  - tempo limite;
  - código de saída diferente de 0 e saída ausente ou vazia tratados como falha.
- **BREAKING**: conversão que falha passa a lançar exceção com a mensagem do `jsonix-lexml`, em vez de devolver texto vazio ou seguir em silêncio.
- Nova propriedade `lexml-jsonix.timeout` (padrão: 60 s).
- **BREAKING (configuração)**: a conversão é sempre por arquivo. As propriedades `lexml-jsonix.usar-entrada-saida-padrao-durante-conversao` e `lexml-jsonix.remover-diretorio-apos-conversao` deixam de ter efeito:
  - continuam aceitas, para não quebrar configurações existentes;
  - ficam marcadas como obsoletas;
  - registram um aviso quando informadas com valor diferente do novo comportamento.
- Remoção do `CliUtils`, que fica sem uso.

## Capabilities

### New Capabilities

- `conversor-lexml-jsonix`: conversão XML <-> JSON (Jsonix) do texto LexML das proposições pelo executável `jsonix-lexml`, usada pelos hosts. Cobre o tratamento de falha e de tempo limite e as propriedades de configuração.

### Modified Capabilities

Nenhuma. O `pdf-documento-articulado` já usa o `ConversorDocumentoArticuladoCli` e não muda.

## Impact

- **Código**:
  - `lexmljsonix/conversor/ConversorLexmlJsonixImpl.java`;
  - `lexmljsonix/conversor/LexmlJsonixProperties.java`;
  - `lexmljsonix/utils/CliUtils.java` (removido) e `DadosInOut.java` (removido, se ficar sem uso).
- **Testes**: novo `ConversorLexmlJsonixImplTest`, sem o executável real (JUnit 5, `mvn test`).
- **Hosts**:
  - lexeditweb (Senado) e demais consumidores do bean `ConversorLexmlJsonix`.
  - Uma falha de conversão vira exceção (HTTP 500 no endpoint do host), e não resposta vazia.
  - Quem usava o modo por entrada e saída padrão passa ao modo por arquivo, sem precisar mudar configuração.
  - O lexeditweb já usa o modo por arquivo (`usar-entrada-saida-padrao-durante-conversao=false`).
- **Dependência**: `jsonix-lexml` 2.0.0 ou superior, a mesma exigida pelo `ConversorDocumentoArticuladoCli`.
