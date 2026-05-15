package com.coditria.footpos.domain.usecase

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.hardware.ReceiptPrinter
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.Receipt

class PrintReceiptUseCase(private val printer: ReceiptPrinter) {
    suspend operator fun invoke(order: Order): Result<String> =
        printer.print(Receipt.fromOrder(order))
}
