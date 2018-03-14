/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.wrappers;

import com.matilda.wikitree.api.exceptions.WikiTreeRequestFailedException;
import com.matilda.wikitree.api.jsonclient.WikiTreeApiJsonSession;
import com.matilda.wikitree.api.util.WikiTreeApiUtilities;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

/**
 A WikiTree space profile.
 */

@SuppressWarnings("ALL")
public class WikiTreeSpaceProfile extends WikiTreeProfile {

    /**
     Construct a wrapper for a space profile.

     @param rval a {@link JSONObject} which is of the form returned by {@link WikiTreeApiJsonSession#getProfile(String)}.
     */

    public WikiTreeSpaceProfile( @NotNull JSONObject rval ) throws WikiTreeRequestFailedException {

        super( rval, new String[]{ "profile" } );

        if ( containsKey( "page_name" ) ) {

            setRequestType( WikiTreeRequestType.SPACE_NAME );

        } else {

            setRequestType( WikiTreeRequestType.UNKNOWN );

        }

    }

    /**
     Get this instance's name.

     @return This space profile's name.
     */

    public String getPageName() {

        return (String)getOriginalJSONObject().get( "page_name" );

    }

    /**
     Get this instance's PageId.

     @return This space profile's PageId.
     */

    public String getPageId() {

        return (String)WikiTreeApiUtilities.getMandatoryJsonValue( this, "PageId" );

    }

    public String toString() {

        return "WikiTreeSpaceProfile( \"" + getPageName() + "\", PageId=\"" + getPageId() + "\" )";

    }

}
