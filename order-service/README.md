# Order Service

Microservicio para gestión de pedidos del sistema Liverpool.

## Stack Tecnológico

| Tecnología | Versión |
|------------|---------|
| Java | 21 |
| Spring Boot | 3.5.10 |
| Spring Cloud | 2025.0.0 |
| MongoDB | 7.0 |
| OpenFeign | - |
| Resilience4j | - |
| MapStruct | 1.5.5 |

## Arquitectura

Implementa **Arquitectura Hexagonal (Ports & Adapters)**:

```
├── domain/                 # Núcleo de negocio
│   ├── model/             # Order, OrderItem, OrderStatus
│   ├── exception/         # Excepciones de dominio
│   └── port/
│       ├── in/            # Casos de uso
│       └── out/           # OrderRepositoryPort, CustomerValidationPort
├── application/
│   └── service/           # OrderService
└── infrastructure/
    ├── adapter/in/rest/   # Controllers, DTOs
    ├── adapter/out/
    │   ├── client/        # Feign Client (Customer Service)
    │   └── persistence/   # MongoDB Adapter
    ├── config/            # Beans, Resilience4j, Swagger
    └── exception/         # GlobalExceptionHandler
```

## Comunicación entre Servicios

```
Order Service ──[Feign + Circuit Breaker]──> Customer Service
     │                                              │
     │  GET /internal/api/v1/customers/{id}/exists  │
     └──────────────────────────────────────────────┘
```

**Resilience4j Config:**
- Circuit Breaker: 50% failure rate, 10s wait
- Retry: 3 intentos, backoff exponencial

## Configuración

| Variable | Descripción | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Perfil (dev, docker) | - |
| `MONGODB_URI` | URI MongoDB | localhost:27017 |
| `CUSTOMER_SERVICE_URL` | URL Customer Service | http://localhost:8081/customer-service |

## Ejecución Local

### Prerrequisitos

- Java 21+
- Maven 3.9+
- MongoDB 7.0 en `localhost:27017`
- Customer Service corriendo en puerto 8081

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
docker-compose up -d order-service
```

## URLs

| Recurso | URL |
|---------|-----|
| API Base | http://localhost:8082/order-service |
| Swagger UI | http://localhost:8082/order-service/swagger-ui.html |
| Health Check | http://localhost:8082/order-service/actuator/health |
| Circuit Breaker Status | http://localhost:8082/order-service/actuator/circuitbreakers |

## Endpoints

### `/api/v1/orders`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/` | Crear pedido |
| GET | `/` | Listar pedidos (paginado) |
| GET | `/{orderId}` | Obtener por ID |
| PUT | `/{orderId}` | Actualizar (solo PENDING) |
| DELETE | `/{orderId}` | Eliminar pedido |
| PATCH | `/{orderId}/status` | Cambiar estado |
| PATCH | `/{orderId}/cancel` | Cancelar pedido |
| GET | `/customer/{customerId}` | Pedidos por cliente |
| GET | `/status/{status}` | Pedidos por estado |

## Estados del Pedido

```
PENDING ──────> CONFIRMED ──────> PROCESSING ──────> SHIPPED ──────> DELIVERED
    │               │
    └───────────────┴──────> CANCELLED
```

| Estado | Descripción | Puede cancelar |
|--------|-------------|----------------|
| PENDING | Pendiente de confirmación | Sí |
| CONFIRMED | Confirmado | Sí |
| PROCESSING | En proceso | No |
| SHIPPED | Enviado | No |
| DELIVERED | Entregado | No |
| CANCELLED | Cancelado | - |

## Ejemplos de Uso

### Crear Pedido

```bash
curl -X POST http://localhost:8082/order-service/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "items": [
      {
        "codigoProducto": "PROD-001",
        "nombreProducto": "Laptop HP",
        "cantidad": 1,
        "precioUnitario": 15999.99
      },
      {
        "codigoProducto": "PROD-002",
        "nombreProducto": "Mouse Logitech",
        "cantidad": 2,
        "precioUnitario": 499.00
      }
    ],
    "notas": "Entregar en horario de oficina"
  }'
```

### Respuesta

```json
{
  "success": true,
  "data": {
    "orderId": "660e8400-e29b-41d4-a716-446655440001",
    "orderNumber": "ORD-20250131-ABC123",
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "PENDING",
    "items": [...],
    "totalAmount": 16997.99,
    "fechaPedido": "2025-01-31T10:30:00"
  },
  "message": "Pedido creado exitosamente",
  "timestamp": "2025-01-31T10:30:00.123Z",
  "path": "/api/v1/orders"
}
```

### Cambiar Estado

```bash
curl -X PATCH http://localhost:8082/order-service/api/v1/orders/{orderId}/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED"
  }'
```

### Cancelar Pedido

```bash
curl -X PATCH http://localhost:8082/order-service/api/v1/orders/{orderId}/cancel
```

### Obtener Pedidos por Cliente

```bash
curl http://localhost:8082/order-service/api/v1/orders/customer/{customerId}
```

## Base de Datos

**Colección:** `orders`

**Índices:**
- `orderId` (único)
- `orderNumber` (único)
- `customerId`
- `status`
- `fechaPedido`
- Compuesto: `customerId + status`
- Compuesto: `status + fechaPedido`

## Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| ORDER_NOT_FOUND | 404 | Pedido no encontrado |
| CUSTOMER_NOT_FOUND | 404 | Cliente no existe |
| INVALID_ORDER_STATUS | 400 | Transición de estado inválida |
| ORDER_CANNOT_BE_CANCELLED | 400 | No se puede cancelar |
| CUSTOMER_SERVICE_UNAVAILABLE | 503 | Customer Service no disponible |
| VALIDATION_ERROR | 400 | Datos inválidos |

## Monitoreo

### Health Check

```bash
curl http://localhost:8082/order-service/actuator/health
```

### Estado del Circuit Breaker

```bash
curl http://localhost:8082/order-service/actuator/circuitbreakers
```

## Tests

```bash
# Unit tests
./mvnw test

# Integration tests (requiere WireMock)
./mvnw verify
```
