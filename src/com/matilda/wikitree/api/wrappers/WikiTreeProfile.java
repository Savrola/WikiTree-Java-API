/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.wrappers;

import com.matilda.wikitree.api.exceptions.ReallyBadNewsError;
import com.matilda.wikitree.api.exceptions.WikiTreeRequestFailedException;
import com.matilda.wikitree.api.jsonclient.WikiTreeApiJsonSession;
import com.matilda.wikitree.api.util.WikiTreeApiUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

/**
 Represent some kind of WikiTree profile.
 <p/>Note that this is an abstract class.
 The implementation of this class that is used to represent a particular profile will match the type of said profile.
 For example, person profiles are always represented by {@link WikiTreePersonProfile} instances whereas
 space profiles are always represented by {@link WikiTreeSpaceProfile} instances.
 */

public abstract class WikiTreeProfile extends WikiTreeWrapper {

    private final JSONObject _originalJSONObject;

    protected WikiTreeProfile( final @NotNull JSONObject jsonObject, final String[] profileLocation ) throws WikiTreeRequestFailedException {
        super( findProfile( jsonObject, profileLocation )

        );

        _originalJSONObject = jsonObject;

    }

    private static JSONObject findProfile( final @NotNull JSONObject jsonObject, final String[] profileLocation )
            throws WikiTreeRequestFailedException {


        // Apologies for using an inline-if in a call to {@code super()}.
        // This seems cleaner than the alternative of passing something arbitrary to this {@code super()} call and then
        // fixing things up later if we guessed wrong.

        // Were we handed a naked profile object?
        // I am not sure if profileLocation can be null but let's check it to avoid surprises.
        // After all, the caller's meaning is presumably clear if it is null - that the path is empty.

        if ( profileLocation == null || profileLocation.length == 0 ) {

            // Yes - that's what we want to encapsulate.

            return jsonObject;

        }

        // No - fetch the actual profile and encapsulate that.
        //
        // If the request worked then the profile will be where the caller told us to look.

        JSONObject profile = (JSONObject)WikiTreeApiUtilities.getOptionalJsonValue( JSONObject.class, jsonObject, profileLocation );
        if ( profile != null ) {

            return profile;

        }

        // The request appears to have failed.
        // We still have to encapsulate something or throw an exception.
        // Let's throw an exception as otherwise we risk the caller not realizing that they're getting junk.

        throw new WikiTreeRequestFailedException( "unable to wrap the result of a request that failed - \"" + jsonObject + "\"", jsonObject );

    }

    public JSONObject getOriginalJSONObject() {

        return _originalJSONObject;

    }

    /**
     Take a response to a {@link WikiTreeApiJsonSession#getProfile(WikiTreeId)} call and turn it into
     a {@link WikiTreePersonProfile} or a {@link WikiTreeSpaceProfile} as appropriate.

     @param jsonObject the JSON object of the form returned by a call to {@link WikiTreeApiJsonSession#getProfile(WikiTreeId)}.
     @return {@code null} if {@code jsonObject} is null. Otherwise, the provided {@code jsonObject} wrapped in a {@link WikiTreePersonProfile} or a {@link WikiTreeSpaceProfile} as appropriate.
     */

    @Nullable
    public static WikiTreeProfile distinguish( final JSONObject jsonObject ) throws WikiTreeRequestFailedException {

        WikiTreeProfile rval;
        Object responseObj = jsonObject.get( "profile" );
        if ( responseObj instanceof JSONObject ) {

            JSONObject response = (JSONObject)responseObj;
            JSONObject profile = response;

            Object isPersonObj = profile.get( "IsPerson" );
            if ( isPersonObj instanceof Number ? ( (Number)isPersonObj ).longValue() == 1L : "1".equals( isPersonObj ) ) {

                WikiTreePersonProfile personProfile =
                        new WikiTreePersonProfile( null, jsonObject, WikiTreePersonProfile.ProfileType.PROFILE, "profile" );

                rval = personProfile;

            } else {

                Object isSpaceObj = profile.get( "IsSpace" );
                if ( isSpaceObj instanceof Number ? ( (Number)isSpaceObj ).longValue() == 1L : "1".equals( isSpaceObj ) ) {

                    WikiTreeSpaceProfile spaceProfile = new WikiTreeSpaceProfile( jsonObject );

                    rval = spaceProfile;

                } else {

                    throw new ReallyBadNewsError( "WikiTreeProfile.distinguish:  unable to figure out what it is (" + jsonObject + ")" );

                }

            }

        } else if ( responseObj == null ) {

            rval = null;

        } else {

            throw new ReallyBadNewsError( "WikiTreeApiWrappersSession.getProfile:  asked for a profile, got a " +
                                          responseObj.getClass().getCanonicalName() );

        }

        return rval;

    }

    /**
     Get this instance's manager's {@code Person.Id}.

     @return this instance's {@code Person.Id} as a {@code long} value.
     @throws NullPointerException  if this instance has no \"Manager\" field.
     @throws NumberFormatException if this instance's {@code "Manager"} field is something other than a {@link Number} or a {@link String}
     which can be parsed by {@link Long#parseLong(String)} as a positive long value.
     */

    public long getManagerPersonId() {

        Object managerPersonId = get( "Manager" );
        if ( managerPersonId == null ) {

            throw new NullPointerException( "profile has no \"Manager\" field - " + this );

        }

        if ( managerPersonId instanceof Number ) {

            return ( (Number)managerPersonId ).longValue();

        }

        // I don't think that the Manager is ever a String but no harm in being permissive here.

        if ( managerPersonId instanceof String ) {

            //noinspection CaughtExceptionImmediatelyRethrown
            try {

                long id = Long.parseLong( (String)managerPersonId );

                if ( id <= 0 ) {

                    throw new NumberFormatException( "profile's \"Manager\" field is not a positive number - " + this );

                }
                return id;

            } catch ( NumberFormatException e ) {

                // Provide a place for a breakpoint.

                throw e;

            }

        } else {

            throw new NumberFormatException( "profile's \"Manager\" field is neither a String or a Number - " + this );

        }

    }

    public String toString() {

        return "WikiTreeProfile( " + super.toString() + " )";

    }

}
