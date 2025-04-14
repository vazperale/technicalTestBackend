package com.technical_test_backend.technical_test_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import com.technical_test_backend.technical_test_backend.model.ProductDetail;
import java.util.concurrent.TimeUnit;

@Service
public class ProductService {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://localhost:3001";
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final long TIMEOUT = 8; // pongo 8 porque salvo con el producto 10000,el resto las procesa en máximo 5s,y asi se ve en funcionamiento el timeout

    public ProductService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Función que genera la respuesta final que devuelva la llamada a la api
    public List<Map<String, Object>> getSimilarProducts(Number productId) {
        // Obtener los IDs de productos similares
        Number[] similarIds = getSimilarProductIds(productId);

        // Crear la lista de tareas asincrónicas para obtener los detalles de los productos similares
        List<CompletableFuture<ProductDetail>> futures = getProductDetailsFutures(similarIds);

        // Recoger los resultados de las tareas asincrónicas
        return collectResults(futures);
    }

    // Función que obtiene los IDs de productos similares
    private Number[] getSimilarProductIds(Number productId) {
        String urlSimilarIds = UriComponentsBuilder.fromUriString(BASE_URL)
                .path("/product/{productId}/similarids")
                .buildAndExpand(productId)
                .toUriString();

        try {
            return restTemplate.getForObject(urlSimilarIds, Number[].class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Sin productos similares asignados o producto con id " + productId + " no existente");
        }
    }

    // Función que crea las tareas asincrónicas para obtener los detalles de cada producto
    private List<CompletableFuture<ProductDetail>> getProductDetailsFutures(Number[] similarIds) {
        List<CompletableFuture<ProductDetail>> futures = new ArrayList<>();

        // Iniciar las llamadas asincrónicas para obtener los detalles de los productos similares
        for (Number id : similarIds) {
            CompletableFuture<ProductDetail> future = fetchProductDetails(id);
            futures.add(future);
        }

        return futures;
    }

    // Función que realiza la llamada asincrónica a la API para obtener los detalles del producto
    private CompletableFuture<ProductDetail> fetchProductDetails(Number id) {
        String urlProduct = UriComponentsBuilder.fromUriString(BASE_URL)
                .path("/product/{id}")
                .buildAndExpand(id)
                .toUriString();

        return CompletableFuture.supplyAsync(() -> {
            try {
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        urlProduct,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Map<String, Object>>() {});
                return new ProductDetail(id, response.getBody(), false);
            } catch (Exception e) {
                logger.error("Error al obtener los detalles del producto con ID " + id);
                return new ProductDetail(id, null, false); // Error no relacionado con timeout
            }
        })
        .completeOnTimeout(new ProductDetail(id, null, true), TIMEOUT, TimeUnit.SECONDS) // Indica timeout explícitamente
        .exceptionally(ex -> {
            logger.error("Ocurrió un error inesperado durante la ejecución de la tarea asíncrona");
            return new ProductDetail(id, null, false);
        });
    }

    // Función que recoge los resultados de las tareas asincrónicas
    private List<Map<String, Object>> collectResults(List<CompletableFuture<ProductDetail>> futures) {
        List<Map<String, Object>> similarProducts = new ArrayList<>();

        for (CompletableFuture<ProductDetail> future : futures) {
            try {
                ProductDetail productDetail = future.get();
                if (productDetail != null && productDetail.getProductData() != null) {
                    similarProducts.add(productDetail.getProductData());
                } else if (productDetail != null && productDetail.isTimeout()) {
                    logger.warn("Producto con ID {} omitido debido a timeout.", productDetail.getProductId());
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error al esperar los resultados de la tarea asincrónica", e);
            }
        }

        return similarProducts;
    }
}

