package app.vaazar.service;

import app.vaazar.domain.upload.entity.UploadType;

public interface FileStorage {
    String preSignWithObjectKey(String key, UploadType type);
}
