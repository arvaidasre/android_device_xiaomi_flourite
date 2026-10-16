/* SPDX-License-Identifier: Apache-2.0 */
package org.lineageos.settings.flourite.cpu

/** SM7635: four efficiency cores, three performance cores and one prime core. */
internal object CpuTopology {
    const val EFFICIENCY = 0
    const val PERFORMANCE = 4
    const val PRIME = 7
    val policyIds = listOf(EFFICIENCY, PERFORMANCE, PRIME)
}
