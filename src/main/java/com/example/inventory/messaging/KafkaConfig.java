package com.example.inventory.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${inventory.kafka.topics.inventory-events}")
    private String inventoryTopic;

    @Value("${inventory.kafka.topics.audit}")
    private String auditTopic;

    @Value("${inventory.kafka.topics.bulk-upload}")
    private String bulkTopic;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic inventoryTopic() {
        return TopicBuilder.name(inventoryTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditTopic() {
        return TopicBuilder.name(auditTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic bulkUploadTopic() {
        return TopicBuilder.name(bulkTopic).partitions(3).replicas(1).build();
    }
}
