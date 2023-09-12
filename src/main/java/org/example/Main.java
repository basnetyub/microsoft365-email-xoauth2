package org.example;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.mail.imap.IMAPFolder;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import javax.mail.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static sun.misc.IOUtils.readAllBytes;

public class Main {
    public static void main(String[] args) throws IOException, MessagingException {
        Main main = new Main();
        String tanantId = "your id";
        String clientId = "your id";
        String client_secret = "your id";
        String mailAddress = "your email";

        Properties props = new Properties();
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", "outlook.office365.com");
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.imap.starttls.enable", "true");
        props.put("mail.imap.auth", "true");
        props.put("mail.imap.auth.mechanisms", "XOAUTH2");
        props.put("mail.imap.user", mailAddress);
        props.put("mail.debug", "true");
        props.put("mail.debug.auth", "true");

        // open mailbox....
        String token = getAuthToken(tanantId,clientId,client_secret);
        Session session = Session.getInstance(props);
        session.setDebug(true);
        Store store = session.getStore("imap");
        store.connect("outlook.office365.com", mailAddress, token);

        //open the inbox folder
        IMAPFolder inbox = (IMAPFolder)store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
//fetch messages
        Message[] messages = inbox.getMessages();
//read messages
        for (int i = 0; i < messages.length; i++) {
            Message msg = messages[i];
            Address[] fromAddress = msg.getFrom();
            String from = fromAddress[0].toString();
            String subject = msg.getSubject();
            Address[] toList = msg.getRecipients(Message.RecipientType.TO);
            Address[] ccList = msg.getRecipients(Message.RecipientType.CC);
            String contentType = msg.getContentType();
        }
    }

    public static String getAuthToken(String tanantId, String clientId, String client_secret) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost loginPost = new HttpPost("https://login.microsoftonline.com/" + tanantId + "/oauth2/v2.0/token");
        String scopes = "https://outlook.office365.com/.default";
        String encodedBody = "client_id=" + clientId + "&scope=" + scopes + "&client_secret=" + client_secret
                + "&grant_type=client_credentials";
        loginPost.setEntity(new StringEntity(encodedBody, ContentType.APPLICATION_FORM_URLENCODED));
        loginPost.addHeader(new BasicHeader("cache-control", "no-cache"));
        CloseableHttpResponse loginResponse = client.execute(loginPost);
        InputStream inputStream = loginResponse.getEntity().getContent();
        byte[] response = readAllBytes(inputStream);
        ObjectMapper objectMapper = new ObjectMapper();
        JavaType type = objectMapper.constructType(
                objectMapper.getTypeFactory().constructParametricType(Map.class, String.class, String.class));
        Map<String, String> parsed = new ObjectMapper().readValue(response, type);
        return parsed.get("access_token");
    }
}