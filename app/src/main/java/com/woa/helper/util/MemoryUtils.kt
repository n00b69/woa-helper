package com.woa.helper.util

import java.util.Locale /*
 *  * Copyright (C) 2020 Vern Kuato
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */

internal class MemoryUtils {
    private fun floatForm(d: Double): String {
        return String.format(Locale.US, "%.2f", d)
    }

    fun bytesToHuman(size: Long): String {
        val Kb = 1024L
        val Mb = Kb shl 10
        val Gb = Mb shl 10
        val Tb = Gb shl 10
        val Pb = Tb shl 10
        val Eb = Pb shl 10

        if (Kb > size) return floatForm(size.toDouble())
        if (Mb > size) return floatForm(size.toDouble() / Kb)
        if (Gb > size) return floatForm(size.toDouble() / Mb)
        if (Tb > size) return floatForm(size.toDouble() / Gb)
        if (Pb > size) return floatForm(size.toDouble() / Tb)
        if (Eb > size) return floatForm(size.toDouble() / Pb)
        return floatForm(size.toDouble() / Eb)
    }
}
