package check4tech.firstdueresponseservice.security;

import check4tech.firstdueresponseservice.Entity.Role;

/** The signed-in employee, as resolved from the token and the database on each request. */
public record AuthenticatedEmployee(Long id, String username, Long locationId, Role role) {
}
