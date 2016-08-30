package thecat.tools.sqlite

import groovy.json.JsonOutput
import groovy.sql.Sql;

class JsonConverter {

    static main(args) {
        def keyValueArgs = args.collect { def token = it.split('='); [key: token[0], value: token.length > 1 ? token[1] : null] }
        keyValueArgs.findAll { it.key in ['-all', '-pretty'] }.each { it.value = true }

        if (	(keyValueArgs.size() < 1) ||
                (keyValueArgs.findAll { it.key in ['-db'] }.size() != 1) ||
                ((keyValueArgs.findAll { it.key in ['-table'] }.size() != 1) &&
                        (keyValueArgs.findAll { it.key in ['-all'] }.size() != 1) &&
                        (keyValueArgs.findAll { it.key in ['-sql'] }.size() != 1))) {
            showUsage()
            return
        }

        def dbFileName = keyValueArgs.find { it.key == '-db'}?.value
        def table = keyValueArgs.find { it.key == '-table'}?.value ?: ''
        def all = keyValueArgs.find { it.key == '-all'}?.value ?: false
        def sql = keyValueArgs.find { it.key == '-sql'}?.value ?: ''
        def fields = keyValueArgs.find { it.key == '-fields' }?.value ?: ''
        def pretty = keyValueArgs.find { it.key == '-pretty' }?.value ?: false

        if (!new File(dbFileName).exists()) {
            println "the source db name (${dbFileName}) doesn't exists!!!"
            return
        }

        def db = Sql.newInstance('jdbc:sqlite:' + dbFileName, '', '', 'org.sqlite.JDBC')

        db.connection.autoCommit = false

        if (all) {
            def data = [:]
            getDbTables(db).each { currentTable ->
                data += exportToJSON(db, currentTable)
            }

            if (pretty) {
                println JsonOutput.prettyPrint(JsonOutput.toJson(data))
            } else {
                println JsonOutput.toJson(data)
            }

        } else {
            def data = exportToJSON(db, table, sql, fields)

            if (pretty) {
                println JsonOutput.prettyPrint(JsonOutput.toJson(data))
            } else {
                println JsonOutput.toJson(data)
            }
        }

        db.close();
    }

    static def exportToJSON(db, table, sql = '', fields = '') {
        def values = []

        if (!sql) {
            sql = 'SELECT * FROM ' + table
        }

        if (!table) {
            table = 'Please specify a table name'
        }

        if (fields) {
            fields = fields.split(',')
        }

        db.rows(sql).each { row ->

            if (!fields) {
                fields = row.keySet()
            }

            def record = [:]

            fields.each { field ->
                record.put(field, row[field])
            }

            values += record
        }

        def map = [:]
        map.put(table, values)

        map
    }

    def static final String SQL_TABLE_LIST = 'SELECT * FROM sqlite_master WHERE type="table"';

    def static final String[] DB_TABLE_TO_EXCLUDE_FROM_SCAN = [ 'sqlite_sequence', 'android_metadata' ];

    static def getDbTables(db) {
        def tableList = []
        db.rows(SQL_TABLE_LIST).each { row ->
            if (!DB_TABLE_TO_EXCLUDE_FROM_SCAN.contains(row.tbl_name)) {
                tableList += row.tbl_name
            }
        }

        tableList
    }

    static def showUsage() {
        println """
Export a Sqlite database in a json format.
Please specify the following parameters:
\t-db=<source-db> [-table=<table-name>|-all|-sql=<sql-stmt>] [-fields=comma separated fields] [-pretty]
"""
    }
}
