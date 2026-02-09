package com.lda.streambox.register;

import com.lda.streambox.anotation.StreamBox;
import com.lda.streambox.anotation.StreamBoxType;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class StreamBoxRegistrar implements ImportBeanDefinitionRegistrar {


    @Override
    public void registerBeanDefinitions(
            AnnotationMetadata metadata,
            @NonNull BeanDefinitionRegistry registry
    ) {

        Map<String, Object> attrs =
                metadata.getAnnotationAttributes(StreamBox.class.getName());

        if (attrs == null) {
            log.error("getAnnotationAttributes failed");
            return;
        }

        StreamBoxType type = (StreamBoxType) attrs.get("type");

        // Base package = where @StreamBox is placed
        String className = metadata.getClassName();
        String basePackage = ClassUtils.getPackageName(className);

        // Extra user-specified packages
        String[] extraPackages = (String[]) attrs.get("scanBasePackages");

        // Combine
        List<String> allPackages = new ArrayList<>();
        allPackages.add(basePackage);

        if (extraPackages != null) {
            allPackages.addAll(Arrays.asList(extraPackages));
        }

        // Register the registry with all packages + type
        RootBeanDefinition def = new RootBeanDefinition(StreamBoxEventRegistry.class);
        def.getConstructorArgumentValues().addIndexedArgumentValue(0, allPackages);
        def.getConstructorArgumentValues().addIndexedArgumentValue(1, type);

        registry.registerBeanDefinition("streamBoxEventRegistry", def);
    }
}
