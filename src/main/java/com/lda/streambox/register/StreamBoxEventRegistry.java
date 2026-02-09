package com.lda.streambox.register;

import com.lda.streambox.anotation.StreamBoxEventType;
import com.lda.streambox.anotation.StreamBoxType;
import com.lda.streambox.model.StreamBoxEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class StreamBoxEventRegistry {

    private final Map<String, Class<? extends StreamBoxEvent>> registry = new HashMap<>();

    public StreamBoxEventRegistry(List<String> packagesToScan, StreamBoxType type) {
        packagesToScan.forEach(this::scanPackage);
    }

    private void scanPackage(String basePackage) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(StreamBoxEventType.class));

        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                if (!StreamBoxEvent.class.isAssignableFrom(clazz)) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                Class<? extends StreamBoxEvent> eventClass =
                        (Class<? extends StreamBoxEvent>) clazz;

                StreamBoxEventType ann = eventClass.getAnnotation(StreamBoxEventType.class);

                registry.put(ann.value(), eventClass);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Class<? extends StreamBoxEvent> resolve(String type) {
        return registry.get(type);
    }
}