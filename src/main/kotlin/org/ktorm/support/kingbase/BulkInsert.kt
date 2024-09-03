package org.ktorm.support.kingbase

import org.ktorm.database.Database
import org.ktorm.dsl.AliasRemover
import org.ktorm.dsl.AssignmentsBuilder
import org.ktorm.dsl.KtormDsl
import org.ktorm.expression.ColumnAssignmentExpression
import org.ktorm.expression.SqlExpression
import org.ktorm.expression.TableExpression
import org.ktorm.schema.BaseTable

data class BulkInsertExpression(
    val table: TableExpression,
    val assignments: List<List<ColumnAssignmentExpression<*>>>,
    val updateAssignments: List<ColumnAssignmentExpression<*>> = emptyList(),
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

fun <T : BaseTable<*>> Database.bulkInsert(table: T, block: BulkInsertStatementBuilder<T>.() -> Unit): Int {
    val builder = BulkInsertStatementBuilder(table).apply(block)
    if (builder.assignments.isEmpty()) {
        throw IllegalArgumentException("There are no items in the bulk operation.")
    }
    for (assignments in builder.assignments) {
        if (assignments.isEmpty()) {
            throw IllegalArgumentException("There are no columns to insert in the statement.")
        }
    }

    val expression = dialect.createExpressionVisitor(AliasRemover).visit(BulkInsertExpression(table.asExpression(), builder.assignments))

    return executeUpdate(expression)
}

@KtormDsl
open class BulkInsertStatementBuilder<T : BaseTable<*>>(internal val table: T) {
    internal val assignments = ArrayList<List<ColumnAssignmentExpression<*>>>()

    fun item(block: AssignmentsBuilder.(T) -> Unit) {
        val builder = KingbaseAssignmentsBuilder()
        builder.block(table)

        if (assignments.isEmpty() || assignments[0].map { it.column.name } == builder.assignments.map { it.column.name }) {
            assignments += builder.assignments
        } else {
            throw IllegalArgumentException("Every item in a batch operation must be the same.")
        }
    }
}

@KtormDsl
open class KingbaseAssignmentsBuilder : AssignmentsBuilder() {
    internal val assignments: List<ColumnAssignmentExpression<*>> get() = _assignments
}