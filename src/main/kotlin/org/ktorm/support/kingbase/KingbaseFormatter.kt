package org.ktorm.support.kingbase

import org.ktorm.database.Database
import org.ktorm.expression.*
import org.ktorm.schema.IntSqlType

/**
 * [SqlFormatter] implementation for Kingbase, formatting SQL expressions as strings with their execution arguments.
 */
open class KingbaseFormatter(database: Database, beautifySql: Boolean, indentSize: Int) : SqlFormatter(database, beautifySql, indentSize), KingbaseExpressionVisitor {

    override fun visit(expr: SqlExpression): SqlExpression {
        val result = super<KingbaseExpressionVisitor>.visit(expr)
        check(result === expr) { "SqlFormatter cannot modify the expression tree." }
        return result
    }

    override fun shouldQuote(identifier: String): Boolean = identifier.startsWith('_') || super.shouldQuote(identifier)

    override fun visitQuerySource(expr: QuerySourceExpression): QuerySourceExpression = super<SqlFormatter>.visitQuerySource(expr)

    override fun visitSelect(expr: SelectExpression): SelectExpression {
        super<SqlFormatter>.visitSelect(expr)
        return expr
    }

    override fun visitQuery(expr: QueryExpression): QueryExpression {
        return super<SqlFormatter>.visitQuery(expr)

//        if (expr.offset == null && expr.limit == null) {
//            return super<SqlFormatter>.visitQuery(expr)
//        }
//
//        val offset = expr.offset ?: 0
//        val minRowNum = offset + 1
//        val maxRowNum = expr.limit?.let { offset + it } ?: Int.MAX_VALUE
//
//        val tempTableName = "_t"
//
//        writeKeyword("select * ")
//        newLine(Indentation.SAME)
//        writeKeyword("from (")
//        newLine(Indentation.INNER)
//        writeKeyword("select ")
//        write("${tempTableName.quoted}.*, ")
//        writeKeyword("rownum ")
//        write("${"_rn".quoted} ")
//        newLine(Indentation.SAME)
//        writeKeyword("from ")
//
//        visitQuerySource(
//            when (expr) {
//                is SelectExpression -> expr.copy(tableAlias = tempTableName, offset = null, limit = null)
//                is UnionExpression -> expr.copy(tableAlias = tempTableName, offset = null, limit = null)
//            }
//        )
//
//        newLine(Indentation.SAME)
//        writeKeyword("where rownum <= ?")
//        newLine(Indentation.OUTER)
//        write(") ")
//        newLine(Indentation.SAME)
//        writeKeyword("where ")
//        write("${"_rn".quoted} >= ? ")
//
//        _parameters += ArgumentExpression(maxRowNum, IntSqlType)
//        _parameters += ArgumentExpression(minRowNum, IntSqlType)
//
//        return expr
    }

    override fun writePagination(expr: QueryExpression) {
        newLine(Indentation.SAME)
        writeKeyword("limit ?, ? ")
        _parameters += ArgumentExpression(expr.offset ?: 0, IntSqlType)
        _parameters += ArgumentExpression(expr.limit ?: Int.MAX_VALUE, IntSqlType)
    }

    override fun visitBulkInsert(expr: BulkInsertExpression): BulkInsertExpression {
        writeKeyword("insert into ")
        visitTable(expr.table)
        writeInsertColumnNames(expr.assignments[0].map { it.column })
        writeKeyword("values ")

        for ((i, assignments) in expr.assignments.withIndex()) {
            if (i > 0) {
                removeLastBlank()
                write(", ")
            }
            writeInsertValues(assignments)
        }

        if (expr.updateAssignments.isNotEmpty()) {
            writeKeyword("on duplicate key update ")
            writeColumnAssignments(expr.updateAssignments)
        }

        return expr
    }

    override fun visitUnion(expr: UnionExpression): UnionExpression {
        if (expr.orderBy.isEmpty()) {
            return super<SqlFormatter>.visitUnion(expr)
        }
        writeKeyword("select * ")
        newLine(Indentation.SAME)
        writeKeyword("from ")
        visitQuerySource(expr.copy(orderBy = emptyList(), tableAlias = null))
        newLine(Indentation.SAME)
        writeKeyword("order by ")
        visitExpressionList(expr.orderBy)
        return expr
    }
}
