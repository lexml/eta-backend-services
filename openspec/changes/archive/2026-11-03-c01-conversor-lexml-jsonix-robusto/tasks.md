# Tasks

## 1. Levantamento

- [x] 1.1 Verificar com busca no repositório (código principal e testes) e nos hosts locais (`lexeditweb`) quem usa `CliUtils`, `DadosInOut`, `isUsarEntradaSaidaPadraoDuranteConversao()` e `isRemoverDiretorioAposConversao()`; registrar o resultado no `design.md` e, se houver uso externo dos getters, ajustar a decisão das propriedades antes de seguir

## 2. Conversor

- [x] 2.1 Adicionar `timeout` (`Duration`, padrão 60 s) e marcar `usarEntradaSaidaPadraoDuranteConversao`/`removerDiretorioAposConversao` como obsoletas em `LexmlJsonixProperties` (tipo conforme a 1.1); verificar com teste JUnit o padrão do `timeout` e que as obsoletas sem valor informado ficam sem valor
- [x] 2.2 Fazer o `ConversorLexmlJsonixImpl` delegar ao `ConversorDocumentoArticuladoCli` (construtor de pacote com o delegado para testes), mantendo a verificação do executável fora do perfil `test`, o prefixo das mensagens de erro e o aviso de propriedade obsoleta; remover `CliUtils` e `DadosInOut` se a 1.1 confirmar que ficam sem uso; verificar com `ConversorLexmlJsonixImplTest` (JUnit 5, sem executável real): delegação `tojson`/`toxml`, exceção com prefixo e causa, `FileNotFoundException` com executável inexistente, aviso no log para `usar-entrada-saida-padrao-durante-conversao=true` e ausência de aviso sem a propriedade

## 3. Verificação e documentação

- [x] 3.1 Rodar `mvn test` e verificar que toda a suíte passa
- [x] 3.2 Atualizar a documentação do projeto que descreve as propriedades `lexml-jsonix.*` (README/`CLAUDE.md`, se houver) com `timeout` e as obsoletas; verificar relendo o trecho
- [x] 3.3 Verificação manual no lexeditweb com o jar local (`mvn install` aqui e no lexeditweb): o texto da proposição na emenda continua carregando e o `parser/jsonix` continua convertendo, no Windows com o `jsonix-lexml-win.exe` 2.3.0 executado da própria pasta `lexeditweb-editor` (cópias executadas em outras pastas vão para a quarentena do Defender); registrar o resultado nesta task
  - Resultado (04/11/2026), com o `ConversorLexmlJsonixImpl` novo (`target/classes`) e a configuração do lexeditweb (`cli=jsonix-lexml-win.exe` relativo, `usar-entrada-saida-padrao-durante-conversao=false`), rodando na pasta `lexeditweb-editor` com o `jsonix-lexml-win.exe` 2.3.0 de lá (SHA-256 `b7b65ed2…`), sobre `documento-articulado-exemplo.xml`/`.json` dos testes:
    - `xmlToJson` e `jsonToXml` convertem, e a ida e volta preserva a articulação;
    - XML inválido gera exceção com o prefixo e a mensagem do `jsonix-lexml`, em vez de texto vazio;
    - nenhum aviso de propriedade obsoleta no log.
  - Observação: com `NoDefaultCurrentDirectoryInExePath` definida no ambiente, o Windows não procura o executável na pasta atual e o caminho relativo falha com `CreateProcess error=2`. A variável não está definida para o usuário nem para a máquina; só no terminal usado no teste. O legado (`cmd.exe /c`) também depende da pasta atual.
  - Endpoints (04/11/2026), com o `LexEditWebDevApplication` no STS carregando o `eta-backend-services/target/classes` pela resolução do workspace (front em `localhost:4200`):
    - `GET /resources/shared/texto-json-proposicao` devolveu o JSON LexML do PL 2338/2023 (172 KB) e da MPV 1185/2023 (39 KB), nenhuma das duas em `resources/proposicoes/`, ou seja, convertidas pelo `jsonix-lexml`;
    - `POST /resources/shared/parser/jsonix` com dois artigos e um parágrafo devolveu o JSON com o texto enviado;
    - o executável continuou no lugar, sem quarentena;
    - os endpoints foram chamados diretamente (são os que a tela da emenda usa); a tela não foi aberta no navegador.
