/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.crossversion.function;

import de.eldoria.eldoutilities.crossversion.builder.FunctionBuilder;
import de.eldoria.eldoutilities.utils.Version;
import org.junit.jupiter.api.Test;

class VersionFunctionTest {
    @Test
    public void test() throws NoSuchFieldException, IllegalAccessException {

        VersionFunction<String, String> func = FunctionBuilder.functionBuilder(String.class, String.class)
                .addExclusiveVersion(Version.of(1, 13), Version.of(1, 16),
                        n -> n)
                .addExclusiveVersion(Version.of(1, 16), Version.of(1, Integer.MAX_VALUE),
                        n -> n)
                .build();
        func.get(Version.of(1,15,5));
    }
}
