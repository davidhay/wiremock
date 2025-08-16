package com.example.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@EnableWireMock
@TestPropertySource(properties = "logging.level.com.github.tomakehurst.wiremock=DEBUG")
class ExampleWireMockTest {

    @InjectWireMock
    WireMockServer wireMock;

    private RestClient restClient;

    @BeforeEach
    void setup() {

        String baseUrl = wireMock.baseUrl();
        assertNotNull(baseUrl, "Base URL should not be null");

        System.out.println("BASE URL " + wireMock.baseUrl());
        restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        assertEquals(wireMock.isRunning(), true, "WireMock server should be running");

    }

    @Test
    void testFixedMappings() {
        wireMock.getStubMappings().forEach(stubMapping -> {
            System.out.println("Stub Mapping: " + stubMapping.getName());
        });

        ResponseEntity<String> responseEntity = restClient.get().uri("__admin/mappings").retrieve().toEntity(String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        System.out.println("Mappings: " + responseEntity.getBody());

        assertThat(responseEntity.getBody()).contains("cdb9953c-d1da-4f0b-bfc8-626ca955c38d");
    }

    @Test
    void testDynamicMapping() throws Exception {
        StubMapping sm2 = stubFor(get(urlEqualTo("/users/2"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "name" : "David",
                                    "id"   : 2
                                }
                                """)));

        UUID newId = sm2.getId();
        
        User user = restClient.get()
                .uri("/users/2")
                .retrieve()
                .toEntity(User.class)
                .getBody();

        assertThat(user.getName()).isEqualTo("David");
        assertThat(user.getId()).isEqualTo(2);
        
        assertEquals(newId, wireMock.getStubMapping(newId).getItem().getId());
    }

    @Test
    void testStaticMapping() {

        User user = restClient.get()
                .uri("/users/1")
                .retrieve()
                .toEntity(User.class)
                .getBody();

        assertThat(user.getName()).isEqualTo("Jenna");
        assertThat(user.getId()).isEqualTo(1);
    }
}
