package com.technical_test_backend.technical_test_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ProductService {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://localhost:3001";

    public ProductService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Map<String, Object>> getSimilarProducts(Number productId) {
        String urlSimilarIds = UriComponentsBuilder.fromUriString(BASE_URL)
                .path("/product/{productId}/similarids")
                .buildAndExpand(productId)
                .toUriString();

        Number[] similarIds = null;
        try {
            similarIds = restTemplate.getForObject(urlSimilarIds, Number[].class);
        } catch (HttpClientErrorException.NotFound e) {
           
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto con id " + productId + " no encontrado");

        }

        List<Map<String, Object>> similarProducts = new ArrayList<>();
        for (Number id : similarIds) {
            try {
                String urlProduct = UriComponentsBuilder.fromUriString(BASE_URL)
                        .path("/product/{id}")
                        .buildAndExpand(id)
                        .toUriString();

                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        urlProduct,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Map<String, Object>>() {
                        });

                Map<String, Object> productDetail = response.getBody();
                if (productDetail != null) {
                    similarProducts.add(productDetail);
                }

            } catch (Exception e) {
                // Ignorar productos no encontrados
            }
        }
        return similarProducts;
    }
}
