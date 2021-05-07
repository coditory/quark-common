package com.coditory.quark.common.util.string


import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.common.util.Strings.unwrap
import static com.coditory.quark.common.util.Strings.wrap
import static com.coditory.quark.common.util.Strings.wrapNonEmpty

class StringsWrapSpec extends Specification {
    @Unroll
    def "wrap(#value, #c) == '#expected'"() {
        expect:
            wrap(value, c as char) == expected
        where:
            value | c    || expected
            "abc" | "'"  || "'abc'"
            ""    | "'"  || "''"
            " "   | "\"" || "\" \""
    }

    @Unroll
    def "wrapNonEmpty(#value, #c) == '#expected'"() {
        expect:
            wrapNonEmpty(value, c as char) == expected
        where:
            value | c    || expected
            "abc" | "'"  || "'abc'"
            ""    | "'"  || ""
            " "   | "\"" || "\" \""
    }

    @Unroll
    def "unwrap(#value, #c) == '#expected'"() {
        expect:
            unwrap(value, c as char) == expected
        where:
            value   | c    || expected
            "'abc'" | "'"  || "abc"
            "'abc'" | "\"" || "'abc'"
            "''"    | "'"  || ""
            "\" \"" | "\"" || " "
    }
}
