/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.wrappers;

import com.matilda.wikitree.api.exceptions.ReallyBadNewsError;
import com.matilda.wikitree.api.jsonclient.WikiTreeApiJsonSession;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

/**
 %%% Something clever goes here.
 */
public class WikiTreeBiography extends WikiTreeWrapper {

    /**
     Create a biography from the specified JSON object.

     @param key       the key that was used to fetch the biography (used to determine the request type).
     @param bioObject the specified JSON object.
     Assumed to be in the form returned by {@link WikiTreeApiJsonSession#getBio(WikiTreeId)}.
     */

    public WikiTreeBiography( final WikiTreeId key, @NotNull final JSONObject bioObject ) {

        super( bioObject );

        setRequestType( WikiTreeRequestType.WIKITREE_ID );

    }

    /**
     Get the person's Person.Id.

     @return the person's Person.Id.
     */

    public int getPersonId() {

        Object personIdObj = get( "user_id" );
        if ( personIdObj == null ) {

            return -1;

        } else if ( personIdObj instanceof String ) {

            try {

                return Integer.parseInt( (String)personIdObj );

            } catch ( NumberFormatException e ) {

                throw new ReallyBadNewsError( "WikiTreeBiography.getPersonId:  user_id is not parseable as an integer" );

            }

        } else if ( personIdObj instanceof Number ) {

            return ( (Number)personIdObj ).intValue();

        } else {

            throw new ReallyBadNewsError( "WikiTreeBiography.getPersonId:  user_id is not a String, a Number or null; it's a " +
                                          personIdObj.getClass().getCanonicalName() );

        }

    }

    /**
     Get the person's WikiTree ID.

     @return the person's WikiTree ID or {@code "Id=" + getPersonId()} if the person's WikiTree ID is unavailable (not sure if that is even possible).
     */

    @NotNull
    public String getWikiTreeId() {

        Object wikiTreeIdObj = get( "page_name" );
        if ( wikiTreeIdObj == null ) {

            return "Id=" + getPersonId();

        } else if ( wikiTreeIdObj instanceof String ) {

            return (String)wikiTreeIdObj;

        } else {

            throw new ReallyBadNewsError( "WikiTreePersonProfile.getWikiTreeId:  WikiTreeId is neither null or a String; it's a " +
                                          wikiTreeIdObj.getClass().getCanonicalName() );

        }

    }

    /**
     Get the bio string.
     */

    @NotNull
    public String getBio() {

        Object bioObj = get( "bio" );
        if ( bioObj instanceof String ) {

            return (String)bioObj;

        } else if ( bioObj == null ) {

            throw new ReallyBadNewsError( "WikiTreeBiography.getBio:  no bio (should be impossible)" );

        } else {

            throw new ReallyBadNewsError( "WikiTreeBiography.getBio:  bio is not a String; it's a " + bioObj.getClass().getCanonicalName() );

        }

    }

    public String toString() {

        return "WikiTreeBio( PageName=" + getWikiTreeId() + ", Id=" + getPersonId() + ", bio=" + getBio() + " )";

    }

}
