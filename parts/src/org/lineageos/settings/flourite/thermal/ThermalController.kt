/* SPDX-License-Identifier: Apache-2.0 */
package org.lineageos.settings.flourite.thermal

import java.io.File

internal data class ThermalState(val mode: String, val error: String, val errno: Int, val profile: String, val resultId: String) {
    val disabled get() = mode == "disabled"
    val confirmed get() = mode == "disabled" || mode == "enabled"
}

/** The UI must confirm risk before request(true). No boot persistence or daemon shutdown. */
internal class ThermalControlManager(
    private val nodes: List<File>,
    private val writer: (File, String) -> Unit = { file, value -> file.writeText(value) },
) {
    private var result = 0L
    private var error = "none"
    private fun value(file: File) = runCatching { file.readText().trim().toInt().takeIf { it in 0..1 } }.getOrNull()
    @Synchronized fun read(): ThermalState {
        val actual = nodes.map(::value)
        val mode = when {
            actual.all { it == 0 } -> "enabled"
            actual.all { it == 1 } -> "disabled"
            else -> "unknown"
        }
        return ThermalState(mode, error, 0, "", result.toString())
    }
    @Synchronized fun request(disabled: Boolean) {
        error = "none"
        try {
            if (disabled && (nodes.any { !it.canWrite() } || nodes.any { value(it) == null })) {
                // Never leave a partially active bypass after a failed enable.
                nodes.forEach { runCatching { writer(it, "0\n") } }
                error = "current_missing"
                return
            }
            val target = if (disabled) 1 else 0
            var failed = false
            for (node in nodes) {
                if (runCatching { writer(node, "$target\n"); value(node) == target }.getOrDefault(false).not()) failed = true
            }
            if (failed) {
                // Fail safe: an incomplete enable attempts to restore ALL available controls.
                val restored = nodes.map { runCatching { writer(it, "0\n"); value(it) == 0 }.getOrDefault(false) }.all { it }
                error = if (restored) "apply_unconfirmed" else "restore_failed"
            }
        } finally { result++ }
    }
}

internal object ThermalController {
    private val manager = ThermalControlManager(listOf(0, 3, 7).map {
        File("/sys/devices/system/cpu/cpufreq/policy$it/scaling_parts_thermal_bypass")
    } + File("/sys/class/kgsl/kgsl-3d0/parts_thermal_bypass"))
    fun read() = manager.read()
    fun request(disabled: Boolean) = manager.request(disabled)
}
