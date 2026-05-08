package org.citycare.notificationservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "AUTHSERVICE", fallback = AuthClientFallback.class)
public interface AuthClient {

    @GetMapping("/admin/users/by-role")
    List<UserResponse> getUsersByRole(@RequestParam("role") String role);

    record UserResponse(Long id, String name, String email, String role) {}
}
