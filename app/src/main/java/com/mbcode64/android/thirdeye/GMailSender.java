package com.mbcode64.android.thirdeye;


/**
 * Created by Scott on 12/12/2017.
 */

//todo verify email address


import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import de.agitos.dkim.Canonicalization;
import de.agitos.dkim.DKIMSigner;
import de.agitos.dkim.SigningAlgorithm;

public class GMailSender extends javax.mail.Authenticator {
    static {
        Security.addProvider(new JSSEProvider());
    }


    //private  String mailhost = "smtp.1and1.com";
    private String user;
    private String password;
    private Session session;
    private Multipart _multipart;

    public GMailSender(String user, String password) {
        this.user = user;
        this.password = password;

        final String fromEmail = this.user;
        final String fromPassword = this.password;
        _multipart = new MimeMultipart();
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.1and1.com"); //SMTP Host
        props.put("mail.smtp.port", "587"); //TLS Port
        props.put("mail.smtp.auth", "true"); //enable authentication
        props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
//        props.put("mail.debug", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.socketFactory.port", 587);
        props.put("mail.smtp.socketFactory.fallback", "false");

        // dkim sign the email
        DKIMSigner dkimSigner = null;
        try {
            dkimSigner = new DKIMSigner(props.getProperty("mail.smtp.dkim.signingdomain"), props.getProperty("mail.smtp.dkim.selector"), props.getProperty("mail.smtp.dkim.privatekey"));
            dkimSigner.setIdentity(props.getProperty("mail.user") + "@" + props.getProperty("mail.smtp.dkim.signingdomain"));
            dkimSigner.setHeaderCanonicalization(Canonicalization.SIMPLE);
            dkimSigner.setBodyCanonicalization(Canonicalization.RELAXED);
            dkimSigner.setLengthParam(true);
            dkimSigner.setSigningAlgorithm(SigningAlgorithm.SHA1withRSA);
            dkimSigner.setZParam(true);
        } catch (Exception e) {
        }
        //create Authenticator object to pass in Session.getInstance argument
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, fromPassword);
            }
        };
        session = Session.getDefaultInstance(props, auth);
    }


    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }


    public synchronized void sendMail(String subject, String body, String sender, String recipients, String attachment) throws Exception {
        try {
            MimeMessage message = new MimeMessage(session);
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(body, "text/html; charset=utf-8");
            _multipart.addBodyPart(htmlPart);
            //addAttachment(attachment);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
            message.setFrom(new InternetAddress("thirdeye@mbcode.net"));
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject);
            message.setDataHandler(handler);
            message.setContent(_multipart);

            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));

            Transport.send(message);

            Log.i("SendMail", "message sent");

        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
    }

    public void addAttachment(String filename) throws Exception {
        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);
        _multipart.addBodyPart(messageBodyPart);
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}

