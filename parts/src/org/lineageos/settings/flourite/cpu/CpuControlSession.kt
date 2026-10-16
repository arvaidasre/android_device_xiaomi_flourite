/* SPDX-License-Identifier: Apache-2.0 */
package org.lineageos.settings.flourite.cpu

internal object CpuControlSession {
    private val manager = CpuControlManager()
    @Synchronized fun <T> read(block: (CpuControlManager) -> T): T = block(manager)
    @Synchronized fun <T> manual(block: (CpuControlManager) -> T): T = block(manager)
}
