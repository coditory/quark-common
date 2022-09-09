package com.coditory.quark.common.throwable

import com.coditory.quark.common.test.SimulatedCheckedException
import com.coditory.quark.common.test.SimulatedException
import groovy.transform.Synchronized
import spock.lang.Specification

class ThrowablesSpec extends Specification {
    static Throwable root = new RuntimeException("root")
    static Throwable exceptionA = new RuntimeException("A", root)
    static Throwable exceptionB = new RuntimeException("B", exceptionA)
    static Throwable exceptionWithCycleA = new RuntimeException("cyclic-A")
    static Throwable exceptionWithCycleB = new RuntimeException("cyclic-B", exceptionWithCycleA)
    static Throwable exceptionWithCycleC = new RuntimeException("cyclic-C", exceptionWithCycleB)

    static {
        exceptionWithCycleA.setCause(exceptionWithCycleC)
    }

    def "getRootCause(e) - should extract root cause"() {
        when:
        Throwable result = Throwables.getRootCause(input)
        then:
        result == expected
        where:
        input               || expected
        root                || root
        exceptionA          || root
        exceptionB          || root
        exceptionWithCycleA || exceptionWithCycleB
    }

    def "throwCause(e) - should throw cause exception"() {
        when:
        Throwables.throwCause(exceptionB)
        then:
        Throwable result = thrown(Throwable)
        result == exceptionA
    }

    def "throwCause(e) - should not throw cause exception when there is none"() {
        when:
        Throwables.throwCause(root)
        then:
        noExceptionThrown()
    }

    def "getRootCauseOfType(e, type) - should extract root cause of specific type or null"() {
        given:
        Throwable root = new RuntimeException("root")
        Throwable exceptionA = new IllegalArgumentException("A", root)
        Throwable exceptionB = new IllegalArgumentException("B", exceptionA)

        when:
        Throwable result = Throwables.getRootCauseOfType(exceptionB, IllegalArgumentException)
        then:
        result == exceptionA

        when:
        result = Throwables.getRootCauseOfType(exceptionB, IllegalStateException)
        then:
        result == null
    }

    def "getStackTrace(e) - should create string with stacktrace"() {
        when:
        String result = Throwables.getStackTrace(input)
        then:
        containsInOrder(result, expected)
        where:
        input               || expected
        root                || ["RuntimeException: root"]
        exceptionA          || ["RuntimeException: A", "RuntimeException: root"]
        exceptionB          || ["RuntimeException: B", "RuntimeException: A", "RuntimeException: root"]
        exceptionWithCycleA || ["RuntimeException: cyclic-A", "RuntimeException: cyclic-C", "RuntimeException: cyclic-B", "CIRCULAR REFERENCE"]
    }

    def "sneakyThrow(supplier) - should sneaky throw a supplier"() {
        given:
        SimulatedException simulatedException = new SimulatedException()
        SimulatedCheckedException simulatedCheckedException = new SimulatedCheckedException()

        when:
        String result = Throwables.sneakyThrow({ throw simulatedCheckedException } as ThrowingSupplier<String>)
        then:
        RuntimeException e = thrown(RuntimeException)
        e.cause == simulatedCheckedException
        result == null

        when:
        result = Throwables.sneakyThrow({ throw simulatedException } as ThrowingSupplier<String>)
        then:
        e = thrown(RuntimeException)
        e == simulatedException
        result == null

        when:
        result = Throwables.sneakyThrow({ "abc" } as ThrowingSupplier<String>)
        then:
        noExceptionThrown()
        result == "abc"
    }

    def "sneakyThrow(runnable) - should sneaky throw a runnable"() {
        given:
        SimulatedException simulatedException = new SimulatedException()
        SimulatedCheckedException simulatedCheckedException = new SimulatedCheckedException()

        when:
        Throwables.sneakyThrow({ throw simulatedCheckedException } as ThrowingRunnable)
        then:
        RuntimeException e = thrown(RuntimeException)
        e.cause == simulatedCheckedException

        when:
        Throwables.sneakyThrow({ throw simulatedException } as ThrowingRunnable)
        then:
        e = thrown(RuntimeException)
        e == simulatedException

        when:
        Throwables.sneakyThrow({ "abc" } as ThrowingRunnable)
        then:
        noExceptionThrown()
    }

    def "onErrorDefault(supplier, defaultValue) - should return default value on exception"() {
        given:
        String defaultValue = "default"
        when:
        String result = Throwables.onAnyErrorDefault({ throw new SimulatedException() }, defaultValue)
        then:
        result == defaultValue
        when:
        result = Throwables.onAnyErrorDefault({ "abc" }, defaultValue)
        then:
        result == "abc"
    }

    def "onErrorGet(supplier, defaultValueSupplier) - should return default value from supplier on exception"() {
        given:
        String defaultValue = "default"
        and:
        int executions = 0
        ThrowingSupplier<String> defaultValueSupplier = { executions++; defaultValue }
        when:
        String result = Throwables.onAnyErrorGet({ throw new SimulatedException() }, defaultValueSupplier)
        then:
        result == defaultValue
        executions == 1
        when:
        result = Throwables.onAnyErrorGet({ "abc" }, defaultValueSupplier)
        then:
        result == "abc"
        executions == 1
    }

    def "onErrorNull(supplier) - should return null on exception"() {
        when:
        String result = Throwables.onAnyErrorNull({ throw new SimulatedException() })
        then:
        result == null
        when:
        result = Throwables.onAnyErrorNull({ "abc" })
        then:
        result == "abc"
    }

    def "onErrorEmpty(supplier) - should return empty optional on exception"() {
        when:
        Optional<String> result = Throwables.onAnyErrorEmpty({ throw new SimulatedException() })
        then:
        result.isEmpty()
        when:
        result = Throwables.onAnyErrorEmpty({ "abc" })
        then:
        result == Optional.of("abc")
    }

    private boolean containsInOrder(String text, List<String> parts) {
        if (parts.isEmpty()) {
            return true
        }
        String sliced = text
        for (int i = 0; i < parts.size(); ++i) {
            String part = parts.get(i)
            int index = sliced.indexOf(part)
            assert index >= 0: "Part not found: " + part + ". Got: " + text
            sliced = sliced.substring(index + part.length())
        }
        return true
    }
}
