# Sistema de Pedidos Liverpool

Sistema de microservicios para gestión de clientes y pedidos.

## Tabla de Contenidos

- [Arquitectura](#arquitectura-del-sistema)
- [Stack Tecnológico](#stack-tecnológico)
- [Inicio Rápido](#inicio-rápido)
- [Desarrollo Local](#desarrollo-local)
- [URLs](#urls-desplegadas)
- [Troubleshooting](#troubleshooting)

## Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENTES                                 │
│                    (Web/Mobile/API)                             │
└─────────────────────────┬───────────────────────────────────────┘
                          │
            ┌─────────────┴─────────────┐
            │                           │
            ▼                           ▼
┌───────────────────────┐   ┌───────────────────────┐
│   Customer Service    │   │    Order Service      │
│      :8081            │◄──│       :8082           │
│                       │   │                       │
│  • Gestión clientes   │   │  • Gestión pedidos    │
│  • CRUD completo      │   │  • Estados            │
│  • Validación interna │   │  • Circuit Breaker    │
└───────────┬───────────┘   └───────────┬───────────┘
            │                           │
            └─────────────┬─────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │       MongoDB         │
              │        :27017         │
              │                       │
              │  • customer_db        │
              │  • order_db           │
              └───────────────────────┘
```

## Stack Tecnológico

| Componente | Tecnología | Versión |
|------------|------------|---------|
| Runtime | Java | 21 |
| Framework | Spring Boot | 3.5.10 |
| Cloud | Spring Cloud | 2025.0.0 |
| Base de Datos | MongoDB | 7.0 |
| Contenedores | Docker | 24+ |
| HTTP Client | OpenFeign | - |
| Resiliencia | Resilience4j | - |
| Mapeo | MapStruct | 1.5.5 |
| Documentación | OpenAPI/Swagger | 3.0 |

## Microservicios

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| customer-service | 8081 | Gestión de clientes |
| order-service | 8082 | Gestión de pedidos |
| mongodb | 27017 | Base de datos |

## Inicio Rápido

### Prerrequisitos

- Docker Desktop 24+
- Docker Compose v2

### Opción 1: Todo Local (con MongoDB en Docker)

```bash
cd C:\dev\pedidos

# Levantar MongoDB + servicios
docker-compose --profile local up -d

# Ver logs
docker-compose logs -f

# Detener todo
docker-compose --profile local down
```

### Opción 2: Servicios en Docker + MongoDB Atlas

```bash
# Crear archivo .env
cp .env.example .env

# Editar .env con tu MONGODB_URI de Atlas
# MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/...

# Levantar solo los microservicios
docker-compose up -d

# Ver logs
docker-compose logs -f
```

### Verificar que Todo Funciona

```bash
# Health checks
curl http://localhost:8081/customer-service/actuator/health
curl http://localhost:8082/order-service/actuator/health
```

## URLs Desplegadas

### Customer Service

| Recurso | URL |
|---------|-----|
| API Base | http://localhost:8081/customer-service/api/v1/customers |
| Swagger UI | http://localhost:8081/customer-service/swagger-ui.html |
| Health | http://localhost:8081/customer-service/actuator/health |

### Order Service

| Recurso | URL |
|---------|-----|
| API Base | http://localhost:8082/order-service/api/v1/orders |
| Swagger UI | http://localhost:8082/order-service/swagger-ui.html |
| Health | http://localhost:8082/order-service/actuator/health |
| Circuit Breakers | http://localhost:8082/order-service/actuator/circuitbreakers |

## Desarrollo Local

### Opción 1: Solo MongoDB en Docker

```bash
# Levantar MongoDB
docker-compose up -d mongodb

# Customer Service (terminal 1)
cd customer-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Order Service (terminal 2)
cd order-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Opción 2: Todo en Docker

```bash
docker-compose up -d --build
```

## Flujo de Prueba Completo

### 1. Crear un Cliente

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
      "colonia": "Del Valle",
      "ciudad": "Ciudad de México",
      "estado": "CDMX",
      "codigoPostal": "03100"
    }
  }'
```

Guarda el `customerId` de la respuesta.

### 2. Crear un Pedido

```bash
curl -X POST http://localhost:8082/order-service/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "<CUSTOMER_ID_DEL_PASO_1>",
    "items": [
      {
        "codigoProducto": "LAPTOP-001",
        "nombreProducto": "MacBook Pro 14",
        "cantidad": 1,
        "precioUnitario": 45999.00
      }
    ],
    "notas": "Entregar en horario de oficina"
  }'
```

### 3. Confirmar el Pedido

```bash
curl -X PATCH http://localhost:8082/order-service/api/v1/orders/<ORDER_ID>/status \
  -H "Content-Type: application/json" \
  -d '{"status": "CONFIRMED"}'
```

### 4. Ver Pedidos del Cliente

```bash
curl http://localhost:8082/order-service/api/v1/orders/customer/<CUSTOMER_ID>
```

## Estructura del Proyecto

```
pedidos/
├── customer-service/          # Microservicio de clientes
│   ├── src/
│   ├── Dockerfile
│   ├── pom.xml
│   └── README.md
├── order-service/             # Microservicio de pedidos
│   ├── src/
│   ├── Dockerfile
│   ├── pom.xml
│   └── README.md
├── docker-compose.yml         # Orquestación de contenedores
└── README.md                  # Este archivo
```

## Comandos Útiles

```bash
# === DESARROLLO LOCAL (con MongoDB en Docker) ===
docker-compose --profile local up -d
docker-compose --profile local down
docker-compose --profile local down -v  # Elimina datos de MongoDB

# === CON MONGODB ATLAS ===
docker-compose up -d
docker-compose down

# === GENERAL ===
# Reconstruir imágenes
docker-compose build --no-cache

# Ver estado de contenedores
docker-compose ps

# Logs de un servicio específico
docker-compose logs -f customer-service
docker-compose logs -f order-service

# Reiniciar un servicio
docker-compose restart order-service

# Acceder a MongoDB local
docker exec -it mongodb mongosh -u root -p root
```

## Troubleshooting

### Customer Service no responde

```bash
# Verificar logs
docker-compose logs customer-service

# Verificar health
curl http://localhost:8081/customer-service/actuator/health
```

### Order Service marca Customer Service como no disponible

```bash
# Verificar estado del circuit breaker
curl http://localhost:8082/order-service/actuator/circuitbreakers

# Verificar conectividad entre servicios
docker exec order-service wget -qO- http://customer-service:8081/customer-service/actuator/health
```

### MongoDB no inicia

```bash
# Verificar logs
docker-compose logs mongodb

# Limpiar volumen y reiniciar
docker-compose down -v
docker-compose up -d mongodb
```
