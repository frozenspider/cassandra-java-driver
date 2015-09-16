/*
 *      Copyright (C) 2012-2015 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.datastax.driver.core;

import java.util.Collection;
import java.util.Collections;

import org.testng.annotations.Test;

import com.datastax.driver.core.utils.CassandraVersion;

import static com.datastax.driver.core.Assertions.assertThat;
import static com.datastax.driver.core.DataType.*;
import static com.datastax.driver.core.TableOrView.Order.DESC;

@CassandraVersion(major = 3)
public class MaterializedViewMetadataTest extends CCMBridge.PerClassSingleNodeCluster {

    @Test(groups = "short")
    public void should_create_view_metadata() {

        // given
        String createTable = String.format(
            "CREATE TABLE %s.scores("
                + "user TEXT,"
                + "game TEXT,"
                + "year INT,"
                + "month INT,"
                + "day INT,"
                + "score INT,"
                + "PRIMARY KEY (user, game, year, month, day)"
                + ")",
            keyspace);
        String createMV = String.format(
            "CREATE MATERIALIZED VIEW %s.monthlyhigh AS "
                + "SELECT user FROM %s.scores "
                + "WHERE game IS NOT NULL AND year IS NOT NULL AND month IS NOT NULL AND score IS NOT NULL AND user IS NOT NULL AND day IS NOT NULL "
                + "PRIMARY KEY ((game, year, month), score, user, day) "
                + "WITH CLUSTERING ORDER BY (score DESC)",
            keyspace, keyspace);

        // when
        session.execute(createTable);
        session.execute(createMV);

        // then
        TableMetadata table = cluster.getMetadata().getKeyspace(keyspace).getTable("scores");
        MaterializedViewMetadata mv = cluster.getMetadata().getKeyspace(keyspace).getMaterializedView("monthlyhigh");

        assertThat(table).isNotNull().hasName("scores").hasMaterializedView(mv).hasNumberOfColumns(6);
        assertThat(table.getColumns().get(0)).isNotNull().hasName("user").isPartitionKey();
        assertThat(table.getColumns().get(1)).isNotNull().hasName("game").isClusteringColumn();
        assertThat(table.getColumns().get(2)).isNotNull().hasName("year").isClusteringColumn();
        assertThat(table.getColumns().get(3)).isNotNull().hasName("month").isClusteringColumn();
        assertThat(table.getColumns().get(4)).isNotNull().hasName("day").isClusteringColumn();
        assertThat(table.getColumns().get(5)).isNotNull().hasName("score").isRegularColumn();

        assertThat(mv).isNotNull().hasName("monthlyhigh").hasBaseTable(table).hasNumberOfColumns(6).isEqualTo(table.getView("monthlyhigh"));
        assertThat(mv.getColumns().get(0)).isNotNull().hasName("game").isPartitionKey();
        assertThat(mv.getColumns().get(1)).isNotNull().hasName("year").isPartitionKey();
        assertThat(mv.getColumns().get(2)).isNotNull().hasName("month").isPartitionKey();
        assertThat(mv.getColumns().get(3)).isNotNull().hasName("score").isClusteringColumn().hasClusteringOrder(DESC);
        assertThat(mv.getColumns().get(4)).isNotNull().hasName("user").isClusteringColumn();
        assertThat(mv.getColumns().get(5)).isNotNull().hasName("day").isClusteringColumn();

        assertThat(mv.asCQLQuery(false)).contains(createMV);

    }

    @Test(groups = "short")
    public void should_create_view_metadata_with_case_sensitive_column_names() {
        // given
        String createTable = String.format(
            "CREATE TABLE %s.t1 ("
                + "\"theKey\" int, "
                + "\"theClustering\" int, "
                + "\"theValue\" int, "
                + "PRIMARY KEY (\"theKey\", \"theClustering\"))",
            keyspace);
        String createMV = String.format(
            "CREATE MATERIALIZED VIEW %s.mv1 AS "
                + "SELECT \"theKey\", \"theClustering\", \"theValue\" "
                + "FROM %s.t1 "
                + "WHERE \"theKey\" IS NOT NULL AND \"theClustering\" IS NOT NULL AND \"theValue\" IS NOT NULL "
                + "PRIMARY KEY (\"theKey\", \"theClustering\")",
            keyspace, keyspace);
        // when
        session.execute(createTable);
        session.execute(createMV);
        // then
        TableMetadata table = cluster.getMetadata().getKeyspace(keyspace).getTable("t1");
        MaterializedViewMetadata mv = cluster.getMetadata().getKeyspace(keyspace).getMaterializedView("mv1");
        assertThat(table).isNotNull().hasName("t1").hasMaterializedView(mv).hasNumberOfColumns(3);
        assertThat(table.getColumns().get(0)).isNotNull().hasName("theKey").isPartitionKey().hasType(cint());
        assertThat(table.getColumns().get(1)).isNotNull().hasName("theClustering").isClusteringColumn().hasType(cint());
        assertThat(table.getColumns().get(2)).isNotNull().hasName("theValue").isRegularColumn().hasType(cint());
        assertThat(mv).isNotNull().hasName("mv1").hasBaseTable(table).hasNumberOfColumns(3);
        assertThat(mv.getColumns().get(0)).isNotNull().hasName("theKey").isPartitionKey().hasType(cint());
        assertThat(mv.getColumns().get(1)).isNotNull().hasName("theClustering").isClusteringColumn().hasType(cint());
        assertThat(mv.getColumns().get(2)).isNotNull().hasName("theValue").isRegularColumn().hasType(cint());
    }

    @Override
    protected Collection<String> getTableDefinitions() {
        return Collections.emptyList();
    }
}