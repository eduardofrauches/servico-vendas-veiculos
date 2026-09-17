# --- Stage 1: build (JDK + Maven) ---
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Baixa as dependencias primeiro (cache de camada) usando o wrapper do proprio projeto.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src ./src
RUN ./mvnw -B -q clean package -DskipTests

# --- Stage 2: runtime (so JRE) ---
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/target/servico-vendas-veiculos-*.jar app.jar
RUN chown spring:spring app.jar
USER spring

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
