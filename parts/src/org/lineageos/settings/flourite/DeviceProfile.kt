/* SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.settings.flourite

internal enum class DeviceVariant(val deviceName: String, val batteryMah: Int?) {
    POCO("POCO M8 Pro 5G", 6500),
    REDMI_GLOBAL("REDMI Note 15 Pro+ 5G", 6500),
    REDMI_CHINA("REDMI Note 15 Pro+", 7000),
    UNKNOWN("", null),
}

internal fun resolveDeviceVariant(
    hardwareSku: String,
    productSku: String,
    vendorSku: String,
    hwc: String,
    marketName: String,
): DeviceVariant {
    // The vendor SKU selects regional HALs; it does not distinguish POCO from REDMI.
    if (vendorSku == "flourite_cn" || (hardwareSku == "flourite" && hwc == "CN")) {
        return DeviceVariant.REDMI_CHINA
    }
    if (hardwareSku == "flouritep" || productSku == "flouritep" ||
        marketName.contains("POCO M8 Pro", ignoreCase = true)) {
        return DeviceVariant.POCO
    }
    if ((hardwareSku == "flourite" || productSku == "flourite" ||
            marketName.contains("REDMI Note 15 Pro+", ignoreCase = true)) &&
        hwc.isNotBlank() && hwc != "CN") {
        return DeviceVariant.REDMI_GLOBAL
    }
    return DeviceVariant.UNKNOWN
}
