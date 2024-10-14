package id.kuato.woahelper.util;

/*
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

public class MemoryUtils {

    private String floatForm(double d) {
        return String.format(java.util.Locale.US, "%.2f", d);
    }

    String bytesToHuman(long size) {
        final long Kb = 1024L;
        final long Mb = Kb << 10;
        final long Gb = Mb << 10;
        final long Tb = Gb << 10;
        final long Pb = Tb << 10;
        final long Eb = Pb << 10;

        if (Kb > size)
            return this.floatForm(size);
        if (Mb > size)
            return this.floatForm((double) size / Kb);
        if (Gb > size)
            return this.floatForm((double) size / Mb);
        if (Tb > size)
            return this.floatForm((double) size / Gb);
        if (Pb > size)
            return this.floatForm((double) size / Tb);
        if (Eb > size)
            return this.floatForm((double) size / Pb);
        return this.floatForm((double) size / Eb);

    }

}
