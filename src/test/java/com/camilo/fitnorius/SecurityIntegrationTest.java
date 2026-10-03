package com.camilo.fitnorius;

import com.camilo.fitnorius.model.Category;
import com.camilo.fitnorius.model.Product;
import com.camilo.fitnorius.repository.CategoryRepository;
import com.camilo.fitnorius.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void publicReadsWorkButMutationsRequireBearerAuthentication() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/scratch/results"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicProductReadsInitializeLazyRelations() throws Exception {
        String categoryName = "Categoria-test-" + UUID.randomUUID();
        Category category = categoryRepository.saveAndFlush(
                Category.builder().name(categoryName).build()
        );
        productRepository.saveAndFlush(
                Product.builder()
                        .name("Producto-test")
                        .price(new BigDecimal("1000"))
                        .category(category)
                        .displayOrder(0)
                        .build()
        );

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(categoryName)));
    }

    @Test
    void loginSetsHttpOnlyCookieAndBearerCanAccessAdminEndpoint() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test-admin\",\"password\":\"test-password-123\"}"))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("fitnorius_refresh", true))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        String setCookie = login.getResponse().getHeader("Set-Cookie");
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("Path=/api/auth"));
        assertTrue(setCookie.contains("SameSite=Lax"));

        JsonNode body = objectMapper.readTree(login.getResponse().getContentAsString());
        String accessToken = body.get("accessToken").asText();
        assertTrue(accessToken.split("\\.").length == 3);

        mockMvc.perform(get("/api/admin/scratch/visible")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/scratch/visible")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshRotatesTokenAndRejectsReuseOfOldToken() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test-admin\",\"password\":\"test-password-123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Cookie originalCookie = login.getResponse().getCookie("fitnorius_refresh");
        assertNotNull(originalCookie);

        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh")
                        .header("X-Refresh-Request", "1")
                        .cookie(originalCookie))
                .andExpect(status().isOk())
                .andReturn();
        Cookie rotatedCookie = refreshed.getResponse().getCookie("fitnorius_refresh");
        assertNotNull(rotatedCookie);
        assertTrue(!rotatedCookie.getValue().equals(originalCookie.getValue()));

        mockMvc.perform(post("/api/auth/refresh")
                        .header("X-Refresh-Request", "1")
                        .cookie(originalCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsDoesNotAllowArbitraryOriginsWithCredentials() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://attacker.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());

        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk());
    }

    @Test
    void actuatorHealthWorksAndTheRestOfActuatorStaysClosed() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        // actuator/env y actuator/configprops volcarían secretos: nunca abiertos.
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anUnknownApiRouteIsDeniedBeforeReachingAnyController() throws Exception {
        // denyAll() responde 401/403 aquí; nunca debe ejecutarse código ni
        // devolver un 500 por una ruta inexistente.
        mockMvc.perform(get("/api/ruta-que-no-existe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void promoBannerIsPublicToReadButOnlyAdminCanWrite() throws Exception {
        // Sin banner configurado, la tienda recibe null y no muestra nada.
        mockMvc.perform(get("/api/promo-banner"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/promo-banner")
                        .param("title", "Intento no autorizado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanConfigureThePromoBanner() throws Exception {
        String adminToken = login("test-admin", "test-password-123");

        mockMvc.perform(multipart("/api/promo-banner")
                        .file(new MockMultipartFile("file", new byte[0]))
                        .header("Authorization", "Bearer " + adminToken)
                        .param("title", "¡Ofertas especiales!")
                        .param("subtitle", "Solo por tiempo limitado")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ofertas especiales")))
                .andExpect(content().string(containsString("Solo por tiempo limitado")));

        // Y desde ese momento es visible públicamente.
        mockMvc.perform(get("/api/promo-banner"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ofertas especiales")));

        mockMvc.perform(patch("/api/promo-banner/visibility")
                        .param("active", "false"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/promo-banner/visibility")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"active\":false")));

        mockMvc.perform(delete("/api/promo-banner")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void creatingAUserRequiresAnAdminToken() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nuevo@fitnorius.co\",\"password\":\"Passw0rd!Segura\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anAdminCreatesAUserThatCanLogInAndUseProtectedEndpoints() throws Exception {
        String adminToken = login("test-admin", "test-password-123");
        String email = "nuevo-" + UUID.randomUUID() + "@fitnorius.co";
        String password = "Passw0rd!Segura";

        MvcResult created = mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        String response = created.getResponse().getContentAsString();
        // El hash de la contraseña nunca sale por la API.
        assertFalse(response.contains(password));
        assertFalse(response.contains("passwordHash"));
        assertEquals(email, objectMapper.readTree(response).get("email").asText());
        assertEquals("ADMIN", objectMapper.readTree(response).get("role").asText());

        // La cuenta creada inicia sesión y su token sirve para toda petición protegida.
        String newUserToken = login(email, password);
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + newUserToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(email)));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + newUserToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(email)));
    }

    @Test
    void rejectsInvalidEmailWeakPasswordAndDuplicates() throws Exception {
        String adminToken = login("test-admin", "test-password-123");
        String email = "duplicado-" + UUID.randomUUID() + "@fitnorius.co";

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"no-es-correo\",\"password\":\"Passw0rd!Segura\"}"))
                .andExpect(status().isBadRequest());

        // Sin mayúscula, sin número y con menos de 12 caracteres.
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"debil\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Passw0rd!Segura\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email.toUpperCase() + "\",\"password\":\"Passw0rd!Segura\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void aUserWithAWrongPasswordCannotLogIn() throws Exception {
        String adminToken = login("test-admin", "test-password-123");
        String email = "clave-" + UUID.randomUUID() + "@fitnorius.co";

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Passw0rd!Segura\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + email + "\",\"password\":\"Passw0rd!Incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publishesThePasswordPolicySoTheFormDoesNotDuplicateIt() throws Exception {
        MvcResult policy = mockMvc.perform(get("/api/auth/password-policy"))
                .andExpect(status().isOk())
                .andReturn();

        String body = policy.getResponse().getContentAsString();
        assertTrue(body.contains("uppercase"));
        assertTrue(body.contains("digit"));
        assertTrue(body.contains("special"));
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken")
                .asText();
    }
}
