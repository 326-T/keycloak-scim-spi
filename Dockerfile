# ────────────────
# 1) Maven で JAR をビルド
# ────────────────
FROM openjdk:21 AS build
WORKDIR /workspace

COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

COPY pom.xml .
COPY src ./src

RUN ./mvnw dependency:go-offline -B
RUN ./mvnw clean package -DskipTests -B
RUN ls -l /workspace/target

# ─────────────────────────
# 2) Quarkus 拡張を取り込んでビルド
# ─────────────────────────
FROM quay.io/keycloak/keycloak:26.2.4 AS builder
COPY --from=build /workspace/target/keycloak-scim-spi-1.0-SNAPSHOT.jar /opt/keycloak/providers/
RUN /opt/keycloak/bin/kc.sh build

# ────────────────
# 3) 実行用イメージ
# ────────────────
FROM quay.io/keycloak/keycloak:26.2.4
COPY --from=builder /opt/keycloak /opt/keycloak
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
