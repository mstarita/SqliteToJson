# SqliteToJson
Command line tool for converting a Sqlite database to a Json.

You can specify the following parameters:
-db=<source-db> - is a Sqlite database
-table=<table-name> - Table name to export
-all - Export all the tables
-sql=<sql-stmt> - Export the result of a SQL statement
-fields=comma separated fields - Export only the specified fields for -table or -sql export
-pretty - Render the output in a human readable format

The tool generate the Json to standard output.

