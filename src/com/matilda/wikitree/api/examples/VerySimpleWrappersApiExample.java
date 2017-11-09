/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.examples;

import com.matilda.wikitree.api.exceptions.WikiTreeLoginRequestFailedException;
import com.matilda.wikitree.api.exceptions.WikiTreeRequestFailedException;
import com.matilda.wikitree.api.util.WikiTreeApiUtilities;
import com.matilda.wikitree.api.wrappers.*;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 A simple wrappers-style API example.
 */

public class VerySimpleWrappersApiExample {

    public static void main( final String[] args ) {

        // Get ourselves a wrappers-style session instance.

        WikiTreeApiWrappersSession session = new WikiTreeApiWrappersSession();

        try {

            // Login to the API server.
            //
            // ###### If you don't have a WikiTree account then just comment out the maybeLoginToWikiTree call below.
            // ###### Other than that you won't get a watchlist (it will be empty), everything should work since this example uses public profiles.
            //
            // The first line of the specified file must contain the email address of a WikiTree user account.
            // The second line must contain the WikiTree password for that account.
            //
            // Note that if a .wtu file is specified, as it is here, then any failure at all results in our JVM exiting.
            // This includes but is not limited to:
            //    - the .wtu file not existing
            //    - the .two file not having the correct contents (see above)
            //    - incorrect password
            //    - trouble in network-land
            //
            // ###### If you don't have a WikiTree account then just comment out the maybeLoginToWikiTree call below.
            // ###### Other than that you won't get a watchlist (it will be empty), everything should work since this example uses public profiles.

            WikiTreeApiUtilities.maybeLoginToWikiTree( session, new String[]{ ".wikiTreeUserInfo.wtu" } );

            // Let's see who owns the session.

            WikiTreeApiUtilities.printAuthenticatedSessionUserInfo( session );

            // The rest of this involves at least some calls which can throw exceptions our way.
            // We'll just catch them and print out a stack traceback.

            // Let's exercise each of the types of requests.

            // Get Sir Winston S. Churchill's person profile via a getPerson request and print it out.

            WikiTreePersonProfile churchillsProfile = session.getPerson( new WikiTreeId( "5589" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing( "Winston S. Churchill's person profile", churchillsProfile );

            // Get Winston S. Churchill's person profile via a getProfile request and print that out.
            // This is not much different from a getPerson request other than that a getProfile request
            // doesn't also get any of the target person's relatives.

            churchillsProfile = session.getPersonProfile( new WikiTreeId( "5589" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing( "Winston S. Churchill's person profile", churchillsProfile );

            // Get Winston's biography.

            WikiTreeBiography churchillsBiography = session.getBio( new WikiTreeId( "5589" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing( "Winston's biography", churchillsBiography );

            // Let's get the logged in user's watch list (we'll put a small limit on the request to keep the amount of output reasonable).

            WikiTreeWatchlist watchList = session.getWatchlist( true, true, null, null, null, 5, null, null );
            WikiTreeApiUtilities.prettyPrintJsonThing( "Our watch list", watchList );

            // Let's have a look at Winston's ancestors.
            // We're going to use Winston's Person.Id (5589) to demonstrate another way to specify which person we are interested in.
            // We are also limiting the output to three generations (Winston, his parents and their parents) to keep things reasonable.
            // Note that if we want three generations of Churchills then we ask for two (the target person's generation doesn't count).

            WikiTreeAncestors churchillsAncestors = session.getAncestors( new WikiTreeId( "5589" ), 2 );
            WikiTreeApiUtilities.prettyPrintJsonThing( "Winston's ancestors", churchillsAncestors );

            // Let's see the ancestral tree in an easier to comprehend format.

            churchillsAncestors.printAncestralTree( System.out );

            // Finally, let's get Winston's spouses (all one of them) and children (a few more than one).

            WikiTreeRelatives churchillsRelatives = session.getRelatives( "5589", false, true, true, false );
            WikiTreeApiUtilities.prettyPrintJsonThing( "Winston's wife and children", churchillsRelatives );

            // We're done.

            System.out.println( "done" );

        } catch ( IOException | ParseException | WikiTreeRequestFailedException e ) {

            System.out.flush();
            e.printStackTrace();

        } catch ( WikiTreeLoginRequestFailedException e ) {

            System.out.flush();
            System.err.println( "login failed (reason=" + e.getReason() + ")" );
            e.printStackTrace();

        }

    }

}
