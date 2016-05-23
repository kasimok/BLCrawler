/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package Mail;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Created by evilisn(kasimok@163.com)) on 2016/5/19.
 */
@Configuration
public class MailConfig {


    //Mail 设置
    public Properties javaMailProperties() {
        Properties properties = new Properties();
        // add more properties in the same way
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.timeout", 25000);
        properties.put("mail.smtp.starttls.enable",true);
        properties.put("mail.smtp.ssl.trust", "657602.51mypc.cn");
        properties.put("mail.smtp.socketFactory.class", "javax.net.SocketFactory");
        return properties;
    }

    //Sender Bean
    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
        javaMailSenderImpl.setHost("657602.51mypc.cn");
        javaMailSenderImpl.setPort(587);
        javaMailSenderImpl.setUsername("subscribe");
        javaMailSenderImpl.setPassword("test@1234");
        javaMailSenderImpl.setJavaMailProperties(javaMailProperties());
        return javaMailSenderImpl;
    }

    @Bean
    public SimpleMailMessage templateMessage() {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("subscribe@beautyleg.tw");
        simpleMailMessage.setSubject("New Free Subscribe");
        return simpleMailMessage;
    }
    @Bean
    public MailService mailService(){
        MailService mailService=new MailService();
        mailService.setMailSender(mailSender());
        mailService.setTemplateMessage(templateMessage());
        return mailService;
    }
}
