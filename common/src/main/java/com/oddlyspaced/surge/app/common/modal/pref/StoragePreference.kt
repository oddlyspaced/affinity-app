package com.oddlyspaced.surge.app.common.modal.pref

enum class StoragePreference(val dataType: DataType) {
    PREF_USER_ID(DataType.INT),
}

enum class DataType {
    BOOLEAN,
    INT,
    STRING,
}