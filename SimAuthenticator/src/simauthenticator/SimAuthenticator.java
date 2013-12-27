/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simauthenticator;

import java.security.KeyStore;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author maskimko
 */
public class SimAuthenticator {

    private static String usern;
    private static String passwu;
    private static String passwk;
    private static String keyStorePath;
    private static String eventUrl;
    private static CommandLine cmd;
    private static Options cliOpts;
    private static CookieStore cookieStore;
    private static String ipAddr = "10.10.214.34";

    private static boolean verbose = false;
    private static String domain = "ASTELIT";
    private static String userAgent = "Mozilla/5.0 (X11; Linux x86_64; rv:26.0) Gecko/20100101 Firefox/26.0";

    private static boolean init(String[] args) {
        boolean result = false;
        if (cmd.hasOption("url") && cmd.hasOption("user") && cmd.hasOption("password") && cmd.hasOption("keystore") && cmd.hasOption("keystorepass")) {
            eventUrl = cmd.getOptionValue("url");
            usern = cmd.getOptionValue("user");
            passwu = cmd.getOptionValue("password");
            if (cmd.hasOption("domain")) {
                domain = cmd.getOptionValue("domain");
            }
            keyStorePath = cmd.getOptionValue("keystore");
            passwk = cmd.getOptionValue("keystorepass");
            if (cmd.hasOption('v')) {
                verbose = true;
            }
            result = true;
        } else {
            usage();
        }
        return result;
    }

    private static void usage() {
        System.err.println("FOR HELP USE -h OR --help OPTION");
        System.err.println("Usage: java -jar SimAuthenticator.jar <--url|-U URL> <-u|--user username> <-p|--password userpassword>");
        System.err.print("\t<-d|--domain domain> [-v|--verbose]\n\t<-k|--keystore Path to java keystore file> <-K|--keystorepass> KeyStore password");
    }

    private static void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("SimAuthenticator", cliOpts);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        cliOpts = new Options();
        cliOpts.addOption("U", "url", true, "Connection URL");
        cliOpts.addOption("u", "user", true, "User name");
        cliOpts.addOption("p", "password", true, "User password");
        cliOpts.addOption("d", "domain", true, "Domain name");
        cliOpts.addOption("v", "verbose", false, "Verbose output");
        cliOpts.addOption("k", "keystore", true, "KeyStore path");
        cliOpts.addOption("K", "keystorepass", true, "KeyStore password");
        cliOpts.addOption("h", "help", false, "Print help info");

        CommandLineParser clip = new GnuParser();
        cmd = clip.parse(cliOpts, args);

        if (cmd.hasOption("help")) {
            help();
            return;
        } else {
            boolean valid = init(args);
            if (!valid) {
                return;
            }
        }

        HttpClientContext clientContext = HttpClientContext.create();

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] keystorePassword = passwk.toCharArray();
        FileInputStream kfis = null;
        try {
            kfis = new FileInputStream(keyStorePath);
            ks.load(kfis, keystorePassword);
        } finally {
            if (kfis != null) {
                kfis.close();
            }
        }

        SSLContext sslContext = SSLContexts.custom().useSSL().loadTrustMaterial(ks).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().setSslcontext(sslContext).setSSLSocketFactory(sslsf).setUserAgent(userAgent);;

        cookieStore = new BasicCookieStore();
        /* BasicClientCookie cookie = new BasicClientCookie("SIM authenticator", "Utility for getting event details");
         cookie.setVersion(0);
         cookie.setDomain(".astelit.ukr");
         cookie.setPath("/");
         cookieStore.addCookie(cookie);*/

        CloseableHttpClient client = httpClientBuilder.build();

        try {

            NTCredentials creds = new NTCredentials(usern, passwu, InetAddress.getLocalHost().getHostName(), domain);
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, creds);
            HttpClientContext context = HttpClientContext.create();
            context.setCredentialsProvider(credsProvider);
            context.setCookieStore(cookieStore);
            HttpGet httpget = new HttpGet(eventUrl);
            if (verbose) {
                System.out.println("executing request " + httpget.getRequestLine());
            }
            HttpResponse response = client.execute(httpget, context);
            HttpEntity entity = response.getEntity();

            HttpPost httppost = new HttpPost(eventUrl);
            List<Cookie> cookies = cookieStore.getCookies();

            if (verbose) {
                System.out.println("----------------------------------------------");
                System.out.println(response.getStatusLine());
                System.out.print("Initial set of cookies: ");
                if (cookies.isEmpty()) {
                    System.out.println("none");
                } else {
                    for (int i = 0; i < cookies.size(); i++) {
                        System.out.println("- " + cookies.get(i).toString());
                    }
                }
            }

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("usernameInput", usern));
            nvps.add(new BasicNameValuePair("passwordInput", passwu));
            nvps.add(new BasicNameValuePair("domainInput", domain));
            //nvps.add(new BasicNameValuePair("j_username", domain + "\\" + usern));
            //nvps.add(new BasicNameValuePair("j_password", ipAddr + ";" + passwu));
            if (entity != null && verbose) {
                System.out.println("Responce content length: " + entity.getContentLength());

            }

            //System.out.println(EntityUtils.toString(entity));
            

            httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            HttpResponse afterPostResponse = client.execute(httppost, context);
            HttpEntity afterPostEntity = afterPostResponse.getEntity();
            cookies = cookieStore.getCookies();
            if (entity != null && verbose) {
                System.out.println("----------------------------------------------");
                System.out.println(afterPostResponse.getStatusLine());
                System.out.println("Responce content length: " + afterPostEntity.getContentLength());
                System.out.print("After POST set of cookies: ");
                if (cookies.isEmpty()) {
                    System.out.println("none");
                } else {
                    for (int i = 0; i < cookies.size(); i++) {
                        System.out.println("- " + cookies.get(i).toString());
                    }
                }
            }

            System.out.println(EntityUtils.toString(afterPostEntity));
            EntityUtils.consume(entity);
            EntityUtils.consume(afterPostEntity);

        } finally {

            client.getConnectionManager().shutdown();
        }

    }

}
