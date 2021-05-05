package com.coditory.quark.common.uri

import spock.lang.Specification

class UriBuilder_queryParamsSpec extends Specification {
    def "should add query params with addQueryParam"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                    .addQueryParam("a", "X")
                    .addQueryParam("a", "X")
                    .addQueryParam("a", "Y")
                    .addQueryParam("b", "Y")
                    .addQueryParam("e", "")
                    .toUriString()
        then:
            result == "https://coditory.com?a=A&a=X&a=X&a=Y&b=Y&e=&w=W"
    }

    def "should add query params with addQueryMultiParams"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                    .addQueryMultiParams([
                            a : ["X", "X", "Y"],
                            b : ["Y"],
                            e1: [],
                            e2: [""]
                    ])
                    .toUriString()
        then:
            result == "https://coditory.com?a=A&a=X&a=X&a=Y&b=Y&w=W&e2="
    }

    def "should add query params with addQueryParams"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                    .addQueryParams([
                            a: "X",
                            b: "Y",
                            e: ""
                    ])
                    .toUriString()
        then:
            result == "https://coditory.com?a=A&a=X&b=Y&e=&w=W"
    }

    def "should add query params with putQueryParam"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                    .putQueryParam("a", "X")
                    .putQueryParam("a", "X")
                    .putQueryParam("a", "Y")
                    .putQueryParam("b", "Y")
                    .putQueryParam("e", "")
                    .toUriString()
        then:
            result == "https://coditory.com?a=Y&b=Y&e=&w=W"
    }

    def "should add query params with putQueryMultiParams"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                    .putQueryMultiParams([
                            a : ["X", "X", "Y"],
                            b : ["Y"],
                            e1: [],
                            e2: [""]
                    ])
                    .toUriString()
        then:
            result == "https://coditory.com?a=X&a=X&a=Y&b=Y&w=W&e2="
    }

    def "should add query params with putQueryParams"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                    .putQueryParams([
                            a: "X",
                            b: "Y",
                            e: ""
                    ])
                    .toUriString()
        then:
            result == "https://coditory.com?a=X&b=Y&e=&w=W"
    }

    def "should remove query param by name"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?a=X&a=Y&b=Z")
                    .removeQueryParam("a")
                    .toUriString()
        then:
            result == "https://coditory.com?b=Z"
    }

    def "should remove query param by name and value"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?a=X&a=Y&b=Z")
                    .removeQueryParam("a", "Y")
                    .toUriString()
        then:
            result == "https://coditory.com?a=X&b=Z"
    }

    def "should remove all query params"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?a=X&a=Y&b=Z")
                    .removeQueryParams()
                    .toUriString()
        then:
            result == "https://coditory.com"
    }
}
