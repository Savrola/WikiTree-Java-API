/*
 * Copyright © 2017 Daniel Boulet
 * All rights reserved.
 */

package com.matilda.wikitree.api.exceptions;

/**
 Thrown if a WikiTree API login request yielded a failure status.
 */

public class WikiTreeLoginRequestFailedException extends Exception {

    public enum Reason {
        /**
         The .wtu file doesn't contain a user name
         */
        MISSING_USERNAME,
        /**
         The .wtu file doesn't contain a password line
         */
        MISSING_PASSWORD,
        /**
         The WikiTree server was unimpressed.
         */
        AUTHENTICATION_FAILED,
        /**
         We caught an exception during the actual login attempt to the WikiTree server. Use {@link #getCause} for more info.
         */
        CAUGHT_EXCEPTION,
        /**
         The args parameter must specify either one or two arguments.
         */
        TOO_MANY_ARGUMENTS,
        /**
         The specified .wtu file does not actually have a .wtu suffix.
         */
        INVALID_WTU_FILENAME

    }

    private final Reason _reason;

    public WikiTreeLoginRequestFailedException( final String why, final Reason reason ) {

        super( why );

        _reason = reason;

    }

    public WikiTreeLoginRequestFailedException( final String why, final Reason reason, final Throwable e ) {

        super( why, e );

        _reason = reason;

    }

    public Reason getReason() {

        return _reason;

    }

    public String toString() {

        return "WikiTreeLoginRequestFailedException:  " + getMessage();

    }

}
