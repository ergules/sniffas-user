package app.vaazar.service.amqp;

import app.vaazar.service.NotificationSender;
import app.vaazar.service.model.NotificationOptions;
import org.slf4j.Logger;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(name = "app.rabbit.active", havingValue = "true")
public class RabbitService implements NotificationSender {

    private final RabbitTemplate rabbitTemplate;
    private final DirectExchange defaultDirectExchange;
    private final Logger log;

    public void sendNotification(NotificationOptions options) {
        log.info("publish notification: {}", options.getData());
        rabbitTemplate.convertAndSend(
                defaultDirectExchange.getName(), "notification", options);
    }


    public RabbitService(RabbitTemplate rabbitTemplate, Logger log, Jackson2JsonMessageConverter converter) {
        this.log = log;
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter(converter);
        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitTemplate);

        defaultDirectExchange = new DirectExchange("amq.direct");
        Queue notificationQueue = new Queue("notification", true);
        rabbitAdmin.declareBinding(BindingBuilder
                .bind(notificationQueue)
                .to(defaultDirectExchange)
                .with("notification")
        );
    } // does not affect existing configuration, this ensures queues and exchanges created properly

}
