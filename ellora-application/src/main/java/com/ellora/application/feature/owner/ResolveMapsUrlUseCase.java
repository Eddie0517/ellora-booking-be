package com.ellora.application.feature.owner;

import com.ellora.application.feature.owner.dto.ResolvedCoordinatesResponse;
import com.ellora.domain.exception.BusinessRuleException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ResolveMapsUrlUseCase {

    private static final Pattern PLACE_PATTERN = Pattern.compile("!3d(-?\\d+(?:\\.\\d+)?)[^!]*!4d(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern AT_PATTERN = Pattern.compile("@(-?\\d+(?:\\.\\d+)?),(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern QUERY_PATTERN = Pattern.compile("[?&](?:q|query|ll|destination|center)=(-?\\d+(?:\\.\\d+)?)[,\\s]+(-?\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern RAW_PATTERN = Pattern.compile("^\\s*(-?\\d+(?:\\.\\d+)?)[,\\s]+(-?\\d+(?:\\.\\d+)?)\\s*$");

    public ResolvedCoordinatesResponse execute(String inputUrl) {
        if (inputUrl == null || inputUrl.isBlank()) {
            throw new BusinessRuleException("INVALID_INPUT", "Đường dẫn không được để trống");
        }

        String trimmed = inputUrl.trim();

        // 1. Direct regex extract
        ResolvedCoordinatesResponse direct = extractFromText(trimmed, trimmed);
        if (direct != null) {
            return direct;
        }

        // 2. If it's a web URL (especially shortened like maps.app.goo.gl or goo.gl/maps)
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .connectTimeout(Duration.ofSeconds(6))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(trimmed))
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .GET()
                        .build();

                HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
                String finalUrl = response.uri().toString();

                ResolvedCoordinatesResponse resolved = extractFromText(finalUrl, finalUrl);
                if (resolved != null) {
                    return resolved;
                }
            } catch (Exception ignored) {
                // Ignore redirect failure and fall through
            }
        }

        throw new BusinessRuleException("COORDINATES_NOT_FOUND",
                "Không trích xuất được tọa độ từ liên kết đã cung cấp. Vui lòng dán liên kết chi tiết hoặc nhập vĩ độ/kinh độ.");
    }

    private ResolvedCoordinatesResponse extractFromText(String text, String resolvedUrl) {
        if (text == null) return null;

        // Try !3d...!4d... first (exact place pin)
        Matcher placeMatcher = PLACE_PATTERN.matcher(text);
        if (placeMatcher.find()) {
            double lat = Double.parseDouble(placeMatcher.group(1));
            double lng = Double.parseDouble(placeMatcher.group(2));
            if (isValid(lat, lng)) {
                return new ResolvedCoordinatesResponse(lat, lng, resolvedUrl);
            }
        }

        // Try @lat,lng
        Matcher atMatcher = AT_PATTERN.matcher(text);
        if (atMatcher.find()) {
            double lat = Double.parseDouble(atMatcher.group(1));
            double lng = Double.parseDouble(atMatcher.group(2));
            if (isValid(lat, lng)) {
                return new ResolvedCoordinatesResponse(lat, lng, resolvedUrl);
            }
        }

        // Try query params
        Matcher queryMatcher = QUERY_PATTERN.matcher(text);
        if (queryMatcher.find()) {
            double lat = Double.parseDouble(queryMatcher.group(1));
            double lng = Double.parseDouble(queryMatcher.group(2));
            if (isValid(lat, lng)) {
                return new ResolvedCoordinatesResponse(lat, lng, resolvedUrl);
            }
        }

        // Try raw coordinates
        Matcher rawMatcher = RAW_PATTERN.matcher(text);
        if (rawMatcher.find()) {
            double lat = Double.parseDouble(rawMatcher.group(1));
            double lng = Double.parseDouble(rawMatcher.group(2));
            if (isValid(lat, lng)) {
                return new ResolvedCoordinatesResponse(lat, lng, resolvedUrl);
            }
        }

        return null;
    }

    private boolean isValid(double lat, double lng) {
        return lat >= -90.0 && lat <= 90.0 && lng >= -180.0 && lng <= 180.0;
    }
}
