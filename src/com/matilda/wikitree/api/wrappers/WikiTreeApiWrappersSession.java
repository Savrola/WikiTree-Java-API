/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.wrappers;

import com.matilda.wikitree.api.WikiTreeApiClient;
import com.matilda.wikitree.api.exceptions.WikiTreeRequestFailedException;
import com.matilda.wikitree.api.jsonclient.WikiTreeApiJsonSession;
import com.matilda.wikitree.api.util.WikiTreeApiUtilities;
import com.sun.corba.se.spi.monitoring.StatisticsAccumulator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Optional;
import java.util.SortedSet;

/**
 An API that operates as a layer on top of the API provided by {@link WikiTreeApiJsonSession}.
 <p/>
 Let's start with a simple example.
 The first step in using this API is to create an instance of this class.
 In most cases, using the default constructor will do what needs to be done (we'll see an example shortly that uses this class' other constructor):
 <pre>
 {@code WikiTreeApiWrappersSession wSession = new WikiTreeApiWrappersSession();}
 </pre>
 Now let's get the WikiTree profile for Sir Winston S. Churchill (his WikiTree ID is {@code Churchill-4}).
 <pre>
 {@code churchillWikiTreeId = new WikiTreeId( "Churchill-4" );}
 </pre>
 There are two ways of getting someone's profile and each way provides a slightly different set of information.
 Let's start with the way that gets us the most information:
 <pre>
 {@code WikiTreePersonProfile churchillByWikiTreeID = wSession.getPerson( churchillWikiTreeId );}
 </pre>
 This will get us Churchill's profile complete with links to his parents, his spouse, his sibling, and his children.
 Note that since Churchill's profile's privacy is "open", we able to request and obtain any of his profile information without needing
 to login to WikiTree first.
 <p/>Profiles and other information returned by Let's print out the profile (including Churchill's spouse and relatives) obtained just above:
 <pre>
 {@code WikiTreeApiUtilities.prettyPrintJsonThing( "Winston S. Churchill's person instance", churchillByWikiTreeID );}
 </pre>
 If we happen to know that Churchill's WikiTree {@code Person.Id} is 5589 then we can also get this same version of his profile using
 <pre>
 {@code WikiTreePersonProfile churchillByPersonId = wSession.getPerson( 5589 );}
 </pre>
 Also, if we're not interested in Churchill's wife or his other immediate relatives then we can get just his profile using either of the next two statements:
 <pre>
 {@code WikiTreePersonProfile justWinstonByWikiTreeId = wSession.getPersonProfile( churchillWikiTreeId ); }
 {@code WikiTreePersonProfile justWinstonByPersonId = wSession.getPersonProfile( 5589 ); }
 </pre>
 You should probably take a look at the {@code getPerson} and {@code getProfile} requests on the WikiTree API's Help page for a better understanding of
 the difference between these two types of requests.
 <p/>
 Up until this point, we've been accessing the WikiTree API server without bothering to authenticate ourselves (i.e. without bothering to login).
 While that works for public profiles and such, we are bound to reach a point where we want to access profiles which are rather less open.
 This will require us to authenticate ourselves using a WikiTree user account that has the rather-less-open profile on their watchlist.
 <p/>
 Since everybody's watchlist is different, we're going to pretend that we have the totally imaginary profile associated with the WikiTree ID
 {@code Churchill-123456789} on our watchlist (note that this really is an imaginary WikiTree ID - there is
 no profile on WikiTree associated with WikiTree ID {@code Churchill-123456789}; you can try to fetch this profile if you like but what you'll get is
 an opportunity to explore what happens when you request a non-existent profile).
 <p/>
 In order to access rather-less-open profiles that are on our watchlist, we need to 'login' to the WikiTree API server
 using our {@link WikiTreeApiWrappersSession} instance:
 <pre>
 {@code if ( wSession.login( "username@example.com", "hello-world" ) &lbrace;
 // login worked - the session referenced by wSession is now authenticated
 System.out.println( "Successful login to WikiTree API server by username@example.com" );
 } else {
 // login failed
 System.err.println( "*Failed login to WikiTree API server - bye!" );
 System.exit( 1 );
 }
 </pre>
 <p>
 A couple of things to note here:
 <ul>
 <li>Most of the methods in this API that take a WikiTree ID insist that the WikiTree ID be encapsulated within a {@link WikiTreeId} instance.</li>
 <li>The {@link #getPerson(WikiTreeId)} method takes a {@link WikiTreeId} instance which specifies which person's profile we're interested in.
 The {@link WikiTreeId#WikiTreeId(String)} constructor takes a string containing the WikiTree ID, performs fairly rudimentary vetting of the WikiTree ID
 (see {@link WikiTreeId} for more info), and, assuming that the specified WikiTree ID passes vetting,
 returns a {@code WikiTreeId} instance which represents the specified WikiTree ID (if the specified WikiTree ID fails vetting then the constructor
 throws a {@link IllegalArgumentException}).</li>
 <li>The {@link WikiTreeApiWrappersSession#getPerson(WikiTreeId)} method uses its {@link WikiTreeApiJsonSession} to retrieve a bundle which,
 in this case, contains
 Winston S. Churchill's profile as well as the profiles for his father, his mother, his one sibling, his one spouse, and his five children).</li>
 </ul>
 <p/>If the WikiTreeId constructor concludes that the specified WikiTree ID cannot possibly be valid (doesn't end in a minus followed by a number or has nothing
 in front of the minus sign) then
 The methods in this API that take WikiTree IDs expect the WikiTree ID to be wrapped within a {@link WikiTreeId} instance.
 <p>
 <p/>An instance of this class provides a session-style interface to the WikiTree API server.
 The actual communication with the WikiTree API server is handled by a {@link WikiTreeApiJsonSession} instance which is encapsulated within
 each {@code WikiTreeApiWrappersSession} instance. In other words, calls to most of the methods in this class result in calls to
 similarly if not identically named methods in the encapsulated {@code WikiTreeApiWrappersSession} instance.
 <p/>
 <p/>The first step in using the wrappers API is to create a session object.
 The easiest and probably most common way to do that is to just use this class' default constructor:
 <pre>
 {@code WikiTreeApiWrappersSession wSession = new WikiTreeApiWrappersSession();}</pre>
 <p/>Note that a {@code WikiTreeApiJsonSession} can be wrapped at any time.
 For example, the {@code WikiTreeApiJsonSession} could be wrapped upon creation like this:
 <pre>
 {@code WikiTreeApiWrappersSession wrapper = new WikiTreeApiWrappersSession(
 new WikiTreeApiJsonSession()
 );}
 </pre>
 or, probably more sensibly, like this:
 <pre>
 {@code WikiTreeApiWrappersSession wrapper = new WikiTreeApiWrappersSession();}
 </pre>
 Alternatively, a {@code WikiTreeApiJsonSession} instance could be used as is for a while and then later wrapped like this:
 <pre>
 {@code WikiTreeApiJsonSession jsonClient = new WikiTreeApiJsonSession();}<br>
 .<br>
 . <em>(various things including method calls on the {@code jsonClient} variable happen here)</em><br>
 .<br>
 {@code WikiTreeApiWrappersSession wrapper = new WikiTreeApiWrappersSession( jsonClient );}
 </pre>
 Once wrapped, either the {@code WikiTreeApiWrappersSession} wrapper or the wrapped {@code WikiTreeApiJsonSession} can be used to make API calls.
 For example, one could do the following:
 <pre>
 {@code WikiTreeApiJsonSession jsonClient = new WikiTreeApiJsonSession();}<br>
 {@code WikiTreeApiWrappersSession wrapper = new WikiTreeApiWrappersSession( jsonClient );}<br>
 JSONObject profileObject = jsonClient.get( "Churchill-4" );<br>
 {@code WikiTreePersonProfile churchillFamilyGroup = wrapper.getPerson( "Churchill-4" );}<br>
 </pre>
 One could even mimic a call to {@code getPerson} as follows:
 <pre>
 {@code WikiTreeApiJsonSession jsonClient = new WikiTreeApiJsonSession();}<br>
 {@code JSONObject jsonChurchillFamily = jsonClient.getPerson( "Churchill-4" );}<br>
 {@code WikiTreePersonProfile wrappedFamilyGroup = new WikiTreePersonProfile( null, jsonChurchillFamily, "person" );}
 </pre>
 */

@SuppressWarnings({ "WeakerAccess", "SameParameterValue" })
public class WikiTreeApiWrappersSession implements WikiTreeApiClient {

    private final WikiTreeApiJsonSession _jsonClient;

    private boolean _authenticated = false;

    /**
     Wrap a new {@link WikiTreeApiJsonSession} instance.
     */

    public WikiTreeApiWrappersSession() {

        this( new WikiTreeApiJsonSession() );

    }

    /**
     Wrap an existing {@code WikiTreeApiJsonSession} instance.

     @param wikiTreeApiJsonSession the {@code WikiTreeApiJsonSession} which this instance is to wrap.
     */

    public WikiTreeApiWrappersSession( final @NotNull WikiTreeApiJsonSession wikiTreeApiJsonSession ) {

        super();

        _jsonClient = wikiTreeApiJsonSession;

    }

    /**
     Get the email address of the WikiTree user for whom the wrapped {@link WikiTreeApiJsonSession} is authenticated.

     @return the email address of the user for whom this session is authenticated; {@code null} if this session is not authenticated.
     <p/>See {@link WikiTreeApiJsonSession#getAuthenticatedUserEmailAddress()} for more information.
     */

    @Override
    public String getAuthenticatedUserEmailAddress() {

        return _jsonClient.getAuthenticatedUserEmailAddress();

    }

    /**
     Get the WikiTree ID of the user for whom this session is authenticated.

     @return the WikiTree ID of the user for whom this session is authenticated; {@code null} if this session is not authenticated.
     <p/>See {@link WikiTreeApiJsonSession#getAuthenticatedWikiTreeId()} for more information.
     */

    @Override
    public String getAuthenticatedWikiTreeId() {

        return _jsonClient.getAuthenticatedWikiTreeId();

    }

    /**
     Login to the WikiTree API server.
     <p/>Assuming that {@code jsonClient} refers to a {@link WikiTreeApiJsonSession} instance,
     this method is exactly equivalent to
     <blockquote>
     {@code jsonClient.login( }<em>{@code emailAddress}</em>{@code , }<em>{@code password}</em>{@code );}
     </blockquote>
     See {@link WikiTreeApiJsonSession#login(String, String)} for more information.

     @param emailAddress the email address associated with a www.wikitree.com account.
     @param password     the password associated with the same www.wikitree.com account.
     @return {@code true} if the login worked; {@code false otherwise}.
     @throws IOException    if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException if this client is unable to process the response from the WikiTree API server (definitely see {@link WikiTreeApiJsonSession#login(String, String)} for more information).
     */

    public boolean login( final @NotNull String emailAddress, final @NotNull String password )
            throws IOException, ParseException {

        if ( _jsonClient.login( emailAddress, password ) ) {

            _authenticated = true;

        } else {

            _authenticated = false;
            System.err.println( "authentication failed for \"" + emailAddress + "\"" );

        }

        return isAuthenticated();

    }

    @Override
    public String getBaseServerUrlString() {

        return getWrappedJsonWikiTreeApiClient().getBaseServerUrlString();

    }

    @Override
    public String getLoginResultStatus() {

        return _jsonClient.getLoginResultStatus();

    }

    /**
     Get the {@link WikiTreeApiJsonSession} instance that this instance wraps.

     @return the {@link WikiTreeApiJsonSession} instance that this instance wraps.
     */

    public WikiTreeApiJsonSession getWrappedJsonWikiTreeApiClient() {

        return _jsonClient;

    }

    @Override
    public boolean isAuthenticated() {

        return _authenticated;

    }

    /**
     Request information about a specified person (someone with a WikiTree profile).

     @param key the specified person's WikiTree ID or Person.Id.
     This parameter will be treated as a Person.Id if it can be successfully parsed by {@link Integer#parseInt(String)}.
     For example, specify either {@code "Churchill-4"} (his WikiTree ID) or {@code "5589"} (his Person.Id) to request information about Winston S. Churchill (UK Prime Minister during the Second World War).
     @return A {@link WikiTreePersonProfile} containing the profile information for the specified person, their parents, their siblings, and their children ({@see WikiTreePersonProfile} for more info).
     @throws IOException                    if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException                 if this client is unable to process the response from the WikiTree API server. Seeing this exception should be a rather rare occurrence.
     If you do see one, you have probably encountered a bug in this software. Please notify danny@matilda.com if you get this exception (be prepared to work with Danny
     to reproduce the problem).
     @throws WikiTreeRequestFailedException if the WikiTree API server returned a result which is not a profile. This returned result is contained
     within the {@link WikiTreeRequestFailedException} instance. It seems to always have a string field named {@code "status"} which specifies
     what went wrong (not sure what possible values can appear - sorry). It also seems to always have one other string field which
     specifies the key that was being used for the {@code getPerson} request. The name of this other field is either {@code "user_name"} or {@code "user_id"}
     depending on whether the request specified a WikiTree ID or a numeric Person.Id respectively.
     */

    public WikiTreePersonProfile getPerson( final @NotNull WikiTreeId key )
            throws IOException, ParseException, WikiTreeRequestFailedException {

        @SuppressWarnings("UnnecessaryLocalVariable")
        WikiTreePersonProfile rval = getPerson( key, "*" );

        return rval;

    }

    public WikiTreePersonProfile getPerson( final long personId )
            throws IOException, ParseException, WikiTreeRequestFailedException {

        @SuppressWarnings("UnnecessaryLocalVariable")
        WikiTreePersonProfile rval = getPerson( personId, "*" );

        return rval;

    }

    public WikiTreePersonProfile getPerson( final @NotNull long personId, final String fields )
            throws IOException, ParseException, WikiTreeRequestFailedException {

        WikiTreePersonProfile rval = getPerson( "" + personId, fields );

        return rval;

    }

    public WikiTreePersonProfile getPerson( final @NotNull WikiTreeId key, final String fields )
            throws IOException, ParseException, WikiTreeRequestFailedException {

        WikiTreePersonProfile rval = getPerson( key.getValueString(), fields );

        return rval;

    }


    /**
     Request information about a specified person (someone with a WikiTree profile).

     @param key    a {@link String} containing the specified person's WikiTree ID or Person.Id.
     If this parameter yields a positive value when parsed by {@link Long#longValue()} then it will be treated as a Person.Id.
     Otherwise, if the string ends in a minus sign followed by a positive number then it will be treated as a WikiTree ID.
     Any other value will result in an {@link IllegalArgumentException} being thrown.
     Otherwise, it is assumed to be a WikiTree ID (family name followed by a minus sign followed by a positive number).
     For example, specify either {@code "Churchill-4"} (his WikiTree ID) or {@code "5589"} (his Person.Id) to request information about Winston S. Churchill (UK Prime Minister during the Second World War).
     <p/>
     This method constructs and returns a {@code WikiTreePersonProfile} instance using the {@link JSONObject} returned by the underlying call to {@link WikiTreeApiJsonSession#getPerson(String, String)} method.
     <p/>
     Regardless of what the caller asks for, this method always returns the value of the {@code "Name"} and {@code "IsLiving"} fields as they are required for the proper functioning of the
     construction of the {@code WikiTreePersonProfile} instance (it's a long story but the {@code "Name"} field contains the pretty-much-essential WikiTree ID of the returned profile
     and the presence of the {@code "IsLiving"} field is used to verify that call to the WikiTree API server returned a {@code JSONObject} containing the expected sort of "bag of information").
     <p/>
     Many of the getters in the resulting {@code WikiTreePersonProfile} instance will either fail (possibly with exceptions being thrown) or return useless or missing information if
     corresponding fields are not requested.
     For example, failing to ask for the {@code "Gender"} field will cause invocations of {@link WikiTreePersonProfile#getGender()} on the resulting
     {@code WikiTreePersonProfile} instance to return {@link com.matilda.wikitree.api.WikiTreeApiClient.BiologicalGender#UNKNOWN}.
     Another example is that failing to ask for the {@code "Parents"} field will cause invocations of either
     {@link WikiTreePersonProfile#getBiologicalFather()} or {@link WikiTreePersonProfile#getBiologicalMother()} on the resulting
     {@code WikiTreePersonProfile} instance to return {@code null} because the profiles returned by {@code WikiTreePersonProfile} class's {@code getBiologicalFather()} and {@code getBiologicalMother()}
     get their information from the returned {@code JSONObject}'s {@code "Parents"} field.
     @param fields a comma separated list of the fields that you want returned. Specifying {@code "*"} will get you all the available fields.
     <p/>
     See {@link WikiTreeApiUtilities#constructExcludedGetPersonFieldsSet(SortedSet)},
     {@link WikiTreeApiUtilities#constructExcludedGetPersonFieldsSet(String[])}, and
     {@link WikiTreeApiUtilities#constructGetPersonFieldsString(String[])}
     for a relatively painless way to construct a value for this parameter which includes all but a few of the available fields.
     For example,
     <blockquote>
     <pre>
     WikiTreeApiUtilities.constructGetPersonFieldsString(
     WikiTreeApiUtilities.constructExcludedGetPersonFieldsSet(
     "Spouses", "Children", "Siblings"
     )
     )
     </pre>
     </blockquote>
     will construct a {@code fields} parameter value which fetches everything except the lists of spouses, children and siblings.
     <p/>Excluding the {@code "Spouses"}, {@code "Children"}, and {@code "Siblings"} fields probably makes sense in a lot of situations as it has the potential to
     reduce the size of the response by eliminating potentially a fair number of profiles.
     @return A {@link WikiTreePersonProfile} containing the profile information for the specified person, their parents, their siblings, and their children.
     @throws IOException    if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException if this client is unable to process the response from the WikiTree API server. Seeing this exception should be a rather rare occurrence.
     If you do see one, you have probably encountered a bug in this software. Please notify danny@matilda.com if you get this exception (be prepared to work with Danny
     to reproduce the problem).
     */

    public WikiTreePersonProfile getPerson( final @NotNull String key, final String fields )
            throws IOException, ParseException, WikiTreeRequestFailedException {

        // We need the request to include the "Name" field.

        StringBuilder sb = new StringBuilder( fields );

        if ( !"*".equals( fields ) ) {

            if ( !( "," + fields + "," ).contains( ",Name," ) ) {

                sb.append( ",Name" );

            }

            if ( !( "," + fields + "," ).contains( ",IsLiving," ) ) {

                sb.append( ",IsLiving" );

            }

        }

        Optional<JSONObject> optRval = _jsonClient.getPerson( key, sb.toString() );
        if ( optRval.isPresent() ) {

            // Create a WikiTreePersonProfile instance for the result.
            // The instructor invoked here requires that the profile have a "Name" field.
            // This is why we forced it into the list of requested fields above.

            return new WikiTreePersonProfile(
                    null,
                    optRval.get(),
                    "*".equals( fields ) ? WikiTreePersonProfile.ProfileType.PRIMARY_PERSON : WikiTreePersonProfile.ProfileType.OTHER,
                    "person"
            );

        } else {

            return null;

        }

    }

    @SuppressWarnings("Duplicates")
    public WikiTreeProfile getProfile( final WikiTreeId key )
            throws IOException, ParseException, WikiTreeRequestFailedException {

        Optional<JSONObject> optResultObject = _jsonClient.getProfile( key );

        if ( optResultObject.isPresent() ) {

            WikiTreeProfile rval;

            rval = WikiTreeProfile.distinguish( optResultObject.get() );

            return rval;

        } else {

            return null;    // No such fish.

        }

    }

    @SuppressWarnings("Duplicates")
    public WikiTreeProfile getProfile( final long id )
            throws IOException, ParseException, WikiTreeRequestFailedException {

        Optional<JSONObject> optResultObject = _jsonClient.getProfile( id );

        if ( optResultObject.isPresent() ) {

            WikiTreeProfile rval;

            rval = WikiTreeProfile.distinguish( optResultObject.get() );

            return rval;

        } else {

            return null;    // No such fish.

        }

    }

    //    // Not much point in providing an interface to get a person's profile via their
    //    // page id since I don't know how to get a person's page id.
    //    // Note that a person's page id is NOT the same thing as their Person.Id.
    //
    //    public WikiTreePersonProfile getPersonProfile( final long pageId )
    //            throws IOException, ParseException, WikiTreeRequestFailedException {
    //
    //        WikiTreeProfile profile = getProfile( pageId );
    //        if ( profile == null || profile instanceof WikiTreePersonProfile ) {
    //
    //            return (WikiTreePersonProfile)profile;
    //
    //        } else {
    //
    //            throw new IllegalArgumentException( "WikiTreeApiWrappersSession.getPersonProfile:  pageId \"" +
    //                                                pageId +
    //                                                "\" refers to a Space, not a Person" );
    //
    //        }
    //
    //    }

    /**
     Get a person's profile via their {@link WikiTreeId}.
     @param wikiTreeId the person's {@code WikiTreeId}.
     @return a {@link WikiTreePersonProfile} containing the person's profile if it exists; {@code null} otherwise.
     @throws IOException when something goes wrong in network land.
     @throws ParseException when the result of the request to the WikiTree server cannot be interpreted at this end.
     @throws WikiTreeRequestFailedException if the request fails.
     */

    @Nullable
    public WikiTreePersonProfile getPersonProfile( final WikiTreeId wikiTreeId )
            throws IOException, ParseException, WikiTreeRequestFailedException {

        WikiTreeProfile profile = getProfile( wikiTreeId );
        if ( profile == null ) {

            return null;

        } else {

            if ( profile instanceof WikiTreePersonProfile ) {

                return (WikiTreePersonProfile)profile;

            } else {

                throw new IllegalArgumentException( "WikiTreeApiWrappersSession.getPersonProfile:  wikiTreeId \"" + wikiTreeId + "\" refers to a Space, not a Person" );

            }

        }

    }

    /**
     Get a 'just the basic facts' profile for a specified person.

     @param personId The specified person's long {@code Person.Id}.
     @return a {@link WikiTreePersonProfile} containing the person's
     {@code Id}, {@code Name}, {@code Derived.ShortName}, {@code LastNameAtBirth}, {@code Gender},
     {@code BirthDate}, {@code DeathDate}, {@code BirthDateDecade}, and {@code DeathDateDecade}.
     @throws IOException                    if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException                 if this client is unable to process the response from the WikiTree API server.
     Seeing this exception should be a rather rare occurrence. If you do see one, you have probably encountered a bug in this software.
     Please notify danny@matilda.com if you get this exception (be prepared to work with Danny to reproduce the problem).
     @throws WikiTreeRequestFailedException if the WikiTree API server returned a result which is not a profile.
     See {@link #getPerson(WikiTreeId)} for more info.
     */

    public WikiTreePersonProfile getBasicPersonProfile( final long personId )
            throws ParseException, WikiTreeRequestFailedException, IOException {

        WikiTreePersonProfile rval = getPerson(
                personId,
                "Id,Name,Derived.ShortName,LastNameAtBirth,Gender,BirthDate,DeathDate,BirthDateDecade,DeathDateDecade"
        );

        return rval;

    }

    /**
     Get a 'just the basic facts' profile for a specified person.

     @param wikiTreeId The specified person's WikiTree ID.
     @return a {@link WikiTreePersonProfile} containing the person's
     {@code Id}, {@code Name}, {@code Derived.ShortName}, {@code LastNameAtBirth}, {@code Gender},
     {@code BirthDate}, {@code DeathDate}, {@code BirthDateDecade}, and {@code DeathDateDecade}.
     @throws IOException                    if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException                 if this client is unable to process the response from the WikiTree API server.
     Seeing this exception should be a rather rare occurrence. If you do see one, you have probably encountered a bug in this software.
     Please notify danny@matilda.com if you get this exception (be prepared to work with Danny to reproduce the problem).
     @throws WikiTreeRequestFailedException if the WikiTree API server returned a result which is not a profile.
     See {@link #getPerson(WikiTreeId)} for more info.
     */

    public WikiTreePersonProfile getBasicPersonProfile( final WikiTreeId wikiTreeId )
            throws ParseException, WikiTreeRequestFailedException, IOException {

        WikiTreePersonProfile rval = getPerson(
                wikiTreeId,
                "Id,Name,Derived.ShortName,LastNameAtBirth,Gender,BirthDate,DeathDate,BirthDateDecade,DeathDateDecade"
        );

        return rval;

    }

    public WikiTreeBiography getBio( final WikiTreeId key )
            throws IOException, ParseException {

        Optional<JSONObject> optResultObject = _jsonClient.getBio( key );

        return optResultObject.map( jsonObject -> new WikiTreeBiography( key, jsonObject ) ).orElse( null );

    }

    @SuppressWarnings("unchecked")
    public WikiTreeWatchlist getWatchlist(
            final Boolean getPerson,
            final Boolean getSpace,
            final Boolean onlyLiving,
            final Boolean excludeLiving,
            final String fields,
            final Integer limit,
            final Integer offset,
            final String order
    )
            throws IOException, ParseException, WikiTreeRequestFailedException {

        Optional<JSONObject> optResultObject =
                _jsonClient.getWatchlist( getPerson, getSpace, onlyLiving, excludeLiving, fields, limit, offset, order );

        if ( optResultObject.isPresent() ) {

            return new WikiTreeWatchlist( getPerson, getSpace, onlyLiving, excludeLiving, fields, limit, offset, order, optResultObject.get() );

        } else {

            return null;

        }

    }

    public WikiTreeAncestors getAncestors( final WikiTreeId key, final Integer depth )
            throws IOException, ParseException, WikiTreeRequestFailedException {

        Optional<JSONObject> optRequestObject = _jsonClient.getAncestors( key, depth );

        if ( optRequestObject.isPresent() ) {

            return new WikiTreeAncestors( key, depth, optRequestObject.get() );

        } else {

            return null;

        }
    }

    public WikiTreeAncestors getAncestors( final long key, final Integer depth )
            throws IOException, ParseException, WikiTreeRequestFailedException {

        Optional<JSONObject> optRequestObject = _jsonClient.getAncestors( key, depth );

        if ( optRequestObject.isPresent() ) {

            return new WikiTreeAncestors(
                    WikiTreeId.getWikiTreeId( optRequestObject.get() ),
                    depth,
                    optRequestObject.get()
            );

        } else {

            return null;

        }
    }

    public WikiTreeRelatives getRelatives(
            final String keys,
            final boolean getParents,
            final boolean getChildren,
            final boolean getSpouses,
            final boolean getSiblings
    )
            throws IOException, ParseException, WikiTreeRequestFailedException {

        Optional<JSONObject> optRequestObject = _jsonClient.getRelatives( keys, getParents, getChildren, getSpouses, getSiblings );

        if ( optRequestObject.isPresent() ) {

            @SuppressWarnings("UnnecessaryLocalVariable")
            WikiTreeRelatives rval = new WikiTreeRelatives( keys, getParents, getChildren, getSpouses, getSiblings, optRequestObject.get() );

            return rval;

        } else {

            return null;

        }

    }

    public static StatisticsAccumulator getTimingStats() {

        return WikiTreeApiJsonSession.getTimingStats();

    }

    public String toString() {

        return "WikiTreeApiWrappersSession( url=" + getBaseServerUrlString() + " )";

    }

}
