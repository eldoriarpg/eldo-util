/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.command.util;

public class SubCommand extends Argument {
    protected SubCommand(String name) {
        super(name);
    }

    @Override
    public String formatted() {
        return name();
    }

    @Override
    public boolean isRequired() {
        return true;
    }
}
