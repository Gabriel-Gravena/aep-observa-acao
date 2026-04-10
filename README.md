# ObservaAcao

Sistema academico em Java para registro e acompanhamento de solicitacoes e demandas publicas. O projeto foi desenvolvido em CLI para o desafio da govtech ficticia **ObservaAcao**, com foco em transparencia, rastreabilidade e reducao de barreiras no acesso a servicos publicos.

## Objetivo

O sistema permite que cidadaos registrem solicitacoes e acompanhem o atendimento por protocolo, enquanto servidores e gestores organizam a fila, atualizam status e mantem um historico claro de movimentacoes.

O problema atacado pelo projeto inclui:

- dificuldade para entender como solicitar servicos publicos;
- falta de transparencia no andamento de protocolos;
- pouca clareza sobre prazos e justificativas;
- necessidade de registrar demandas sensiveis com protecao de identidade.

O projeto conversa principalmente com os ODS:

- **ODS 16**: Paz, Justica e Instituicoes Eficazes;
- **ODS 10**: Reducao das Desigualdades;
- **ODS 11**: Cidades e Comunidades Sustentaveis.

## Funcionalidades implementadas

- tela inicial com opcoes de login, registro e saida;
- cadastro de usuarios com perfil de `CIDADAO` ou `SERVIDOR`;
- autenticacao em memoria por login e senha;
- normalizacao de login para busca e autenticacao;
- separacao de menus por perfil apos login;
- cadastro de solicitacao com categoria, descricao, localizacao e prioridade;
- opcao de registro identificado ou anonimo;
- anonimato forcado para categorias sensiveis, como assedio e denuncia de irregularidade;
- geracao de protocolo para acompanhamento;
- consulta de solicitacao por protocolo;
- historico completo de status com data, responsavel e comentario;
- fila de atendimento ordenada por prioridade;
- atualizacao de status com comentario obrigatorio;
- listagem de demandas atrasadas;
- filtros por bairro e categoria;
- carga inicial com solicitacoes de exemplo para demonstracao.

## Regras de negocio atuais

- usuarios do tipo `CIDADAO` acessam o menu de abertura e acompanhamento de solicitacoes;
- usuarios do tipo `SERVIDOR` acessam o menu operacional de fila e atualizacao de status;
- apenas usuarios do tipo `SERVIDOR` podem atualizar status de solicitacoes;
- fluxo de status: `ABERTO -> TRIAGEM -> EM_EXECUCAO -> RESOLVIDO -> ENCERRADO`;
- login e obrigatorio para autenticacao e nao diferencia maiusculas de minusculas;
- senha deve ter no minimo 4 caracteres;
- descricao deve ter no minimo 20 caracteres;
- localizacao deve ter no minimo 5 caracteres;
- comentario e obrigatorio ao atualizar status;
- registro identificado exige usuario autenticado valido;
- categorias sensiveis podem forcar anonimato mesmo quando o usuario escolhe se identificar;
- prioridade influencia o prazo alvo da solicitacao;
- os dados ficam em memoria durante a execucao.

## Estrutura do projeto

```text
observaacao/
|-- Main.java
|-- model/
|-- repository/
|-- service/
`-- ui/
```

Resumo dos pacotes:

- `model`: entidades principais, como `Solicitacao`, `Usuario`, `ContaAcesso`, `Categoria` e `HistoricoStatus`;
- `repository`: armazenamento em memoria das solicitacoes e das contas de usuario;
- `service`: regras de negocio, fila, anonimato, autenticacao, cadastro de usuarios e geracao de protocolo;
- `ui`: interface de linha de comando com fluxo de acesso, sessao e menus por perfil.

## Como executar

### Pre-requisitos

- Java JDK 21 ou superior instalado;
- terminal com suporte a UTF-8 de preferencia.

### Compilar

No PowerShell, na raiz do projeto:

```powershell
New-Item -ItemType Directory -Force -Path build | Out-Null
javac -encoding UTF-8 -d build (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

### Executar

```powershell
java -cp build observaacao.Main
```

Se o terminal do Windows exibir caracteres estranhos, execute antes:

```powershell
chcp 65001
```

## Como usar

Ao iniciar, o sistema apresenta uma tela inicial com:

1. Login
2. Registro
3. Sair

Depois do login, o menu muda de acordo com o perfil:

- `CIDADAO`: registrar solicitacao, acompanhar por protocolo e ver historico;
- `SERVIDOR`: acompanhar protocolos, ver historico, listar fila, atualizar status, listar atrasadas e filtrar demandas.

No cadastro de solicitacao por usuario autenticado:

- o sistema usa o usuario logado como solicitante identificado;
- o usuario pode optar por anonimato;
- em categorias sensiveis, o anonimato pode ser aplicado automaticamente.

O sistema ja inicia com alguns protocolos de exemplo para facilitar a demonstracao.

### Usuarios iniciais para demonstracao

- cidadao: login `marcos` / senha `1234`
- servidor: login `joana` / senha `1234`
- servidor: login `carlos` / senha `1234`

## Estado atual

- versao beta academica;
- interface via terminal;
- sem banco de dados;
- persistencia apenas em memoria;
- orientado a objetos, sem framework;
- autenticacao simples sem criptografia, adequada apenas para fins academicos;
- separacao basica entre dados de usuario e credenciais de acesso.

## Possiveis evolucoes

- persistencia em arquivo ou banco de dados;
- anexos reais nas solicitacoes;
- painel grafico web;
- relatorios gerenciais;
- testes automatizados.
