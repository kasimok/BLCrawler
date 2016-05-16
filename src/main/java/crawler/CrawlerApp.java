package crawler;/*
 * Copyright 2014 NAKANO Hideo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import Models.Artwork;
import Models.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties
public class CrawlerApp {
    private static Logger LOG = LoggerFactory.getLogger(CrawlerApp.class);

    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = SpringApplication.run(CrawlerApp.class, args);
        System.in.read();
        Runtime.getRuntime().exit(SpringApplication.exit(ctx));
    }


    @Autowired
    private CrawlerConfig config;

    @PostConstruct
    public void postConstruct() {
        LOG.info("starting crawler with config={},the site is now monitored", config);
    }

    @MessageEndpoint
    public static class Endpoint {
        @Autowired
        private ArtworkRepository artworkRepository;
        @Autowired
        private ModelRepository modelRepository;


        @ServiceActivator (inputChannel = "channel4")
        public void logAndCheck(Artwork artwork) {
            if (modelRepository.getModelByName(artwork.getModelNickname())==null) {
                LOG.info(String.format("Model=%s,not exist in db,inserting", artwork.getModelNickname()));
                modelRepository.insertModel(new Model(
                        artwork.getModelNickname()
                ));
            }
            if (artworkRepository.getArtwork(artwork.getArtId())==null) {
                LOG.info("Artwork={},not exist in db,inserting", artwork);
                artworkRepository.insertArtwork(artwork);
            }
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Trigger the crawler for index job periodically.
     *
     * @return
     */
    @Bean
    public PollerMetadata downloadIndexTrigger() {
        PeriodicTrigger trigger = new PeriodicTrigger(config.getDownloadInterval());
        trigger.setFixedRate(true);
        PollerMetadata pollerMetadata = new PollerMetadata();
        pollerMetadata.setTrigger(trigger);
        pollerMetadata.setMaxMessagesPerPoll(1);
        return pollerMetadata;
    }


    @Bean
    public MessageChannel channel1() {
        return new QueueChannel(10);
    }

    @Bean
    public MessageChannel channel2() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel channel3() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel channel4() {
        return new QueueChannel(10);
    }

    // <int:poller id="poller" default="true" fixed-rate="10"/>
    @Bean (name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata poller() {
        PeriodicTrigger trigger = new PeriodicTrigger(10);
        trigger.setFixedRate(true);
        PollerMetadata pollerMetadata = new PollerMetadata();
        pollerMetadata.setTrigger(trigger);
        return pollerMetadata;
    }
}
