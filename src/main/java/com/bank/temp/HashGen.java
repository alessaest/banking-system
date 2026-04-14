package com.bank.temp;

import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class HashGen {

    private static final Logger logger = Logger.getLogger(HashGen.class);

    public static void main(String[] args) {
        logger.infof(BCrypt.hashpw("password123", BCrypt.gensalt(12)));
        logger.infof(BCrypt.hashpw("admin123", BCrypt.gensalt(12)));
    }
}
