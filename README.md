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
- Arquitetura em camadas inspirada em Clean Architecture (ver [CLAUDE.md](CLAUDE.md))

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

## Como rodar

1. Suba a infraestrutura de banco (na raiz do repo, `C:\Dev\revenda-veiculos`):
   ```bash
   docker compose up -d
   ```
2. Rode a aplicacao:
   ```bash
   ./mvnw spring-boot:run
   ```

A aplicacao sobe na porta `8082` e conecta no PostgreSQL do container
`revenda-postgres-vendas` (`localhost:5433/veiculos_vendas_db`, ver
`application.yml`). O schema e criado automaticamente
(`ddl-auto: update`) — ainda nao ha migrations (Flyway/Liquibase).

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
# 1. Bancos (repositorio infra-databases-revenda-veiculos)
kubectl apply -k .
kubectl rollout status statefulset/postgres-vendas --timeout=120s

# 2. Este servico (a partir deste repositorio)
minikube image load servico-vendas-veiculos:local
kubectl apply -k k8s/overlays/local
```

## Testado manualmente

Fluxo ponta a ponta validado com `curl` junto com o
`sistema-principal-veiculos`. Ver [CLAUDE.md](CLAUDE.md) para os
detalhes arquiteturais e o resumo de entrega do repositorio para os
comandos usados e as respostas recebidas.

## Testes

```bash
./mvnw test      # unitarios + integracao (Testcontainers) + BDD (Cucumber)
./mvnw verify     # idem, e falha o build se a cobertura de linha ficar < 80%
```

Requer Docker rodando (os testes de integracao e o BDD sobem um
Postgres real via Testcontainers, container por teste). Cobertura via
JaCoCo em `target/site/jacoco/index.html` apos `./mvnw test`.

- **Unitarios** (`application/**/usecase/*Test.java`): cada UseCase
  testado com Mockito, mockando os `port/out` — sem Spring, sem banco.
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

**Ultima medicao:** 22 testes, 0 falhas, cobertura de linha ~86%
(acima do minimo de 80% exigido pelo enunciado). Ver o resumo de
entrega na raiz do repositorio para o detalhamento.

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
- [ ] Testes de contrato/erro para os controllers e o `GlobalExceptionHandler` (hoje cobertos so indiretamente pelo BDD).
