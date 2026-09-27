# servico-vendas-veiculos

Servico responsavel pela listagem de veiculos disponiveis/vendidos e
pela efetivacao de vendas, incluindo o webhook de confirmacao de
pagamento. Faz parte de uma arquitetura de dois servicos independentes;
o outro, [sistema-principal-veiculos](../sistema-principal-veiculos),
e o dono do cadastro/edicao de veiculos (dados-mestre).

## O que ele faz

- Mantem uma copia local dos veiculos, sincronizada via HTTP a partir
  do `sistema-principal-veiculos` a cada cadastro/edicao — permite
  listar e vender sem depender do sistema-principal estar no ar.
- Lista veiculos disponiveis e vendidos.
- Efetiva uma venda (CPF do comprador + veiculo), reservando o veiculo
  e gerando um `codigoPagamento`.
- Recebe o webhook da entidade de pagamento externa (simulada) e, a
  partir do resultado, aprova ou cancela a venda, atualiza o status do
  veiculo e notifica o `sistema-principal-veiculos` do resultado.
- Banco fisicamente separado (`veiculos_vendas_db`, container proprio)
  para suportar picos de acesso sem afetar o cadastro.

## Stack

- Java 17
- Spring Boot 3.5.x (Web, Data JPA, Validation)
- PostgreSQL
- Lombok
- Arquitetura em camadas inspirada em Clean Architecture, com Presenter
  dedicado por dominio (`adapter/in/presenter`) separando a formatacao
  da resposta HTTP do UseCase (ver [ARCHITECTURE.md](ARCHITECTURE.md))

## Endpoints

| Metodo | Rota | Descricao |
|---|---|---|
| `GET` | `/veiculos/disponiveis` | Lista veiculos com status `DISPONIVEL` |
| `GET` | `/veiculos/vendidos` | Lista veiculos com status `VENDIDO` |
| `POST` | `/veiculos/sync` | Callback do sistema-principal-veiculos a cada cadastro/edicao |
| `POST` | `/vendas` | Efetiva uma venda (CPF + veiculo) |
| `POST` | `/webhooks/pagamento` | Callback da entidade de pagamento externa (simulada) |

Erros seguem um formato padrao (`ErrorResponse`): `400` para validacao
(Bean Validation) ou dado de dominio invalido (ex.: CPF invalido),
`404` para recurso nao encontrado, `409` para conflito de estado (ex.:
vender veiculo que nao esta `DISPONIVEL`).

## Fluxo de uma venda

1. `POST /veiculos/sync` mantem a copia local do veiculo atualizada.
2. `POST /vendas` reserva o veiculo (`DISPONIVEL -> RESERVADO`), gera um
   `codigoPagamento`, cria a venda como `PENDENTE` **e notifica o
   sistema-principal-veiculos** (`PATCH /veiculos/{id}/status` com
   `RESERVADO`) — essencial para o dado-mestre nao ficar desatualizado
   enquanto a venda esta em andamento.
3. `POST /webhooks/pagamento` informa o resultado:
   - `APROVADO` -> venda vira `PAGAMENTO_APROVADO`, veiculo vira `VENDIDO`.
   - `CANCELADO` -> venda vira `PAGAMENTO_CANCELADO`, veiculo volta a `DISPONIVEL`.
4. O resultado final e notificado ao `sistema-principal-veiculos` do
   mesmo jeito (`SistemaPrincipalPort` / `PATCH /veiculos/{id}/status`).

## Instruções de Execução

### Pré-requisitos
- JDK 17 ou superior (o Maven Wrapper baixa o Maven sozinho)
- Docker rodando
- Infraestrutura de banco já no ar (ver repositório
  [infra-databases-revenda-veiculos](https://github.com/eduardofrauches/infra-databases-revenda-veiculos), passo anterior)

### 1. Clonar o repositório
```bash
git clone https://github.com/eduardofrauches/servico-vendas-veiculos.git
cd servico-vendas-veiculos
```

### 2. Subir os bancos (se ainda não tiver feito)
Clone [infra-databases-revenda-veiculos](https://github.com/eduardofrauches/infra-databases-revenda-veiculos) como pasta irmã
(mesmo nível) deste repositório e siga o README de lá.

### 3. Rodar a aplicação
```bash
./mvnw spring-boot:run
```

**Fluxo completo:** suba os bancos e **os dois serviços antes de cadastrar
veículos** no
[sistema-principal-veiculos](https://github.com/eduardofrauches/sistema-principal-veiculos)
(porta `8081`). O cadastro é enviado a este serviço no momento em que
acontece: um veículo cadastrado com este serviço fora do ar não aparece em
`/veiculos/disponiveis`. Para sincronizar de novo, basta editar o veículo no
sistema-principal (`PUT /veiculos/{id}`).

A aplicação sobe na porta `8082` e conecta no PostgreSQL do
container `revenda-postgres-vendas`
(`localhost:5433/veiculos_vendas_db`, ver `application.yml`). O schema
é criado automaticamente (`ddl-auto: update`).

## Kubernetes

Manifests Kustomize em `k8s/` (`base/` + `overlays/local/`, para uso
com Minikube). **Importante:** o banco de dados (`postgres-vendas`)
**nao esta neste repositorio** — ele vive em um repositorio separado,
[infra-databases-revenda-veiculos](https://github.com/eduardofrauches/infra-databases-revenda-veiculos),
porque a infraestrutura de banco e compartilhada e nao pertence a
nenhum dos dois microsservicos individualmente. **Aplique os
manifests desse repositorio antes de subir este servico no cluster**
— sem o banco no ar, os pods deste Deployment nunca ficam `Ready`
(falham a `readinessProbe`/`livenessProbe` em `/actuator/health`).

```bash
# 0. Cluster local (so na primeira vez; requer Minikube e kubectl)
minikube start

# 1. Bancos (so na primeira vez), a partir do repositorio
#    infra-databases-revenda-veiculos
kubectl apply -k .
kubectl rollout status statefulset/postgres-vendas --timeout=300s

# 2. Este servico (a partir deste repositorio): constroi a imagem
#    dentro do Minikube e aplica os manifests
minikube image build -t servico-vendas-veiculos:local .
kubectl apply -k k8s/overlays/local
kubectl rollout status deployment/servico-vendas-veiculos --timeout=300s

# 3. Acesso (o Service e interno ao cluster): deixe rodando em outro
#    terminal e use http://localhost:8082
kubectl port-forward svc/servico-vendas-veiculos 8082:8082
```

- Os passos 0 e 1 so precisam ser feitos uma vez: se voce ja seguiu o
  README do outro servico, comece pelo passo 2. Rodar `minikube start` de
  novo com o cluster no ar derruba os `port-forward` abertos (o terminal
  mostra `lost connection to pod`); se acontecer, basta rodar o
  `port-forward` de novo.
- Para o fluxo completo, repita os passos 2 e 3 no
  [sistema-principal-veiculos](https://github.com/eduardofrauches/sistema-principal-veiculos)
  (porta `8081`) **antes de cadastrar veiculos**.
- Se este servico ja estiver rodando localmente com `./mvnw spring-boot:run`,
  pare-o antes do `port-forward` (mesma porta `8082`).
- Para atualizar a imagem depois de mudar o codigo: rode de novo o
  `minikube image build` do passo 2 e depois
  `kubectl rollout restart deployment/servico-vendas-veiculos`.

## Testado manualmente

Fluxo ponta a ponta validado com `curl` junto com o
`sistema-principal-veiculos`. Ver [ARCHITECTURE.md](ARCHITECTURE.md) para os
detalhes arquiteturais.

## Testando manualmente (exemplos de requisicao)

Exemplos prontos para os 5 endpoints deste servico (porta `8082`).

> **Atencao (Windows/PowerShell):** o `curl` do PowerShell e um apelido
> (alias) para `Invoke-WebRequest` e nao aceita a sintaxe `-H`/`-d` do
> curl tradicional. Chamar `curl.exe` diretamente tambem pode falhar,
> porque o parsing de linha de comando do Windows reinterpreta as aspas
> do JSON antes de repassar ao programa — o servidor acaba recebendo um
> JSON corrompido e devolve `400`/`500`. **No PowerShell, use sempre
> `Invoke-RestMethod`**, como nos exemplos abaixo. Em Linux/Mac/Git Bash,
> os exemplos com `curl` funcionam normalmente.

### Listar disponiveis (`GET /veiculos/disponiveis`)

```bash
curl http://localhost:8082/veiculos/disponiveis
```
```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8082/veiculos/disponiveis"
```

### Listar vendidos (`GET /veiculos/vendidos`)

```bash
curl http://localhost:8082/veiculos/vendidos
```
```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8082/veiculos/vendidos"
```

### Sincronizar veiculo (`POST /veiculos/sync`) — uso interno, chamado pelo sistema-principal-veiculos

```bash
curl -X POST http://localhost:8082/veiculos/sync \
  -H "Content-Type: application/json" \
  -d '{"id":1,"marca":"Fiat","modelo":"Uno","ano":2020,"cor":"Branco","preco":35000.00,"status":"DISPONIVEL"}'
```
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8082/veiculos/sync" -ContentType "application/json" -Body '{"id":1,"marca":"Fiat","modelo":"Uno","ano":2020,"cor":"Branco","preco":35000.00,"status":"DISPONIVEL"}'
```

### Efetuar venda (`POST /vendas`)

```bash
curl -X POST http://localhost:8082/vendas \
  -H "Content-Type: application/json" \
  -d '{"veiculoId":1,"cpfComprador":"11144477735"}'
```
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8082/vendas" -ContentType "application/json" -Body '{"veiculoId":1,"cpfComprador":"11144477735"}'
```

*(o CPF precisa ter digito verificador valido — o dominio valida modulo 11, nao so o formato)*

O campo `dataVenda` e **opcional**: se nao for enviado, o servidor usa a
data/hora atual. Para informar uma data explicita:

```bash
curl -X POST http://localhost:8082/vendas \
  -H "Content-Type: application/json" \
  -d '{"veiculoId":1,"cpfComprador":"11144477735","dataVenda":"2026-01-15T10:30:00"}'
```

### Webhook de pagamento (`POST /webhooks/pagamento`)

```bash
curl -X POST http://localhost:8082/webhooks/pagamento \
  -H "Content-Type: application/json" \
  -d '{"codigoPagamento":"<codigo-recebido-na-venda>","resultado":"APROVADO"}'
```
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8082/webhooks/pagamento" -ContentType "application/json" -Body '{"codigoPagamento":"<codigo-recebido-na-venda>","resultado":"APROVADO"}'
```

*(valores possiveis de `resultado`: `APROVADO`, `CANCELADO`)*

### Respostas de erro possiveis

| Situacao | Status |
|---|---|
| Campo obrigatorio faltando ou invalido | `400` |
| CPF invalido (digito verificador) | `400` |
| Corpo da requisicao ausente ou JSON mal formado | `400` |
| Veiculo/venda nao encontrado | `404` |
| Vender veiculo indisponivel | `409` |
| Metodo HTTP nao suportado nessa rota | `405` |

## Testes

./mvnw test      # unitarios + integracao (Testcontainers) + BDD (Cucumber)
./mvnw verify     # idem, e falha o build se a cobertura de linha ficar < 80%

Requer Docker rodando (os testes de integracao e o BDD sobem um
Postgres real via Testcontainers, container por teste). Cobertura via
JaCoCo em `target/site/jacoco/index.html` apos `./mvnw test`.

- **Unitarios** (`application/**/usecase/*Test.java`): cada UseCase
  testado com Mockito, mockando os `port/out` — sem Spring, sem banco.
  Os UseCases devolvem a Entity de dominio (`Veiculo`/`Venda`, ou
  `List<Veiculo>`), nao mais o DTO de resposta, entao as asserções
  verificam a Entity retornada.
- **Value Objects** (`domain/vo/*Test.java`): `CpfTest` e `PrecoTest`
  cobrem os casos de borda de cada um isoladamente (CPF nulo, tamanho
  errado, digitos repetidos, digito verificador invalido, formatacao;
  preco nulo, zero, negativo, arredondamento), sem depender de nenhuma
  outra camada.
- **Presenter** (`adapter/in/presenter/**/*PresenterTest.java`):
  `VeiculoPresenterTest` e `VendaPresenterTest` cobrem a conversao para
  `VeiculoResponse`/`VendaResponse`, isolada do UseCase.
- **Client HTTP** (`adapter/out/venda/client/*Test.java`):
  `SistemaPrincipalHttpAdapterTest` cobre os 3 mapeamentos de status
  enviados ao sistema-principal-veiculos (pendente -> reservado,
  aprovado -> vendido, cancelado -> disponivel), mockando o
  `RestTemplate`, alem do caso de falha de rede sendo tratada sem
  quebrar o fluxo.
- **Integracao** (`adapter/out/**/*IT.java`): `VeiculoRepositoryAdapterIT`
  e `VendaRepositoryAdapterIT`, `@DataJpaTest` contra Postgres real
  (Testcontainers, nao H2).
- **BDD** (`bdd/`): `RunCucumberTest` roda o `.feature` em
  `src/test/resources/features/venda_veiculo.feature` — fluxo completo
  (cadastro simulado -> venda -> webhook de pagamento) via MockMvc
  contra a aplicacao real (Spring context + Postgres real via
  Testcontainers). `SistemaPrincipalPort` e mockado (`@MockitoBean`)
  nesse teste, ja que a comunicacao com o outro servico nao e o alvo do
  cenario.

**Ultima medicao:** cobertura de 98% de instrucoes e 90% de branches
(JaCoCo).

## Em construcao

Este projeto ja sobe de verdade, tem o fluxo principal funcionando,
uma suite de testes com cobertura acima de 80%, Dockerfile, pipeline
de CI/CD e manifests Kubernetes. Ainda faltam, para as proximas
etapas:

- [ ] Migrations versionadas (Flyway/Liquibase) em vez de `ddl-auto: update`.
- [ ] Push da imagem Docker para um registry (Docker Hub/GHCR) — hoje o estagio `docker` do CI so builda localmente.
- [ ] Overlay Kubernetes para nuvem (`k8s/overlays/aws` ou equivalente) — hoje so existe `overlays/local`.
- [ ] Gerenciamento de segredos de verdade (Sealed Secrets, Vault, External Secrets) no lugar do `Secret` placeholder.
- [ ] Resiliencia mais robusta nas chamadas HTTP (retry/circuit breaker) — hoje uma falha so gera um log de warning.
- [x] Tratamento de erros HTTP validado manualmente (JSON malformado, metodo nao suportado, CPF invalido, recurso nao encontrado, conflito de estado) — ver secao "Testando manualmente" acima.
- [ ] Testes de contrato/erro para os controllers e o `GlobalExceptionHandler` (hoje cobertos so indiretamente pelo BDD).
