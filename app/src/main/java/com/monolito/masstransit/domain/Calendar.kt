package com.monolito.masstransit.domain

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "Calendar")
data class Calendar(@DatabaseField(id = true) val date: String, @DatabaseField val code:String) {
    constructor() : this("", "")  //NOTICE: ORMLite needs a no-args constructor
}
