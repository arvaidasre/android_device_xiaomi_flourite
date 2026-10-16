/* SPDX-License-Identifier: Apache-2.0 */
package org.lineageos.settings.flourite.gpu

internal data class GpuSessionSnapshot(val state: GpuState)
internal object GpuControlSession {
    private val manager = GpuControlManager()
    @Synchronized fun snapshot() = GpuSessionSnapshot(manager.readState())
    @Synchronized fun <T> manual(block: (GpuControlManager) -> T): T = block(manager)
}
