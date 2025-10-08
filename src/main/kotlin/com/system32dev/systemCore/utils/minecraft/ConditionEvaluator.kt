package com.system32dev.systemCore.utils.minecraft

import org.bukkit.entity.Player

object ConditionEvaluator {

    private val operatorRegex = Regex("""(==|!=|<=|>=|<|>)""")

    var parseLeft: (Player, String) -> String = { _, it -> it }
    var parseRight: (Player, String) -> String = { _, it -> it }
    var parse: (Player, String) -> String = { _, it -> it }

    /**
     * Evaluates a single simple expression in the form of "left operator right".
     * Supported operators: ==, !=, <, <=, >, >=
     */
    private fun evaluateSimple(expression: String, player: Player): Boolean {
        return try {
            val cleaned = expression.replace("\\s+".toRegex(), "")
            val match = operatorRegex.find(cleaned) ?: return false

            val operator = match.value
            val parts = expression.split(operator).map { it.trim() }
            if (parts.size != 2) return false

            val left = parse(player, parseLeft(player, parts[0]))
            val right = parse(player, parseRight(player, parts[1]))

            when (operator) {
                "==" -> left == right
                "!=" -> left != right
                "<", "<=", ">", ">=" -> {
                    val lNum = left.toDoubleOrNull()
                    val rNum = right.toDoubleOrNull()
                    if (lNum == null || rNum == null) return false
                    when (operator) {
                        "<"  -> lNum < rNum
                        "<=" -> lNum <= rNum
                        ">"  -> lNum > rNum
                        ">=" -> lNum >= rNum
                        else -> false
                    }
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Evaluates an expression that may include && (AND) and || (OR).
     * Example: "%player_health% > 5 && %player_level% >= 10 || %player_name% == Steve"
     */
    fun evaluate(expression: String, player: Player): Boolean {
        val orParts = expression.split("||")
        return orParts.any { orExpr ->
            val andParts = orExpr.split("&&")
            andParts.all { andExpr ->
                evaluateSimple(andExpr.trim(), player)
            }
        }
    }

    /**
     * Evaluates multiple independent expressions (all must be true).
     */
    fun evaluateAll(expressions: Iterable<String>, player: Player): Boolean {
        return expressions.all { evaluate(it, player) }
    }

    /**
     * Evaluates multiple independent expressions (at least one must be true).
     */
    fun evaluateAny(expressions: Iterable<String>, player: Player): Boolean {
        return expressions.any { evaluate(it, player) }
    }
}
