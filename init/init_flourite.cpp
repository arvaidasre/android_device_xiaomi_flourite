// SPDX-License-Identifier: Apache-2.0
#include <android-base/properties.h>
#define _REALLY_INCLUDE_SYS__SYSTEM_PROPERTIES_H_
#include <sys/_system_properties.h>

#include <cstring>
#include "vendor_init.h"

void vendor_load_properties() {
    // Keep Xiaomi's product SKU and camera region intact. Use a separate
    // vendor SKU for VINTF and security feature selection before HAL startup.
    const bool china = android::base::GetProperty("ro.boot.hardware.sku", "") == "flourite" &&
                       android::base::GetProperty("ro.boot.hwc", "") == "CN";
    const char* key = "ro.boot.product.vendor.sku";
    const char* value = china ? "flourite_cn" : "flourite_global";
    auto* info = const_cast<prop_info*>(__system_property_find(key));
    if (info != nullptr) {
        __system_property_update(info, value, std::strlen(value));
    } else {
        __system_property_add(key, std::strlen(key), value, std::strlen(value));
    }
}
