/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.wrappers;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

/**
 Wrap a JSONObject in something that might prove useful in the long run.
 <p/>Currently, just an empty class except for a method and associated field that reports the type of thing that is being wrapped.
 */

public class WikiTreeWrapper extends JSONObject {

    private WikiTreeRequestType _requestType = WikiTreeRequestType.UNSPECIFIED;

    protected WikiTreeWrapper( final @NotNull JSONObject jsonObject ) {

        super( jsonObject );

    }

    protected void setRequestType( final @NotNull WikiTreeRequestType requestType ) {

        if ( requestType == WikiTreeRequestType.UNSPECIFIED ) {

            throw new IllegalArgumentException(
                    "WikiTreeWrapper.setRequestType:  cannot set request type to " + WikiTreeRequestType.UNSPECIFIED
            );

        } else if ( _requestType == WikiTreeRequestType.UNSPECIFIED ) {

            _requestType = requestType;

        } else {

            throw new IllegalArgumentException(
                    "WikiTreeWrapper.setRequestType:  request type can only be set once (is " + _requestType +
                    ", asked to set to " + requestType + ")"
            );

        }

    }

    public WikiTreeRequestType getRequestType() {

        return _requestType;

    }

    public String toString() {

        return "WikiTreeWrapper( " + super.toString() + " )";

    }

}
