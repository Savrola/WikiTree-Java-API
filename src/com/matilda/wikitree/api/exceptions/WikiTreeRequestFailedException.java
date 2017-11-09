/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.exceptions;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

/**
 Thrown if a WikiTree API request yielded a failure status.
 */

public class WikiTreeRequestFailedException extends Exception {

    private final JSONObject _resultObject;

    public WikiTreeRequestFailedException( final String why, @NotNull final JSONObject resultObject ) {

        super( why );

        _resultObject = resultObject;

    }

    public WikiTreeRequestFailedException( final String why, @NotNull final JSONObject resultObject, final Throwable e ) {

        super( why, e );

        _resultObject = resultObject;

    }

    @NotNull
    public JSONObject getResultObject() {

        return _resultObject;

    }

    public String toString() {

        return "WikiTreeRequestFailedException:  " + getMessage();

    }

}
