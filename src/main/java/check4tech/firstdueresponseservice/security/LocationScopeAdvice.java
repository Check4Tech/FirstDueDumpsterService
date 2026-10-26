package check4tech.firstdueresponseservice.security;

import check4tech.firstdueresponseservice.Entity.Location;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.Hibernate;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lookups by bare id (findById / deleteById) only see rows of the caller's own location: another
 * location's row looks like "not found", and deleting it is refused. Rows without a location and
 * calls with no signed-in employee (scheduled jobs) pass through.
 */
@Configuration
public class LocationScopeAdvice {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static Advisor locationScopeAdvisor() {
        StaticMethodMatcherPointcut pointcut = new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                return method.getParameterCount() == 1
                        && ("findById".equals(method.getName()) || "deleteById".equals(method.getName()))
                        && CrudRepository.class.isAssignableFrom(targetClass);
            }
        };
        return new DefaultPointcutAdvisor(pointcut, new Interceptor());
    }

    static class Interceptor implements MethodInterceptor {

        private static final Field NONE;

        static {
            try {
                NONE = Interceptor.class.getDeclaredField("NONE");
            } catch (NoSuchFieldException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        private final Map<Class<?>, Field> locationFields = new ConcurrentHashMap<>();

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Object invoke(MethodInvocation invocation) throws Throwable {
            if ("deleteById".equals(invocation.getMethod().getName()) && invocation.getThis() instanceof CrudRepository repo
                    && invocation.getArguments().length == 1 && invocation.getArguments()[0] != null) {
                Optional<?> existing = repo.findById(invocation.getArguments()[0]);
                if (existing.isPresent() && !isAllowed(existing.get())) {
                    throw new AccessDeniedException("Not allowed.");
                }
                return invocation.proceed();
            }
            Object result = invocation.proceed();
            if (result instanceof Optional<?> opt && opt.isPresent() && !isAllowed(opt.get())) {
                return Optional.empty();
            }
            return result;
        }

        boolean isAllowed(Object entity) {
            Long own = LocationAccess.currentLocationId();
            if (own == null) {
                return true;
            }
            Object real = Hibernate.unproxy(entity);
            Field field = locationFields.computeIfAbsent(real.getClass(), Interceptor::findLocationField);
            if (field == NONE) {
                return true;
            }
            try {
                Object owner = field.get(real);
                if (owner == null) {
                    return true;
                }
                Long ownerId = ((Location) Hibernate.unproxy(owner)).getId();
                return ownerId == null || ownerId.equals(own);
            } catch (IllegalAccessException e) {
                return true;
            }
        }

        private static Field findLocationField(Class<?> type) {
            for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
                try {
                    Field f = c.getDeclaredField("location");
                    if (Location.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        return f;
                    }
                } catch (NoSuchFieldException ignored) {
                    // keep walking up
                }
            }
            return NONE;
        }
    }
}
