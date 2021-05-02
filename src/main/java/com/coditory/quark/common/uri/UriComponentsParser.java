package com.coditory.quark.common.uri;

import com.coditory.quark.common.throwable.Throwables;
import com.coditory.quark.common.util.Objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.coditory.quark.common.util.Objects.mapNotNullOrDefault;
import static com.coditory.quark.common.util.Strings.isNotEmpty;
import static com.coditory.quark.common.check.Args.checkNotNull;

class UriComponentsParser {
    private static final Pattern QUERY_PARAM_PATTERN = Pattern.compile("([^&=]+)(=?)([^&]+)?");

    private static final String SCHEME_PATTERN = "([^:/?#]+):";

    private static final String USERINFO_PATTERN = "([^@\\[/?#]*)";

    private static final String HOST_IPV4_PATTERN = "[^\\[/?#:]*";

    private static final String HOST_IPV6_PATTERN = "\\[[\\p{XDigit}:.]*[%\\p{Alnum}]*]";

    private static final String HOST_PATTERN = "(" + HOST_IPV6_PATTERN + "|" + HOST_IPV4_PATTERN + ")";

    private static final String PORT_PATTERN = "([^/?#]*)";

    private static final String PATH_PATTERN = "([^?#]*)";

    private static final String QUERY_PATTERN = "([^#]*)";

    private static final String LAST_PATTERN = "(.*)";

    // Regex patterns that matches URIs. See RFC 3986, appendix B
    private static final Pattern URI_PATTERN = Pattern.compile(
            "^(" + SCHEME_PATTERN + ")?" +
                    "(//(" + USERINFO_PATTERN + "@)?" + HOST_PATTERN + "(:" + PORT_PATTERN + ")?" + ")?" +
                    PATH_PATTERN + "(\\?" + QUERY_PATTERN + ")?" + "(#" + LAST_PATTERN + ")?");

    public static UriComponents parseHttpUrl(String uri) {
        checkNotNull(uri, "uri");
        try {
            UriComponents components = UriComponentsParser.parseUri(uri).build();
            if (!components.isHttpUrl()) {
                throw new InvalidHttpUrlException("Invalid http url: \"" + uri + "\"");
            }
            return components;
        } catch (InvalidUriException e) {
            InvalidUriException root = Throwables.getRootCauseOfType(e, InvalidUriException.class);
            String suffix = Objects.mapNotNullOrDefault(root, (x) -> ". Cause: " + x.getMessage(), "");
            throw new InvalidHttpUrlException("Could not parse http url: \"" + uri + "\"" + suffix, root);
        } catch (RuntimeException e) {
            throw new InvalidHttpUrlException("Could not parse http url: \"" + uri + "\"", e);
        }
    }

    public static UriComponents parseHttpUrlOrNull(String uri) {
        checkNotNull(uri, "uri");
        UriBuilder builder = UriComponentsParser.parseUriOrNull(uri);
        if (builder == null) {
            return null;
        }
        UriComponents components = builder.build();
        if (!components.isHttpUrl()) {
            return null;
        }
        return components;
    }

    public static UriBuilder parseUri(String uri) {
        checkNotNull(uri, "uri");
        Matcher matcher = URI_PATTERN.matcher(uri);
        if (!matcher.matches()) {
            throw new InvalidUriException("Could not parse uri: \"" + uri + "\"");
        }
        try {
            return parseUri(uri, matcher);
        } catch (InvalidUriException e) {
            throw new InvalidUriException("Could not parse uri: \"" + uri + "\". Cause: " + e.getMessage());
        } catch (RuntimeException e) {
            throw new InvalidUriException("Could not parse uri: \"" + uri + "\"");
        }
    }

    public static UriBuilder parseUriOrNull(String uri) {
        checkNotNull(uri, "uri");
        Matcher matcher = URI_PATTERN.matcher(uri);
        if (!matcher.matches()) {
            return null;
        }
        try {
            return parseUri(uri, matcher);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static UriBuilder parseUri(String uri, Matcher matcher) {
        UriBuilder builder = new UriBuilder();
        String scheme = matcher.group(2);
        String userInfo = matcher.group(5);
        String host = matcher.group(6);
        String port = matcher.group(8);
        String path = matcher.group(9);
        String query = matcher.group(11);
        String fragment = matcher.group(13);
        boolean opaque = false;
        if (isNotEmpty(scheme)) {
            String rest = uri.substring(scheme.length());
            if (!rest.startsWith(":/")) {
                opaque = true;
            }
            builder.setScheme(UriRfc.SCHEME.validateAndDecode(scheme));
        } else if (uri.startsWith("//")) {
            builder.setProtocolRelative(true);
        }
        if (opaque) {
            String ssp = uri.substring(scheme.length()).substring(1);
            if (isNotEmpty(fragment)) {
                ssp = ssp.substring(0, ssp.length() - (fragment.length() + 1));
            }
            builder.setSchemeSpecificPart(UriRfc.SCHEME_SPECIFIC_PART.validateAndDecode(ssp));
        } else {
            Objects.onNotNull(userInfo, it -> builder.setUserInfo(UriRfc.USER_INFO.validateAndDecode(it)));
            Objects.onNotNull(host, it -> builder.setHost(UriRfc.HOST.validateAndDecode(it)));
            Objects.onNotNull(port, it -> {
                String decodedPort = UriRfc.PORT.validateAndDecode(it);
                builder.setPort(Integer.parseInt(decodedPort));
            });
            Objects.onNotNull(path, builder::setPath);
            Objects.onNotNull(query, q -> builder.setQueryMultiParams(parseQuery(q)));
        }
        Objects.onNotNull(fragment, it -> builder.setFragment(UriRfc.FRAGMENT.validateAndDecode(it)));
        builder.validate();
        return builder;
    }

    static Map<String, List<String>> parseQuery(String query) {
        checkNotNull(query, "query");
        Matcher matcher = QUERY_PARAM_PATTERN.matcher(query);
        UriRfc.QUERY.checkValidEncoded(query);
        Map<String, List<String>> result = new HashMap<>();
        while (matcher.find()) {
            String name = matcher.group(1);
            String decodedName = UriRfc.QUERY_PARAM.validateAndDecode(name);
            String value = matcher.group(3);
            String normalizedValue = value != null ? value : "";
            result
                    .computeIfAbsent(decodedName, k -> new ArrayList<>())
                    .add(UriRfc.QUERY_PARAM.validateAndDecode(normalizedValue));
        }
        return result;
    }
}
