package com.plooh.adssi.dial.data.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class JDBCUrlTest {

    @Test
    void roundtrip() {
        var url = "jdbc:postgresql://localhost:5432/dialdatadb?currentSchema=myschema";
        var actual = JDBCUrl.parse(url).get();
        assertThat(actual.getHostname()).isEqualTo("localhost:5432");
        assertThat(actual.getDatabase()).isEqualTo("dialdatadb");
        assertThat(actual.getQuery()).isEqualTo("currentSchema=myschema");
        assertThat(actual.getProtocol()).isEqualTo("jdbc:postgresql:");
    }

    @ParameterizedTest
    @MethodSource("testData")
    void add_tests(String input, JDBCUrl expected) {
        assertThat(JDBCUrl.parse(input)).contains(expected);
    }

    @ParameterizedTest
    @MethodSource("failingData")
    void shouldFail(String input) {
        assertThat(JDBCUrl.parse(input)).isEmpty();
    }

    static Stream<Arguments> testData() {
        return Stream.of(
            arguments("jdbc:postgresql://localhost:5432/dialdatadb?currentSchema=myschema",
                new JDBCUrl("jdbc:postgresql:", "localhost:5432", "dialdatadb", "currentSchema=myschema")),
            arguments("jdbc:postgresql://localhost:5432/dialdatadb",
                new JDBCUrl("jdbc:postgresql:","localhost:5432", "dialdatadb",  null)),
            arguments("jdbc:postgresql://localhost/dialdatadb",
                new JDBCUrl("jdbc:postgresql:", "localhost", "dialdatadb", null))
        );
    }

    static Stream<Arguments> failingData() {
        return Stream.of(null, "", "jdbc:postgresql:", "jdbc:postgresql://localhost:5432").map(Arguments::arguments);
    }

}
