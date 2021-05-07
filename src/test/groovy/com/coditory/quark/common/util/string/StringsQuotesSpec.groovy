package com.coditory.quark.common.util.string


import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.common.util.Strings.isQuoted
import static com.coditory.quark.common.util.Strings.isSingleQuoted
import static com.coditory.quark.common.util.Strings.quote
import static com.coditory.quark.common.util.Strings.singleQuote
import static com.coditory.quark.common.util.Strings.unquote
import static com.coditory.quark.common.util.Strings.unquoteSingle

class StringsQuotesSpec extends Specification {
    @Unroll
    def "quote(#value) == #expected"() {
        expect:
            quote(value) == expected
        where:
            value            || expected
            ""               || "\"\""
            "ab cd"          || "\"ab cd\""
            "a\"bc\"efg"     || "\"a\\\"bc\\\"efg\""
            "a\\\"bc\\\"efg" || "\"a\\\\\"bc\\\\\"efg\""
            "a'bc'efg"       || "\"a'bc'efg\""
    }

    @Unroll
    def "unquote(#value) == #expected"() {
        expect:
            unquote(value) == expected
        where:
            value                    || expected
            "\""                     || "\""
            "abc"                    || "abc"
            "\"\""                   || ""
            "\"ab cd\""              || "ab cd"
            "\"a\\\"bc\\\"efg\""     || "a\"bc\"efg"
            "\"a\\\\\"bc\\\\\"efg\"" || "a\\\"bc\\\"efg"
            "\"a'bc'efg\""           || "a'bc'efg"
    }

    @Unroll
    def "quoteSingle(#value) == #expected"() {
        expect:
            singleQuote(value) == expected
        where:
            value          || expected
            ""             || "''"
            "ab cd"        || "'ab cd'"
            "a'bc'efg"     || "'a\\'bc\\'efg'"
            "a\\'bc\\'efg" || "'a\\\\'bc\\\\'efg'"
            "a\"bc\"efg"   || "'a\"bc\"efg\'"
    }

    @Unroll
    def "unquoteSingle(#value) == #expected"() {
        expect:
            unquoteSingle(value) == expected
        where:
            value                || expected
            "'"                  || "'"
            "abc"                || "abc"
            "''"                 || ""
            "'ab cd'"            || "ab cd"
            "'a\\'bc\\'efg'"     || "a'bc'efg"
            "'a\\\\'bc\\\\'efg'" || "a\\'bc\\'efg"
            "'a\"bc\"efg\'"      || "a\"bc\"efg"
    }

    @Unroll
    def "isQuoted(#value) == #expected"() {
        expect:
            isQuoted(value) == expected
        where:
            value     || expected
            "\""      || false
            "\"\""    || true
            "''"      || false
            " \"\""   || false
            "\"abc\"" || true
            "\"abc"   || false
    }

    @Unroll
    def "isSingleQuoted(#value) == #expected"() {
        expect:
            isSingleQuoted(value) == expected
        where:
            value   || expected
            "'"     || false
            "''"    || true
            "\"\""  || false
            " ''"   || false
            "'abc'" || true
            "'abc"  || false
    }
}
