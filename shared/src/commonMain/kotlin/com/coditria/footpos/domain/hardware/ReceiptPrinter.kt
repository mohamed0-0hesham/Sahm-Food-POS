package com.coditria.footpos.domain.hardware

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.model.Receipt

interface ReceiptPrinter {
    suspend fun print(receipt: Receipt): Result<String>
    suspend fun isReady(): Boolean
}
