package app.vaazar.Domain.Upload.Control;

import app.vaazar.Domain.Upload.Entity.UploadType;
import app.vaazar.Security.HashUtil;

import java.time.Instant;

public class UploadUtil {

    private static final int hashLength = 18;

    public static String nameUpload(UploadType type, Long userId) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(type.getTemplate(), userId.toString()));
        if (type == UploadType.SH_IDENTITY) {
            sb.append(HashUtil.expressHash(Instant.now().toString()), 0, hashLength);
        } else {
            sb.append(HashUtil.expressHash(sb.toString()), 0, hashLength);
        }
        return sb.toString();
    }

}
