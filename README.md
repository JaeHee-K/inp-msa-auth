![header](https://capsule-render.vercel.app/api?type=waving&color=auto&height=300&section=header&text=MSA_Project_Authentication_Server&fontSize=40&animation=fadeIn&fontAlignY=38&desc=inp_msa_auth&descAlignY=51&descAlign=62)
<div align="center">
	<img src="https://img.shields.io/badge/Java-007396?style=flat&logo=Java&logoColor=white" />
	<img src="https://img.shields.io/badge/HTML5-E34F26?style=flat&logo=HTML5&logoColor=white" />
	<img src="https://img.shields.io/badge/CSS3-1572B6?style=flat&logo=CSS3&logoColor=white" />
    <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=MySQL&logoColor=white" />
    <img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=flat&logo=Spring Boot&logoColor=white" />
    <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=flat&logo=Spring Security&logoColor=white" />
    <img src="https://img.shields.io/badge/Gradle-1572B6?style=flat&logo=gradle&logoColor=white" />
    <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=flat&logo=Thymeleaf&logoColor=white" />
    <img src="https://img.shields.io/badge/IntelliJ IDEA-000000?style=flat&logo=IntelliJ IDEA&logoColor=white" />
</div>

<br/>

## ⚙️ Skill & Library
JAVA (JDK 17)  
MySQL (8.0.33)  
Spring boot (3.3.1)  
Spring Security (3.3.1)  
Gradle
spring-boot-starter-data-jpa  
spring-boot-starter-web    
mysql-connector-java  
lombok (1.18.32)  
thymeleaf (3.3.1)    

<br/>

## 💿 IDE

Intellij

<br/>

## 🗄 DB Tool

DBeaver

<br/>

# API Documentation

## Base URL
`http://localhost:8080`

## Endpoints

### 1. User Register
**Endpoint:** `POST /user/register`

**Description:** Register a user.

**Request Headers:**
- `Content-Type: application/json`

**Request Body:**
```json
{
    "account" : "userId",
    "password" : "userPassword",
    "confirmPassword" : "userPassword",
    "username" : "Honggildong"
}
```

**Response:**
```json
{
    "status": 201
}
```
<br/>

### 2. Client Register(OIDC)
**Endpoint:** `POST /api/clients/register`

**Description:** Register a authorization_code client.

**Request Headers:**
- `Content-Type: application/json`

**Request Body:**
```json
{
    "clientName": "oidc_client",
    "clientAuthenticationMethods": ["client_secret_basic"],
    "authorizationGrantTypes": ["authorization_code", "refresh_token"],
    "redirectUris": ["http://localhost:8080"],
    "scopes": ["openid", "profile", "email"],
    "clientSettings": {
        "requireProofKey": false,
        "requireAuthorizationConsent": false
      },
      "tokenSettings": {
        "authorizationCodeTimeToLive": "PT5M",
        "accessTokenTimeToLive": "PT1H",
        "accessTokenFormat": {
            "value": "self-contained"
        },
        "deviceCodeTimeToLive": "PT5M",
        "refreshTokenTimeToLive": "P30D",
        "reuseRefreshTokens": false,
        "idTokenSignatureAlgorithm": "RS256",
        "x509CertificateBoundAccessTokens": false
    }
}
```

**Response:**
```json
{
    "status": 201
}
```
<br/>

### 3. Client Register(Client Credential)
**Endpoint:** `POST /api/clients/register`

**Description:** Register a client_credential client.

**Request Headers:**
- `Content-Type: application/json`

**Request Body:**
```json
{
    "clientName": "client_credentials_client",
    "clientAuthenticationMethods": ["client_secret_basic"],
    "authorizationGrantTypes": ["client_credentials"],
    "redirectUris": ["http://localhost:8080"],
    "scopes": ["profile", "email"],
    "clientSettings": {
        "requireProofKey": false,
        "requireAuthorizationConsent": false
    },
    "tokenSettings": {
        "authorizationCodeTimeToLive": "PT5M",
        "accessTokenTimeToLive": "PT1H",
        "accessTokenFormat": {
            "value": "self-contained"
        },
        "deviceCodeTimeToLive": "PT5M",
        "idTokenSignatureAlgorithm": "RS256",
        "x509CertificateBoundAccessTokens": false
    }
}
```

**Response:**
```json
{
    "status": 201
}
```
<br/>

### 4. OIDC Login
**Endpoint:** `GET /api/oauth2/authorize`

**Description:** If user authentication is successful, a code is generated to issue a token.

**Parameters:**
- `response_type` (String, required): The type of response requested. Typically set to code to request an authorization code.
- `client_id` (String, required): Identifier of the client application.
- `redirect_uri` (String, required): The URI to which the client will be redirected after successful or failed authentication. Must match the value set during client registration.
- `scope` (String, required): The scope of the requested permissions. Must include openid, and can include profile, email, address, phone, etc.
- `state` (String, Optional): An opaque value set by the client to prevent CSRF attacks. Returned as-is in the authentication response

**Parameters Example:**
- `response_type=code&client_id=client&redirect_uri=http://localhost:8080&scope=openid email profile&state=state`

**Response:** Go to the login page  
<br/>

### 5. Generate Token(OIDC)
**Endpoint:** `POST /oauth2/token`

**Description:** Generates a "authorization_code" token.

**Request Headers:**
- `Content-Type: application/x-www-form-urlencoded`
- `Authorization: Basic {clientId:clientSecret base64 encoding}`

**Request Body:**
```json
{
    "grant_type": "authorization_code",
    "code": "{code}",
    "redirect_uri": "http://localhost:8080"
}
```

**Response:**
```json
{
    "access_token": "{access_token}",
    "refresh_token": "{refresh_token}",
    "scope": "openid profile email",
    "id_token": "{id_token}",
    "token_type": "Bearer",
    "expires_in": 3599
}
```
<br/>

### 6. Generate Token(Client Credential)
**Endpoint:** `POST /oauth2/token`

**Description:** Generates a "client_credential" token.

**Request Headers:**
- `Content-Type: application/x-www-form-urlencoded`
- `Authorization: Basic {clientId:clientSecret base64 encoding}`

**Request Body:**
```json
{
  "grant_type": "client_credentials",
  "scope": "profile email"
}
```

**Response:**
```json
{
    "access_token": "{access_token}",
    "scope": "profile email",
    "token_type": "Bearer",
    "expires_in": 3599
}
```
<br/>

### 7. Introspect Token
**Endpoint:** `POST /oauth2/introspect`

**Description:** Verify the token.

**Request Headers:**
- `Content-Type: application/x-www-form-urlencoded`
- `Authorization: Basic {clientId:clientSecret base64 encoding}`

**Request Body:**
```json
{
  "token": "{access_token}"
}
```

**Response:**
```json
{
    "active": true,
    "sub": "{client_id}",
    "aud": [
        "{client_id}"
    ],
    "nbf": 1721198572,
    "scope": "profile email",
    "iss": "http://localhost:8080",
    "exp": 1721202172,
    "iat": 1721198572,
    "jti": "921ae4e3-d116-43e0-9566-0416a90c3ba1",
    "client_id": "{client_id}",
    "token_type": "Bearer"
}
```
<br/>

### 8. Revoke Token
**Endpoint:** `POST /oauth2/revoke`

**Description:** Revoke the token.

**Request Headers:**
- `Content-Type: application/x-www-form-urlencoded`
- `Authorization: Basic {clientId:clientSecret base64 encoding}`

**Request Body:**
```json
{
  "token": "{access_token}",
  "token_type_hint": "access_token"
}
```

**Response:**
```json
{
    "status": 200
}
```
<br/>

### 9. User info
**Endpoint:** `GET /userinfo`

**Description:** Check logged in user information.

**Request Headers:**
- `Authorization: Bearer {access_token}`

**Response:**
```json
{
    "sub": "{userId}"
}
```
<br/>

## Error Handling
All endpoints may return the following error responses:

**400 Bad Request:**
```json
{
  "error": "unauthorized_client",
  "error": "unsupported_grant_type",
  "error": "invalid_grant",
  "error": "invalid_scope"
}
```
**401 Unauthorized:**
```json
{
  "error": "invalid_client"
}
```
<br/>

## Authentication
Some endpoints may require authentication. Include the following header in your requests:

**Authorization:**
- `Basic {clientId:clientSecret base64 encoding}`
- `Bearer {access_token}`  
<br/>




## 📁 Table DDL
schema는 x입니다  

- **[Member]**  
>x
  

- **[storage]**  
>x


<br/>  

## 📒 Note
- `application.properties의 spring.datasource url, username, password는 로컬 환경에 맞게 변경`
  
- `csrf는 사용하지 않습니다`
