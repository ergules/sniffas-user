package app.vaazar.Domain.BaseEntity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class IdSerializer extends JsonSerializer<BaseEntity> {

    @Override
    public void serialize(BaseEntity entity,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeNumber(entity.getId());
    }
}
