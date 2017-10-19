/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.examples;

import com.matilda.wikitree.api.exceptions.WikiTreeLoginRequestFailedException;
import com.matilda.wikitree.api.jsonclient.WikiTreeApiJsonSession;
import com.matilda.wikitree.api.util.WikiTreeApiUtilities;
import com.matilda.wikitree.api.wrappers.WikiTreeId;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Optional;

/**
 Take the JSON-based WikiTree Java API out for a test drive.
 */

public class JsonApiTestDrive {

    public static void main( String[] args ) {

        // Create a JSON-based API session.

        WikiTreeApiJsonSession request = new WikiTreeApiJsonSession();

        try {

            // Login using the info in the authentication file specified on the command line.
            // First line contains the email address associated with your WikiTree account
            // and the second line contains that account's WikiTree password.
            // If there is no file specified on the command line then

            WikiTreeApiUtilities.maybeLoginToWikiTree( request, args );

            // Public person with parents, spouse and multiple children.

            Optional<JSONObject> churchillPersonByWikiTreeID = request.getPerson( new WikiTreeId( "Churchill-4" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "getPerson for Churchill-4 (WikiTree ID for W. S. Churchill)",
                    churchillPersonByWikiTreeID
            );

            Optional<JSONObject> churchillMarigoldPersonByWikiTreeID = request.getPerson( new WikiTreeId( "Churchill-9" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "getPerson for Churchill-9 (WikiTree ID for Marigold F. Churchill - died very young with no children)",
                    churchillMarigoldPersonByWikiTreeID
            );

            Optional<JSONObject> churchillPersonByPersonId = request.getPerson( new WikiTreeId( "5589" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "getPerson for 5589 (Person.Id for W. S. Churchill)",
                    churchillPersonByPersonId
            );

            Optional<JSONObject> nonExistentPerson = request.getPerson( new WikiTreeId( "Churchill-4548988" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "getPerson for non-existent Churchill-4548988",
                    nonExistentPerson
            );

            Optional<JSONObject> churchillProfileByWikiTreeID = request.getProfile( new WikiTreeId( "Churchill-4" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "getProfile for Churchill-4 (WikiTree ID for W. S. Churchill)",
                    churchillProfileByWikiTreeID
            );

            Optional<JSONObject> alliedPowCampsSpaceProfileByPageName = request.getProfile( new WikiTreeId( "Space:Allied_POW_camps" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "getProfile for Space:Allied_POW_camps",
                    alliedPowCampsSpaceProfileByPageName
            );

            Optional<JSONObject> alliedPowCampsSpaceProfileByPageId = request.getProfile( new WikiTreeId( "7933538" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "getProfile for Profile Id 7933358 (Space:Allied_POW_camps)",
                    alliedPowCampsSpaceProfileByPageId
            );

            Optional<JSONObject> churchillBioByWikiTreeID = request.getBio( new WikiTreeId( "Churchill-4" ) );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "getBio for Churchill-4",
                    churchillBioByWikiTreeID
            );

            Optional<JSONObject> churchillsAncestors = request.getAncestors( new WikiTreeId( "Churchill-4" ), 3 );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "getAncestors to depth of 3 for Churchill-4 (WikiTree ID for W. S. Churchill)",
                    churchillsAncestors
            );

            Optional<JSONObject> churchillsRelatives = request.getRelatives(
                    "Churchill-4",
                    true,
                    true,
                    true,
                    true
            );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "getRelatives (parents, children, spouses, siblings) of Churchill-4",
                    churchillsRelatives
            );

            Optional<JSONObject> churchillsChildrenAndSpouses = request.getRelatives(
                    "Churchill-4",
                    false,
                    true,
                    true,
                    false
            );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "getRelatives children and spouses of Churchill-4",
                    churchillsChildrenAndSpouses
            );

            Optional<JSONObject> parentsOfChurchillAndRoosevelt = request.getRelatives(
                    "5589,Roosevelt-1",
                    true,
                    false,
                    false,
                    false
            );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "getRelatives parents of 5589 (W. S. Churchill) and Roosevelt-1 (F. D. Roosevelt)",
                    parentsOfChurchillAndRoosevelt
            );

            Optional<JSONObject> watchlist = request.getWatchlist(
                    true,
                    true,
                    null,
                    null,
                    null,
                    10,
                    null,
                    null
            );
            WikiTreeApiUtilities.prettyPrintJsonThing(
                    "watchlist of authenticated user for this session (space and person profiles, limit of 10)",
                    watchlist
            );

            return;

        } catch ( IOException | ParseException | WikiTreeLoginRequestFailedException e ) {

            e.printStackTrace();

        }

        // Add a final word (mostly to provide a place for a final breakpoint).

        System.out.println( "done" );

    }
}
