/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api;

import com.matilda.wikitree.api.jsonclient.WikiTreeApiJsonSession;
import com.matilda.wikitree.api.wrappers.WikiTreeApiWrappersSession;
import org.jetbrains.annotations.NotNull;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 Describe methods which must be shared by any implementation of this API.
 <p/>
 This interface exists to facilitate the implementation of certain <em>utility</em> methods used by this Java implementation of
 the WikiTree API.
 The methods defined by this interface are either used today or are at least likely to be used someday by said <em>utility</em> methods.
 <p/>
 This interface is currently implemented by at least two different classes.
 <ol>
 <li>The implementation by the {@link WikiTreeApiJsonSession} class communicates directly with the WikiTree API server.
 Consequently, the implementation of this interface's methods by the {@link WikiTreeApiJsonSession} class are, in a sense,
 the 'real' implementations of this interface's methods.</li>
 <li>In contrast, the implementation of this interface's methods by a class which wraps a {@link WikiTreeApiJsonSession} instance
 are 'proxy' implementations of these methods.
 For example, the implementation of {@link WikiTreeApiWrappersSession#getBaseServerUrlString()}
 is as follows:
 <blockquote><pre>
 public String getBaseServerUrlString() {
 <p>
 return getWrappedJsonWikiTreeApiClient().getBaseServerUrlString();
 <p>
 }
 </pre>
 </blockquote></li>
 </ol>
 The Javadoc documentation for the methods defined within this class is written to describe the effect of invoking these methods.
 */

public interface WikiTreeApiClient {

    /**
     Get the base WikiTree app server URL that this instance will augment when processing/implementing API requests.

     @return the base URL that this instance will augment when processing/implementing API requests.
     */

    String getBaseServerUrlString();

    /**
     Get the most recent login attempt's result status.

     @return a variety of return values are possible including
     <ul>
     <li>{@code "Success"} if the most recent attempt works</li>
     <li>{@code "Illegal"} if the supplied email address does not correspond to a valid WikiTree user account</li>
     <li>{@code "WrongPass"} if the supplied password is incorrect</li>
     <li>{@code null} if this method has been called before any attempt has been made to login to the WikiTree API server
     by this instance or,
     in the case of a wrapper implementation of this interface, by the wrapped {@link WikiTreeApiJsonSession} instance</li>
     </ul>
     Other return values are possible (sorry).
     */

    String getLoginResultStatus();

    /**
     Determine if this instance is currently authenticated.
     <p/>See {@link WikiTreeApiJsonSession#isAuthenticated()} for more information.

     @return {@code true} if this instance is currently authenticated; {@code false} otherwise.
     */

    boolean isAuthenticated();

    /**
     Get the email address used to authenticate this instance.

     @return the email address used to authenticate this instance or {@code null} if this instance is not currently authenticated.
     */

    String getAuthenticatedUserEmailAddress();

    /**
     Get the WikiTree ID of the WikiTree user used to authenticate this instance.

     @return the WikiTreeId of the WikiTree user used to authenticate this instance.
     This method returns {@code null} if this instance is not currently authenticated.
     <p/>For example, if <a href="https://www.wikitree.com/wiki/Weisz-93">Erik (Weisz) Houdini</a>
     used his email address to authenticate this instance then this method would return
     {@code "Weisz-93}" (little known fact: Harry Houdini was a very early WikiTree user).
     */

    String getAuthenticatedWikiTreeId();

    /**
     Login to the WikiTree API server.
     <p/>This method sends a 'login' request to this instance's WikiTree API server.
     If successful, this instance becomes an authenticated WikiTree API client instance with respect to this instance's WikiTree API server.
     If unsuccessful, this instance either remains or becomes an anonymous WikiTree API client instance with respect to this instance's WikiTree API server.
     <p/>See {@link WikiTreeApiJsonSession#WikiTreeApiJsonSession()} for more information about anonymous vs authenticated WikiTree API client instances.
     <p/>See {@link WikiTreeApiJsonSession#WikiTreeApiJsonSession(String)} for more info on using alternative WikiTree API servers.

     @param emailAddress the email address associated with a www.wikitree.com account.
     @param password     the password associated with the same www.wikitree.com account.
     @return {@code true} if the login request succeeded (valid www.wikitree.com account email address and associated password and no "technical difficulties"
     between here and the WikiTree API server).
     <p/>Note that if this method returns {@code false} then this instance is now an anonymous WikiTree API client instance
     regardless of whether or not it was an authenticated WikiTree API client instance before it called this method.
     @throws IOException    if an IOException is thrown by the networking facilities used to send and receive the login request.
     @throws ParseException if this client is unable to process the response from the WikiTree API server. Seeing this exception should be a rather rare occurrence.
     If you do see one, you have probably encountered a bug in this software. Please notify danny@matilda.com if you get this exception (be prepared to work with Danny
     to reproduce the problem).
     */

    boolean login( @NotNull String emailAddress, @NotNull String password )
            throws IOException, ParseException;

    /**
     The biological genders.
     */

    enum BiologicalGender {

        UNKNOWN {
            public String childGender() {

                return "child";
            }

            public String parentGender() {

                return "parent";
            }

            public String siblingGender() {

                return "sibling";
            }
        },
        MALE {
            public String childGender() {

                return "son";
            }

            public String parentGender() {

                return "father";
            }

            public String siblingGender() {

                return "brother";
            }
        },
        FEMALE {
            public String childGender() {

                return "daughter";
            }

            public String parentGender() {

                return "mother";
            }

            public String siblingGender() {

                return "sister";
            }
        };

        public abstract String childGender();

        public abstract String parentGender();

        public abstract String siblingGender();

    }

}
