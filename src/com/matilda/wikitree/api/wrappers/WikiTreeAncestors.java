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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.PrintStream;
import java.util.*;

/**
 Wrap someone's ancestors.
 */

@SuppressWarnings({ "WeakerAccess", "unused" })
public class WikiTreeAncestors extends WikiTreeWrapper {

    private final WikiTreeId _resultWikiTreeID;

    private final long _resultPersonId;

    private final String _resultKeyString;

    private final WikiTreeId _requestKey;

    private final Integer _requestDepth;

    private final Vector<WikiTreePersonProfile> _resultAncestors;

    private final SortedMap<Long, WikiTreePersonProfile> _profilesByPersonId = new TreeMap<>();

    private final SortedMap<WikiTreeId, WikiTreePersonProfile> _profilesByWikiTreeId = new TreeMap<>();

    private final SortedMap<Long, WikiTreePersonProfile> _fathersOf = new TreeMap<>();

    private final SortedMap<Long, WikiTreePersonProfile> _mothersOf = new TreeMap<>();

    private final WikiTreePersonProfile _basePersonProfile;

    private final WikiTreePersonProfile _ancestralTree;

    private static final int INDENT_PER_LEVEL = 4;

    public static final String HANGER = ( "+" + WikiTreeApiUtilities.repl( "-", INDENT_PER_LEVEL - 1 ) );

    private static final String EMPTY_INDENT = WikiTreeApiUtilities.repl( " ", INDENT_PER_LEVEL );
    private static final String NONEMPTY_INDENT = "|" + WikiTreeApiUtilities.repl( " ", INDENT_PER_LEVEL - 1 );

    /**
     Create a wrapper for the {@link JSONObject} provided by a call to {@link WikiTreeApiJsonSession#getAncestors(WikiTreeId, Integer)}.

     @param key          the WikiTree ID or the Person.Id who's ancestors are being used to create this instance.
     @param depth        the requested depth (could be {@code null}; see {@link WikiTreeApiJsonSession#getAncestors(WikiTreeId, Integer)} for more info).
     @param resultObject the result received from the WikiTree API server in response to a {@code getAncestors} request (in other words,
     what was returned by the call to {@link WikiTreeApiJsonSession#getAncestors(WikiTreeId, Integer)} call).
     */

    public WikiTreeAncestors(
            @NotNull WikiTreeId key,
            @Nullable Integer depth,
            @NotNull JSONObject resultObject
    )
            throws WikiTreeRequestFailedException {

        super( resultObject );

        _requestKey = key;
        _requestDepth = depth;
        boolean resultKeyIsId;
        if ( resultObject.containsKey( "user_name" ) ) {

            _resultKeyString = (String)WikiTreeApiUtilities.getMandatoryJsonValue( String.class, resultObject, "user_name" );
            resultKeyIsId = false;

        } else if ( resultObject.containsKey( "user_id" ) ) {

            _resultKeyString = (String)WikiTreeApiUtilities.getMandatoryJsonValue( String.class, resultObject, "user_id" );
            resultKeyIsId = true;

        } else {

            throw new ReallyBadNewsError( "WikiTreeAncestors:  JSON object contains neither a \"user_name\" nor a \"user_id\" field name - " +
                                          resultObject );

        }

        JSONArray resultAncestors = (JSONArray)WikiTreeApiUtilities.getMandatoryJsonValue( JSONArray.class, resultObject, "ancestors" );
        _resultAncestors = new Vector<>();
        for ( Object ancestorObject : resultAncestors ) {

            if ( ancestorObject instanceof JSONObject ) {

                WikiTreePersonProfile ancestorProfile = new WikiTreePersonProfile(
                        WikiTreeRequestType.UNKNOWN,
                        (JSONObject)ancestorObject,
                        WikiTreePersonProfile.ProfileType.PROFILE
                );

                _resultAncestors.add( ancestorProfile );

            } else {

                throw new ReallyBadNewsError(
                        "WikiTreeAncestors:  found a " +
                        ( ancestorObject == null ? "null" : ancestorObject.getClass().getCanonicalName() ) +
                        " in ancestors array"
                );

            }

        }

        // Time to turn what we got from the WikiTree API into an ancestral tree.

        // Discard any parent findings that the WikiTreePersonProfile constructor may have made.

        for ( WikiTreePersonProfile profile : _resultAncestors ) {

            profile.setBiologicalFather( null );
            profile.setBiologicalMother( null );

        }

        // Build maps of everyone involved.
        // Find the base person's profile on the way by.

        WikiTreePersonProfile basePersonProfile = null;
        for ( WikiTreePersonProfile profile : _resultAncestors ) {

            if ( _resultKeyString.equals( "" + profile.getPersonId() ) || _resultKeyString.equals( "" + profile.getWikiTreeId() ) ) {

                basePersonProfile = profile;

            }

            _profilesByPersonId.put( profile.getPersonId(), profile );
            _profilesByWikiTreeId.put( profile.getWikiTreeId(), profile );

        }

        if ( basePersonProfile == null ) {

            throw new ReallyBadNewsError( "WikiTreeAncestors:  did not find the base person's profile (" + _resultKeyString + ") in the results" );

        }

        _resultWikiTreeID = basePersonProfile.getWikiTreeId();
        _resultPersonId = basePersonProfile.getPersonId();

        _basePersonProfile = basePersonProfile;

        // Collect the mothers and fathers.

        for ( WikiTreePersonProfile profile : _resultAncestors ) {

            rememberParent( _profilesByPersonId, _fathersOf, "Father", profile );
            rememberParent( _profilesByPersonId, _mothersOf, "Mother", profile );

        }

        // Keep track of ancestors already in the lineage that we're working on.

        SortedSet<Long> lineage = new TreeSet<>();

        // Boom!

        _ancestralTree = buildAncestralTree( _basePersonProfile, lineage );

//	printAncestralTree( System.out );

    }

    private void rememberParent(
            SortedMap<Long, WikiTreePersonProfile> profiles,
            SortedMap<Long, WikiTreePersonProfile> myParent,
            String relationship,
            WikiTreePersonProfile personProfile
    ) {

        Number parentNumber = (Number)WikiTreeApiUtilities.getOptionalJsonValue( Number.class, personProfile, relationship );
        if ( parentNumber != null ) {

            WikiTreePersonProfile parent = profiles.get( parentNumber.longValue() );
            if ( parent != null ) {

                myParent.put( personProfile.getPersonId(), parent );

            }

        }

    }

    private WikiTreePersonProfile buildAncestralTree( WikiTreePersonProfile targetPersonProfile, SortedSet<Long> lineage ) {

        // Is this person a descendant in the line we're working on?
        // For example, is this person a son, grandson, daughter, granddaughter, etc of the current targeted person?

        if ( lineage.contains( targetPersonProfile.getPersonId() ) ) {

            // We're our own grandpa (or something like that).
            // That's not a game we are interested in playing.
            // Chop the tree of ancestors here (i.e. don't return a profile for the part of the tree we're working on).

            return null;

        }

        // Put the targeted person into the current lineage.

        lineage.add( targetPersonProfile.getPersonId() );

        // Use recursion to add the targeted person's father to the tree.

        WikiTreePersonProfile father = _fathersOf.get( targetPersonProfile.getPersonId() );
        if ( father != null ) {

            targetPersonProfile.setBiologicalFather( buildAncestralTree( father, lineage ) );

        }

        // Use recursion to add the targeted person's mother to the tree.

        WikiTreePersonProfile mother = _mothersOf.get( targetPersonProfile.getPersonId() );
        if ( mother != null ) {

            targetPersonProfile.setBiologicalMother( buildAncestralTree( mother, lineage ) );

        }

        // We're done with this person so remove them from the lineage.

        lineage.remove( targetPersonProfile.getPersonId() );

        return targetPersonProfile;

    }

    /**
     Print this instance's ancestral tree in a vaguely human readable format.

     @param ps where to print it.
     */

    public void printAncestralTree( PrintStream ps ) {

        printAncestralTree( ps, true, EMPTY_INDENT, "B", _basePersonProfile );

    }

    /**
     Print an ancestral tree in a vaguely human readable format.

     @param ps          where to print it.
     @param baseProfile the profile at the base of the ancestral tree.
     */

    public void printAnestralTree( PrintStream ps, WikiTreePersonProfile baseProfile ) {

        printAncestralTree( ps, true, EMPTY_INDENT, "B", baseProfile );

    }

    private void printAncestralTree( PrintStream ps, boolean goingLeft, String fullIndentString, String role, WikiTreePersonProfile profile ) {

        if ( profile != null ) {

            String chopped = fullIndentString.substring( 0, fullIndentString.length() - INDENT_PER_LEVEL );
            String replaced =
                    chopped + EMPTY_INDENT + NONEMPTY_INDENT;
            Optional<WikiTreePersonProfile> optBiologicalFather = profile.getBiologicalFather();
            Optional<WikiTreePersonProfile> optBiologicalMother = profile.getBiologicalMother();
            if ( goingLeft ) {

                printAncestralTree( ps, true, replaced, "F", optBiologicalFather.orElse( null ) );
                printLine(
                        ps,
                        chopped + HANGER + role + " - " + profile.getShortName() +
                        " (" + WikiTreeApiUtilities.cleanupStringDate( profile.get( "BirthDate" ) ) + "," +
                        WikiTreeApiUtilities.cleanupStringDate( profile.get( "DeathDate" ) ) + ")"
                );
                printAncestralTree( ps, false, fullIndentString + NONEMPTY_INDENT, "M", optBiologicalMother.orElse( null ) );
                printLine( ps, fullIndentString );

            } else {

                printLine( ps, fullIndentString );
                printAncestralTree( ps, true, fullIndentString + NONEMPTY_INDENT, "F", optBiologicalFather.orElse( null ) );
                printLine(
                        ps,
                        chopped + HANGER + role + " - " + profile.getShortName() +
                        " (" + WikiTreeApiUtilities.cleanupStringDate( profile.get( "BirthDate" ) ) + "," +
                        WikiTreeApiUtilities.cleanupStringDate( profile.get( "DeathDate" ) ) + ")"
                );
                printAncestralTree( ps, false, replaced, "M", optBiologicalMother.orElse( null ) );

            }

        }

    }

    /**
     A handy place to hang a breakpoint and an easy way to avoid the extra blank line that appears after the
     ancestral tree has been printed.

     @param ps   where to send the line.
     @param line the line to send.
     */

    private void printLine( PrintStream ps, String line ) {

        if ( !line.trim().isEmpty() ) {

            ps.println( line );

        }

    }

    /**
     Get the WikiTree ID of the person who's ancestors were used to create this instance.
     <p/>This is called the result WikiTreeId because it is extracted from the result returned by
     the {@code getAncestors} request to the WikiTree API server used to create this instance.
     <p/>
     Note that this corresponds to what a {@code get( "user_name" ) } request on this instance will yield
     (unless someone has fiddled with this instance's values using {@link #put(Object, Object)} (or some variant)
     since this instance was created).

     @return the WikiTree ID of the person who's ancestors appear in this instance.
     This is also the WikiTree ID of the person specified when these ancestors were fetched.
     */

    public WikiTreeId getResultWikiTreeID() {

        return _resultWikiTreeID;

    }

    /**
     Get the key that was specified when fetching these ancestors.

     @return the key that was specified when fetching these ancestors.
     See {@link WikiTreeApiJsonSession#getAncestors(WikiTreeId, Integer)} for more info.
     */

    public WikiTreeId getRequestKey() {

        return _requestKey;
    }

    /**
     Get the depth that was requested when these ancestors were fetched.

     @return the depth / number of generations of ancestors requested when these ancestors were fetched.
     If {@code null}, the depth defaults to a value specified in the documentation for
     {@link WikiTreeApiJsonSession#getAncestors(WikiTreeId, Integer)}.
     This includes the person who's ancestors were requested.
     For example, if depth is 2 then you'll get the specified person and their parents whereas if the depth is 3 then
     you'll get the specified person, their parents and their grandparents.
     Obviously, you don't get profiles that WikiTree doesn't know about.
     */

    public Integer getRequestDepth() {

        return _requestDepth;

    }

    /**
     Get the base person's profile.
     <p/>The base person is the person who's ancestors were used to create this instance.
     The base person is also the person who's WikiTree ID is returned by {@link #getResultWikiTreeID()}.

     @return the base person's profile.
     */

    public WikiTreePersonProfile getBasePersonProfile() {

        return _basePersonProfile;

    }

    /**
     Get the ancestors of the base person (the person who's ancestors were used to create this instance).

     @return a list of the base person's ancestor's {@link WikiTreePersonProfile} instances.
     The list will include the profile for the person who's WikiTree ID or Person.Id was used to create this instance.
     */

    public List<WikiTreePersonProfile> getResultAncestors() {

        return Collections.unmodifiableList( _resultAncestors );

    }

    /**
     Get the ancestral tree.
     <p/>This method returns the copy of the ancestral tree that our constructor created earlier.
     Expect confusion if you change the contents of this tree.
     <p/>The return value is a tree of {@link WikiTreePersonProfile} instances.
     Calls to {@link WikiTreePersonProfile#getBiologicalFather()} or {@link WikiTreePersonProfile#getBiologicalMother()}
     methods on an instance in this tree will return the WikiTreePersonProfile instances for the said instance (or {@code null}
     if the father/mother is not in the tree).
     <p/>This tree is constructed in a manner which is intended to ensure that the tree will have no loops within it (no occurrences of
     a person appearing to be their own ancestor). While I believe that it is impossible for this method to return an ancestral tree
     that has loops, that doesn't mean that it <u>is</u> impossible. Please contact me at danny@matilda.com with details
     if you run into a tree from this method which has loops.
     <p/>Note that if the collection of ancestors that you get back from the WikiTree API server has loops in it then the method used
     to construct the tree that this method returns is designed to drop an intended-to-be-minimal number of nodes such that the resulting
     tree has no loops in it. In other words, even in the presumably very unlikely event that the WikiTree API server returns a tree
     that has loops in it, you should not get a tree that has loops in it from this method. Again, please contact me if you find any
     counter-examples.

     @return the ancestral tree.
     */

    public WikiTreePersonProfile getAncestralTree() {

        return _ancestralTree;

    }

    /**
     Get a sorted map of this instance's {@link WikiTreePersonProfile} instances mapped by their {@code Person.Id}.

     @return a {@link SortedMap<Long,WikiTreePersonProfile>} of this instance's {@link WikiTreePersonProfile} instances
     mapped by their {@code Person.Id}.
     Note that this map contains all the WikiTreePersonProfile instances held by this instance (i.e. used to create this instance).
     This includes every instance in the ancestral tree that can be obtained by calling {@link #getAncestralTree()} plus any
     WikiTreePersonProfile instances which were dropped from the ancestral tree to avoid the ancestral tree having loops within it
     (profiles in the tree which appear to be their own ancestor). See {@link #getAncestralTree()} for more information about this
     whole loops in ancestral trees business.
     */

    public SortedMap<Long, WikiTreePersonProfile> getProfilesByPersonId() {

        return _profilesByPersonId;

    }

    /**
     Get a sorted map of this instance's {@link WikiTreePersonProfile} instances mapped by their WikiTree ID.

     @return a {@link SortedMap<Long,WikiTreePersonProfile>} of this instance's {@link WikiTreePersonProfile} instances
     mapped by their {@code WikiTree ID}.
     Note that this map contains all the WikiTreePersonProfile instances held by this instance (i.e. used to create this instance).
     This includes every instance in the ancestral tree that can be obtained by calling {@link #getAncestralTree()} plus any
     WikiTreePersonProfile instances which were dropped from the ancestral tree to avoid the ancestral tree having loops within it
     (profiles in the tree which appear to be their own ancestor). See {@link #getAncestralTree()} for more information about this
     whole loops in ancestral trees business.
     */

    public SortedMap<WikiTreeId, WikiTreePersonProfile> getProfilesByWikiTreeId() {

        return _profilesByWikiTreeId;

    }

    /**
     Get a sorted map of the {@link WikiTreePersonProfile} instances held by this instance who's mother's profiles are also in this instance.
     <p/>
     This call provides a fast way to find the mother of a particular WikiTreePersonProfile held by this instance
     (subject to the condition that said mother's profile is also held by this instance).
     For example, If the profile with {@code Person.Id} {@code 5584} (Sir Winston S. Churchill's {@code Person.Id}) and the profile
     with {@code Person.Id} {@code 5554} (Churchill's mother, Lady Randolph Churchill) are both in this instance then the following
     would yield Lady Randolph Churchill's profile (assuming that the variable named {@code instance} refers to this instance):
     <blockquote>{@code instance.getMothersOfMapping().get( 5584L )}</blockquote>
     Note that the mapping returned by this method was constructed to facilitate the construction of the ancestral tree associated with
     this instance (see {@link #getAncestralTree()} for more info regarding this instance's ancestral tree).
     Consequently, there is no additional cost associated with asking for this mapping. Keeping the mapping so that it can
     be provided when requested costs a really quite minimal amount of memory.

     @return a {@link SortedMap<Long,WikiTreePersonProfile>} of this instance's {@link WikiTreePersonProfile} instances
     who's mothers are also in this instance.
     */

    public SortedMap<Long, WikiTreePersonProfile> getMothersOfMapping() {

        return _mothersOf;

    }

    /**
     Get a sorted map of the {@link WikiTreePersonProfile} instances held by this instance who's father's profiles are also in this instance.
     <p/>
     This call provides a fast way to find the father of a particular WikiTreePersonProfile held by this instance
     (subject to the condition that said father's profile is also held by this instance).
     For example, If the profile with {@code Person.Id} {@code 5584} (Sir Winston S. Churchill's {@code Person.Id}) and the profile
     with {@code Person.Id} {@code 5587} (Churchill's father, Lord Randolph Churchill) are both in this instance then the following
     would yield Lord Randolph Churchill's profile (assuming that the variable named {@code instance} refers to this instance):
     <blockquote>{@code instance.getFathersOfMapping().get( 5584L )}</blockquote>
     Note that the mapping returned by this method was constructed to facilitate the construction of the ancestral tree associated with
     this instance (see {@link #getAncestralTree()} for more info regarding this instance's ancestral tree).
     Consequently, there is no additional cost associated with asking for this mapping. Keeping the mapping so that it can
     be provided when requested costs a really quite minimal amount of memory.

     @return a {@link SortedMap<Long,WikiTreePersonProfile>} of this instance's {@link WikiTreePersonProfile} instances
     who's fathers are also in this instance.
     */

    public SortedMap<Long, WikiTreePersonProfile> getFathersOfMapping() {

        return _fathersOf;

    }

    public String toString() {

        return
                "WikiTreeAncestors( " +
                "requestKey=\"" + _requestKey + "\", " +
                "requestDepth=" + getRequestDepth() + ", " +
                "resultUserName=" + getResultWikiTreeID() + ", " +
                "count=" + _resultAncestors.size() + " " +
                ")";

    }

}
