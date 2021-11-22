package com.plooh.adssi.dial.data.dto;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class JDBCUrl {

    // Postgres Schema:  jdbc:postgresql://host:port/database?XXXXX
    private final String protocol;
    private final String hostname;
    private final String database;
    private final String query;

    public static Optional<JDBCUrl> parse(String connectionUrl){
        try {
            var split1 = connectionUrl.split("//");

            var protocol = split1[0];
            var split2 = split1[1].split("/");

            var hostname = split2[0];

            var databaseQuery = split2[1].split("\\?");
            var database = databaseQuery[0];
            String query = null;
            if ( databaseQuery.length > 1 ){
                query = databaseQuery[1];
            }

            return Optional.of(new JDBCUrl(protocol, hostname, database, query));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public String toString() {
        return protocol + "//" + hostname + "/" + database + ( StringUtils.isBlank(query) ? "" : "?"+query );
    }

}
