/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.wrappers;

import com.matilda.wikitree.api.exceptions.ReallyBadNewsError;
import com.matilda.wikitree.api.exceptions.WikiTreeRequestFailedException;
import com.matilda.wikitree.api.jsonclient.WikiTreeApiJsonSession;
import com.matilda.wikitree.api.util.WikiTreeApiUtilities;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 Wrap someone's relatives.
 */

@SuppressWarnings({ "WeakerAccess", "unused" })
public class WikiTreeRelatives extends WikiTreeWrapper {

    private final SortedMap<String, WikiTreePersonProfile> _basePeopleByKey = new TreeMap<>();

    private final SortedMap<WikiTreeId, WikiTreePersonProfile> _basePeopleProfilesByWikiTreeId = new TreeMap<>();

    private final SortedMap<Long, WikiTreePersonProfile> _basePeopleProfilesByPersonId = new TreeMap<>();

    private final String _requestKeys;

    private final boolean _requestParents;

    private final boolean _requestChildren;

    private final boolean _requestSpouses;

    private final boolean _requestSiblings;

    private final boolean _allRelativeClassesRequest;

    /**
     Create a wrapper for the {@link JSONObject} provided by a call to {@link WikiTreeApiJsonSession#getRelatives(String, boolean, boolean, boolean, boolean)}.

     @param requestKeys     a comma separated list of WikiTree ID or Person.Id values for the profiles to return relatives for.
     Elements of the list will be treated as a Person.Id if they can be successfully parsed by {@link Integer#parseInt(String)}.
     For example, specifying {@code "5589,Hozier-1"} would return the relatives of Sir Winston S. Churchill (Person.Id 5589) and his wife Baroness Clementine Churchill (Hozier-1).
     @param requestParents  {@code true} returns parents of the specified profiles.
     @param requestChildren {@code true} returns children of the specified profiles.
     @param requestSpouses  {@code true} returns spouses of the specified profiles.
     @param requestSiblings {@code true} returns siblings of the specified profiles.
     */

    public WikiTreeRelatives(
            final String requestKeys,
            final boolean requestParents,
            final boolean requestChildren,
            final boolean requestSpouses,
            final boolean requestSiblings,
            final JSONObject requestObject
    ) throws WikiTreeRequestFailedException {

        super( requestObject );

        _requestKeys = requestKeys;
        _requestParents = requestParents;
        _requestChildren = requestChildren;
        _requestSpouses = requestSpouses;
        _requestSiblings = requestSiblings;
        _allRelativeClassesRequest = requestParents && requestChildren && requestSpouses && requestSiblings;

        JSONArray basePeople = (JSONArray)WikiTreeApiUtilities.getOptionalJsonValue( JSONArray.class, requestObject, "items" );
        if ( basePeople != null ) {

            for ( Object basePersonObject : basePeople ) {

                if ( basePersonObject instanceof JSONObject ) {

                    JSONObject jsonBasePerson = (JSONObject)basePersonObject;

                    // Figure out what this profile's key is?
                    // It MUST be a String (anything else means that we have really misunderstood what's going on here).
                    // If the String is parseable as an integer then it is a Person.Id.
                    // Otherwise, it can only be a WikiTree ID.

                    String key = (String)WikiTreeApiUtilities.getMandatoryJsonValue( String.class, jsonBasePerson, "key" );
                    Object keyValue = WikiTreeApiJsonSession.interpretIdParameter( "WikiTreeRelatives", key );
                    WikiTreeRequestType requestType =
                            keyValue instanceof Number ? WikiTreeRequestType.PERSON_ID : WikiTreeRequestType.WIKITREE_ID;

                    WikiTreePersonProfile basePersonProfile = new WikiTreePersonProfile(
                            requestType,
                            jsonBasePerson,
                            isAllRelativeClassesRequest()
                                    ?
                                    WikiTreePersonProfile.ProfileType.RELATIVES_COMPLETE
                                    :
                                    WikiTreePersonProfile.ProfileType.RELATIVES_INCOMPLETE,
                            "person"
                    );

                    _basePeopleByKey.put( key, basePersonProfile );
                    _basePeopleProfilesByWikiTreeId.put( basePersonProfile.getWikiTreeId(), basePersonProfile );
                    _basePeopleProfilesByPersonId.put( basePersonProfile.getPersonId(), basePersonProfile );

                } else {

                    throw new ReallyBadNewsError(
                            "WikiTreeRelatives:  found a " +
                            ( basePersonObject == null ? "null" : basePersonObject.getClass().getCanonicalName() ) +
                            " in items array"
                    );

                }

            }

        }

    }

    /**
     Determine if this was a request for all relative classes.
     @return {@code true} if all of the class selection booleans were {@code true}, {@code false} otherwise.
     */

    public boolean isAllRelativeClassesRequest() {

        return _allRelativeClassesRequest;

    }

    /**
     Get the keys that were specified on the original call to {@link WikiTreeApiWrappersSession#getRelatives(String, boolean, boolean, boolean, boolean)}.

     @return a comma separated list of WikiTreeIDs and Person.Ids.
     */

    public String requestKeys() {

        return _requestKeys;

    }

    /**
     Determine if the original call to {@link WikiTreeApiWrappersSession#getRelatives(String, boolean, boolean, boolean, boolean)} asked for
     parents to be returned.

     @return true if parents were requested; false otherwise.
     */

    public boolean requestParents() {

        return _requestParents;

    }

    /**
     Determine if the original call to {@link WikiTreeApiWrappersSession#getRelatives(String, boolean, boolean, boolean, boolean)} asked for
     children to be returned.

     @return true if children were requested; false otherwise.
     */

    public boolean requestChildren() {

        return _requestChildren;

    }

    /**
     Determine if the original call to {@link WikiTreeApiWrappersSession#getRelatives(String, boolean, boolean, boolean, boolean)} asked for
     spouses to be returned.

     @return true if spouses were requested; false otherwise.
     */

    public boolean requestSpouses() {

        return _requestSpouses;

    }

    /**
     Determine if the original call to {@link WikiTreeApiWrappersSession#getRelatives(String, boolean, boolean, boolean, boolean)} asked for
     siblings to be returned.

     @return true if siblings were requested; false otherwise.
     */

    public boolean requestSiblings() {

        return _requestSiblings;

    }

    /**
     Get the base people's profiles returned by the call to {@link WikiTreeApiWrappersSession#getRelatives(String, boolean, boolean, boolean, boolean)} indexed by their keys.
     A base person is a person who was specified in the list of keys specified in the call to WikiTreeApiWrappersSession.getRelatives.

     @return an unmodifiable mapping of key values to the profile that they yielded.
     */

    public SortedMap<String, WikiTreePersonProfile> getBasePeopleByKey() {

        return Collections.unmodifiableSortedMap( _basePeopleByKey );

    }

    /**
     Get the base people's profiles returned by the call to {@link WikiTreeApiWrappersSession#getRelatives(String, boolean, boolean, boolean, boolean)} indexed by their WikiTreeID.
     A base person is a person who was specified in the list of keys specified in the call to WikiTreeApiWrappersSession.getRelatives.
     <p/>Note that if the same person's profile is specified by both Person.Id and WikiTree ID in the list of keys specified in the original
     call to WikiTreeApiWrappersSession.getRelatives then only one of the copies of the returned profiles will appear in this mapping.
     Use the {@link #getBasePeopleByKey()} method if you must see all of the profiles, including duplicates, returned by the original request.

     @return an unmodifiable mapping of WikiTree IDs to the profiles that they yielded.
     */

    public SortedMap<WikiTreeId, WikiTreePersonProfile> getBasePeopleByWikiTreeID() {

        return Collections.unmodifiableSortedMap( _basePeopleProfilesByWikiTreeId );

    }

    /**
     Get the base people's profiles returned by the call to {@link WikiTreeApiWrappersSession#getRelatives(String, boolean, boolean, boolean, boolean)} indexed by their Person.Id.
     A base person is a person who was specified in the list of keys specified in the call to WikiTreeApiWrappersSession.getRelatives.
     <p/>Note that if the same person's profile is specified by both Person.Id and WikiTree ID in the list of keys specified in the original
     call to WikiTreeApiWrappersSession.getRelatives then only one of the copies of the returned profiles will appear in this mapping.
     Use the {@link #getBasePeopleByKey()} method if you must see all of the profiles, including duplicates, returned by the original request.

     @return an unmodifiable mapping of Person.Ids to the profiles that they yielded.
     */

    public SortedMap<Long, WikiTreePersonProfile> getBasePeopleByPersonId() {

        return Collections.unmodifiableSortedMap( _basePeopleProfilesByPersonId );

    }

    private void rememberParent(
            final SortedMap<Long, WikiTreePersonProfile> profiles,
            final SortedMap<Long, WikiTreePersonProfile> myParent,
            final String relationship,
            final WikiTreePersonProfile personProfile
    ) {

        Number parentNumber = (Number)WikiTreeApiUtilities.getOptionalJsonValue( Number.class, personProfile, relationship );
        if ( parentNumber != null ) {

            WikiTreePersonProfile parent = profiles.get( parentNumber.longValue() );
            if ( parent != null ) {

                myParent.put( personProfile.getPersonId(), parent );

            }

        }

    }

    public String toString() {

        return
                "WikiTreeRelatives( " +
                "requestKeys=\"" + requestKeys() + "\", " +
                "requestParents=" + requestParents() + ", " +
                "requestChildren=" + requestChildren() + ", " +
                "requestSpouses=" + requestSpouses() + ", " +
                "requestSiblings=" + requestSiblings() +
                ")";

    }

}
