/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.examples;

import com.matilda.wikitree.api.exceptions.ReallyBadNewsError;
import com.matilda.wikitree.api.exceptions.WikiTreeLoginRequestFailedException;
import com.matilda.wikitree.api.exceptions.WikiTreeRequestFailedException;
import com.matilda.wikitree.api.util.WikiTreeApiUtilities;
import com.matilda.wikitree.api.wrappers.*;
import org.json.simple.parser.ParseException;

import java.io.IOException;

import static com.matilda.wikitree.api.util.WikiTreeApiUtilities.doNothing;

/**
 Take the wrappers API out for a test drive.
 */

public class WrappersApiTestDrive {

    public static void main( final String[] args ) {

        // Create ourselves a wrappers-style session.

        WikiTreeApiWrappersSession request = new WikiTreeApiWrappersSession();

        try {

            // Login using the info in the authentication file specified on the command line.
            // First line contains the email address associated with your WikiTree account
            // and the second line contains that account's WikiTree password.
            // If there is no file specified on the command line then

            WikiTreeApiUtilities.maybeLoginToWikiTree( request, args );

            WikiTreePersonProfile churchillPersonByWikiTreeID = request.getPerson( new WikiTreeId( "Churchill-4" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "Winston S. Churchill's person instance",
                    churchillPersonByWikiTreeID
            );

            WikiTreePersonProfile marigoldChurchillPersonByWikiTreeID = request.getPerson( new WikiTreeId( "Churchill-9" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "Winston S. Churchill's daughter Marigold's person instance (died very young; no spouse or children)",
                    marigoldChurchillPersonByWikiTreeID
            );

            WikiTreePersonProfile churchillPersonById = request.getPerson( 5589 );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "Churchill's person instance by Person.Id",
                    churchillPersonById
            );

            {
                String relativeKeys = "5589,Hozier-1,Churchill-4";
                WikiTreeRelatives relatives = request.getRelatives( relativeKeys, true, true, true, true );
                System.out.println( "getRelatives( all types ) for \"" + relativeKeys + "\":  " + relatives );
                System.out.println(
                        "" + relatives.getBasePeopleByKey().size() + " relatives returned, " +
                        relatives.getBasePeopleByWikiTreeID().size() + " by WikiTreeID and " +
                        relatives.getBasePeopleByPersonId().size() + " by Person.Id"
                );
                System.out.println( "keys yielding relatives: " + relatives.getBasePeopleByKey().keySet() );
                System.out.println( "WikiTree IDs yielding relatives: " + relatives.getBasePeopleByWikiTreeID().keySet() );
                System.out.println( "Person.Ids yielding relatives: " + relatives.getBasePeopleByPersonId().keySet() );
                WikiTreeApiUtilities.prettyPrintJsonThing(
                        "relatives of 5589",
                        relatives.getBasePeopleByPersonId().get( 5589L ).getChildren()
                );
                WikiTreeApiUtilities.prettyPrintJsonThing(
                        "everything by WikiTree ID",
                        relatives.getBasePeopleByWikiTreeID()
                );
            }

            {
                String relativeKeys = "Churchill-4";
                WikiTreeRelatives relatives = request.getRelatives( relativeKeys, true, true, true, true );
                System.out.println( "getRelatives( all types ) for \"" + relativeKeys + "\":  " + relatives );
                System.out.println(
                        "" + relatives.getBasePeopleByKey().size() + " relatives returned, " +
                        relatives.getBasePeopleByWikiTreeID().size() + " by WikiTreeID and " +
                        relatives.getBasePeopleByPersonId().size() + " by Person.Id"
                );
                System.out.println( "keys yielding relatives: " + relatives.getBasePeopleByKey().keySet() );
                System.out.println( "WikiTree IDs yielding relatives: " + relatives.getBasePeopleByWikiTreeID().keySet() );
                System.out.println( "Person.Ids yielding relatives: " + relatives.getBasePeopleByPersonId().keySet() );
                WikiTreeApiUtilities.prettyPrintJsonThing(
                        "relatives of 5589",
                        relatives.getBasePeopleByPersonId().get( 5589L ).getChildren()
                );
                WikiTreeApiUtilities.prettyPrintJsonThing(
                        "everything by WikiTree ID",
                        relatives.getBasePeopleByWikiTreeID()
                );
            }

            WikiTreePersonProfile churchillProfile2 = request.getPersonProfile( new WikiTreeId( "Churchill-4" ) );
            System.out.println( "Churchill's profile via getPersonProfile is " + churchillProfile2 );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "Churchill's person profile via getPersonProfile request",
                    churchillProfile2
            );

            WikiTreePersonProfile bouletProfile = request.getPersonProfile( new WikiTreeId( "Boulet-169" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing( "boulet profile", bouletProfile );
            WikiTreePersonProfile bouletManagerProfile =
                    request.getPersonProfile( new WikiTreeId( "Boulet-169" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing( "boulet manager profile", bouletManagerProfile );
            if ( bouletProfile == null ) {

                throw new ReallyBadNewsError(
                        "bouletProfile is null - you probably need to change this section to use your WikiTree id)"
                );

            }

            WikiTreePersonProfile bouletManagerPerson = request.getPerson( bouletProfile.getManagerPersonId() );
            WikiTreeApiUtilities.prettyPrintJsonThing( "boulet manager person", bouletManagerPerson );

            WikiTreePersonProfile profileManagerCondensed =
                    request.getBasicPersonProfile( bouletProfile.getManagerPersonId() );
            WikiTreeApiUtilities.prettyPrintJsonThing( "condensed boulet manager person", profileManagerCondensed );

            WikiTreePersonProfile profileManagerCondensedWithChildren =
                    request.getPerson(
                            bouletProfile.getManagerPersonId(),
                            "Id,Name,Derived.ShortName,HasChildren,NoChildren,Children"
                    );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "condensed with children boulet manager person",
                    profileManagerCondensedWithChildren
            );

            WikiTreePersonProfile bouletViaLimitedGetPerson = request.getPerson(
                    new WikiTreeId( "Boulet-169" ),
                    WikiTreeApiUtilities.constructGetPersonFieldsString(
                            WikiTreeApiUtilities.constructExcludedGetPersonFieldsSet(
                                    "Parents", "Spouses", "Children", "Siblings"
                            )
                    )
            );
            WikiTreeApiUtilities.prettyPrintJsonThing( "boulet via limited getPerson", bouletViaLimitedGetPerson );

            WikiTreeProfile spaceProfile = request.getProfile( new WikiTreeId( "Space:Allied_POW_camps" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing( "getProfile for Space:Allied_POW_camps", spaceProfile );
            System.out.println( "space profile is " + spaceProfile );

            WikiTreeBiography bio = request.getBio( new WikiTreeId( "Churchill-4" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing( "bio for Churchill-4", bio );
            System.out.println( "Churchill-4 bio is " + bio );

            WikiTreeWatchlist watchList = request.getWatchlist( true, true, null, null, null, 3, 10, null );
            WikiTreeApiUtilities.prettyPrintJsonThing( "Our watch list", watchList );
            System.out.println( "Our watch list's info:  " + watchList );

            WikiTreeAncestors ancestors = request.getAncestors( new WikiTreeId( "Churchill-4" ), 4 );
            WikiTreeApiUtilities.prettyPrintJsonThing( "Churchill-4's ancestors", ancestors );
            System.out.println( "Churchill-4 ancestors is " + ancestors );

            // Try Boutin-148 (parent's were cousins)

            WikiTreeAncestors onilBoutinAncestors = request.getAncestors( new WikiTreeId( "Boutin-148" ), 5 );
            WikiTreeApiUtilities.prettyPrintJsonThing( "Boutin=148's ancestors", onilBoutinAncestors );
            System.out.println( "Boutin-148's ancestors is " + onilBoutinAncestors );

            System.out.println( "done" );

        } catch (
                IllegalArgumentException |
                        ReallyBadNewsError |
                        IOException |
                        ParseException |
                        WikiTreeRequestFailedException e
                ) {

            System.out.flush();
            e.printStackTrace();

            doNothing();

        } catch ( WikiTreeLoginRequestFailedException e ) {

            System.out.flush();
            System.err.println( "login failed (reason=" + e.getReason() );

            e.printStackTrace();

        } catch ( Throwable e ) {

            System.out.flush();
            System.err.println( "totally unexpected exception:  " + e );
            e.printStackTrace();

            doNothing();
        }

    }

}
