package com.nostalgia.webserver;

import com.google.common.hash.Hashing;

public class CachedAsset {
    private final byte[] resource;
    private final String eTag;
    private final long lastModifiedTime;

    public CachedAsset(byte[] resource, long lastModifiedTime) {
        this.resource = resource;
        this.eTag = '"' + Hashing.murmur3_128().hashBytes(resource).toString() + '"';
        this.lastModifiedTime = lastModifiedTime;
    }

    public byte[] getResource() {
        return resource;
    }

    public String getETag() {
        return eTag;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }
}
