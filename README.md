# Spring Boot Similar Products API

Este proyecto es una API de Spring Boot que se conecta a una API externa para obtener productos similares a un producto dado.

## Endpoints

### GET `/product/{productId}/similar`

Obtiene la información de los productos similares a un producto dado.

**Parámetros de ruta:**
- `productId`: ID del producto para el cual se desean los productos similares.

**Ejemplo de respuesta:**
```json
[
  {
    "id": "2",
    "name": "Dress",
    "price": 19.99,
    "availability": true
  },
  {
    "id": "3",
    "name": "Blazer",
    "price": 29.99,
    "availability": false
  }
]
```

How to run the project:

1. Clone the repository in a folder:

    ```bash
    git clone https://github.com/vazperale/technicalTestBackend.git
    ```

2. Navigate to the project:

    ```bash
    cd technical-test-backend
    ```

3.instalar dependencias:

    ```bash
    mvn clean install
    ```

  4.Arranca la api:

    ```bash
    mvn spring-boot:run
    ```
