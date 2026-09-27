# Decisões de Arquitetura — servico-vendas-veiculos

Registro das decisões arquiteturais deste serviço, mantido ao longo
do desenvolvimento para referência futura.

## O que este servico e

Um dos dois microsservicos da plataforma de revenda de veiculos.
Responsavel por listagens de veiculos e pela efetivacao de vendas,
incluindo o webhook de pagamento. O outro servico,
`sistema-principal-veiculos` (repo irmao, em
`../sistema-principal-veiculos`), e o dono do cadastro/edicao de
veiculos. Este servico mantem apenas uma **copia local** dos veiculos,
sincronizada via HTTP — nunca acessa o banco do sistema-principal.

## Build e comandos

```bash
./mvnw clean compile      # compilar
docker compose up -d      # no repo irmao infra-databases-revenda-veiculos: sobe o Postgres deste servico e o do outro
./mvnw spring-boot:run    # rodar (porta 8082) — precisa do Postgres acima no ar
./mvnw test                # quando houver testes
```

## Estrutura de pastas (Clean Architecture)

```
src/main/java/com/revendaveiculos/servicovendas/
├── adapter/
│   ├── in/
│   │   ├── controller/
│   │   │   ├── veiculo/VeiculoListingController      <- GET /veiculos/disponiveis, /vendidos, POST /veiculos/sync
│   │   │   └── venda/
│   │   │       ├── VendaController                    <- POST /vendas
│   │   │       └── PagamentoWebhookController          <- POST /webhooks/pagamento
│   │   ├── presenter/
│   │   │   ├── veiculo/VeiculoPresenter                <- formata Entity Veiculo (ou List<Veiculo>) em VeiculoResponse
│   │   │   └── venda/VendaPresenter                     <- formata Entity Venda em VendaResponse
│   │   └── exception/GlobalExceptionHandler            <- @RestControllerAdvice, traduz exceptions -> HTTP
│   └── out/
│       ├── veiculo/persistence/jpa/{entity,mapper,repository}/   <- VeiculoEntity (id NAO gerado aqui, vem do sistema-principal), VeiculoEntityMapper, VeiculoJpaRepository + VeiculoRepositoryAdapter
│       └── venda/
│           ├── persistence/jpa/{entity,mapper,repository}/       <- VendaEntity, VendaEntityMapper, VendaJpaRepository + VendaRepositoryAdapter
│           └── client/                                            <- SistemaPrincipalHttpAdapter (implementa SistemaPrincipalPort via RestTemplate)
├── application/
│   ├── veiculo/
│   │   ├── dto/{request,response}/, mapper/     <- DTOs com Bean Validation
│   │   ├── port/{in,out}/          <- Listar*InputPort, SincronizarVeiculoInputPort / VeiculoRepositoryPort
│   │   └── usecase/                <- ListarVeiculosDisponiveisUseCase, ListarVeiculosVendidosUseCase, SincronizarVeiculoUseCase
│   └── venda/
│       ├── dto/{request,response}/, mapper/     <- DTOs com Bean Validation
│       ├── port/{in,out}/          <- EfetuarVendaInputPort, ProcessarWebhookPagamentoInputPort / VendaRepositoryPort, SistemaPrincipalPort
│       └── usecase/                <- EfetuarVendaUseCase, ProcessarWebhookPagamentoUseCase
├── domain/
│   ├── model/
│   │   ├── veiculo/                <- Veiculo (copia local, com metodos reservar/confirmarVenda/cancelarReserva), StatusVeiculo
│   │   └── venda/                  <- Venda (regras de pagamento), StatusVenda, ResultadoPagamento
│   ├── vo/                         <- Preco, Cpf (Value Objects imutaveis)
│   └── exception/                  <- exceptions de dominio (RuntimeException)
└── infrastructure/config/venda/RestTemplateConfig  <- bean RestTemplate (JdkClientHttpRequestFactory, suporta PATCH)
```

## Decisoes arquiteturais

1. **Mesmo padrao de Clean Architecture do sistema-principal-veiculos**
   (porta de entrada/saida explicitas, por dominio de negocio). Ver
   `ARCHITECTURE.md` do repo irmao para o racional completo — aqui vale a
   mesma regra: controllers dependem so de `port/in`; UseCases
   implementam `port/in` e dependem de `port/out`.
1a. **Presenter dedicado por dominio** (`adapter/in/presenter/veiculo/VeiculoPresenter`,
   `adapter/in/presenter/venda/VendaPresenter`), adicionado apos
   auditoria de Clean Architecture que apontou a ausencia dessa camada
   como lacuna de um feedback de trabalho anterior do curso — mesma
   motivacao e mesmo local (`adapter/in/presenter/`, ao lado de
   `adapter/in/controller/`) do sistema-principal-veiculos (ver
   decisao 1a de la para o racional completo). Os `port/in`
   (`SincronizarVeiculoInputPort`, `ListarVeiculosDisponiveisInputPort`,
   `ListarVeiculosVendidosInputPort`, `EfetuarVendaInputPort`,
   `ProcessarWebhookPagamentoInputPort`) agora devolvem a Entity de
   dominio (`Veiculo`/`Venda`, ou `List<Veiculo>`), nao mais o DTO de
   resposta; os Controllers chamam o Presenter correspondente antes de
   montar o `ResponseEntity`. `VeiculoMapper` manteve so `paraDominio`
   (usado por `SincronizarVeiculoUseCase`); `VendaMapper` foi **removido**
   por completo — so tinha o metodo `paraResponse`, que foi movido para
   `VendaPresenter`, entao a classe ficou sem nenhuma responsabilidade
   (nem `EfetuarVendaUseCase` nem `ProcessarWebhookPagamentoUseCase`
   nunca precisaram converter DTO -> dominio; a Entity `Venda` sempre
   foi construida via `Venda.efetuar(...)`). Mudanca estrutural, sem
   alterar nenhuma regra de negocio — suite inteira (unitarios +
   integracao + BDD) permanece verde e a cobertura continua acima de
   80% depois da migracao (ver secao Testes).
2. **`EfetuarVendaUseCase` depende de `VeiculoRepositoryPort`
   (do dominio `veiculo`), `VendaRepositoryPort` e `SistemaPrincipalPort`
   (do dominio `venda`)** — uma excecao consciente ao isolamento por
   dominio: o caso de uso de venda precisa reservar o veiculo antes de
   criar a venda, entao ele cruza os `port/out`. O mesmo vale para
   `ProcessarWebhookPagamentoUseCase`, que atualiza tanto a `Venda`
   quanto o `Veiculo` (e chama `SistemaPrincipalPort` ao final).
3. **`Veiculo` neste servico e uma copia resumida**, sem `placa` (o
   dado-mestre completo, incluindo `placa`, vive so no
   sistema-principal). Campos: `id, marca, modelo, ano, cor, preco,
   status`. Sincronizada via `POST /veiculos/sync`
   (`SincronizarVeiculoUseCase`), chamado pelo sistema-principal a cada
   cadastro/edicao. **`VeiculoEntity.id` NAO usa `@GeneratedValue`** —
   e o mesmo id atribuido pelo sistema-principal; `save()` funciona como
   upsert (Hibernate faz merge: insere se nao existir, atualiza se ja
   existir).
4. **Maquina de estados do veiculo** (`domain/model/veiculo/Veiculo`):
   `DISPONIVEL -> RESERVADO -> VENDIDO`, com `RESERVADO -> DISPONIVEL`
   para cancelamento. `reservar()` lanca `VeiculoNaoDisponivelException`
   se o veiculo nao estiver `DISPONIVEL` (protege contra venda em
   paralelo do mesmo veiculo a nivel de dominio — falta ainda garantir
   isso a nivel de banco, ex.: lock otimista).
5. **`Venda`** (`domain/model/venda/Venda`) gera seu proprio
   `codigoPagamento` (`UUID`) ao ser efetuada (`Venda.efetuar(...)`) e
   comeca `PENDENTE`. So pode ser confirmada/cancelada uma unica vez —
   `confirmarPagamento()`/`cancelarPagamento()` lancam
   `TransicaoStatusInvalidaException` se o status nao for `PENDENTE`
   (evita processar o mesmo webhook duas vezes).
6. **Value Object `Cpf`** (`domain/vo/Cpf`) valida o CPF com o
   algoritmo oficial de digitos verificadores (modulo 11), rejeita
   sequencias de digitos repetidos (ex.: `111.111.111-11`) e normaliza
   a entrada (aceita com ou sem mascara). `Preco` e identico ao do
   sistema-principal (duplicado por design — cada servico e um
   deployable independente, sem biblioteca compartilhada por enquanto).
7. **`ResultadoPagamento`** (`APROVADO`/`CANCELADO`) e o enum que o
   webhook usa para decidir entre `confirmarPagamento()` e
   `cancelarPagamento()` — modela o "resultado" do pagamento simulado
   citado no documento de arquitetura.
8. **`SistemaPrincipalHttpAdapter` propaga TRES momentos, nao so o
   final**: `EfetuarVendaUseCase` chama `SistemaPrincipalPort` logo
   apos reservar (`Venda.PENDENTE -> "RESERVADO"` no sistema-principal);
   `ProcessarWebhookPagamentoUseCase` chama de novo no resultado final
   (`PAGAMENTO_APROVADO -> "VENDIDO"`, `PAGAMENTO_CANCELADO -> "DISPONIVEL"`).
   **Isso corrigiu um bug real encontrado no teste manual ponta a
   ponta**: sem notificar a reserva, o sistema-principal ficava com o
   veiculo em `DISPONIVEL` durante toda a venda e, ao tentar aplicar o
   resultado final via `PATCH /veiculos/{id}/status`, a propria maquina
   de estados de `Veiculo` la (que so permite `DISPONIVEL -> RESERVADO`,
   nao `DISPONIVEL -> VENDIDO` direto) rejeitava com `409`. Ver o mapa
   de status em `SistemaPrincipalHttpAdapter.notificarResultadoVenda`.
9. **`RestTemplateConfig`** usa `JdkClientHttpRequestFactory`
   (java.net.http.HttpClient) explicitamente, em vez do
   `SimpleClientHttpRequestFactory` padrao do Spring Boot — este ultimo
   **nao suporta o metodo PATCH** (lanca `ProtocolException`) e e usado
   pelo `SistemaPrincipalHttpAdapter`. O timeout de conexao e
   configurado direto no `HttpClient` (nao via
   `RestTemplateBuilder.connectTimeout(...)`, que e incompativel com
   essa factory por reflection).
10. **Bean Validation** nos DTOs de request
    (`SincronizarVeiculoRequest`, `EfetuarVendaRequest`,
    `WebhookPagamentoRequest`). `GlobalExceptionHandler`
    (`adapter/in/exception`) traduz: `MethodArgumentNotValidException`,
    `PrecoInvalidoException`, `CpfInvalidoException` -> `400`;
    `VeiculoNaoEncontradoException`, `VendaNaoEncontradaException` ->
    `404`; `TransicaoStatusInvalidaException`,
    `VeiculoNaoDisponivelException` -> `409`. Note que ha DUAS
    exceptions distintas para veiculo: `VeiculoNaoEncontradoException`
    (id nao existe, 404) vs `VeiculoNaoDisponivelException` (existe mas
    nao esta `DISPONIVEL`, 409) — nao confundir. **Adicionado em
    26/09**, apos teste manual ponta a ponta ter revelado dois casos
    que caiam no handler generico (`500`) em vez de um status
    especifico: `HttpMessageNotReadableException` -> `400` (corpo da
    requisicao ausente ou JSON mal formado) e
    `HttpRequestMethodNotSupportedException` -> `405` (metodo HTTP nao
    suportado na rota).
11. **Persistencia**: `ddl-auto: update` (sem Flyway/Liquibase ainda) —
    decisao pragmatica para esta etapa inicial, documentada como
    pendencia.
12. **`application.yml`** aponta para o PostgreSQL do container
    `revenda-postgres-vendas` (`veiculos_vendas_db`, porta **5433** do
    host — fisicamente separado do banco do sistema-principal, que usa
    5432) e define a porta HTTP do servico (`8082`) e a URL base do
    `sistema-principal-veiculos`. O `docker-compose.yml` que sobe esse
    banco fica no repositorio irmao **`infra-databases-revenda-veiculos`**
    (clonado como pasta irma deste), nao dentro deste projeto.

## Testado manualmente (fluxo ponta a ponta)

Validado com `docker compose up -d` + `mvn spring-boot:run` nos dois
servicos + `curl`: veiculo cadastrado no sistema-principal aparece
automaticamente em `GET /veiculos/disponiveis` aqui -> `POST /vendas`
reserva e some da lista de disponiveis -> sistema-principal reflete
`RESERVADO` -> `POST /webhooks/pagamento` com `APROVADO` -> veiculo
aparece em `GET /veiculos/vendidos` aqui e como `VENDIDO` no
sistema-principal. Tambem validados os erros `400` (preco invalido,
CPF invalido), `404` (veiculo inexistente) e `409` (vender veiculo ja
vendido). Ver o resumo de entrega na raiz do repositorio para os
comandos `curl` exatos e as respostas recebidas.

## Testes

- **Unitarios** (`src/test/.../application/{veiculo,venda}/usecase/*Test.java`):
  JUnit 5 + Mockito, mockando `port/out` — sem Spring, sem banco. Um
  teste (`EfetuarVendaUseCaseTest`) expos um bug real: o UseCase
  reservava e salvava o veiculo **antes** de validar o CPF; corrigido
  invertendo a ordem (`Cpf.de(...)` roda primeiro) para nunca deixar um
  veiculo reservado sem venda associada quando o CPF e invalido.
- **Value Objects** (`domain/vo/*Test.java`): `CpfTest` e `PrecoTest`,
  adicionados em 26/09 ao revisar a cobertura, cobrem os casos de borda
  de cada um isoladamente (CPF nulo, tamanho errado, digitos repetidos,
  digito verificador invalido, formatacao; preco nulo, zero, negativo,
  arredondamento).
- **Client HTTP** (`adapter/out/venda/client/*Test.java`):
  `SistemaPrincipalHttpAdapterTest`, tambem adicionado em 26/09, cobre
  os 3 mapeamentos de status enviados ao sistema-principal-veiculos
  (pendente -> reservado, aprovado -> vendido, cancelado ->
  disponivel), mockando o `RestTemplate`, alem do caso de falha de rede
  sendo tratada sem quebrar o fluxo.
- **Integracao** (`*RepositoryAdapterIT.java`): `@DataJpaTest` +
  `@AutoConfigureTestDatabase(replace = NONE)` + Testcontainers
  (`postgres:16-alpine` de verdade, nao H2) — uma para
  `VeiculoRepositoryAdapter` (inclui `listarDisponiveis`/`listarVendidos`),
  outra para `VendaRepositoryAdapter`.
- **BDD** (`src/test/java/.../bdd/`): `venda_veiculo.feature` (Gherkin
  em portugues, `# language: pt`, keywords `Dado/Quando/E/Entao`) com 2
  cenarios (pagamento aprovado e cancelado). `CucumberSpringConfiguration`
  sobe o Spring Boot inteiro (`@SpringBootTest` + `@AutoConfigureMockMvc`)
  contra um Postgres real via Testcontainers; `SistemaPrincipalPort` e
  substituido por `@MockitoBean` — e o unico ponto mockado, exatamente
  a comunicacao com o sistema-principal-veiculos (o cadastro do veiculo
  em si e simulado chamando `POST /veiculos/sync` diretamente, o mesmo
  callback que o outro servico chamaria). `VendaVeiculoSteps` usa
  `MockMvc` para bater nos endpoints reais.
- **Surefire configurado para rodar tambem os `*IT`** na fase `test` —
  decisao deliberada para que `mvnw test` sozinho rode unitarios +
  integracao + BDD, sem precisar de `mvnw verify`/Failsafe.
- **JaCoCo** (`jacoco-maven-plugin`), com **gate de cobertura minima de
  80% de instrucoes, bloqueante, bound a fase `verify`**
  (`mvnw verify` falha se ficar abaixo — `mvnw test` sozinho so mede e
  gera o relatorio, nao bloqueia). Exclusoes do calculo: `*Application`,
  `infrastructure/config/**`, `dto/**`, `persistence/jpa/{entity,mapper}/**`,
  `domain/exception/**`, `adapter/in/exception/**` (boilerplate sem
  logica de negocio) — mesmo espirito das `sonar.coverage.exclusions`
  do `oficina-service-mvp` de referencia.
- **Ultima medicao (26/09):** apos adicionar os testes de `domain.vo` e
  do `adapter.out.venda.client` (os dois unicos pontos ainda fracos,
  com 53%/46% e 0% de cobertura, respectivamente), a cobertura total do
  projeto subiu para **98% de instrucoes e 90% de branches** (JaCoCo),
  bem acima do minimo de 80% exigido, confirmado com `mvnw verify`
  (`jacoco:check` -> "All coverage checks have been met").

## O que ainda falta (proximas etapas)

Migrations versionadas, Dockerfile da aplicacao, CI/CD (o
`mvnw verify` + gate de 80% ja estao prontos pra plugar num pipeline),
Kubernetes, resiliencia mais robusta nas chamadas HTTP (retry/circuit
breaker), lock otimista na reserva de veiculo para concorrencia real,
teste unitario dedicado ao `SistemaPrincipalHttpAdapter`. Ver checklist
detalhado no README.md.
