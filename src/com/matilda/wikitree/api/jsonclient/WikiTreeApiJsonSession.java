/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.jsonclient;

import com.matilda.wikitree.api.WikiTreeApiClient;
import com.matilda.wikitree.api.exceptions.ReallyBadNewsError;
import com.matilda.wikitree.api.util.WikiTreeApiUtilities;
import com.matilda.wikitree.api.wrappers.WikiTreeId;
import com.sun.corba.se.spi.monitoring.StatisticsAccumulator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Optional;
import java.util.Vector;

/**
 A purely(?) JSON-based Java implementation of the WikiTree API.
 <p/>See <a href="https://www.wikitree.com/wiki/Help:API_Documentation">https://www.wikitree.com/wiki/Help:API_Documentation</a>
 for more information about the original WikiTree API (intended for use within web pages).
 */

@SuppressWarnings({ "WeakerAccess", "unused", "SameParameterValue" })
public class WikiTreeApiJsonSession implements WikiTreeApiClient {

    /**
     The default base URL used to access the WikiTree API server.
     <p/>This URL will be augmented with appropriate query parameters for each request.
     For example, a {@code getPerson} request for Winston S. Churchill would use the following URL to make the request to the default WikiTree API server:
     <blockquote>{@code https://apps.wikitree.com/api.php?format=json&action=getPerson&fields=*&key=Churchill-4}</blockquote>
     <p/>See {@link #WikiTreeApiJsonSession(String)} if you want to use a different URL.
     */

    public static final String DEFAULT_BASE_SERVER_URL_STRING = "https://apps.wikitree.com/api.php";

    private static final StatisticsAccumulator s_innerRequestStats = new StatisticsAccumulator( "ms" );

    private static int _miniServerPort;

    private static String _miniServerUrlString;

    private String _baseServerUrlString;

    private Vector<String> _loginCookies;

    private String _authenticatedUserEmailAddress;

    private String _authenticatedWikiTreeId;

    private String _loginResultStatus;

    private static boolean s_showUrls = false;

    private JSONObject _lastLoginResult;

    /**
     Create a reusable anonymous WikiTree API client instance which sends its requests to the production WikiTree API server.
     <p/>Requests made via an anonymous client instance are only able to access WikiTree information which is publicly available.
     If you want access to the same information that you are able to access on www.WikiTree.com when you've logged in using your WikiTree account
     then you must convert this anonymous client instance into an authenticated client instance using your WikiTree account (i.e. email address) and associated password.
     <p/>See {@link #login(String, String)} for more information.
     */

    public WikiTreeApiJsonSession() {

        this( null );

    }

    /**
     Specify whether or not the actual URLs used to connect to the WikiTree API server should be printed on System.out.
     <p/>This is intended for debuggin' purposes. In order to provide better password security, URLs for login requests are never printed.

     @param showUrls {@code true} if URLs should be printed on System.out; {@code false} if not.
     */

    public static void setShowUrls( final boolean showUrls ) {

        s_showUrls = showUrls;

    }

    /**
     Create a reusable anonymous WikiTree API client instance which sends its requests to a specified WikiTree API server.

     @param baseServerUrlString the URL of the specified WikiTree API server.
     <p/>See {@link #WikiTreeApiJsonSession()} for more information about anonymous vs authenticated WikiTree API client instances.
     */

    public WikiTreeApiJsonSession( final String baseServerUrlString ) {

        _baseServerUrlString = baseServerUrlString == null ? DEFAULT_BASE_SERVER_URL_STRING : baseServerUrlString;

    }

    /**
     Determine if this session is using the default base server URL string.
     <p/>Assuming that the variable {@code instance} is an instance of this class, this method is exactly equivalent to
     <blockquote>{@code WikiTreeApiJsonSession.DEFAULT_BASE_SERVER_URL_STRING.equals( instance.getBaseServerUrlString() )}</blockquote>
     <p/>See {@link #DEFAULT_BASE_SERVER_URL_STRING}, {@link #getBaseServerUrlString()} and {@link #WikiTreeApiJsonSession(String)} for more information.

     @return {@code true} if this instance's base server URL string is, ignoring differences in upper vs lower case, textually equal to the value of {@link #DEFAULT_BASE_SERVER_URL_STRING}; {@code false} otherwise.
     */

    public boolean isDefaultBaseServer() {

        return DEFAULT_BASE_SERVER_URL_STRING.equals( getBaseServerUrlString() );

    }

    /**
     Get this instance's server URL.

     @return the base URL that this instance will augment when processing API requests.
     */

    @Override
    public String getBaseServerUrlString() {

        return _baseServerUrlString;

    }

    /**
     Get the most recent login attempt's result status.

     @return {@code "Success"} if the most recent attempt works. Other return values are possible including
     {@code "Illegal"} if the supplied email address does not correspond to a valid WikiTree user account or
     {@code "WrongPass"} if the supplied password is incorrect. Other return values are possible (sorry).
     The return value will be {@code null} if this method is called before
     an attempt to login via this instance has occurred.
     */

    @Override
    public String getLoginResultStatus() {

        return _loginResultStatus;

    }

    /**
     Determine if this instance is authenticated.
     <p/>An instance is considered to be authenticated if any WikiTree API requests that are made via said instance are made with
     the login credentials of a WikiTree user.
     This in turn implies that at least one call to one of this class's {@code login} methods has occurred and that the last such call succeeded.

     @return {@code true} if this instance is authenticated; {@code false} otherwise.
     <p/>See either {@link #login(String, String)} for more information.
     */

    @Override
    public boolean isAuthenticated() {

        return _loginCookies != null;

    }

    /**
     Get the email address of the WikiTree user for whom this session is authenticated.

     @return the email address of the user for whom this session is authenticated; {@code null} if this session is not authenticated.
     <p/>See {@link #isAuthenticated()} or {@link #login(String, String)} for more information.
     */

    @Override
    public String getAuthenticatedUserEmailAddress() {

        return _authenticatedUserEmailAddress;

    }

    /**
     Get the WikiTree ID of the user for whom this session is authenticated.

     @return the WikiTree ID of the user for whom this session is authenticated; {@code null} if this session is not authenticated.
     <p/>See {@link #isAuthenticated()} or {@link #login(String, String)} for more information.
     */

    @Override
    public String getAuthenticatedWikiTreeId() {

        return _authenticatedWikiTreeId;

    }

    /**
     Login to the WikiTree API server.
     <p/>See {@link WikiTreeApiClient#login(String, String)} for more info.
     */

    @SuppressWarnings("unchecked")
    @Override
    public boolean login( @NotNull final String emailAddress, @NotNull final String password )
            throws IOException, ParseException {

        JSONObject requestParams = new JSONObject();
        requestParams.put( "action", "login" );
        requestParams.put( "email", emailAddress );
        requestParams.put( "password", password );
        requestParams.put( "fields", "*" );
        requestParams.put( "format", "json" );

        // Make the login attempt.
        // Note that we don't need to do anything with the result of this call since it will have been
        // preserved in {@code _lastLoginRequest} by {@link #requestViaHttpGet(JSONObject)}
        // when it intercepts and processes the login attempt that this call to {@code makeRequest} is making.

        makeRequest( requestParams );

	/*
     Figure out if the login worked.
	 */

        boolean worked = analyzeLoginResult( emailAddress, _lastLoginResult );

        // Paranoia rules supreme!

        if ( !worked || _loginCookies == null ) {

            _authenticatedWikiTreeId = null;
            _authenticatedUserEmailAddress = null;
            _loginCookies = null;

        }

	/*
    Note that a successful login request results in the server sending us http cookies which must be provided on all future
	calls to the server via this instance. These cookies are grabbed by one of the rather low level routines that this method
	calls (quite indirectly). Consequently, the only sure way to determine if this call actually worked is to check if this instance has
	references to these login cookies. This check must be done just before this method returns. Consequently, we do it here.
	 */

        return _loginCookies != null;

    }

    /**
     Get the result from the most recent login attempt.

     @return the {@link JSONObject} returned by the WikiTree API server.
     */

    @NotNull
    public Optional<JSONObject> getLastLoginResult() {

        return Optional.ofNullable( _lastLoginResult );

    }

    /**
     Analyze the result of a login attempt.
     <p/>This method also erases any authentication information left by a previous successful login attempt and records
     equivalent authentication information if this login attempt is deemed to have worked.

     @param emailAddress the email address used to identify the WikiTree user requesting the login.
     @param resultObject the value returned by the login request to the WikiTree API server.
     @return {@code true} if the login worked; {@code false} otherwise.
     */

    private boolean analyzeLoginResult( @NotNull final String emailAddress, @Nullable final JSONObject resultObject ) {

        // Start off by assuming that the login attempt failed.

        _authenticatedWikiTreeId = null;
        _authenticatedUserEmailAddress = null;
        _loginResultStatus = null;

        // Did we back the auxiliary information that we should have gotten back from a login attempt?

        // First, we need to make sure that we got at least a JSON result object back.

        if ( resultObject == null ) {

            // It cannot possibly have worked completely if we didn't get a JSON result object back.
            // If we did get login cookies back then the screw up is in the logic on our end or we don't understand the protocol
            // (which also means that the screwup is in the logic on our end).

            if ( _loginCookies != null ) {

                throw new ReallyBadNewsError( "WikiTreeApiJsonSession.login:  did not get a result object even though we got login cookies" );

            }

        } else {

            // We got a result object back. Let's see what's inside.

            // Does it contain a "login" object?

            Object actualResultObj = resultObject.get( "login" );
            if ( actualResultObj instanceof JSONObject ) {

                // We got a "login" object. Does it contain a "result" string?

                JSONObject actualResult = (JSONObject)actualResultObj;
                Object resultStatus = actualResult.get( "result" );
                if ( resultStatus instanceof String ) {

                    _loginResultStatus = (String)resultStatus;

                    // Is the result string equal to "Success"?

                    if ( "Success".equals( _loginResultStatus ) ) {

                        // The login seems to have completely worked.
                        // Let's extract user's WikiTree ID and remember the email address used to do this authentication.

                        Object wikiTreeIdObj = actualResult.get( "username" );
                        if ( wikiTreeIdObj instanceof String ) {

                            _authenticatedWikiTreeId = (String)wikiTreeIdObj;
                            _authenticatedUserEmailAddress = emailAddress;

                        } else if ( wikiTreeIdObj == null ) {

                            throw new ReallyBadNewsError(
                                    "JSonWikiTreeApiClient.login:  request result's \"login\" object does not contain a \"username\" entity"
                            );

                        } else {

                            throw new ReallyBadNewsError(
                                    "JSonWikiTreeApiClient.login:  request result's \"login\" object's \"username\" entity is not a string " +
                                    "(it is a " + wikiTreeIdObj.getClass().getCanonicalName() + ")"
                            );

                        }

                    }

                } else if ( resultStatus == null ) {

                    throw new ReallyBadNewsError(
                            "JSonWikiTreeApiClient.login:  request result's \"login\" object does not contain a \"result\" entity"
                    );

                } else {

                    // The "result" entity is not a string.

                    throw new ReallyBadNewsError(
                            "JSonWikiTreeApiClient.login:  request result's \"login\" object's \"result\" entity " +
                            "is not a string " +
                            "(it is a " + resultStatus.getClass().getCanonicalName() + ")"
                    );

                }

            } else if ( actualResultObj == null ) {

                throw new ReallyBadNewsError( "JSonWikiTreeApiClient.login:  request result does not contain a \"login\" entity" );

            } else {

                throw new ReallyBadNewsError(
                        "JSonWikiTreeApiClient.login:  request result's \"login\" entity is not a JSON object " +
                        "(it is a " + actualResultObj.getClass().getCanonicalName() + ")"
                );

            }

        }

        // If the login worked then the authenticated WikiTree ID, the authenticated user email address and the login cookies must not be null.
        // If the login failed then all three must be null.
        // Let's make sure that that is what happened.

        boolean wtiNull = _authenticatedWikiTreeId == null;
        boolean aueaNull = _authenticatedUserEmailAddress == null;
        boolean lcNull = _loginCookies == null;

        // Are some but not all of them null?

        if ( wtiNull != aueaNull || aueaNull != lcNull ) {

            // Delete the login cookies if the above analysis screwed up.
            // This may seem a bit brutal but the alternative is to end up with an authenticated session
            // which we don't know who it is authenticated for.

            Vector<String> loginCookies = _loginCookies;
            _loginCookies = null;

            throw new ReallyBadNewsError( "JSonWikiTreeApiClient.login:  supposedly " +
                                          ( lcNull ? "failed" : "successful" ) + " login request " +
                                          "did not yield " + ( lcNull ? "null" : "non-null" ) + " values for all of " +
                                          "authenticated WikiTree ID (got " + _authenticatedWikiTreeId + "), " +
                                          "authenticated User Email Address (got " + _authenticatedUserEmailAddress + "), and " +
                                          "login cookies (got " +
                                          (
                                                  loginCookies == null
                                                          ?
                                                          "null"
                                                          :
                                                          loginCookies.size() + " cookie" + ( loginCookies.size() == 1 ? "" : "s" )
                                          ) + ")"
            );

        }

        return _loginCookies != null;

    }

    /**
     Request information about a specified person (someone with a WikiTree profile).

     @param key the specified person's WikiTree ID.
     @return A {@link JSONObject} containing the profile information for the specified person, their parents, their siblings, and their children.
     @throws IOException    if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException if this client is unable to process the response from the WikiTree API server. Seeing this exception should be a rather rare occurrence.
     If you do see one, you have probably encountered a bug in this software. Please notify danny@matilda.com if you get this exception (be prepared to work with Danny
     to reproduce the problem).
     */

    @NotNull
    public Optional<JSONObject> getPerson( @NotNull final WikiTreeId key )
            throws IOException, ParseException {

        @SuppressWarnings("UnnecessaryLocalVariable")
        Optional<JSONObject> rval = getPerson( key, "*" );

        return rval;

    }

    /**
     Request information about a specified person (someone with a WikiTree profile).

     @param personId the specified person's {@code Person.Id}.
     @return A {@link JSONObject} containing the profile information for the specified person, their parents, their siblings, and their children.
     @throws IOException    if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException if this client is unable to process the response from the WikiTree API server. Seeing this exception should be a rather rare occurrence.
     If you do see one, you have probably encountered a bug in this software. Please notify danny@matilda.com if you get this exception (be prepared to work with Danny
     to reproduce the problem).
     */

    @NotNull
    public Optional<JSONObject> getPerson( final long personId )
            throws IOException, ParseException {

        @SuppressWarnings("UnnecessaryLocalVariable")
        Optional<JSONObject> rval = getPerson( personId, "*" );

        return rval;

    }

    /**
     Request information about a specified person (someone with a WikiTree profile).

     @param key    the specified person's WikiTree ID.
     This parameter will be treated as a Person.Id if it can be successfully parsed by {@link Integer#parseInt(String)}.
     For example, specify either {@code "Churchill-4"} (his WikiTree ID) or {@code "5589"} (his Person.Id) to request information about Winston S. Churchill (UK Prime Minister during the Second World War).
     @param fields a comma separated list of the fields that you want returned. Specifying {@code "*"} will get you all the available fields
     (see {@link WikiTreeApiUtilities#constructGetPersonFieldsString(String[])} for a relatively painless way to construct a value for this parameter which
     includes all but a few of the available fields).
     @return A JSONObject containing the profile information for the specified person, their parents, their siblings, and their children.
     @throws IOException    if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException if this client is unable to process the response from the WikiTree API server. Seeing this exception should be a rather rare occurrence.
     If you do see one, you have probably encountered a bug in this software. Please notify danny@matilda.com if you get this exception (be prepared to work with Danny
     to reproduce the problem).
     */

    @SuppressWarnings("unchecked")
    @NotNull
    public Optional<JSONObject> getPerson( @NotNull final WikiTreeId key, final String fields )
            throws IOException, ParseException {

        return getPerson( key.getValueString(), fields );
//        JSONObject requestParams = new JSONObject();
//        requestParams.put( "action", "getPerson" );
//        requestParams.put( "key", key.getValueString() ); // interpretIdParameter( "getPerson", key ) );
//        requestParams.put( "fields", fields );
//        requestParams.put( "format", "json" );
//
//        JSONObject resultObject = makeRequest( requestParams );
//
//        if ( resultObject == null ) {
//
//            return null;    // No such fish.
//
//        } else {
//
//            return resultObject;
//
//        }

    }

    @NotNull
    public Optional<JSONObject> getPerson( final long personId, final String fields )
            throws IOException, ParseException {

        return getPerson( "" + personId, fields );
//        JSONObject requestParams = new JSONObject();
//        requestParams.put( "action", "getPerson" );
//        requestParams.put( "key", personId ); // interpretIdParameter( "getPerson", key ) );
//        requestParams.put( "fields", fields );
//        requestParams.put( "format", "json" );
//
//        JSONObject resultObject = makeRequest( requestParams );
//
//        if ( resultObject == null ) {
//
//            return null;    // No such fish.
//
//        } else {
//
//            return resultObject;
//
//        }

    }

    @SuppressWarnings("unchecked")
    @NotNull
    public Optional<JSONObject> getPerson( final String key, final String fields )
            throws IOException, ParseException {

        JSONObject requestParams = new JSONObject();
        requestParams.put( "action", "getPerson" );
        requestParams.put( "key", key ); // interpretIdParameter( "getPerson", key ) );
        requestParams.put( "fields", fields );
        requestParams.put( "format", "json" );

        Optional<JSONObject> optResultObject = makeRequest( requestParams );

        return optResultObject;
//        return optResultObject == null ? Optional.empty() : Optional.of( optResultObject );
//        if ( optResultObject == null ) {
//
//            return null;    // No such fish.
//
//        } else {
//
//            return optResultObject;
//
//        }

    }

    public static Object interpretIdParameter( final String who, @NotNull final String key ) {

        if ( key.length() == 0 ) {

            throw new IllegalArgumentException( who + ":  key must not be an empty string" );

        } else {

            try {

                @SuppressWarnings("UnnecessaryLocalVariable")
                long personId = Long.parseLong( key );

                return personId;

            } catch ( NumberFormatException e ) {

                return key;

            }

        }

    }

    /**
     Request a person profile or a Free-Space Profile.
     <p/>See the <b>getProfile</b> section
     <a href="https://www.wikitree.com/wiki/Help:API_Documentation">https://www.wikitree.com/wiki/Help:API_Documentation</a>
     for more information about requesting profiles.

     @param key The Person.Name (e.g. {@code "Churchill-4"}) or the Space.page_name (e.g. {@code "Space:Allied_POW_camps") of the profile of interest.
     Use {@link #getProfile(long)} if all you've got is the page id or the profile of interest.
     @return a JSONObject containing the requested information.
     @throws IOException    if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException if this client is unable to process the response from the WikiTree API server. Seeing this exception should be a rather rare occurrence.
     If you do see one, you have probably encountered a bug in this software. Please notify danny@matilda.com if you get this exception (be prepared to work with Danny
     to reproduce the problem).
     @throws IOException
     @throws ParseException
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public Optional<JSONObject> getProfile( final WikiTreeId key )
            throws IOException, ParseException {

        JSONObject requestParams = new JSONObject();
        requestParams.put( "action", "getProfile" );
        requestParams.put( "key", key.getValueString() );
        requestParams.put( "format", "json" );

        Optional<JSONObject> optResultObject = makeRequest( requestParams );
        return optResultObject;

    }

    /**
     Request a person profile or a Free-Space Profile.
     <p/>See the <b>getProfile</b> section
     <a href="https://www.wikitree.com/wiki/Help:API_Documentation">https://www.wikitree.com/wiki/Help:API_Documentation</a>
     for more information about requesting profiles.

     @param profileId The page id or the profile of interest (use {@link #getProfile(WikiTreeId)} if you have the profile's WikiTree Id (e.g. {@code "Churchill-4"}) or its Space.page name (e.g. {@code "Space:Allied_POW_camps").
     @return a JSONObject containing the requested information.
     @throws IOException    if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException if this client is unable to process the response from the WikiTree API server. Seeing this exception should be a rather rare occurrence.
     If you do see one, you have probably encountered a bug in this software. Please notify danny@matilda.com if you get this exception (be prepared to work with Danny
     to reproduce the problem).
     @throws IOException
     @throws ParseException
     */

    @NotNull
    public Optional<JSONObject> getProfile( final long profileId )
            throws IOException, ParseException {

        JSONObject requestParams = new JSONObject();
        requestParams.put( "action", "getProfile" );
        requestParams.put( "key", profileId );
        requestParams.put( "format", "json" );

        Optional<JSONObject> optResultObject = makeRequest( requestParams );
        return optResultObject;

    }

    /**
     Request the biography for a specified person (someone with a WikiTree profile).

     @param key the specified person's WikiTree ID or Person.Id.
     This parameter will be treated as a Person.Id if it can be successfully parsed by {@link Integer#parseInt(String)}.
     For example, specify either {@code "Churchill-4"} (his WikiTree ID) or {@code "5589"} (his Person.Id) to request the biography section of Winston S. Churchill's profile.
     @return A JSONObject containing the specified person's biography.
     @throws IOException    if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException if this client is unable to process the response from the WikiTree API server. Seeing this exception should be a rather rare occurrence.
     If you do see one, you have probably encountered a bug in this software. Please notify danny@matilda.com if you get this exception (be prepared to work with Danny
     to reproduce the problem).
     */

    @SuppressWarnings("unchecked")
    @NotNull
    public Optional<JSONObject> getBio( final WikiTreeId key )
            throws IOException, ParseException {

        JSONObject requestParams = new JSONObject();
        requestParams.put( "action", "getBio" );
        requestParams.put( "key", key.getValueString() );
        requestParams.put( "format", "json" );

        Optional<JSONObject> optResultObject = makeRequest( requestParams );
        return optResultObject;

    }

    /**
     Request the profile's which are on the logged-in user's watchlist.
     <p/>Note that calls to this method will result in an {@link IllegalArgumentException} being thrown if the anonymous WikiTree API client instance.

     @param getPerson     if {@code true} or {@code null} then person profiles on the user's watchlist will be returned;
     otherwise, if {@code false} then person profiles are excluded.
     @param getSpace      if {@code true} or {@code null} then space profiles on the user's watchlist will be returned;
     if {@code false} then space profiles are excluded.
     @param onlyLiving    if {@code true} then only person profiles of living people on the user's watchlist will be returned
     (i.e. person profiles for dead people will be excluded);
     otherwise, if {@code false} or {@code null} then person profiles for both living and dead people on the user's watchlist will be returned.
     @param excludeLiving if {@code true} then only person profiles of dead people on the user's watchlist will be returned
     (i.e. person profiles for living people will be excluded);
     otherwise, if {@code false} or {@code null} then person profiles for both living and dead people on the user's watchlist will be returned.
     This parameter will be treated as a Person.Id if it can be successfully parsed by {@link Integer#parseInt(String)}.
     @param fields        a comma separated list of the fields to be returned; if {@code "*"} or {@code null} then all fields are returned.
     @param limit         if {@code null} then a maximum of 100 profiles are returned;
     otherwise, specifies the maximum number of profiles to be returned.
     @param offset        if {@code null} then all profiles (up to the limit specified by the <b>limit</b>} parameter) are returned;
     otherwise, specifies how many profiles should be skipped.
     @param order         if {@code null} then the returned profiles are sorted by their <b>user_id</b>;
     otherwise, the returned profiles are sorted by the field named by this parameter.
     @return A JSONObject containing the requested profiles.
     @throws IllegalArgumentException if this method is called on an anonymous WikiTree API client instance.
     @throws IOException              if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException           if this client is unable to process the response from the WikiTree API server.
     Seeing this exception should be a rather rare occurrence. If you do see one, you have probably
     encountered a bug in this software. Please notify danny@matilda.com if you get this exception
     (be prepared to work with Danny to reproduce the problem).
     */

    @SuppressWarnings("unchecked")
    @NotNull
    public Optional<JSONObject> getWatchlist(
            final Boolean getPerson,
            final Boolean getSpace,
            final Boolean onlyLiving,
            final Boolean excludeLiving,
            final String fields,
            final Integer limit,
            final Integer offset,
            final String order
    )
            throws IOException, ParseException {

        JSONObject requestParams = new JSONObject();
        requestParams.put( "action", "getWatchlist" );

        if ( getPerson != null ) {

            requestParams.put( "getPerson", getPerson.booleanValue() ? 1 : 0 );

        }

        if ( getSpace != null ) {

            requestParams.put( "getSpace", getSpace.booleanValue() ? 1 : 0 );

        }

        if ( onlyLiving != null && onlyLiving.booleanValue() ) {

            requestParams.put( "onlyLiving", 1 );

        }

        if ( excludeLiving != null && excludeLiving.booleanValue() ) {

            requestParams.put( "excludeLiving", 1 );

        }

        if ( fields != null ) {

            requestParams.put( "fields", fields );

        }

        if ( limit != null ) {

            requestParams.put( "limit", limit.intValue() );

        }

        if ( offset != null ) {

            requestParams.put( "offset", offset.intValue() );

        }

        if ( order != null ) {

            requestParams.put( "order", order );

        }

        requestParams.put( "format", "json" );

        Optional<JSONObject> optResultObject = makeRequest( requestParams );
        return optResultObject;

//        if ( optResultObject == null ) {
//
//            return null;    // No such fish.
//
//        } else {
//
//            return optResultObject;
//
//        }

    }

    /**
     Request the ancestors of a specified person (someone with a WikiTree profile).

     @param key   the specified person's WikiTree ID or Person.Id.
     This parameter will be treated as a Person.Id if it can be successfully parsed by {@link Integer#parseInt(String)}.
     For example, specify either {@code "Churchill-4"} (his WikiTree ID) or {@code "5589"} (his Person.Id)
     to request the biography section of Winston S. Churchill's profile.
     @param depth how many generations back to retrieve. if {@code null} then a depth of 5 is used. Valid values are 1-10.
     @return A JSONObject containing the specified person's ancestors.
     @throws IOException    if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException if this client is unable to process the response from the WikiTree API server. Seeing this exception
     should be a rather rare occurrence. If you do see one, you have probably encountered a bug in this
     software. Please notify danny@matilda.com if you get this exception (be prepared to work with Danny
     to reproduce the problem).
     */

    @SuppressWarnings("unchecked")
    @NotNull
    public Optional<JSONObject> getAncestors( @NotNull final WikiTreeId key, @Nullable final Integer depth )
            throws IOException, ParseException {

        JSONObject requestParams = new JSONObject();
        requestParams.put( "action", "getAncestors" );
        requestParams.put( "key", key.getValueString() );

        if ( depth != null ) {

            requestParams.put( "depth", depth.intValue() );

        }

        requestParams.put( "format", "json" );

        Optional<JSONObject> optResultObject = makeRequest( requestParams );
        return optResultObject;

//        if ( optResultObject == null ) {
//
//            return null;    // No such fish.
//
//        } else {
//
//            return optResultObject;
//
//        }

    }

    /**
     Request the immediate relatives of a specified person (someone with a WikiTree profile).

     @param keys        a comma separated list of WikiTree ID or Person.Id values for the profiles to return relatives for.
     Elements of the list will be treated as a Person.Id if they can be successfully parsed by {@link Integer#parseInt(String)}.
     For example, specifying {@code "5589,Hozier-1"} would return the relatives of Sir Winston S. Churchill (Person.Id 5589)
     and his wife Baroness Clementine Churchill (Hozier-1).
     @param getParents  {@code true} returns parents of the specified profiles.
     @param getChildren {@code true} returns children of the specified profiles.
     @param getSpouses  {@code true} returns spouses of the specified profiles.
     @param getSiblings {@code true} returns siblings of the specified profiles.
     @return A JSONObject containing the specified person's ancestors.
     @throws IOException    if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException if this client is unable to process the response from the WikiTree API server. Seeing this exception
     should be a rather rare occurrence. If you do see one, you have probably encountered a bug in this
     software. Please notify danny@matilda.com if you get this exception (be prepared to work with Danny
     to reproduce the problem).
     */

    @SuppressWarnings("unchecked")
    @NotNull
    public Optional<JSONObject> getRelatives(
            final String keys,
            final boolean getParents,
            final boolean getChildren,
            final boolean getSpouses,
            final boolean getSiblings
    )
            throws IOException, ParseException {

        JSONObject requestParams = new JSONObject();
        requestParams.put( "action", "getRelatives" );
        requestParams.put( "keys", keys );
        requestParams.put( "getParents", getParents ? 1 : 0 );
        requestParams.put( "getChildren", getChildren ? 1 : 0 );
        requestParams.put( "getSpouses", getSpouses ? 1 : 0 );
        requestParams.put( "getSiblings", getSiblings ? 1 : 0 );
        requestParams.put( "format", "json" );

        Optional<JSONObject> optResultObject = makeRequest( requestParams );
        return optResultObject;

//        if ( optResultObject == null ) {
//
//            return null;    // No such fish.
//
//        } else {
//
//            return optResultObject;
//
//        }

    }

    @NotNull
    private Optional<JSONObject> makeRequest( final JSONObject requestObject )
            throws IOException, ParseException {

//	System.out.println( "... starting @ " + WikiTreeApiUtilities.formatStandardMs( new Date() ) );
        long startTime = System.currentTimeMillis();
        Optional<JSONObject> rval = requestViaHttpGet( requestObject );
        long endTime = System.currentTimeMillis();
//	System.out.println( "... done @ " + WikiTreeApiUtilities.formatStandardMs( new Date() ) );

        synchronized ( s_innerRequestStats ) {

            double delta = ( endTime - startTime ) / 1000.0;
            s_innerRequestStats.sample( delta );
//	    System.out.println( "delta = " + delta );

        }

        return rval;

    }

    @NotNull
    private synchronized Optional<JSONObject> requestViaHttpGet( final JSONObject requestObject )
            throws IOException, ParseException {

        String who = "requestViaHttpGet";

        String what = "formatting request URL";

        StringBuffer requestSb = new StringBuffer( _baseServerUrlString );
        WikiTreeApiUtilities.formatRequestAsUrlQueryParameters( who, requestObject, requestSb );

        String actualUrlString = requestSb.toString();

        try {

            if ( s_showUrls ) {

                if ( actualUrlString.contains( "action=login" ) ) {

                    System.out.println( "not showing URL for login request (protects the password)" );

                } else {

                    System.out.println( "URL will be " + actualUrlString );

                }

            }

            URL actualUrl = new URL( actualUrlString );

            what = "initializing connection";

            URLConnection urlConnection = actualUrl.openConnection();
            HttpURLConnection connection = (HttpURLConnection)urlConnection;
            connection.setDoOutput( false );
            connection.setDoInput( true );
            connection.setRequestProperty( "Accept", "application/json" );
            if ( !"login".equals( requestObject.get( "action" ) ) && _loginCookies != null ) {

//		System.out.println( "it's not a login attempt and we have cookies" );

                StringBuilder sb = new StringBuilder();
                String semiColon = "";
                for ( String cookie : _loginCookies ) {

                    sb.append( semiColon ).append( cookie.split( ";", 2 )[0] );
                    semiColon = "; ";

                }

                connection.addRequestProperty( "Cookie", sb.toString() );

//		System.out.println( "done adding in cookies" );

            }

            connection.setRequestMethod( "GET" );

            what = "getting response";

            Object rval = WikiTreeApiUtilities.readResponse( connection, true );

            if ( "login".equals( requestObject.get( "action" ) ) ) {

                what = "It's a login attempt";

                // Throw away the old cookies regardless of whether or not the login request worked.

                _loginCookies = null;
                _authenticatedUserEmailAddress = null;
                _authenticatedWikiTreeId = null;

                _lastLoginResult = (JSONObject)rval;
                if ( _lastLoginResult == null ) {

                    System.err.println( "got null response from login attempt for " + requestObject.get( "email" ) );

                } else {

                    JSONObject loginObject = (JSONObject)_lastLoginResult.get( "login" );
                    if ( loginObject == null ) {

                        System.err.println( "got response from login attempt for " +
                                            requestObject.get( "email" ) +
                                            " which doesn't contain a \"login\" response object" );

                    } else {

                        String resultString = (String)loginObject.get( "result" );
                        if ( "Success".equals( resultString ) ) {

//			    System.out.println( "login worked!" );

                            Collection<String> cookies = connection.getHeaderFields().get( "Set-Cookie" );
                            if ( cookies != null ) {

                                _loginCookies = new Vector<>( cookies );

//				for ( String cookie : _loginCookies ) {
//
//				    System.out.println( "cookie:  " + WikiTreeApiUtilities.enquoteJavaString( cookie ) );
//
//				}
//
//				System.out.println( "done printing cookies" );

                            }

                        }

                    }

                }

            }

            if ( rval == null ) {

                return Optional.empty();

            } else if ( rval instanceof JSONObject ) {

                return Optional.of( (JSONObject)rval );

            } else {

                throw new ReallyBadNewsError( "requestViaJsonGet:  expected a JSONObject, got this instead:  " + rval );

            }

        } catch ( RuntimeException e ) {

            System.err.println( "unable to issue GET with \"" + requestSb + "\" (doing " + what + "):  " + what );

            e.printStackTrace();

            throw e;

        }

    }

//    private JSONObject requestViaJsonPost( JSONObject requestObject )
//            throws IOException, ParseException {
//
//        String requestString = JSONObject.toJSONString( requestObject );
//
//        String what = "initializing connection";
//
//        try {
//
//            URL serverUrl = new URL( _baseServerUrlString );
//
//            URLConnection urlConnection = serverUrl.openConnection();
//            HttpURLConnection connection = (HttpURLConnection)urlConnection;
//            connection.setDoOutput( true );
//            connection.setDoInput( true );
//            connection.setRequestProperty( "Content-Type", "application/json; charset=UTF-8" );
//            connection.setRequestProperty( "Accept", "application/json" );
//            connection.setRequestMethod( "POST" );
//
//            what = "posting request";
//
//            try ( OutputStream writer = connection.getOutputStream() ) {
//
//                writer.write( ( requestString + '\n' ).getBytes( "UTF-8" ) );
//                writer.flush();
//
//            } finally {
//
//                System.out.println( "writer closed" );
//
//            }
//
//            what = "getting response";
//
//            @SuppressWarnings("UnnecessaryLocalVariable")
//            Object rval = WikiTreeApiUtilities.readResponse( connection, true );
//
//            if ( rval == null || rval instanceof JSONObject ) {
//
//                return (JSONObject)rval;
//
//            } else {
//
//                throw new ReallyBadNewsError( "requestViaJsonPost:  expected a JSONObject, got this instead:  " + rval );
//
//            }
//
//        } catch ( IOException | ParseException | RuntimeException e ) {
//
//            System.err.println( "unable to issue POST to \"" + _baseServerUrlString + "\" (doing " + what + "):  " + what );
//
//            e.printStackTrace();
//
//            throw e;
//
//        }
//
//    }

    public static StatisticsAccumulator getTimingStats() {

        return s_innerRequestStats;

    }

    public String toString() {

        return "WikiTreeApiJsonSession()";

    }

}
