package app.vaazar.domain.upload.control;

import app.vaazar.domain.upload.entity.UploadType;
import app.vaazar.domain.user.entity.User;
import app.vaazar.security.HashUtil;

import java.time.Instant;
import java.util.List;

import static app.vaazar.domain.upload.entity.UploadType.*;

public class UploadUtil {

    private static final List<UploadType> USER_AND_COMPANY_UPLOADS =
            List.of(PROFILE_PIC, COMPANY_REGISTRY, TAX_REGISTRY, GM_IDENTITY, SH_IDENTITY);

    public static String nameUpload(UploadType type, User loggedUser, Long entityId, String extension) {

        StringBuilder sb = new StringBuilder();

        if (USER_AND_COMPANY_UPLOADS.contains(type))
            sb.append(String.format(type.getTemplate(), loggedUser.getId()));
        else if (entityId != null)
            sb.append(String.format(type.getTemplate(), entityId));
        else
            sb.append(type.getTemplate());

        if (type.isDocument()) {
            sb.append(HashUtil.expressHash(sb.toString()), 0, 18);
        } else {
            sb.append(Instant.now().toEpochMilli() - 1630066006600L);
        } // documents need same name, other files must have new one on each request

        return sb.append(".").append(extension.replace(".", "")).toString();
    }

}
