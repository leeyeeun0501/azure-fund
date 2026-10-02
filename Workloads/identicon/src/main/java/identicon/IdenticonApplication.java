package identicon;

import identicon.http.converter.RenderedImageMessageConverter;
import identicon.renderer.IdenticonRenderer;
import identicon.renderer.NineBlockIdenticonRenderer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.http.converter.HttpMessageConverter;

@SpringBootApplication
public class IdenticonApplication {
	public static void main(String[] args) {
		SpringApplication.run(IdenticonApplication.class, args);
	}

	@Bean
	public IdenticonRenderer identiconRenderer() {
		return new NineBlockIdenticonRenderer();
	}

//	@Bean
//	public HttpMessageConverter<byte[]> renderedImageMessageConverter() {
//		return new RenderedImageMessageConverter();
//	}

	@Bean
	public ValueOperations<?, ?> valueRedisTemplateOperations(RedisTemplate<?, ?> redisTemplate) {
		return redisTemplate.opsForValue();
	}

	@Bean
	public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, byte[]> redisTemplate = new RedisTemplate<>();

		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new GenericToStringSerializer<Object>(Object.class) /* new JdkSerializationRedisSerializer() */);
		redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer() /* Default, Java Native Serialization, Not Recommended */);
		redisTemplate.setHashKeySerializer(new GenericToStringSerializer<Object>(Object.class));
		redisTemplate.setHashValueSerializer(new JdkSerializationRedisSerializer());

		return redisTemplate;
	}
}