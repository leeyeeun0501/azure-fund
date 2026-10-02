package io.kickscar.cloud.workload.gallery.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * NCP Server Role 기반 키리스 자격증명 provider.
 *
 * VPC Server에 Server Role을 부여하면 metadata API로 임시 자격증명이 전달된다
 * (AWS IAM Role -> Instance Profile / Azure Managed Identity에 대응). 이 provider는
 * AWS SDK v2의 AwsCredentialsProvider를 구현해 NCP metadata API에서 임시 키를 받아
 * S3Client(NCP Object Storage)에 주입한다.
 *
 * <p>조회는 두 단계다. AWS EC2 IMDS와 같은 구조이고 응답 필드 이름까지 같다.
 * <pre>
 *   GET /{version}/meta-data/iam/security-credentials           -> 역할 ID
 *   GET /{version}/meta-data/iam/security-credentials/{roleId}  -> 임시 Access Key
 * </pre>
 *
 * <p>응답 필드: {@code Code / LastUpdated / Type / AccessKeyId / SecretAccessKey /
 * Token / Expiration}. {@code Type}만 AWS의 {@code AWS-HMAC} 대신 {@code NCP-HMAC}이고,
 * {@code Token}은 빈 문자열로 오는 것이 확인됐다. 그래서 토큰이 비어 있으면
 * 세션 자격증명 대신 기본 자격증명으로 떨어진다.
 *
 * <p>TODO(실증 - Ch06 Object Storage draft): {@link #METADATA_VERSION}이 {@code latest}인지
 * {@code v1}인지 실제 서버에서 확인한다. v2는 {@code X-NCP-METADATA-TOKEN} 헤더를 요구하므로
 * 이 provider는 v1 계열 경로를 전제로 한다.
 */
@Slf4j
public class NcpServerRoleCredentialsProvider implements AwsCredentialsProvider {

    /** 클라우드 metadata 표준 링크로컬 주소. 하이퍼바이저가 제공하므로 게스트 OS와 무관하다. */
    private static final String METADATA_BASE = "http://169.254.169.254";

    /** TODO(실증): latest 인지 v1 인지 확인한다. */
    private static final String METADATA_VERSION = "latest";

    private static final String CREDENTIALS_PATH =
            "/" + METADATA_VERSION + "/meta-data/iam/security-credentials";

    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    /** 만료 직전에 재발급받기 위한 여유. */
    private static final Duration REFRESH_MARGIN = Duration.ofMinutes(5);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile AwsCredentials cachedCredentials;
    private volatile Instant expiresAt;

    /**
     * AWS SDK는 요청마다 이 메서드를 호출한다. 임시 자격증명은 만료 전까지 유효하므로
     * 캐시해 두고 만료가 가까워질 때만 metadata API를 다시 호출한다.
     */
    @Override
    public AwsCredentials resolveCredentials() {
        AwsCredentials current = cachedCredentials;
        if (current != null && isFresh()) {
            return current;
        }

        synchronized (this) {
            if (cachedCredentials != null && isFresh()) {
                return cachedCredentials;
            }
            return refresh();
        }
    }

    private boolean isFresh() {
        Instant expiry = expiresAt;
        // 만료 시각을 알 수 없으면 캐시하지 않는다.
        return expiry != null && Instant.now().isBefore(expiry.minus(REFRESH_MARGIN));
    }

    private AwsCredentials refresh() {
        try {
            String roleId = get(CREDENTIALS_PATH).trim();
            if (roleId.isBlank()) {
                throw new IllegalStateException("NCP metadata API returned no role id. Is a Server Role assigned?");
            }

            JsonNode node = objectMapper.readTree(get(CREDENTIALS_PATH + "/" + roleId));

            String code = text(node, "Code");
            if (code != null && !"Success".equalsIgnoreCase(code)) {
                throw new IllegalStateException("NCP metadata API returned Code=" + code);
            }

            String accessKeyId = text(node, "AccessKeyId");
            String secretAccessKey = text(node, "SecretAccessKey");
            if (accessKeyId == null || secretAccessKey == null) {
                throw new IllegalStateException("NCP metadata response missing AccessKeyId or SecretAccessKey");
            }

            // NCP는 Token을 빈 문자열로 준다. 값이 있을 때만 세션 자격증명을 쓴다.
            String token = text(node, "Token");
            AwsCredentials credentials = (token == null)
                    ? AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                    : AwsSessionCredentials.create(accessKeyId, secretAccessKey, token);

            this.expiresAt = parseExpiration(text(node, "Expiration"));
            this.cachedCredentials = credentials;

            log.info("NcpServerRoleCredentialsProvider: resolved temporary credentials [roleId: {}, type: {}, expiration: {}]",
                    roleId, text(node, "Type"), this.expiresAt);

            return credentials;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve NCP Server Role credentials from metadata API", e);
        }
    }

    private String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(METADATA_BASE + path))
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "NCP metadata API returned status " + response.statusCode() + " for " + path);
        }
        return response.body();
    }

    /** 값이 없거나 빈 문자열이면 null을 돌려준다. NCP의 빈 Token을 걸러내는 자리다. */
    private static String text(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return (value == null || value.isBlank()) ? null : value;
    }

    /**
     * 만료 시각을 파싱한다. 형식이 확인되지 않았으므로 실패해도 예외를 던지지 않고
     * null을 돌려준다. null이면 캐시하지 않고 매번 다시 발급받는다.
     */
    private static Instant parseExpiration(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            log.warn("NcpServerRoleCredentialsProvider: unrecognized Expiration format [{}]. Credentials will not be cached.", value);
            return null;
        }
    }
}
