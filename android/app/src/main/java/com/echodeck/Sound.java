package com.echodeck;

import android.net.Uri;

public final class Sound {
    public final long id;
    public String name;
    public Uri uri;
    public float volume = 1f;
    public boolean loop;
    public int color;

    public Sound(long id, String name, Uri uri, int color) {
        this.id = id; this.name = name; this.uri = uri; this.color = color;
    }
}
