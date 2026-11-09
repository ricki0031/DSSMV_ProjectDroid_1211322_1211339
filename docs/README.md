# SpotifyClone

Aplicação Android desenvolvida em Java no âmbito da unidade curricular de *Desenvolvimento de Software e Sistemas Móveis* (DSSMV) do ISEP. Utiliza a [Spotify Web API](https://developer.spotify.com/documentation/web-api) para explorar música e controlar a reprodução nos dispositivos Spotify do utilizador.

## Funcionalidades

- **Autenticação:** início de sessão no Spotify com OAuth 2.0 e PKCE, com renovação automática do token.
- **Início:** grelha de acesso rápido, recomendações, "voltar a ouvir" e conteúdos ouvidos recentemente, misturando álbuns, artistas e playlists.
- **Pesquisa:** músicas, artistas, álbuns e playlists, com atalhos por género musical.
- **Biblioteca:** playlists e álbuns guardados, com vários modos de visualização (grelha, grande e lista).
- **Playlists:** criar, editar (nome, descrição e capa), adicionar músicas e remover músicas.
- **Músicas favoritas:** guardar e remover músicas.
- **Páginas de álbum e artista:** músicas do álbum, perfil do artista e discografia.
- **Leitor:** leitor completo e mini player, com reproduzir/pausar, seguinte/anterior, avançar na música, modo aleatório e repetição.
- **Fila:** ver as próximas músicas e adicionar músicas à fila.
- **Interface personalizada:** wallpaper próprio, com o mini player e a barra de navegação nas cores dele.
- **Deteção de falta de ligação:** avisa o utilizador quando não há ligação à Internet.

A interface da aplicação está em português.

## Tecnologias

| Finalidade | Tecnologia |
|---|---|
| Linguagem | Java 11 |
| Plataforma | Android SDK (min SDK 26, target SDK 36) |
| Compilação | Gradle (Kotlin DSL) |
| Cliente HTTP | OkHttp |
| JSON | Gson |
| Imagens | Glide |
| Componentes de interface | Material Components, RecyclerView, ConstraintLayout |
| Testes unitários | JUnit 4 e Robolectric |

## Requisitos

- Android Studio ou IntelliJ IDEA com o Android SDK.
- Dispositivo Android ou emulador com acesso à Internet.
- Conta Spotify Premium, necessária para ser dono da aplicação no Spotify e para controlar a reprodução.
- Dispositivo Spotify ativo (telemóvel, computador ou Web Player) para reproduzir música.

## Configuração

1. Clonar o repositório e abrir o projeto no Android Studio ou IntelliJ IDEA.
2. Criar uma aplicação no [Spotify Developer Dashboard](https://developer.spotify.com/dashboard), com a Web API ativada.
3. Adicionar este Redirect URI à aplicação Spotify:

   ```text
   spotifyclone://callback
   ```

4. Adicionar estas propriedades ao ficheiro `local.properties`, na raiz do projeto, substituindo o valor pelo teu Client ID:

   ```properties
   SPOTIFY_CLIENT_ID=O_TEU_CLIENT_ID
   REDIRECT_URI=spotifyclone://callback
   ```

5. Se a aplicação for usada por outras contas Spotify, adicionar os respetivos emails em **User Management** no Dashboard.
6. Sincronizar o Gradle e executar a aplicação num dispositivo ou emulador.

O ficheiro `local.properties` contém configurações locais e não deve ser incluído no Git.

## Utilização

1. Abrir a aplicação e iniciar sessão com uma conta Spotify autorizada.
2. Iniciar o Spotify num dispositivo, para que a reprodução possa ser controlada.
3. Usar os separadores **Início**, **Pesquisar** e **Biblioteca** para explorar conteúdos.
4. Tocar no mini player para abrir o leitor completo.

## Estrutura do projeto

```text
app/src/main/java/com/example/spotifyclone/
├── MainActivity.java      Autenticação, navegação e mini player
├── PlayerActivity.java    Leitor completo, fila e opções da música
├── adapter/               Adapters das RecyclerView
├── api/                   Clientes da Spotify Web API
├── customization/         Cores do wallpaper na barra de navegação e no mini player
├── fragment/              Ecrãs Início, Pesquisar, Biblioteca, Playlist, Álbum e Artista
├── model/                 Modelos de dados lidos das respostas da API
└── utils/                 Sessão, PKCE, callback da API e funções auxiliares
```

Os ecrãs chamam diretamente as classes de `api/`. As respostas chegam através do `ApiCallback`, que volta à thread principal e converte o JSON nas classes de `model/`.

## Testes

Os testes unitários estão em `app/src/test`, com uma classe de teste por cada classe da aplicação e a mesma estrutura de pastas do código principal. As respostas do Spotify são simuladas, por isso os testes não fazem pedidos reais.

Para os executar, clicar com o botão direito em `app/src/test/java` no IDE e escolher **Run Tests**.

## Documentação de design

| Artefacto | Localização |
|---|---|
| Modelo de Domínio | [`GlobalArtifacts/DM.svg`](GlobalArtifacts/DM.svg) |
| Diagrama de Casos de Uso | [`GlobalArtifacts/UCD.svg`](GlobalArtifacts/UCD.svg) |
| Diagramas de Classes | [`ClassDiagrams`](ClassDiagrams) |
| Diagramas de Sequência de Sistema | [`SSD_SD/SSD`](SSD_SD/SSD) |
| Diagramas de Sequência | [`SSD_SD/SD`](SSD_SD/SD) |

Cada diagrama está disponível em PlantUML (`.puml`) e em imagem (`.svg`). Cada caso de uso tem um SSD e um SD:

| ID | Caso de uso |
|---|---|
| US01 | Log In with Spotify |
| US02 | View Home |
| US03 | Search Content |
| US04 | View Library |
| US05 | Create Playlist |
| US06 | View Playlist |
| US07 | Edit Playlist |
| US08 | Add Track to Playlist |
| US09 | Remove Track from Playlist |
| US10 | Manage Favourite Songs |
| US11 | View Album |
| US12 | View Artist |
| US13 | Play Music |
| US14 | Control Playback |
| US15 | Manage Queue |

## Notas

A reprodução é controlada através da Spotify Web API e depende das permissões e da disponibilidade do serviço Spotify. Este projeto destina-se a fins académicos.
