package check4tech.firstdueresponseservice.security;

import check4tech.firstdueresponseservice.Entity.Employee;
import check4tech.firstdueresponseservice.Entity.Location;
import check4tech.firstdueresponseservice.Entity.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.repository.CrudRepository;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocationScopeTest {

    interface EmployeeRepo extends CrudRepository<Employee, Long> {
    }

    private final AtomicBoolean deleted = new AtomicBoolean();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private static void signIn(long locationId, Role role) {
        AuthenticatedEmployee principal = new AuthenticatedEmployee(1L, "U", locationId, role);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
    }

    private EmployeeRepo guardedRepo(AnnotationConfigApplicationContext ctx) {
        Location location = new Location();
        location.setId(1L);
        Employee employee = new Employee();
        employee.setId(5L);
        employee.setLocation(location);
        EmployeeRepo raw = (EmployeeRepo) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{EmployeeRepo.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.of(employee);
                    case "deleteById" -> {
                        deleted.set(true);
                        yield null;
                    }
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "employeeRepo";
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        AopConfigUtils.registerAutoProxyCreatorIfNecessary(ctx);
        ctx.register(LocationScopeAdvice.class);
        ctx.registerBean("employeeRepo", EmployeeRepo.class, () -> raw);
        ctx.refresh();
        return ctx.getBean(EmployeeRepo.class);
    }

    @Test
    void ownLocationRowsAreVisibleAndDeletable() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            EmployeeRepo repo = guardedRepo(ctx);
            signIn(1, Role.ADMIN);
            assertTrue(repo.findById(5L).isPresent());
            repo.deleteById(5L);
            assertTrue(deleted.get());
        }
    }

    @Test
    void otherLocationRowsLookMissingAndCannotBeDeleted() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            EmployeeRepo repo = guardedRepo(ctx);
            signIn(2, Role.ADMIN);
            assertFalse(repo.findById(5L).isPresent());
            assertThrows(AccessDeniedException.class, () -> repo.deleteById(5L));
            assertFalse(deleted.get());
        }
    }

    @Test
    void scheduledJobsWithNoSignedInUserAreNotRestricted() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            assertTrue(guardedRepo(ctx).findById(5L).isPresent());
        }
    }

    private boolean call(Map<String, String> vars, String paramLocation, MockHttpServletResponse response) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, vars);
        if (paramLocation != null) {
            request.setParameter("locationId", paramLocation);
        }
        return new LocationScopeInterceptor().preHandle(request, response, new Object());
    }

    @Test
    void urlAndQueryLocationMustBeTheCallersOwn() throws Exception {
        signIn(1, Role.USER);
        assertTrue(call(Map.of("locationId", "1"), null, new MockHttpServletResponse()));
        assertTrue(call(Map.of(), "1", new MockHttpServletResponse()));
        assertTrue(call(Map.of("id", "99"), null, new MockHttpServletResponse()));
        MockHttpServletResponse denied = new MockHttpServletResponse();
        assertFalse(call(Map.of("locationId", "2"), null, denied));
        assertEquals(403, denied.getStatus());
        assertFalse(call(Map.of("orgId", "2"), null, new MockHttpServletResponse()));
        assertFalse(call(Map.of(), "2", new MockHttpServletResponse()));
    }
}
