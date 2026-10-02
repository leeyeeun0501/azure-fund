package identicon.controller;

import identicon.renderer.IdenticonRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

@Slf4j
@RestController
public class IdenticonController {
    private final IdenticonRenderer renderer;
    private final ValueOperations<String, byte[]> valueRedisTemplateOperations;

    public IdenticonController(IdenticonRenderer renderer, ValueOperations<String, byte[]> valueRedisTemplateOperations) {
        this.renderer = renderer;
        this.valueRedisTemplateOperations = valueRedisTemplateOperations;
    }

//    @GetMapping("/{id}")
//    public RenderedImage read(@PathVariable("id") String id) {
//        return renderer.render(id.hashCode(), 64);
//    }

    @GetMapping("/{id}")
    public byte[] read(@PathVariable("id") String id) {
        return Optional
                .ofNullable(valueRedisTemplateOperations.get(id))
                .orElseGet(Supplier.THROWS(() -> {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(renderer.render(id.hashCode(), 64), "PNG", byteArrayOutputStream);
                    valueRedisTemplateOperations.set(id, byteArrayOutputStream.toByteArray());

                    return byteArrayOutputStream.toByteArray();
                }));
    }

    //
    // Wrapper for Handling Lambda Exception
    //
    private static class Supplier {
        public static <T> java.util.function.Supplier<T> THROWS(ThrowsProxy<T, Exception> throwsProxy) {
            return () -> {
                T t = null;
                try {
                    t = throwsProxy.get();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                return t;
            };
        }

        @FunctionalInterface
        public interface ThrowsProxy<T, E extends Exception> {
            T get() throws E;
        }
    }
}