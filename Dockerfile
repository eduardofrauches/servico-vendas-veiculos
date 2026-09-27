# --- Stage 1: build (JDK + Maven) ---
FROM eclipse-temurin:17-jdk-noble AS build
WORKDIR /app

# Baixa as dependencias primeiro (cache de camada) usando o wrapper do proprio projeto.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src ./src
RUN ./mvnw -B -q clean package -DskipTests

# --- Stage 2: runtime (so JRE) ---
FROM eclipse-temurin:17-jre-noble AS runtime
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=build /app/target/servico-vendas-veiculos-*.jar app.jar
RUN chown spring:spring app.jar
USER spring

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
