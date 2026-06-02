# Postal Code Service

REST API for importing and retrieving country postal code using the REST Countries API.

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Web
- Spring Data JPA
- Spring Validation
- H2 in-memory database
- springdoc OpenAPI (Swagger UI)


## Run Locally

### Prerequisites

- Java 17+
- Maven

Application default URL:

- `http://localhost:8082`

## API Endpoints

Base path: `/api/countries`

### 1) Get country postal

`GET /api/countries/{countryCode}`

Example:

```bash
curl http://localhost:8082/api/countries/NL
curl http://localhost:8082/api/countries/NLD
curl http://localhost:8082/api/countries/528
```

Success response body (example):

```json
{
  "countryCode": "NL",
  "countryName": "Netherlands",
  "postalCodeFormat": "#### @@",
  "postalCodeRegex": "^(\\d{4}[A-Z]{2})$"
}
```

### 2) Import country metadata from REST Countries:  `https://restcountries.com/v3.1/alpha/`


`POST /api/countries/add/{countryCode}`

Example:

```bash
curl -X POST http://localhost:8082/api/countries/add/NL
curl -X POST http://localhost:8082/api/countries/add/NLD
curl -X POST http://localhost:8082/api/countries/add/528
```

Success response body (created):

```json
{
  "response": {
    "countryCode": "NL",
    "countryName": "Netherlands",
    "postalCodeFormat": "#### @@",
    "postalCodeRegex": "^(\\d{4}[A-Z]{2})$"
  },
  "created": true,
  "message": "Country successfully added to the database for code: NL"
}
```

If already imported, `created` is `false` and the existing record is returned.


## Validation Rules

`countryCode` must match:

- `2 or 3 letters`, or
- `exactly 3 digits`

## Docs and URLS

- Swagger: `http://localhost:8082/swagger-ui/index.html`
- JSON: `http://localhost:8082/v3/api-docs`
