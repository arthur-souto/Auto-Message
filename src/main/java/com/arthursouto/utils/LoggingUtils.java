package com.arthursouto.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.experimental.UtilityClass;
import org.springframework.aop.support.AopUtils;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Helpers para deixar os logs legíveis e seguros:
 * - Serializa objetos para JSON (sem quebrar o log se falhar).
 * - Mascara campos sensíveis (senha, token, secret, etc).
 * - Trunca payloads muito grandes.
 * - Resolve o nome "real" da classe (ignorando proxies do Spring/CGLIB).
 */
@UtilityClass
public class LoggingUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    private static final int MAX_LENGTH = 2000;

    // nomes de campos que nunca devem aparecer em texto puro no log
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "senha", "secret", "token", "accessToken",
            "refreshToken", "authorization", "jwt", "tokenHash"
    );

    // ex: "accessToken":"eyJhbGciOi..."  ->  "accessToken":"***"
    private static final Pattern SENSITIVE_JSON_PATTERN = Pattern.compile(
            "(?i)(\"(" + String.join("|", SENSITIVE_FIELDS) + ")\"\\s*:\\s*\")[^\"]*(\")"
    );

    /** Nome simples da classe real, mesmo quando o objeto é um proxy do Spring AOP/CGLIB. */
    public static String resolveClassName(Object target) {
        if (target == null) {
            return "null";
        }
        Class<?> targetClass = AopUtils.isAopProxy(target)
                ? AopUtils.getTargetClass(target)
                : target.getClass();
        return targetClass.getSimpleName();
    }

    /** Serializa um objeto para JSON, mascarando campos sensíveis e truncando se ficar muito grande. */
    public static String toSafeJson(Object value) {
        if (value == null) {
            return "null";
        }
        try {
            String json = MAPPER.writeValueAsString(value);
            return truncate(mask(json));
        } catch (Exception e) {
            // nunca deixa o log quebrar por causa de um objeto não serializável
            return "[unserializable:" + value.getClass().getSimpleName() + "]";
        }
    }

    /** Serializa um array de argumentos de método (ex: args de um @Around) em uma única linha. */
    public static String toSafeJson(Object[] values) {
        if (values == null || values.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            sb.append(toSafeJson(values[i]));
            if (i < values.length - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }

    private static String mask(String json) {
        return SENSITIVE_JSON_PATTERN.matcher(json).replaceAll("$1***$3");
    }

    private static String truncate(String value) {
        if (value.length() <= MAX_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_LENGTH) + "...(truncated," + value.length() + " chars)";
    }
}