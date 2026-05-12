package org.citycare.notificationservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.citycare.notificationservice.security.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessageDeliveryException("Missing Authorization header in WebSocket CONNECT");
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                throw new MessageDeliveryException("Invalid JWT for WebSocket CONNECT");
            }

            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (userId == null || role == null || role.isBlank()) {
                throw new MessageDeliveryException("Invalid JWT claims for WebSocket CONNECT");
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId.toString(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
            accessor.setUser(authentication);
            log.debug("WebSocket CONNECT authenticated for userId={}", userId);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) && accessor.getUser() == null) {
            throw new MessageDeliveryException("Unauthenticated WebSocket SUBSCRIBE request");
        }

        return message;
    }
}
