package org.ktorm.support.kingbase

import org.ktorm.database.Database
import org.ktorm.database.SqlDialect
import org.ktorm.expression.SqlExpressionVisitor
import org.ktorm.expression.SqlExpressionVisitorInterceptor
import org.ktorm.expression.SqlFormatter
import org.ktorm.expression.newVisitorInstance

/**
 * [SqlDialect] implementation for Kingbase database.
 */
open class KingbaseDialect : SqlDialect {

    override fun createSqlFormatter(database: Database, beautifySql: Boolean, indentSize: Int): SqlFormatter = KingbaseFormatter(database, beautifySql, indentSize)

    override fun createExpressionVisitor(interceptor: SqlExpressionVisitorInterceptor): SqlExpressionVisitor =
        KingbaseExpressionVisitor::class.newVisitorInstance(interceptor)

}
