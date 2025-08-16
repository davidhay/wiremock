package com.example.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.BindMode;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
class WiremockTestContainerTests {

    @Container
    WireMockContainer wiremockServer = new WireMockContainer("wiremock/wiremock:3.13.1-1")
            .withClasspathResourceMapping(
        "/mappings",
                "/home/wiremock/mappings",
        BindMode.READ_ONLY
        );

    private RestClient restClient;

    @BeforeEach
    void setup() {
        String baseUrl = wiremockServer.getBaseUrl();
        assertNotNull(baseUrl, "Base URL should not be null");

        System.out.println("BASE URL " + wiremockServer.getBaseUrl());
        restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        WireMock.configureFor(wiremockServer.getHost(), wiremockServer.getPort());
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
        WireMock wm = new WireMock(wiremockServer.getHost(), wiremockServer.getPort());
        assertEquals(newId, wm.getStubMapping(newId).getItem().getId());
    }
    
}