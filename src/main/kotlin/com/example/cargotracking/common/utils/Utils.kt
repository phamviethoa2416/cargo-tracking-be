package com.example.cargotracking.common.utils

import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

fun Any.getPropValue(fieldName: String): Any? =
    this::class.memberProperties
        .find { it.name == fieldName }
        ?.apply { isAccessible = true }
        ?.call(this)