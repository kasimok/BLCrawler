package crawler;

import Mail.MailConfig;
import Mail.MailService;
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
import org.springframework.context.support.ClassPathXmlApplicationContext;
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
    private FreeCrawlerConfig configForFree;


    @PostConstruct
    public void postConstruct() {
        LOG.info("Starting crawler with config={},the site is now monitored", configForFree);
    }

    @MessageEndpoint
    public static class Endpoint {
        static boolean download=false;
        /**
         * Send notificate if passed in artwork is not null;
         *
         * @param anchorList
         */
        @ServiceActivator (inputChannel = "channel2-4")
        public void sendNotification(List<String> anchorList) {
            if (anchorList == null||anchorList.size()==0) {
                LOG.info("Empty list");
            }else{
                ApplicationContext ctx =
                        new AnnotationConfigApplicationContext(MailConfig.class);
                MailService mailService = ctx.getBean(MailService.class);
                try {
                    mailService.sendHtmlMailNotification("kasimok@163.com".split(";"), String.valueOf(mailService.genNotifyForNewFreePost(anchorList)));
                    LOG.info("Notification Sent!");
                } catch (javax.mail.MessagingException e) {
                    LOG.error("Fail to send mail");
                    e.printStackTrace();
                }
            }
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "SpringBeans.xml");
        return (RestTemplate)context.getBean("restTemplate");
    }


    /**
     * Trigger the crawler for index job periodically.
     *
     * @return
     */
    @Bean
    public PollerMetadata downloadFreeTrigger() {
        PeriodicTrigger trigger = new PeriodicTrigger(configForFree.getDownloadInterval());
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
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");
        PeriodicTrigger trigger = new PeriodicTrigger(10);
        trigger.setFixedRate(true);
        PollerMetadata pollerMetadata = new PollerMetadata();
        pollerMetadata.setTrigger(trigger);
        return pollerMetadata;
    }
}
