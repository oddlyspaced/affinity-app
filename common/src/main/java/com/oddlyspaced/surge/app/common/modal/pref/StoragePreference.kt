package com.oddlyspaced.surge.app.common.modal.pref

/**
 * enum for storage preferences
 */
enum class StoragePreference(val dataType: DataType) {
    PREF_USER_ID(DataType.INT),
}

/**
 * enum for data types of storage preference
 */
enum class DataType {
    BOOLEAN,
    INT,
    STRING,
}