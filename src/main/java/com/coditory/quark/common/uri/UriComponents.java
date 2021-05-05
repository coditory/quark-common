package com.coditory.quark.common.uri;

import com.coditory.quark.common.collection.Lists;
import com.coditory.quark.common.collection.Maps;
import com.coditory.quark.common.throwable.Throwables;
import com.coditory.quark.common.util.Objects;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.coditory.quark.common.check.Args.checkNotBlank;
import static com.coditory.quark.common.check.Args.checkNotNull;
import static com.coditory.quark.common.uri.UriPartValidator.checkPort;
import static com.coditory.quark.common.uri.UriRfc.FRAGMENT;
import static com.coditory.quark.common.uri.UriRfc.PATH_SEGMENT;
import static com.coditory.quark.common.uri.UriRfc.QUERY_PARAM;
import static com.coditory.quark.common.uri.UriRfc.SCHEME;
import static com.coditory.quark.common.uri.UriRfc.SCHEME_SPECIFIC_PART;
import static com.coditory.quark.common.uri.UriRfc.USER_INFO;
import static com.coditory.quark.common.util.Strings.isNotEmpty;
import static com.coditory.quark.common.util.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.joining;

public final class UriComponents {
    public static UriComponents fromUri(String uri) {
        checkNotNull(uri, "uri");
        return builderFromUri(uri).build();
    }

    @Nullable
    public static UriComponents fromUriOrNull(String uri) {
        checkNotNull(uri, "uri");
        return Throwables.onErrorNull(() -> fromUri(uri));
    }

    public static UriComponents fromHttpUrl(String url) {
        checkNotNull(url, "url");
        return builderFromHttpUrl(url).build();
    }

    @Nullable
    public static UriComponents fromHttpUrlOrNull(String url) {
        checkNotNull(url, "url");
        return Throwables.onErrorNull(() -> fromHttpUrl(url));
    }

    public static UriComponents from(URI uri) {
        checkNotNull(uri, "uri");
        return builderFrom(uri).build();
    }

    public static UriComponents from(URL url) {
        checkNotNull(url, "url");
        return builderFrom(url).build();
    }

    public static UriBuilder builder() {
        return new UriBuilder();
    }

    public static UriBuilder builderFrom(UriComponents uriComponents) {
        checkNotNull(uriComponents, "uriComponents");
        return UriBuilder.from(uriComponents);
    }

    public static UriBuilder builderFrom(URL url) {
        checkNotNull(url, "url");
        return UriBuilder.from(url);
    }

    public static UriBuilder builderFrom(URI uri) {
        checkNotNull(uri, "uri");
        return UriBuilder.from(uri);
    }

    public static UriBuilder builderFromUri(String uri) {
        checkNotNull(uri, "uri");
        return UriBuilder.fromUri(uri);
    }

    public static UriBuilder builderFromHttpUrl(String url) {
        checkNotNull(url, "url");
        return UriBuilder.fromHttpUrl(url);
    }

    static UriComponents buildOpaque(
            String scheme,
            String ssp,
            @Nullable String fragment
    ) {
        checkNotBlank(scheme, "scheme");
        checkNotBlank(ssp, "ssp");
        return new UriComponents(scheme, ssp, null, null, -1, false, false, null, null, fragment);
    }

    static UriComponents buildHierarchical(
            @Nullable String scheme,
            @Nullable String userInfo,
            @Nullable String host,
            int port,
            boolean protocolRelative,
            boolean rootRelative,
            @Nullable List<String> pathSegments,
            @Nullable Map<String, List<String>> queryParams,
            @Nullable String fragment
    ) {
        if (isNullOrEmpty(host)) {
            if (isNotEmpty(userInfo)) {
                throw new InvalidUriException("URI with user info must include host");
            }
            if (port >= 0) {
                throw new InvalidUriException("URI with port must include host");
            }
        }
        if (scheme != null && protocolRelative) {
            throw new InvalidUriException("URI cannot be protocol relative and have a scheme");
        }
        Objects.onNotNull(scheme, UriPartValidator::checkScheme);
        Objects.onNotNull(host, UriPartValidator::checkHost);
        if (port >= 0) {
            checkPort(port);
        }
        return new UriComponents(scheme, null, userInfo, host, port, protocolRelative, rootRelative, pathSegments, queryParams, fragment);
    }

    private final String ssp;
    private final String scheme;
    private final String userInfo;
    private final String host;
    private final int port;
    private final boolean protocolRelative;
    private final boolean rootPath;
    private final List<String> pathSegments;
    private final Map<String, List<String>> queryParams;
    private final String fragment;

    private UriComponents(String scheme, String ssp, String userInfo, String host, int port,
                          boolean protocolRelative, boolean rootPath, List<String> pathSegments,
                          Map<String, List<String>> queryParams, String fragment) {
        this.scheme = scheme;
        this.ssp = ssp;
        this.userInfo = userInfo;
        this.host = host;
        this.port = port;
        this.protocolRelative = protocolRelative;
        this.rootPath = rootPath;
        this.pathSegments = Lists.nullToEmpty(pathSegments);
        this.queryParams = Maps.nullToEmpty(queryParams);
        this.fragment = fragment;
    }

    public boolean isOpaque() {
        return ssp != null;
    }

    @Nullable
    public String getSchemeSpecificPart() {
        return ssp;
    }

    @Nullable
    public String getScheme() {
        return scheme;
    }

    @Nullable
    public String getUserInfo() {
        return userInfo;
    }

    @Nullable
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isRootPath() {
        return rootPath;
    }

    public boolean isProtocolRelative() {
        return protocolRelative;
    }

    @Nullable
    public List<String> getPathSegments() {
        return pathSegments;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public Map<String, String> getSingleValueQueryParams() {
        return Maps.mapValues(queryParams, Lists::firstOrNull);
    }

    @Nullable
    public UriAuthority getUriAuthority() {
        if (isOpaque()) {
            return null;
        }
        UriAuthority authority = UriAuthority.of(userInfo, host, port);
        return authority.isEmpty() ? null : authority;
    }

    public String getQueryString() {
        if (this.queryParams.isEmpty()) {
            return null;
        }
        StringBuilder queryBuilder = new StringBuilder();
        this.queryParams.forEach((name, values) -> {
            if (Lists.isNullOrEmpty(values)) {
                if (queryBuilder.length() != 0) {
                    queryBuilder.append('&');
                }
                queryBuilder.append(QUERY_PARAM.encode(name));
            } else {
                for (Object value : values) {
                    if (queryBuilder.length() != 0) {
                        queryBuilder.append('&');
                    }
                    queryBuilder.append(QUERY_PARAM.encode(name))
                            .append('=')
                            .append(QUERY_PARAM.encode(value.toString()));
                }
            }
        });
        return queryBuilder.toString();
    }

    @Nullable
    public String getPath() {
        if (pathSegments.isEmpty()) {
            return null;
        }
        String path = pathSegments.stream()
                .map(PATH_SEGMENT::encode)
                .collect(joining("/"));
        String prefix = rootPath ? "/" : "";
        return prefix + path;
    }

    @Nullable
    public String getFragment() {
        return fragment;
    }

    public boolean isHttpUrl() {
        return !isOpaque() && "http".equals(scheme) || "https".equals(scheme);
    }

    public boolean isValidHttpUrl() {
        return UrlValidator.isValidUrl(this);
    }

    public URL toUrl() {
        try {
            return new URL(toUriString());
        } catch (Exception e) {
            throw new IllegalStateException("Could not build URI", e);
        }
    }

    public URI toUri() {
        try {
            return new URI(toUriString());
        } catch (Exception e) {
            throw new IllegalStateException("Could not build URI", e);
        }
    }

    public String toUriString() {
        return ssp != null
                ? toOpaqueUriString()
                : toHierarchicalUriString();
    }

    private String toOpaqueUriString() {
        StringBuilder uriBuilder = new StringBuilder();
        if (scheme != null) {
            uriBuilder.append(SCHEME.encode(scheme))
                    .append(':');
        }
        uriBuilder.append(SCHEME_SPECIFIC_PART.encode(ssp));
        if (fragment != null) {
            uriBuilder.append('#')
                    .append(FRAGMENT.encode(fragment));
        }
        return uriBuilder.toString();
    }

    private String toHierarchicalUriString() {
        StringBuilder uriBuilder = new StringBuilder();
        if (scheme != null) {
            uriBuilder.append(SCHEME.encode(scheme))
                    .append("://");
        } else if (protocolRelative) {
            uriBuilder.append("//");
        }
        if (userInfo != null || host != null) {
            if (userInfo != null) {
                uriBuilder.append(USER_INFO.encode(this.userInfo))
                        .append('@');
            }
            if (host != null) {
                uriBuilder.append(this.host);
            }
            if (port != -1) {
                uriBuilder.append(':')
                        .append(this.port);
            }
        }
        String path = getPath();
        String query = getQueryString();
        if (path != null) {
            if (!path.equals("/") || query != null || fragment != null || host == null) {
                uriBuilder.append(path);
            }
        } else if (rootPath && host == null) {
            uriBuilder.append("/");
        }
        if (query != null) {
            uriBuilder.append('?')
                    .append(query);
        }
        if (fragment != null) {
            uriBuilder.append('#')
                    .append(FRAGMENT.encode(fragment));
        }
        return uriBuilder.toString();
    }
}
