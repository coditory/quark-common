package com.coditory.quark.common.net;

import com.coditory.quark.common.throwable.ThrowingSupplier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public final class Hosts {
    private Hosts() {
        throw new IllegalStateException("This is a utility class and cannot be instantiated");
    }

    public static String getLocalHostName() {
        return LocalHostNameResolver.LOCAL_HOST_NAME;
    }

    public static String getLocalHostAddress() {
        return LocalHostAddressResolver.LOCAL_HOST_ADDRESS;
    }

    /**
     * Java Language Specification specifies that a class initialization occurs
     * the first time its methods or fields are used.
     * Nested static class can be used to implement lazy initialization.
     *
     * @link https://docs.oracle.com/javase/specs/jls/se7/html/jls-12.html#jls-12.4.2
     */
    private static class LocalHostNameResolver {
        private static final Predicate<String> IP_ADDRESS = Pattern.compile("(\\d{1,3}\\.){3}\\d{1,3}").asPredicate();
        static final String LOCAL_HOST_NAME = ResolverWithOsCommandFallback.resolveWithOsFallback("local host name", LocalHostNameResolver::resolveLocalHostNameUsingJVM, "hostname -f");

        private LocalHostNameResolver() {
            throw new IllegalStateException("This is a utility class and cannot be instantiated");
        }

        private static String resolveLocalHostNameUsingJVM() throws UnknownHostException {
            InetAddress localhost = InetAddress.getLocalHost();
            String canonicalHostName = localhost.getCanonicalHostName();
            String hostName = localhost.getHostName();
            return IP_ADDRESS.test(canonicalHostName)
                    ? hostName
                    : canonicalHostName;
        }
    }

    private static class LocalHostAddressResolver {
        static final String LOCAL_HOST_ADDRESS = ResolverWithOsCommandFallback.resolveWithOsFallback("local host address", LocalHostAddressResolver::resolveLocalHostAddress, "hostname -i");

        private LocalHostAddressResolver() {
            throw new IllegalStateException("This is a utility class and cannot be instantiated");
        }

        private static String resolveLocalHostAddress() throws UnknownHostException {
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.getHostAddress();
        }
    }

    private static class ResolverWithOsCommandFallback {
        static String resolveWithOsFallback(String name, ThrowingSupplier<String> jvmSupplier, String systemCommand) {
            return new ResolverWithOsCommandFallback(name, jvmSupplier, systemCommand)
                    .resolve();
        }

        private final String name;
        private final ThrowingSupplier<String> jvmSupplier;
        private final String[] systemCommand;

        ResolverWithOsCommandFallback(String name, ThrowingSupplier<String> jvmSupplier, String systemCommand) {
            this.name = requireNonNull(name);
            this.jvmSupplier = requireNonNull(jvmSupplier);
            this.systemCommand = systemCommand.split(" ");
        }

        String resolve() {
            try {
                return resolveUsingJVM();
            } catch (Exception exception) {
                return resolveUsingOs();
            }
        }

        private String resolveUsingJVM() throws Exception {
            return jvmSupplier.get();
        }

        private String resolveUsingOs() {
            try (
                    InputStream is = Runtime.getRuntime()
                            .exec(systemCommand)
                            .getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is))
            ) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                return builder.toString().trim();
            } catch (IOException exception) {
                String cmd = String.join(" ", systemCommand);
                throw new RuntimeException(
                        "Could not resolve " + name + ". Your OS does not support command: " + cmd,
                        exception
                );
            }
        }
    }
}
