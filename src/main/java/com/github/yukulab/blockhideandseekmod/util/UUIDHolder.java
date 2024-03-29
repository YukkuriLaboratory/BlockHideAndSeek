package com.github.yukulab.blockhideandseekmod.util;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface UUIDHolder {
    void setUUID(@Nullable UUID uuid);

    @Nullable UUID getUUID();
}
