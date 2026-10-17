package com.example.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.floor
import kotlin.math.max

object CurrencyCalculator {

    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private val usdSymbols = DecimalFormatSymbols(Locale.US)

    fun roundTo(value: Double, decimals: Int = 2): Double {
        if (value.isNaN() || value.isInfinite()) return 0.0
        return BigDecimal(value)
            .setScale(decimals, RoundingMode.HALF_UP)
            .toDouble()
    }

    /**
     * Calcula los montos en USD y VES a partir de la moneda base del producto y la tasa activa.
     * Retorna Pair(precioUsd, precioVes).
     */
    fun convertProductPrice(precioBase: Double, monedaBase: String, tasa: Double): Pair<Double, Double> {
        val safeRate = if (tasa <= 0) 1.0 else tasa
        return if (monedaBase.equals("VES", ignoreCase = true)) {
            val usd = roundTo(precioBase / safeRate, 2)
            val ves = roundTo(precioBase, 2)
            Pair(usd, ves)
        } else {
            val usd = roundTo(precioBase, 2)
            val ves = roundTo(precioBase * safeRate, 2)
            Pair(usd, ves)
        }
    }

    fun usdToVes(usd: Double, tasa: Double): Double {
        val safeRate = if (tasa <= 0) 1.0 else tasa
        return roundTo(usd * safeRate, 2)
    }

    fun vesToUsd(ves: Double, tasa: Double): Double {
        val safeRate = if (tasa <= 0) 1.0 else tasa
        return roundTo(ves / safeRate, 2)
    }

    /**
     * Evalúa expresiones simples en campos de pago, como "20+5" o "10 + 2.5 + 1.25".
     */
    fun evalExpression(input: String): Double {
        val cleaned = input.trim().replace(',', '.')
        if (cleaned.isEmpty()) return 0.0

        return try {
            if (cleaned.contains("+")) {
                val parts = cleaned.split("+")
                parts.sumOf { part ->
                    val trimmed = part.trim()
                    if (trimmed.isNotEmpty()) trimmed.toDouble() else 0.0
                }
            } else {
                cleaned.toDouble()
            }
        } catch (_: Exception) {
            0.0
        }
    }

    data class PaymentCalculation(
        val totalUsd: Double,
        val totalVes: Double,
        val paidUsd: Double,
        val paidVes: Double,
        val totalPaidInUsdEquivalent: Double,
        val totalPaidInVesEquivalent: Double,
        val isFullyPaid: Boolean,
        val remainingUsd: Double,
        val remainingVes: Double,
        val changeUsd: Double,
        val changeVes: Double,
        val suggestedMixedBillsUsd: Int,
        val suggestedMixedChangeVes: Double
    )

    /**
     * Calcula el estado de pago, faltante o vuelto multimoneda exacto.
     */
    fun computePayment(
        totalUsd: Double,
        paidUsd: Double,
        paidVes: Double,
        tasa: Double
    ): PaymentCalculation {
        val safeRate = if (tasa <= 0) 1.0 else tasa
        val totalVes = usdToVes(totalUsd, safeRate)

        val paidVesAsUsd = if (safeRate > 0) paidVes / safeRate else 0.0
        val totalPaidInUsd = paidUsd + paidVesAsUsd
        val totalPaidInVes = (paidUsd * safeRate) + paidVes

        // Tolerancia de 0.009 centavos
        val diffUsd = totalPaidInUsd - totalUsd

        return if (diffUsd >= -0.009) {
            val changeUsd = max(0.0, roundTo(diffUsd, 2))
            val changeVes = max(0.0, roundTo(changeUsd * safeRate, 2))

            // Sugerencia mixta: billetes enteros en USD y el resto en VES
            val mixedBillsUsd = floor(changeUsd).toInt()
            val remainderUsd = changeUsd - mixedBillsUsd
            val mixedChangeVes = roundTo(remainderUsd * safeRate, 2)

            PaymentCalculation(
                totalUsd = roundTo(totalUsd, 2),
                totalVes = totalVes,
                paidUsd = roundTo(paidUsd, 2),
                paidVes = roundTo(paidVes, 2),
                totalPaidInUsdEquivalent = roundTo(totalPaidInUsd, 2),
                totalPaidInVesEquivalent = roundTo(totalPaidInVes, 2),
                isFullyPaid = true,
                remainingUsd = 0.0,
                remainingVes = 0.0,
                changeUsd = changeUsd,
                changeVes = changeVes,
                suggestedMixedBillsUsd = mixedBillsUsd,
                suggestedMixedChangeVes = mixedChangeVes
            )
        } else {
            val deficitUsd = roundTo(-diffUsd, 2)
            val deficitVes = roundTo(deficitUsd * safeRate, 2)

            PaymentCalculation(
                totalUsd = roundTo(totalUsd, 2),
                totalVes = totalVes,
                paidUsd = roundTo(paidUsd, 2),
                paidVes = roundTo(paidVes, 2),
                totalPaidInUsdEquivalent = roundTo(totalPaidInUsd, 2),
                totalPaidInVesEquivalent = roundTo(totalPaidInVes, 2),
                isFullyPaid = false,
                remainingUsd = deficitUsd,
                remainingVes = deficitVes,
                changeUsd = 0.0,
                changeVes = 0.0,
                suggestedMixedBillsUsd = 0,
                suggestedMixedChangeVes = 0.0
            )
        }
    }

    fun formatUsd(amount: Double): String {
        val df = DecimalFormat("#,##0.00", usdSymbols)
        return "$" + df.format(roundTo(amount, 2))
    }

    fun formatVes(amount: Double): String {
        val df = DecimalFormat("#,##0.00", symbols)
        return "Bs. " + df.format(roundTo(amount, 2))
    }

    fun formatRate(rate: Double): String {
        val df = DecimalFormat("#,##0.00", symbols)
        return df.format(rate)
    }
}
