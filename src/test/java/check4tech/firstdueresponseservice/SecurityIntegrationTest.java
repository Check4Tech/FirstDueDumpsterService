package check4tech.firstdueresponseservice;

import check4tech.firstdueresponseservice.Entity.Employee;
import check4tech.firstdueresponseservice.Entity.Location;
import check4tech.firstdueresponseservice.Entity.Role;
import check4tech.firstdueresponseservice.Respository.EmployeeRepository;
import check4tech.firstdueresponseservice.Respository.LocationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Boots the whole application on an in-memory database and exercises login, roles and location scoping. */
@SpringBootTest(properties = {
        "spring.config.import=",
        "spring.application.name=dumpster-it",
        "spring.datasource.url=jdbc:h2:mem:dumpit;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never",
        "spring.mail.host=localhost",
        "spring.mail.port=2525",
        "jasypt.encryptor.password=test-only-not-a-real-secret",
        "jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MA=="
})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecurityIntegrationTest {

    private static final String PASSWORD = "Correct-Horse-9";
    private static final ObjectMapper JSON = new ObjectMapper();
    private final AtomicInteger ipCounter = new AtomicInteger(1);

    @Autowired MockMvc mvc;
    @Autowired LocationRepository locations;
    @Autowired EmployeeRepository employees;
    @MockBean JavaMailSender mailSender;

    Location locA;
    Location locB;
    Employee adminA;
    Employee userA;
    Employee adminB;

    private Location location(String name) {
        Location l = new Location();
        l.setName(name);
        l.setAddress("1 Main St");
        l.setState("NY");
        l.setZipCode("12207");
        return locations.save(l);
    }

    private Employee employee(Location loc, String username, Role role) {
        Employee e = new Employee();
        e.setLocation(loc);
        e.setFirstName("First" + username);
        e.setLastName("Last" + username);
        e.setEmail(username.toLowerCase() + "@example.com");
        e.setUsername(username);
        e.setPassword(new BCryptPasswordEncoder().encode(PASSWORD));
        e.setRole(role);
        e.setIsTerminated(false);
        return employees.save(e);
    }

    @BeforeAll
    void seed() {
        locA = location("Alpha");
        locB = location("Bravo");
        adminA = employee(locA, "ADMINA", Role.ADMIN);
        userA = employee(locA, "USERA", Role.USER);
        adminB = employee(locB, "ADMINB", Role.ADMIN);
    }

    private MockHttpServletRequestBuilder fromNewClient(MockHttpServletRequestBuilder b) {
        return b.header("X-Forwarded-For", "10.9.8." + ipCounter.getAndIncrement());
    }

    private MvcResult loginAttempt(Long locationId, String username, String password) throws Exception {
        return mvc.perform(fromNewClient(post("/auth/location/" + locationId + "/login")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")).andReturn();
    }

    private String token(Long locationId, String username) throws Exception {
        MvcResult res = loginAttempt(locationId, username, PASSWORD);
        assertTrue(res.getResponse().getStatus() == 200, "login failed: " + res.getResponse().getStatus());
        return "Bearer " + JSON.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void everythingExceptLoginNeedsAToken() throws Exception {
        for (String path : new String[]{"/api/employees/location/" + locA.getId() + "/all", "/api/location",
                "/api/location/" + locA.getId(), "/api/vehicles", "/api/equipment?locationId=" + locA.getId(),
                "/admin/dashboard"}) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
        mvc.perform(patch("/api/employees/" + locA.getId() + "/password").param("usernameOrEmail", "USERA").param("newPassword", "hacked-pass-123"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/employees/send-test-email").param("to", "victim@example.com")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/employees/" + userA.getId())).andExpect(status().isUnauthorized());
    }

    @Test
    void loginIssuesATokenAndFailuresAllLookTheSame() throws Exception {
        MvcResult ok = loginAttempt(locA.getId(), "USERA", PASSWORD);
        JsonNode body = JSON.readTree(ok.getResponse().getContentAsString());
        assertTrue(body.hasNonNull("token"));
        assertFalse(body.toString().toLowerCase().contains("password"));
        for (Object[] bad : new Object[][]{{locA.getId(), "USERA", "wrong"}, {locA.getId(), "NOBODY", PASSWORD},
                {locB.getId(), "USERA", PASSWORD}, {9999L, "USERA", PASSWORD}}) {
            org.junit.jupiter.api.Assertions.assertEquals(401, loginAttempt((Long) bad[0], (String) bad[1], (String) bad[2]).getResponse().getStatus());
        }
    }

    @Test
    void aTerminatedEmployeeCannotUseAnOldToken() throws Exception {
        Employee temp = employee(locA, "TEMPY", Role.USER);
        String t = token(locA.getId(), "TEMPY");
        mvc.perform(get("/api/employees/location/" + locA.getId() + "/all").header("Authorization", t)).andExpect(status().isOk());
        temp.setIsTerminated(true);
        employees.save(temp);
        mvc.perform(get("/api/employees/location/" + locA.getId() + "/all").header("Authorization", t)).andExpect(status().isUnauthorized());
    }

    @Test
    void employeesOnlySeeTheirOwnLocationAndNoSecrets() throws Exception {
        String t = token(locA.getId(), "USERA");
        MvcResult own = mvc.perform(get("/api/employees/location/" + locA.getId() + "/all").header("Authorization", t))
                .andExpect(status().isOk()).andReturn();
        String body = own.getResponse().getContentAsString();
        assertTrue(body.contains("firstUSERA") || body.contains("FirstUSERA"));
        assertFalse(body.contains("ADMINB"));
        assertFalse(body.contains("$2a$"));
        assertFalse(body.toLowerCase().contains("\"password\""));

        mvc.perform(get("/api/employees/location/" + locB.getId() + "/all").header("Authorization", t)).andExpect(status().isForbidden());
        mvc.perform(get("/api/location/" + locB.getId()).header("Authorization", t)).andExpect(status().isForbidden());
        mvc.perform(get("/api/employees/search").param("locationId", String.valueOf(locB.getId())).header("Authorization", t))
                .andExpect(status().isForbidden());
    }

    @Test
    void ordinaryEmployeesCannotManageAnything() throws Exception {
        String t = token(locA.getId(), "USERA");
        mvc.perform(delete("/api/employees/" + adminA.getId()).header("Authorization", t)).andExpect(status().isForbidden());
        mvc.perform(post("/api/employees").param("locationId", String.valueOf(locA.getId())).header("Authorization", t)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"locationId\":" + locA.getId() + ",\"firstName\":\"A\",\"lastName\":\"B\",\"email\":\"a@example.com\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/admin/dashboard").header("Authorization", t)).andExpect(status().isForbidden());
        mvc.perform(post("/api/employees/send-test-email").param("to", "x@example.com").header("Authorization", t)).andExpect(status().isForbidden());
        // changing someone else's password
        mvc.perform(patch("/api/employees/" + locA.getId() + "/password").param("usernameOrEmail", "ADMINA")
                .param("newPassword", "hacked-pass-123").header("Authorization", t)).andExpect(status().isForbidden());
        // changing your own is fine, a weak one is not
        mvc.perform(patch("/api/employees/" + locA.getId() + "/password").param("usernameOrEmail", "USERA")
                .param("newPassword", "short").header("Authorization", t)).andExpect(status().isBadRequest());
    }

    @Test
    void adminsManageOnlyTheirOwnLocation() throws Exception {
        String t = token(locA.getId(), "ADMINA");
        // another location's employee looks like it does not exist; cannot be deleted
        try {
            int code = mvc.perform(delete("/api/employees/" + adminB.getId()).header("Authorization", t)).andReturn().getResponse().getStatus();
            assertTrue(code >= 400, "must not succeed");
        } catch (Exception expectedNotFound) {
            // this service has no global handler: "Employee not found" surfaces as an exception; what matters is nothing changed
        }
        org.junit.jupiter.api.Assertions.assertFalse(Boolean.TRUE.equals(employees.findById(adminB.getId()).orElseThrow().getIsTerminated()));
        // cannot create into another location
        mvc.perform(post("/api/employees").param("locationId", String.valueOf(locA.getId())).header("Authorization", t)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"locationId\":" + locB.getId() + ",\"firstName\":\"Evil\",\"lastName\":\"Hire\",\"email\":\"evil@example.com\",\"role\":\"ADMIN\"}"))
                .andReturn();
        // the body's locationId is ignored on create; whatever was created lives in the caller's own location
        employees.findAll().stream().filter(e -> "evil@example.com".equals(e.getEmail()))
                .forEach(e -> org.junit.jupiter.api.Assertions.assertEquals(locA.getId(), e.getLocation().getId()));
        mvc.perform(post("/api/employees").param("locationId", String.valueOf(locB.getId())).header("Authorization", t)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"locationId\":" + locB.getId() + ",\"firstName\":\"Evil\",\"lastName\":\"Hire\",\"email\":\"evil@example.com\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminsCanCreateAnEmployeeWhoGetsARandomTemporaryPasswordByEmail() throws Exception {
        String t = token(locA.getId(), "ADMINA");
        MvcResult res = mvc.perform(post("/api/employees").param("locationId", String.valueOf(locA.getId())).header("Authorization", t)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"locationId\":" + locA.getId() + ",\"firstName\":\"Nora\",\"lastName\":\"Newhire\",\"email\":\"nora@example.com\",\"role\":\"USER\"}"))
                .andExpect(status().isOk()).andReturn();
        String body = res.getResponse().getContentAsString();
        assertFalse(body.contains("$2a$"), "the hash must not be returned");
        assertFalse(body.toLowerCase().contains("\"password\""));

        ArgumentCaptor<SimpleMailMessage> sent = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, atLeastOnce()).send(sent.capture());
        String text = sent.getValue().getText();
        String username = JSON.readTree(body).get("username").asText();
        assertTrue(text.contains(username));
        String temp = text.substring(text.indexOf("temporary password is ") + 22).split("\\s")[0];
        assertTrue(temp.length() >= 14);
        assertFalse(temp.equalsIgnoreCase("Welcome!"));
        // the temporary password works at the right location, and only there
        org.junit.jupiter.api.Assertions.assertEquals(200, loginAttempt(locA.getId(), username, temp).getResponse().getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(401, loginAttempt(locB.getId(), username, temp).getResponse().getStatus());
    }
}
