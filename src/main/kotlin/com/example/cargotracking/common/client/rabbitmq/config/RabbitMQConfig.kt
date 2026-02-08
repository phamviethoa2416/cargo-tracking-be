package com.example.cargotracking.common.client.rabbitmq.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    @Value("\${rabbitmq.exchange:cargo_tracking}")
    private lateinit var exchangeName: String

    @Value("\${rabbitmq.queue.device-events:backend.device.events}")
    private lateinit var deviceEventsQueue: String

    @Value("\${rabbitmq.queue.device-updates:backend.device.updates}")
    private lateinit var deviceUpdatesQueue: String

    @Value("\${rabbitmq.queue.shipment-tracking:backend.shipment.tracking}")
    private lateinit var shipmentTrackingQueue: String

    @Value("\${rabbitmq.queue.device-config:backend.device.config}")
    private lateinit var deviceConfigQueue: String

    @Value("\${rabbitmq.queue.shipment-assignment:backend.shipment.assignment}")
    private lateinit var shipmentAssignmentQueue: String

    @Bean
    fun cargoEventsExchange(): TopicExchange {
        return TopicExchange(exchangeName, true, false)
    }

    @Bean
    fun deviceEventsQueue(): Queue {
        return Queue(deviceEventsQueue, true, false, false)
    }

    @Bean
    fun deviceUpdatesQueue(): Queue {
        return Queue(deviceUpdatesQueue, true, false, false)
    }

    @Bean
    fun shipmentTrackingQueue(): Queue {
        return Queue(shipmentTrackingQueue, true, false, false)
    }

    @Bean
    fun deviceConfigQueue(): Queue {
        return Queue(deviceConfigQueue, true, false, false)
    }

    @Bean
    fun shipmentAssignmentQueue(): Queue {
        return Queue(shipmentAssignmentQueue, true, false, false)
    }

    @Bean
    fun deviceEventsBinding(): Binding {
        return BindingBuilder
            .bind(deviceEventsQueue())
            .to(cargoEventsExchange())
            .with("event.#")
    }

    @Bean
    fun deviceUpdatesBinding(): Binding {
        return BindingBuilder
            .bind(deviceUpdatesQueue())
            .to(cargoEventsExchange())
            .with("device.update.#")
    }

    @Bean
    fun deviceConfigBinding(): Binding {
        return BindingBuilder
            .bind(deviceConfigQueue())
            .to(cargoEventsExchange())
            .with("device.config.update")
    }

    @Bean
    fun shipmentTrackingBinding(): Binding {
        return BindingBuilder
            .bind(shipmentTrackingQueue())
            .to(cargoEventsExchange())
            .with("shipment.tracking")
    }

    @Bean
    fun shipmentAssignmentBinding(): Binding {
        return BindingBuilder
            .bind(shipmentAssignmentQueue())
            .to(cargoEventsExchange())
            .with("device.shipment.assignment")
    }

    @Bean
    fun messageConverter(): MessageConverter {
        return Jackson2JsonMessageConverter()
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = messageConverter()
        return template
    }

    @Bean
    fun rabbitListenerContainerFactory(connectionFactory: ConnectionFactory): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory)
        factory.setMessageConverter(messageConverter())
        factory.setConcurrentConsumers(3)
        factory.setMaxConcurrentConsumers(10)
        factory.setPrefetchCount(10)
        return factory
    }
}