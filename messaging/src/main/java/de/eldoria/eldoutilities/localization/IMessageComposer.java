/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.localization;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.List;

public interface IMessageComposer {
    String build();

    List<TagResolver> replacements();
}
