package com.coditria.footpos.data.hardware

import com.coditria.footpos.core.common.AppError
import com.coditria.footpos.core.common.Logger
import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.hardware.ReceiptPrinter
import com.coditria.footpos.domain.model.Receipt
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/**
 * Mock printer that produces a formatted receipt string instead of writing to hardware.
 * Returns the printed text so the UI can display it in a dialog (per SAHM_POS_SPEC §9).
 * Fails ~5% of the time to exercise the error-recovery path.
 */
class MockReceiptPrinter(
    private val logger: Logger,
    private val failureRate: Float = 0.05f,
    private val latencyMs: LongRange = 600L..1200L,
) : ReceiptPrinter {

    override suspend fun print(receipt: Receipt): Result<String> {
        delay(Random.nextLong(latencyMs.first, latencyMs.last).milliseconds)
        if (Random.nextFloat() < failureRate) {
            logger.warn("Mock printer reported offline")
            return Result.Failure(AppError.HardwareError("Printer offline"))
        }
        val formatted = format(receipt)
        logger.info("Printed receipt:\n$formatted")
        return Result.Success(formatted)
    }

    override suspend fun isReady(): Boolean = true

    private fun format(receipt: Receipt): String = buildString {
        val width = 40
        appendLine("=".repeat(width))
        appendLine(receipt.merchantName.center(width))
        appendLine("Order ${receipt.orderId.value.takeLast(8)}".center(width))
        appendLine("=".repeat(width))
        receipt.items.forEach { line ->
            val left = "${line.name} x${line.quantity}".take(width - 11)
            val right = line.total.formatAmount().padStart(width - left.length - 1)
            appendLine("$left $right")
        }
        appendLine("-".repeat(width))
        appendLine(row("Subtotal", receipt.subtotal.formatAmount(), width))
        appendLine(row("Tax", receipt.tax.formatAmount(), width))
        if (!receipt.discount.isZero()) {
            appendLine(row("Discount", "-" + receipt.discount.formatAmount(), width))
        }
        appendLine("=".repeat(width))
        appendLine(row("TOTAL", receipt.total.format(), width))
        receipt.payment?.let { payment ->
            appendLine(row(payment.method.name.lowercase().replaceFirstChar { it.uppercase() }, payment.amountTendered.formatAmount(), width))
            val change = payment.changeDue(receipt.total)
            if (change.isPositive()) appendLine(row("Change", change.formatAmount(), width))
        }
        appendLine("=".repeat(width))
        appendLine("Thank you".center(width))
    }

    private fun row(label: String, value: String, width: Int): String {
        val padded = value.padStart(width - label.length)
        return "$label$padded"
    }

    private fun String.center(width: Int): String {
        if (length >= width) return take(width)
        val padding = (width - length) / 2
        return " ".repeat(padding) + this
    }
}
