package xyz.acrylicstyle.tomeito_api.utils;

import org.jetbrains.annotations.Nullable;

public interface Callback<T> extends util.Callback<T> {
    void done(@Nullable T t, @Nullable Throwable e);
}
