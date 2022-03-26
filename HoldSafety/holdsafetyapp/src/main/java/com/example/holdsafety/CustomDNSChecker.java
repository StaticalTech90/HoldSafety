package com.example.holdsafety;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

public interface CustomDNSChecker {
    // USED ONLY FOR GMAIL AND YAHOO EMAILS
    static boolean checkEmailDNS(String email) {
        String domain = email.substring((email.lastIndexOf("@") + 1));

        return domain.equals("gmail.com") || domain.equals("yahoo.com");
    }

    // UNUSED FOR NOW. CHECKS IF DOMAIN IS VALID
    static boolean checkEmailDNS(String email, int code) throws UnknownHostException, InterruptedException {
        String domain = email.substring((email.lastIndexOf("@") + 1));
        AtomicBoolean valid = new AtomicBoolean(false);

        Thread DNSCheckerThread = new Thread(() -> {
            try  {
                InetAddress inetAddress = InetAddress.getByName(domain);
                Log.d("DOMAIN-log", "Host Name: " + inetAddress.getHostName());
                Log.d("DOMAIN-log", "Host Addr: " + inetAddress.getHostAddress());
                valid.set(true);
                Log.d("DOMAIN-log", "Valid: " + valid.get());
            } catch (UnknownHostException uhe) {
                Log.d("DOMAIN-log", "Host Name: " + domain);
                Log.d("DOMAIN-log", "Host Addr: " + null);
                Log.d("DOMAIN-log", "Valid: " + valid.get());
                uhe.printStackTrace();
            }
        });
        DNSCheckerThread.start();
        DNSCheckerThread.join();  // continue after the thread finishes

        return valid.get();
    }
}
