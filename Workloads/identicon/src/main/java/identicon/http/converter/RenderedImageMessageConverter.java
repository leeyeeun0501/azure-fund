package identicon.http.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.util.MimeType;

import java.awt.image.RenderedImage;
import java.io.IOException;

@Slf4j
public class RenderedImageMessageConverter extends AbstractHttpMessageConverter<byte[]> {
    public RenderedImageMessageConverter() {
        super(new IdentIconType());
    }

    @Override
    protected boolean supports(@NonNull Class<?> clazz) {
        return RenderedImage.class.isAssignableFrom(clazz);
    }

    @Override
    protected byte[] readInternal(@NonNull Class<? extends byte[]> clazz, @NonNull HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return null;
    }

    @Override
    protected void writeInternal(@NonNull byte[] renderedImage, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        HttpHeaders headers = outputMessage.getHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        outputMessage.getBody().write(renderedImage);
    }

    private static class IdentIconType extends MediaType {
        public IdentIconType() {
            super(new MimeType("application", "identicon"));
        }
    }
}
