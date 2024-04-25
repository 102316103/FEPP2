package com.syscom.fep.frmcommon.net.http;

import com.google.gson.Gson;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpClient {
    private final LogHelper logger = new LogHelper();
    private static final String MESSAGE_IN = "<<<<<<<<<<";
    private static final String MESSAGE_OUT = ">>>>>>>>>>";
    private static final int DEFAULT_TIMEOUT = 30000;
    private boolean recordLog = true;
    private long responseLengthLimit;

    public HttpClient() {
        this(true);
    }

    public HttpClient(boolean recordLog) {
        this(recordLog, 0);
    }

    public HttpClient(long responseLengthLimit) {
        this(true, responseLengthLimit);
    }

    public HttpClient(boolean recordLog, long responseLengthLimit) {
        this.recordLog = recordLog;
        this.responseLengthLimit = responseLengthLimit;
    }

    public void setRecordLog(boolean recordLog) {
        this.recordLog = recordLog;
    }

    public void setResponseLengthLimit(long responseLengthLimit) {
        this.responseLengthLimit = responseLengthLimit;
    }

    public String doGet(String uri, boolean... exceptionLogging) throws Exception {
        return doGet(uri, null, exceptionLogging);
    }

    public String doGet(String uri, Map<String, Object> uriVariablesMap, boolean... exceptionLogging) throws Exception {
        return doGet(uri, (MediaType) null, uriVariablesMap, null, exceptionLogging);
    }

    public String doGet(String uri, Map<String, Object> uriVariablesMap, Charset defaultCharset, boolean... exceptionLogging) throws Exception {
        return doGet(uri, (MediaType) null, uriVariablesMap, defaultCharset, exceptionLogging);
    }

    public String doGet(String uri, MediaType mediaType, Map<String, Object> uriVariablesMap, Charset defaultCharset, boolean... exceptionLogging) throws Exception {
        return doGet(uri, mediaType, DEFAULT_TIMEOUT, uriVariablesMap, defaultCharset, exceptionLogging);
    }

    public String doGet(String uri, MediaType mediaType, int timeout, Map<String, Object> uriVariablesMap, boolean... exceptionLogging) throws Exception {
        return doGet(uri, mediaType, timeout, uriVariablesMap, null, exceptionLogging);
    }

    public String doGet(String uri, MediaType mediaType, int timeout, Map<String, Object> uriVariablesMap, Charset defaultCharset, boolean... exceptionLogging) throws Exception {
        String url = uri;
        if (MapUtils.isNotEmpty(uriVariablesMap)) {
            for (Map.Entry<String, Object> entry : uriVariablesMap.entrySet()) {
                url = StringUtils.replace(url, StringUtils.join("{", entry.getKey(), "}"), String.valueOf(entry.getValue()));
            }
        }
        try {
            // HttpHeaders
            HttpHeaders headers = new HttpHeaders();
            // ContentType
            headers.setContentType(mediaType);
            // requestEntity
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            // RestTemplate
            RestTemplate restTemplate = new RestTemplate();
            if (defaultCharset != null) {
                restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            }
            // SimpleClientHttpRequestFactory
            if (timeout > 0) {
                if (isHttps(uri)) {
                    restTemplate.setRequestFactory(HttpClientConfiguration.createHttpComponentsClientHttpRequestFactory(timeout));
                } else {
                    restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(timeout));
                }
            }
            debug("[", url, "]", MESSAGE_OUT);
            ResponseEntity<String> responseEntity = null;
            if (MapUtils.isEmpty(uriVariablesMap)) {
                responseEntity = restTemplate.exchange(toUriString(uri), HttpMethod.GET, requestEntity, String.class);
            } else {
                // 有可能uri中帶有{param}這樣的, 所以不要轉義
                responseEntity = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class, uriVariablesMap);
            }
            String response = responseEntity.getBody();
            debug("[", url, "]", MESSAGE_IN, this.responseLengthLimit > 0 && StringUtils.isNotBlank(response) && response.length() > this.responseLengthLimit ? StringUtils.join("[", response.length(), " characters]") : response);
            return response;
        } catch (Exception e) {
            throw handleException(url, e, exceptionLogging);
        }
    }

    public <T> String doPost(String uri, T request, boolean... exceptionLogging) throws Exception {
        return doPost(uri, null, request, exceptionLogging);
    }

    public <T> String doPost(String uri, MediaType mediaType, T request, boolean... exceptionLogging) throws Exception {
        return doPost(uri, mediaType, DEFAULT_TIMEOUT, request, StandardCharsets.UTF_8, exceptionLogging);
    }

    public <T> String doPost(String uri, MediaType mediaType, T request, Charset defaultCharset, boolean... exceptionLogging) throws Exception {
        return doPost(uri, mediaType, DEFAULT_TIMEOUT, request, defaultCharset, exceptionLogging);
    }

    public <T> String doPost(String uri, MediaType mediaType, int timeout, T request, boolean... exceptionLogging) throws Exception {
        return doPost(uri, mediaType, timeout, request, StandardCharsets.UTF_8, exceptionLogging);
    }

    public <T> String doPost(String uri, MediaType mediaType, int timeout, T request, Charset defaultCharset, boolean... exceptionLogging) throws Exception {
        try {
            // HttpHeaders
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            // HttpEntity
            HttpEntity<?> requestEntity = null;
            if (request instanceof Map) {
                // MultiValueMap
                MultiValueMap<Object, Object> postParameters = new LinkedMultiValueMap<>();
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) request).entrySet()) {
                    postParameters.add(entry.getKey(), entry.getValue());
                }
                requestEntity = new HttpEntity<>(postParameters, headers);
            } else {
                requestEntity = new HttpEntity<>(request, headers);
            }
            // RestTemplate
            RestTemplate restTemplate = new RestTemplate();
            if (defaultCharset != null) {
                restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(defaultCharset));
            } else {
                restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            }
            // SimpleClientHttpRequestFactory
            if (timeout > 0) {
                if (isHttps(uri)) {
                    restTemplate.setRequestFactory(HttpClientConfiguration.createHttpComponentsClientHttpRequestFactory(timeout));
                } else {
                    restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(timeout));
                }
            }
            debug("[", uri, "]", MESSAGE_OUT, this.printRequest(mediaType, request));
            ResponseEntity<String> responseEntity = restTemplate.exchange(toUriString(uri), HttpMethod.POST, requestEntity, String.class);
            String response = responseEntity.getBody();
            debug("[", uri, "]", MESSAGE_IN, this.responseLengthLimit > 0 && StringUtils.isNotBlank(response) && response.length() > this.responseLengthLimit ? StringUtils.join("[", response.length(), " characters]") : response);
            return response;
        } catch (Exception e) {
            throw handleException(uri, e, exceptionLogging);
        }
    }

    private <T> String printRequest(MediaType mediaType, T request) {
        if (mediaType == MediaType.APPLICATION_JSON) {
            return new Gson().toJson(request);
        }
        return request.toString();
    }

    private Exception handleException(String uri, Exception e, boolean... exceptionLogging) {
        if (ArrayUtils.isEmpty(exceptionLogging) || exceptionLogging[0]) {
            logger.exceptionMsg(e, "do http request, uri = [", uri, "] with exception occur!!");
        }
        if (e instanceof RestClientException) {
            if (e.getCause() instanceof ConnectException) {
                return ExceptionUtil.createException(e, HttpResultCode.CONNECTION_REFUSED);
            } else if (e.getCause() instanceof SocketTimeoutException) {
                return ExceptionUtil.createException(e, HttpResultCode.READ_TIMED_OUT);
            }
            return ExceptionUtil.createException(e, e.getMessage());
        } else {
            return e;
        }
    }

    public static boolean isHttps(String uri) {
        return uri.toLowerCase().startsWith("https");
    }

    public static String toUriString(String uri) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uri);
        return builder.build().encode().toUriString();
    }

    private void debug(Object... messages) {
        if (recordLog)
            logger.debug(messages);
    }
}
