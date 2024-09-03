package org.ktorm.support.kingbase

import org.ktorm.expression.*

/**
 * Base interface designed to visit or modify Kingbase expression trees using visitor pattern.
 *
 * For detailed documents, see [SqlExpressionVisitor].
 */
interface KingbaseExpressionVisitor : SqlExpressionVisitor {

    /**
     * Dispatch different type of expression nodes to their specific `visit*` functions. Custom expression types that
     * are unknown to Ktorm will be dispatched to [visitUnknown].
     */
    override fun visit(expr: SqlExpression): SqlExpression = when (expr) {
        is BulkInsertExpression -> visitBulkInsert(expr)
        else -> super.visit(expr)
    }

    /**
     * Function that visits a [BulkInsertExpression].
     */
    fun visitBulkInsert(expr: BulkInsertExpression): BulkInsertExpression {
        val table = visitTable(expr.table)
        val assignments = visitBulkInsertAssignments(expr.assignments)
        val updateAssignments = visitExpressionList(expr.updateAssignments)

        return if (table === expr.table && assignments === expr.assignments && updateAssignments === expr.updateAssignments) {
            expr
        } else {
            expr.copy(table = table, assignments = assignments, updateAssignments = updateAssignments)
        }
    }

    /**
     * Helper function for visiting insert assignments of [BulkInsertExpression].
     */
    fun visitBulkInsertAssignments(assignments: List<List<ColumnAssignmentExpression<*>>>): List<List<ColumnAssignmentExpression<*>>> {
        val result = ArrayList<List<ColumnAssignmentExpression<*>>>()
        var changed = false

        for (row in assignments) {
            val visited = visitExpressionList(row)
            result += visited

            if (visited !== row) {
                changed = true
            }
        }

        return if (changed) result else assignments
    }
}
