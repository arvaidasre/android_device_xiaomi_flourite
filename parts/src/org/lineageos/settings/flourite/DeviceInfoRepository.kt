/* SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.settings.flourite

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.SystemProperties
import android.os.storage.StorageManager

internal data class DeviceCapacity(val ramBytes: Long?, val storageBytes: Long?)

internal class DeviceInfoRepository(context: Context) {
    private val context = context.applicationContext
    private val marketName = property("ro.product.odm.marketname")
    val variant = resolveDeviceVariant(
        hardwareSku = property("ro.boot.hardware.sku"),
        productSku = property("ro.boot.product.hardware.sku"),
        vendorSku = property("ro.boot.product.vendor.sku"),
        hwc = property("ro.boot.hwc"),
        marketName = marketName,
    )
    val deviceName = variant.deviceName.ifBlank { marketName.ifBlank { Build.MODEL } }

    // No camera query, polling, sysfs writes or hardware wake-up on the overview page.
    fun readCapacity(): DeviceCapacity {
        val ram = runCatching {
            val manager = context.getSystemService(ActivityManager::class.java)
                ?: return@runCatching null
            ActivityManager.MemoryInfo().also(manager::getMemoryInfo).let { info ->
                info.advertisedMem.takeIf { it > 0 } ?: info.totalMem.takeIf { it > 0 }
            }
        }.getOrNull()
        val storage = runCatching {
            context.getSystemService(StorageManager::class.java)
                ?.primaryStorageSize?.takeIf { it > 0 }
        }.getOrNull()
        return DeviceCapacity(ram, storage)
    }

    private fun property(name: String) = runCatching { SystemProperties.get(name) }.getOrDefault("")
}
