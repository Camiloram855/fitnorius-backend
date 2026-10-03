package com.camilo.fitnorius.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Resuelve la IP de un cliente sin confiar ciegamente en cabeceras enviadas por
 * el navegador. Solo se acepta X-Forwarded-For cuando la conexión inmediata
 * pertenece a un proxy conocido/privado; en despliegues con proxy público se
 * puede configurar TRUSTED_PROXY_ADDRESSES.
 */
@Component
public class ClientIpResolver {

    private final Set<String> configuredTrustedProxies;

    public ClientIpResolver(@Value("${TRUSTED_PROXY_ADDRESSES:}") String configuredTrustedProxies) {
        this.configuredTrustedProxies = new HashSet<>();
        if (StringUtils.hasText(configuredTrustedProxies)) {
            Arrays.stream(configuredTrustedProxies.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .forEach(this.configuredTrustedProxies::add);
        }
    }

    public String resolve(HttpServletRequest request) {
        String remoteAddress = safeRemoteAddress(request);
        if (!isTrustedProxy(remoteAddress)) {
            return remoteAddress;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(forwardedFor)) {
            return remoteAddress;
        }

        String[] chain = forwardedFor.split(",");
        // Se toma la primera dirección pública desde la derecha: el proxy
        // confiable suele añadir la IP real al final de la cadena.
        for (int i = chain.length - 1; i >= 0; i--) {
            String candidate = safeAddress(chain[i]);
            if (candidate != null && !isPrivateAddress(candidate)) {
                return candidate;
            }
        }
        for (String value : chain) {
            String candidate = safeAddress(value);
            if (candidate != null) {
                return candidate;
            }
        }
        return remoteAddress;
    }

    private boolean isTrustedProxy(String address) {
        return configuredTrustedProxies.contains(address) || isPrivateAddress(address);
    }

    private static String safeRemoteAddress(HttpServletRequest request) {
        String address = safeAddress(request.getRemoteAddr());
        return address == null ? "unknown" : address;
    }

    private static String safeAddress(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String candidate = value.trim();
        if (candidate.length() > 45
                || (!candidate.contains(".") && !candidate.contains(":"))
                || !candidate.matches("[0-9a-fA-F:.%]+")) {
            return null;
        }
        try {
            return InetAddress.getByName(candidate).getHostAddress();
        } catch (UnknownHostException exception) {
            return null;
        }
    }

    private static boolean isPrivateAddress(String address) {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            return inetAddress.isAnyLocalAddress()
                    || inetAddress.isLoopbackAddress()
                    || inetAddress.isLinkLocalAddress()
                    || inetAddress.isSiteLocalAddress()
                    || address.startsWith("fc")
                    || address.startsWith("fd")
                    || address.startsWith("fe80:");
        } catch (UnknownHostException exception) {
            return false;
        }
    }
}
