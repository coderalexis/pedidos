# Customer Service

Microservicio para gestión de clientes del sistema de pedidos Liverpool.

## Stack Tecnológico

| Tecnología | Versión |
|------------|---------|
| Java | 21 |
| Spring Boot | 3.5.10 |
| MongoDB | 7.0 |
| MapStruct | 1.5.5 |
| Lombok | - |

## Arquitectura

Implementa **Arquitectura Hexagonal (Ports & Adapters)**:

```
├── domain/                 # Núcleo de negocio (sin dependencias)
│   ├── model/             # Entidades: Customer, Address
│   ├── exception/         # Excepciones de dominio
│   └── port/              # Interfaces (contratos)
│       ├── in/            # Casos de uso
│       └── out/           # Repositorios
├── application/           # Orquestación de lógica
│   └── service/           # CustomerService
└── infrastructure/        # Detalles técnicos
    ├── adapter/in/rest/   # Controllers, DTOs, Mappers
    ├── adapter/out/       # MongoDB Adapter
    ├── config/            # Beans, Swagger, CORS
    └── exception/         # GlobalExceptionHandler
```

## Configuración

| Variable | Descripción | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo (dev, docker) | - |
| `MONGODB_URI` | URI de conexión MongoDB | localhost:27017 |

## Ejecución Local

### Prerrequisitos

- Java 21+
- Maven 3.9+
- MongoDB 7.0 corriendo en `localhost:27017`

### Comandos

```bash
# Compilar
./mvnw clean package -DskipTests

# Ejecutar con perfil dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Ejecutar tests
./mvnw test
```

## Ejecución con Docker

```bash
# Desde la raíz del proyecto (C:\dev\pedidos)
docker-compose up -d customer-service
```

## URLs

| Recurso | URL |
|---------|-----|
| API Base | http://localhost:8081/customer-service |
| Swagger UI | http://localhost:8081/customer-service/swagger-ui.html |
| Health Check | http://localhost:8081/customer-service/actuator/health |
| API Docs | http://localhost:8081/customer-service/api-docs |

## Endpoints

### API Pública (`/api/v1/customers`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/` | Crear cliente |
| GET | `/` | Listar clientes (paginado) |
| GET | `/{id}` | Obtener por ID |
| PUT | `/{id}` | Actualizar cliente |
| PATCH | `/{id}` | Actualización parcial |
| DELETE | `/{id}` | Eliminar (soft delete) |
| GET | `/email/{email}` | Buscar por email |

### API Interna (`/internal/api/v1/customers`)

Endpoints para comunicación entre microservicios:

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/{id}/exists` | Validar existencia |
| GET | `/{id}/basic` | Info básica del cliente |
| POST | `/batch/exists` | Validar múltiples clientes |

## Ejemplos de Uso

### Crear Cliente

```bash
curl -X POST http://localhost:8081/customer-service/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellidoPaterno": "García",
    "apellidoMaterno": "López",
    "email": "juan.garcia@email.com",
    "direccionEnvio": {
      "calle": "Av. Insurgentes Sur",
      "numeroExterior": "1234",
      "numeroInterior": "PH1",
      "colonia": "Del Valle",
      "ciudad": "Ciudad de México",
      "estado": "CDMX",
      "codigoPostal": "03100"
    }
  }'
```

### Respuesta

```json
{
  "success": true,
  "data": {
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "nombre": "Juan",
    "apellidoPaterno": "García",
    "apellidoMaterno": "López",
    "email": "juan.garcia@email.com",
    "direccionEnvio": { ... },
    "activo": true,
    "fechaCreacion": "2025-01-31T10:30:00"
  },
  "message": "Cliente creado exitosamente",
  "timestamp": "2025-01-31T10:30:00.123Z",
  "path": "/api/v1/customers"
}
```

### Actualización Parcial (PATCH)

```bash
curl -X PATCH http://localhost:8081/customer-service/api/v1/customers/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevo.email@email.com",
    "direccionEnvio": {
      "calle": "Nueva Calle"
    }
  }'
```

## Base de Datos

**Colección:** `customers`

**Índices:**
- `customerId` (único)
- `email` (único)
- `apellidoPaterno`
- `activo`
- Compuesto: `apellidoPaterno + apellidoMaterno + nombre`
- Compuesto: `direccionEnvio.estado + direccionEnvio.ciudad`

## Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| CUSTOMER_NOT_FOUND | 404 | Cliente no encontrado |
| CUSTOMER_ALREADY_EXISTS | 409 | Email duplicado |
| VALIDATION_ERROR | 400 | Datos inválidos |
| INTERNAL_SERVER_ERROR | 500 | Error interno |

## Tests

```bash
# Unit tests
./mvnw test

# Con cobertura
./mvnw test jacoco:report
```
