package check4tech.firstdueresponseservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;

/**
 * Endpoints name a location in the URL ({locationId}, {organizationId}, {orgId}) or in a
 * {@code locationId} query parameter. A signed-in caller may only use their own location there.
 */
@Configuration
public class LocationScopeInterceptor implements WebMvcConfigurer, HandlerInterceptor {

    private static final String[] PATH_VARIABLES = {"locationId", "organizationId", "orgId"};

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("/**");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long own = LocationAccess.currentLocationId();
        if (own == null) {
            return true; // not signed in: the security chain has already decided (login is the only open route)
        }
        Object vars = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (vars instanceof Map<?, ?> map) {
            for (String name : PATH_VARIABLES) {
                String value = ((Map<String, String>) map).get(name);
                if (value != null && !String.valueOf(own).equals(value)) {
                    return deny(response);
                }
            }
        }
        String[] params = request.getParameterValues("locationId");
        if (params != null) {
            for (String value : params) {
                if (!String.valueOf(own).equals(value)) {
                    return deny(response);
                }
            }
        }
        return true;
    }

    private static boolean deny(HttpServletResponse response) throws java.io.IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not allowed.");
        return false;
    }
}
