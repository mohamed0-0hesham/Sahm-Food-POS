package com.coditria.footpos.core.database

import com.coditria.footpos.database.PosDatabase

object DatabaseFactory {
    fun create(driverFactory: DatabaseDriverFactory): PosDatabase =
        PosDatabase(driverFactory.createDriver())
}
