package crawler;

import Mail.MailConfig;
import Mail.MailService;
import Models.Artwork;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties
@SpringBootApplication
public class CrawlerApp {

    @Autowired
    private ApplicationContext appContext;

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
        static boolean download=false;
        /**
         * Send notificate if passed in artwork is not null;
         *
         * @param artwork
         */
        @ServiceActivator (inputChannel = "channel5")
        public void sendNotification(Artwork artwork) {
            if (download) {
                RestTemplate restTemplate = new RestTemplate();
                File artworkFolder = new File("Beauty" + File.separator + String.format("No.%03d", artwork.getArtId()));
                if (!artworkFolder.exists() || !artworkFolder.isDirectory()) {
                    LOG.debug(artworkFolder.getPath());
                    artworkFolder.mkdirs();
                }
                artwork.getThumbnailImgList().parallelStream().forEach(o -> {
                    String baseName = FilenameUtils.getBaseName(o.toString());
                    String extension = FilenameUtils.getExtension(o.toString());
                    boolean isJpeg = extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg");
                    File img = new File(artworkFolder + File.separator + baseName + "." + extension);
                    if (img.isFile() && img.exists()) {
                        LOG.debug("Skipping existing file " + img.getPath());
                    } else if (!isJpeg) {
                        LOG.debug("Skipping none jpg file " + baseName + "." + extension);
                    } else {
                        byte[] imageBytes = restTemplate.getForObject(o.toString(), byte[].class);
                        try {
                            Files.write(img.toPath(), imageBytes);
                            LOG.info("Downloaded new image: " + img.toPath());
                        } catch (IOException e) {
                            LOG.error("IOE");
                        }
                    }

                });
                LOG.debug("Done checking " + artworkFolder.getPath());

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
        ApplicationContext ctx =
                new AnnotationConfigApplicationContext(MailConfig.class);
        MailService ms = ctx.getBean(MailService.class);
        if (!ms.checkMailServiceAvailable()) {
            LOG.error("Failed to connect to smtp server, shutting down.");
            LOG.error("Bye.");
            System.exit(-1);
        }
        PeriodicTrigger trigger = new PeriodicTrigger(10);
        trigger.setFixedRate(true);
        PollerMetadata pollerMetadata = new PollerMetadata();
        pollerMetadata.setTrigger(trigger);
        return pollerMetadata;
    }
}
