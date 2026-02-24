package com.github.bohnman.squiggly.spring;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.codec.xml.JacksonXmlEncoder;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.dataformat.xml.XmlMapper;

import java.util.Map;

/**
 * Workaround for Spring Framework 7.0.x where {@link JacksonXmlEncoder#encode}
 * unconditionally throws {@link UnsupportedOperationException}, even for single-value
 * {@link Mono} publishers. The parent class {@code AbstractJacksonEncoder.encode()}
 * already handles the Mono case by delegating to {@code encodeValue()}, but
 * {@code JacksonXmlEncoder} overrides it entirely.
 * <p>
 * This subclass restores Mono handling while keeping the streaming (Flux)
 * unsupported restriction.
 */
class StreamSafeJacksonXmlEncoder extends JacksonXmlEncoder {

    StreamSafeJacksonXmlEncoder(XmlMapper mapper) {
        super(mapper);
    }

    @Override
    public Flux<DataBuffer> encode(Publisher<?> inputStream, DataBufferFactory bufferFactory,
                                   ResolvableType elementType, @Nullable MimeType mimeType,
                                   @Nullable Map<String, Object> hints) {
        if (inputStream instanceof Mono<?> mono) {
            return mono
                    .map(value -> encodeValue(value, bufferFactory, elementType, mimeType, hints))
                    .flux();
        }
        throw new UnsupportedOperationException("Stream encoding is currently not supported for XML");
    }
}
