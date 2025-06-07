# Keycloak SCIM SPI

KeycloakにSCIM v2ユーザープロビジョニング機能を追加するService Provider Interface (SPI)です。外部のIDプロバイダー（Microsoft Entra IDなど）からSCIMプロトコルを使用してKeycloakのユーザーを管理できるようになります。

## 機能

- SCIM v2準拠のユーザー管理API
- ユーザーの一覧取得、作成、更新機能
- SCIMフィルター式によるユーザー検索
- Microsoft Entra IDとの連携対応

## 対応エンドポイント

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| GET | `/realms/{realm}/scim/v2/Users` | ユーザー一覧取得・検索 |
| GET | `/realms/{realm}/scim/v2/Users/{userId}` | 特定ユーザーの取得 |
| POST | `/realms/{realm}/scim/v2/Users` | ユーザーの作成 |
| PATCH | `/realms/{realm}/scim/v2/Users/{userId}` | ユーザーの更新 |

## 必要要件

- Java 21
- Kotlin 1.9+
- Maven 3.6+
- Docker & Docker Compose（開発環境用）

## セットアップ

### 1. プロジェクトのビルド

```bash
# 依存関係のダウンロードとJARファイルの作成
./mvnw clean package -DskipTests
```

### 2. Docker環境での実行

```bash
# Keycloak + PostgreSQLの起動
docker-compose up --build
```

このコマンドにより以下が実行されます：
- SPIのビルドとKeycloakへのデプロイ
- PostgreSQLデータベースの起動
- Keycloak（ポート8080）の起動

### 3. Keycloakへのアクセス

- URL: http://localhost:8080
- 管理者ユーザー: `admin`
- パスワード: `password`

## 開発

### テストの実行

```bash
# 単体テストの実行
./mvnw test

# 統合テストの実行
./mvnw verify

# 全テストの実行
./mvnw clean verify
```

### コードの構成

```
src/main/kotlin/org/example/keycloak/
├── provider/                # リソースプロバイダー実装
│   ├── ScimResourceProvider.kt
│   ├── ScimResourceProviderFactory.kt
│   ├── HelloResourceProvider.kt
│   └── HelloResourceProviderFactory.kt
├── schemas/                 # SCIM データモデル
│   ├── ScimCreateUserRequest.kt
│   ├── ScimUserResponse.kt
│   ├── ScimListResponse.kt
│   └── ScimPatchUserRequest.kt
├── util/                    # ユーティリティ
│   └── ScimFilterUtil.kt
└── config/                  # 設定クラス
    └── JacksonConfig.kt
```

### 統合テスト

統合テストはTestcontainersを使用してKeycloakの実際のインスタンスに対して実行されます：

```bash
# 特定の統合テストクラスの実行
./mvnw test -Dtest=ScimEndpointIT
```

## SCIM API使用例

### ユーザー作成

```bash
curl -X POST http://localhost:8080/realms/test/scim/v2/Users \
  -H "Content-Type: application/scim+json" \
  -H "Authorization: Bearer {access_token}" \
  -d '{
    "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
    "userName": "testuser",
    "name": {
      "givenName": "太郎",
      "familyName": "山田"
    },
    "emails": [{
      "value": "yamada@example.com",
      "type": "work"
    }],
    "active": true
  }'
```

### ユーザー検索

```bash
# 全ユーザーの取得
curl -X GET "http://localhost:8080/realms/test/scim/v2/Users" \
  -H "Authorization: Bearer {access_token}"

# フィルター検索
curl -X GET "http://localhost:8080/realms/test/scim/v2/Users?filter=userName eq \"testuser\"" \
  -H "Authorization: Bearer {access_token}"
```

## Microsoft Entra IDとの連携

このSPIはMicrosoft Entra ID（旧Azure AD）のSCIMプロビジョニング機能と連携できます。Entra IDの設定でSCIMエンドポイントとして `http://your-keycloak/realms/{realm}/scim/v2` を指定してください。

## ライセンス

このプロジェクトはサンプル実装です。本番環境での使用前に適切なセキュリティレビューを実施してください。