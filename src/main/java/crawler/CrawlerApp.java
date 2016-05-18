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
import com.sun.tools.javac.util.List;
import org.apache.commons.io.FilenameUtils;
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
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
        /**
         * Send notificate if passed in artwork is not null;
         * @param artwork
         */
        @ServiceActivator (inputChannel = "channel5")
        public void sendNotification(Artwork artwork) {
            RestTemplate restTemplate = new RestTemplate();
            File f = new File("No."+String.valueOf(artwork.getArtId()));
            if (!f.exists()||!f.isDirectory()) {
                LOG.info("Mkdir:"+"No." + String.valueOf(artwork.getArtId()));
                new File("No." + String.valueOf(artwork.getArtId())).mkdir();
            }

            artwork.getThumbnailImgList().parallelStream().forEach(o -> {
                String baseName = FilenameUtils.getBaseName(o.toString());
                String extension = FilenameUtils.getExtension(o.toString());
                File img = new File("No."+String.valueOf(artwork.getArtId())+File.separator+baseName+"."+extension);
                if (img.isFile()&&img.exists()) {
                    LOG.info("Skipping File "+ img.getPath());
                }else{
                    byte[] imageBytes = restTemplate.getForObject(o.toString(), byte[].class);
                    try {
                        Files.write(img.toPath(), imageBytes);
                        LOG.info("Downloaded "+img.toPath());
                    } catch (IOException e) {
                        LOG.error("IOE");
                    }
                }

            });
            LOG.info("Done checking "+f.getPath());

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


    @Bean
    public RestTemplate restTemplate(List<HttpMessageConverter<?>> messageConverters) {
        return new RestTemplate(messageConverters);
    }

    @Bean
    public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
        return new ByteArrayHttpMessageConverter();
    }
}
