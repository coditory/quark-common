package com.coditory.quark.common.uri;

import com.coditory.quark.common.collection.Lists;
import com.coditory.quark.common.net.Ports;
import com.coditory.quark.common.throwable.Throwables;
import com.coditory.quark.common.util.Strings;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coditory.quark.common.check.Args.checkNotBlank;
import static com.coditory.quark.common.check.Args.checkNotNull;
import static com.coditory.quark.common.uri.UriComponentsParser.parseQuery;
import static com.coditory.quark.common.uri.UriRfc.PATH_SEGMENT;
import static com.coditory.quark.common.util.Strings.emptyToNull;
import static com.coditory.quark.common.util.Strings.isNotBlank;
import static com.coditory.quark.common.util.Strings.isNotEmpty;
import static com.coditory.quark.common.util.Strings.isNullOrEmpty;
import static com.coditory.quark.common.util.Strings.lowerCase;
import static java.util.stream.Collectors.toList;

public final class UriBuilder {
    static UriBuilder from(UriComponents uriComponents) {
        UriBuilder builder = new UriBuilder();
        builder.scheme = uriComponents.getScheme();
        builder.ssp = uriComponents.getSchemeSpecificPart();
        builder.fragment = uriComponents.getFragment();
        if (!uriComponents.isOpaque()) {
            builder.userInfo = uriComponents.getUserInfo();
            builder.protocolRelative = uriComponents.isProtocolRelative();
            builder.host = uriComponents.getHost();
            builder.port = uriComponents.getPort();
            builder.setRootPath(uriComponents.isRootPath());
            builder.setPathSegments(uriComponents.getPathSegments());
            builder.setQueryMultiParams(uriComponents.getQueryParams());
        }
        return builder;
    }

    static UriBuilder from(URI uri) {
        return new UriBuilder().setUri(uri);
    }

    static UriBuilder from(URL url) {
        URI uri = Throwables.sneakyThrow(url::toURI);
        return new UriBuilder().setUri(uri);
    }

    static UriBuilder fromUri(String uri) {
        return UriComponentsParser.parseUri(uri);
    }

    public static UriBuilder fromHttpUrl(String url) {
        return UriComponentsParser.parseHttpUrl(url);
    }

    private String scheme;
    private String ssp;
    private String userInfo;
    private String host;
    private int port = -1;
    private boolean protocolRelative = false;
    private boolean rootPath = false;
    private final List<String> pathSegments = new ArrayList<>();
    private final Map<String, List<String>> queryParams = new HashMap<>();
    private String fragment;

    UriBuilder() { }

    public UriBuilder setUri(URI uri) {
        checkNotNull(uri, "URI must not be null");
        this.scheme = uri.getScheme();
        if (uri.isOpaque()) {
            this.ssp = uri.getRawSchemeSpecificPart();
            resetHierarchicalComponents();
        } else {
            if (uri.getRawUserInfo() != null) {
                this.userInfo = uri.getRawUserInfo();
            }
            if (uri.getHost() != null) {
                this.host = uri.getHost();
            }
            if (uri.getPort() != -1) {
                this.port = uri.getPort();
            }
            if (isNotEmpty(uri.getRawPath())) {
                setPath(uri.getRawPath());
            }
            if (isNotEmpty(uri.getRawQuery())) {
                this.queryParams.clear();
                setQuery(uri.getRawQuery());
            }
            resetSchemeSpecificPart();
        }
        if (uri.getRawFragment() != null) {
            this.fragment = uri.getRawFragment();
        }
        return this;
    }

    public UriBuilder copy() {
        return from(build());
    }

    public UriComponents build() {
        return this.ssp != null
                ? UriComponents.buildOpaque(scheme, ssp, fragment)
                : UriComponents.buildHierarchical(scheme, userInfo, host, port, protocolRelative, rootPath, pathSegments, queryParams, fragment);
    }

    public URI toUri() {
        return build().toUri();
    }

    public String toUriString() {
        return build().toUriString();
    }

    public UriBuilder setScheme(String scheme) {
        if (isNotBlank(scheme)) {
            if (scheme.equals("//")) {
                this.scheme = null;
                this.protocolRelative = true;
            } else {
                this.scheme = lowerCase(scheme);
                this.protocolRelative = false;
            }
            resetSchemeSpecificPart();
        } else {
            this.scheme = null;
        }
        return this;
    }

    public UriBuilder setProtocolRelative(boolean protocolRelative) {
        this.scheme = null;
        this.protocolRelative = protocolRelative;
        return this;
    }

    public UriBuilder removeScheme() {
        this.scheme = null;
        return this;
    }

    public UriBuilder setSchemeSpecificPart(String ssp) {
        this.ssp = emptyToNull(ssp);
        if (isNotBlank(ssp)) {
            resetHierarchicalComponents();
        }
        return this;
    }

    public UriBuilder removeSchemeSpecificPart() {
        this.ssp = null;
        return this;
    }

    public UriBuilder setUserInfo(String userInfo) {
        this.userInfo = emptyToNull(userInfo);
        if (isNotBlank(userInfo)) {
            resetSchemeSpecificPart();
        }
        return this;
    }

    public UriBuilder removeUserInfo() {
        this.userInfo = null;
        return this;
    }


    public UriBuilder setHost(String host) {
        if (isNotBlank(host)) {
            this.host = lowerCase(host);
            this.rootPath = true;
            resetSchemeSpecificPart();
        } else {
            this.host = null;
        }
        return this;
    }

    public UriBuilder removeHost() {
        this.host = null;
        return this;
    }

    public UriBuilder setDefaultPort() {
        this.port = -1;
        return this;
    }

    public UriBuilder setPort(int port) {
        Ports.checkPortNumberOrSchemeDefault(port);
        this.port = port;
        if (port > -1) {
            resetSchemeSpecificPart();
        }
        return this;
    }

    public UriBuilder setPath(String path) {
        this.pathSegments.clear();
        this.rootPath = this.host != null;
        addSubPath(path);
        return this;
    }

    public UriBuilder addSubPath(String subPath) {
        if (isNotEmpty(subPath)) {
            if (this.pathSegments.isEmpty()) {
                rootPath = subPath.startsWith("/") || this.host != null;
            }
            List<String> newSegments = Arrays.stream(subPath.split("/"))
                    .filter(Strings::isNotEmpty)
                    .map(PATH_SEGMENT::validateAndDecode)
                    .collect(toList());
            this.pathSegments.addAll(newSegments);
            if (!newSegments.isEmpty()) {
                resetSchemeSpecificPart();
            }
        }
        return this;
    }

    public void setRootPath(boolean rootPath) {
        this.rootPath = rootPath;
        resetSchemeSpecificPart();
    }

    public UriBuilder setPathSegments(List<String> pathSegments) {
        checkNotNull(pathSegments, "pathSegments");
        this.pathSegments.clear();
        addPathSegments(pathSegments);
        return this;
    }

    public UriBuilder addPathSegment(String pathSegment) {
        if (isNullOrEmpty(pathSegment)) {
            return this;
        }
        addPathSegments(List.of(pathSegment));
        return this;
    }

    public UriBuilder addPathSegments(List<String> pathSegments) {
        checkNotNull(pathSegments, "pathSegments");
        List<String> filtered = pathSegments.stream()
                .filter(Strings::isNotEmpty)
                .collect(toList());
        this.pathSegments.addAll(filtered);
        if (!filtered.isEmpty()) {
            resetSchemeSpecificPart();
        }
        return this;
    }

    public UriBuilder setQuery(String query) {
        checkNotNull(query, "query");
        setQueryMultiParams(parseQuery(query));
        return this;
    }

    public UriBuilder setQueryParams(Map<String, String> params) {
        checkNotNull(params, "params");
        this.queryParams.clear();
        params.forEach(this::putQueryParam);
        return this;
    }

    public UriBuilder setQueryMultiParams(Map<String, List<String>> params) {
        checkNotNull(params, "params");
        this.queryParams.clear();
        params.forEach(this::putQueryParam);
        return this;
    }

    public UriBuilder putQueryParams(Map<String, String> params) {
        checkNotNull(params, "params");
        params.entrySet().stream()
                .filter(e -> isNotBlank(e.getKey()))
                .forEach(entry -> putQueryParam(entry.getKey(), entry.getValue()));
        return this;
    }

    public UriBuilder putQueryMultiParams(Map<String, List<String>> params) {
        checkNotNull(params, "params");
        params.entrySet().stream()
                .filter(e -> isNotBlank(e.getKey()))
                .filter(e -> Lists.isNotEmpty(e.getValue()))
                .forEach(entry -> putQueryParam(entry.getKey(), entry.getValue()));
        return this;
    }

    public UriBuilder putQueryParam(String name, String value) {
        checkNotBlank(name, "name");
        checkNotNull(value, "value");
        return putQueryParam(name, List.of(value));
    }

    public UriBuilder putQueryParam(String name, Collection<String> values) {
        checkNotBlank(name, "name");
        checkNotNull(values, "values");
        this.queryParams.put(name, List.copyOf(values));
        resetSchemeSpecificPart();
        return this;
    }

    public UriBuilder addQueryParams(Map<String, String> params) {
        checkNotNull(params, "params");
        params.entrySet().stream()
                .filter(e -> isNotBlank(e.getKey()))
                .forEach(entry -> addQueryParam(entry.getKey(), entry.getValue()));
        resetSchemeSpecificPart();
        return this;
    }

    public UriBuilder addQueryMultiParams(Map<String, List<String>> params) {
        checkNotNull(params, "params");
        params.entrySet().stream()
                .filter(e -> isNotBlank(e.getKey()))
                .filter(e -> Lists.isNotEmpty(e.getValue()))
                .forEach(entry -> addQueryParam(entry.getKey(), entry.getValue()));
        resetSchemeSpecificPart();
        return this;
    }

    public UriBuilder addQueryParam(String name, String value) {
        checkNotBlank(name, "name");
        checkNotNull(value, "value");
        return addQueryParam(name, List.of(value));
    }

    public UriBuilder addQueryParam(String name, Collection<String> values) {
        checkNotBlank(name, "name");
        checkNotNull(values, "values");
        List<String> prevValues = this.queryParams.get(name);
        if (prevValues != null) {
            this.queryParams.put(name, Lists.addAll(prevValues, values));
        } else {
            this.queryParams.put(name, List.copyOf(values));
        }
        resetSchemeSpecificPart();
        return this;
    }

    public UriBuilder removeQueryParams() {
        this.queryParams.clear();
        return this;
    }

    public UriBuilder removeQueryParam(String name) {
        checkNotNull(name, "name");
        this.queryParams.remove(name);
        return this;
    }

    public UriBuilder removeQueryParam(String name, String value) {
        checkNotNull(name, "name");
        List<String> values = this.queryParams.get(name);
        if (values != null && value != null) {
            this.queryParams.put(name, Lists.remove(values, value));
        }
        return this;
    }

    public UriBuilder setFragment(String fragment) {
        this.fragment = emptyToNull(fragment);
        return this;
    }

    public UriBuilder removeFragment() {
        this.fragment = null;
        return this;
    }

    private void resetHierarchicalComponents() {
        this.userInfo = null;
        this.host = null;
        this.port = -1;
        this.pathSegments.clear();
        this.queryParams.clear();
    }

    private void resetSchemeSpecificPart() {
        this.ssp = null;
    }

    void validate() {
        build();
    }
}
