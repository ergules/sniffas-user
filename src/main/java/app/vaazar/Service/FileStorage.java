package app.vaazar.Service;

import app.vaazar.Domain.Upload.Entity.UploadType;

public interface FileStorage {
    String preSignWithObjectKey(String key, UploadType type);
}
