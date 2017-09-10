/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.wrappers;

import com.matilda.wikitree.api.WikiTreeApiClient;
import com.matilda.wikitree.api.exceptions.ReallyBadNewsError;
import com.matilda.wikitree.api.exceptions.WikiTreeRequestFailedException;
import com.matilda.wikitree.api.util.WikiTreeApiUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.function.Supplier;

/**
 A WikiTree person profile.
 */

@SuppressWarnings({ "unused", "WeakerAccess" })
public class WikiTreePersonProfile extends WikiTreeProfile {

    /**
     Specify what type of request yielded this profile.
     */

    public enum ProfileType {

        /**
         The target person of a {@code getPerson} request that asked all fields ("*").
         */

        PRIMARY_PERSON,

        /**
         The relative of a {@link #PRIMARY_PERSON} obtained via the request that got the primary person and their relatives.
         */

        RELATIVE,

        /**
         A person profile returned by a {@code getProfile} or a {@code getPersonProfile} request that asked for all fields ("*").
         */

        PROFILE,

        /**
         Any other profile.
         */

        OTHER
    }

    private static final SortedMap<String, WikiTreeApiClient.BiologicalGender> s_genderMap;

    static {

        s_genderMap = new TreeMap<>();
        s_genderMap.put( "Male", WikiTreeApiClient.BiologicalGender.MALE );
        s_genderMap.put( "Female", WikiTreeApiClient.BiologicalGender.FEMALE );

    }

    private WikiTreeApiClient.BiologicalGender _gender = null;
    private WikiTreePersonProfile _biologicalFather;

    private WikiTreePersonProfile _biologicalMother;
    private Collection<WikiTreePersonProfile> _parents = new LinkedList<>();
    private Collection<WikiTreePersonProfile> _spouses = new LinkedList<>();
    private Collection<WikiTreePersonProfile> _children = new LinkedList<>();

    private Collection<WikiTreePersonProfile> _siblings = new LinkedList<>();

    private Long _personId;

    private final ProfileType _profileType;

    /**
     Create a person profile for the person described by the specified JSON object.

     @throws ReallyBadNewsError if any of the following are true:
     <ol>
     <li>no vararg values are provided (or {@code profileLocation} is an empty array) and {@code jsonObject}
     is not the actual person profile object</li><li>there is nothing in {@code jsonObject} at the specified path location</li>
     <li>the search down the specified path location yields a {@code null} value before we get to the end of the path.</li>
     </ol>
      @param requestType     the type of request to the WikiTree API server that got us this profile.
      If not {@code null} then this generally means that the profile was part of a larger response (for example, it might be one of the potentially
      many profiles returned by a {@link WikiTreeApiWrappersSession#getWatchlist(Boolean, Boolean, Boolean, Boolean, String, Integer, Integer, String)} call).
      If {@code null} then this is generally the entire response from a request to the WikiTree API server (for example, the result of calling
      {@link WikiTreeApiWrappersSession#getProfile(WikiTreeId)} for a person's profile).
     @param jsonObject      the specified JSON object.
     @param profileType
     @param profileLocation a varargs series of {@link String} values (or an array of {@link String} values) indicating the path to where
     in {@code jsonObject} we should expect to find the actual person profile. If no vararg values are provided (or {@code profileLocation} is an empty array)
     then {@code jsonObject} is expected to be the person profile object.
     Note that you will be the not-so-proud recipient of
     a {@link ReallyBadNewsError} unchecked exception if the actual person profile is not where you said it was.
     */

    public WikiTreePersonProfile(
            final @Nullable WikiTreeRequestType requestType,
            final @NotNull JSONObject jsonObject,
            final @NotNull ProfileType profileType,
            String... profileLocation
    ) throws WikiTreeRequestFailedException {

        super( jsonObject, profileLocation );

        if ( requestType == null ) {

            if ( jsonObject.containsKey( "user_name" ) || jsonObject.containsKey( "page_name" ) ) {

                setRequestType( WikiTreeRequestType.WIKITREE_ID );

            } else if ( jsonObject.containsKey( "user_id" ) ) {

                setRequestType( WikiTreeRequestType.PERSON_ID );

            } else {

                setRequestType( WikiTreeRequestType.UNKNOWN );

            }

        } else {

            setRequestType( requestType );

        }

        _profileType = profileType;

        if ( !containsKey( "Id" ) ) {

            throw new ReallyBadNewsError( "WTPP does not have a Person.Id:  " + this );

        }

        if ( !containsKey( "Name" ) ) {

            throw new ReallyBadNewsError( "WTPP does not have a Name:  " + this );

        }

        // Make sure that what we saved is actually a profile.

        if ( !containsKey( "IsLiving" ) ) {

            //noinspection unchecked
            throw new ReallyBadNewsError(
                    "WikiTreePersonProfile:  we did not handle (" + requestType + ", " + jsonObject + ", " +
                    WikiTreeApiUtilities.formatPath( profileLocation ) + " ) correctly; we saved {" + new HashMap( this ) + "}"
            );

        }

        ProfileType relativeProfileType = profileType == ProfileType.PRIMARY_PERSON ? ProfileType.RELATIVE : ProfileType.OTHER;
        _parents.addAll( getPeople( this, relativeProfileType, "Parents" ) );
        _children.addAll( getPeople( this, relativeProfileType, "Children" ) );
        _spouses.addAll( getPeople( this, relativeProfileType, "Spouses" ) );
        _siblings.addAll( getPeople( this, relativeProfileType, "Siblings" ) );

        // See if we can figure out who this person's father and mother are.
        // If there is more than one male parent or more than one female parent then we remember the first one that we found and grumble about the rest.

        for ( WikiTreePersonProfile profile : _parents ) {

            if ( profile.isGenderMale() ) {

                if ( _biologicalFather == null ) {

                    _biologicalFather = profile;

                } else {

                    System.err.println( "WikiTreePersonProfile:  " +
                                        getWikiTreeId() +
                                        " has more than one father (" +
                                        _biologicalFather.getWikiTreeId() +
                                        " and " +
                                        profile.getWikiTreeId() +
                                        ")" );

                }

            }

            if ( profile.isGenderFemale() ) {

                if ( _biologicalMother == null ) {

                    _biologicalMother = profile;

                } else {

                    System.err.println( "WikiTreePersonProfile:  " +
                                        getWikiTreeId() +
                                        " has more than one mother (" +
                                        _biologicalMother.getWikiTreeId() +
                                        " and " +
                                        profile.getWikiTreeId() +
                                        ")" );

                }

            }

        }

//	System.out.println( "WikiTree ID of new profile is \"" + getWikiTreeId() + "\"" );

    }

    /**
     Get this person's parents.

     @return an unmodifiable collection containing this person's parents (to the extent that they are known).
     */

    public Collection<WikiTreePersonProfile> getParents() {

        return Collections.unmodifiableCollection( _parents );

    }

    /**
     Get this person's spouses.

     @return an unmodifiable collection containing this person's spouses (to the extent that they are known).
     */

    public Collection<WikiTreePersonProfile> getSpouses() {

        return Collections.unmodifiableCollection( _spouses );

    }

    /**
     Get this person's children.

     @return an unmodifiable collection containing this person's children (to the extent that they are known).
     */

    public Collection<WikiTreePersonProfile> getChildren() {

        return Collections.unmodifiableCollection( _children );

    }

    /**
     Get this person's siblings.

     @return an unmodifiable collection containing this person's siblings (to the extent that they are known).
     */

    public Collection<WikiTreePersonProfile> getSiblings() {

        return Collections.unmodifiableCollection( _siblings );

    }

    public ProfileType getProfileType() {

        return _profileType;

    }

    /**
     Get the person profiles of all relatives of the primary person within a particular class.

     @param relationship the class of people of interest. Also, the key which specifies which field in the
     primary person's profile object lists the relatives in the specified class.
     @return a collection of the {@link WikiTreePersonProfile} instances for the people in the specified class/relationship.
     */

    @NotNull
    private static Collection<WikiTreePersonProfile> getPeople( JSONObject profileObject, ProfileType profileType, String relationship )
            throws WikiTreeRequestFailedException {

        Collection<WikiTreePersonProfile> rval = new LinkedList<>();

        Object relativesObj = profileObject.get( relationship );
        Collection values;
        if ( relativesObj instanceof JSONObject ) {

//	    System.out.println( relationship + " are in an object" );
            values = ( (JSONObject)relativesObj ).values();

        } else if ( relativesObj instanceof JSONArray ) {

//	    System.out.println( relationship + " are in an array" );
            values = ( (JSONArray)relativesObj );

        } else if ( relativesObj == null ) {

//	    System.out.println( "no " + relationship.toLowerCase() );
            values = new LinkedList();

        } else {

            throw new ReallyBadNewsError( relationship + " are something strange - instance of " + relativesObj.getClass().getCanonicalName() );

        }

        for ( Object personProfileObj : values ) {

            if ( personProfileObj instanceof JSONObject ) {

                JSONObject personProfileJsonObject = (JSONObject)personProfileObj;
                WikiTreePersonProfile personProfile = new WikiTreePersonProfile( WikiTreeRequestType.UNKNOWN, personProfileJsonObject, profileType );
                rval.add( personProfile );

            }

        }

//	System.out.println( "got the " + rval.size() + " " + relationship.toLowerCase() );

        return rval;

    }

    /**
     Augment this instance with person's biological father.
     <p/>Filled in by this class's constructor.
     If this profile is part of a return result for a {@link WikiTreeAncestors} request then this value is
     replaced when WikiTreeAncestors constructs its base person's ancestral tree.
     See {@link WikiTreeAncestors} for more info.

     @param biologicalFather this person's biological father.
     */

    public void setBiologicalFather( WikiTreePersonProfile biologicalFather ) {

        _biologicalFather = biologicalFather;

    }

    /**
     Get this instance's biological father.
     <p/>This information is not directly provided by any of the WikiTree API calls.
     This API's implementation of {@link WikiTreeApiWrappersSession#getAncestors(WikiTreeId, Integer)} derives this information based
     on the array of ancestors provided by the WikiTree API's {@code getAncestors} request.
     {@link WikiTreeApiWrappersSession#getAncestors(WikiTreeId, Integer)} ensures that there are no loops in its ancestral trees although it
     does not guarantee that the ancestral trees actually contain all the ancestors provided by its WikiTree API's {@code getAncestors} request
     (ensuring that the tree has no loops may force it to leave out such ancestors).

     @return this instance's biological father; {@code null} if unknown to this instance (not the same thing as unknown to WikiTree).
     */

    public WikiTreePersonProfile getBiologicalFather() {

        return _biologicalFather;

    }

    /**
     Augment this instance with person's biological mother.
     <p/>Filled in by this class's constructor.
     If this profile is part of a return result for a {@link WikiTreeAncestors} request then this value is
     replaced when WikiTreeAncestors constructs its base person's ancestral tree.
     See {@link WikiTreeAncestors} for more info.

     @param biologicalMother this person's biological mother.
     */

    public void setBiologicalMother( WikiTreePersonProfile biologicalMother ) {

        _biologicalMother = biologicalMother;

    }

    /**
     Get this instance's biological mother.
     <p/>This information

     @return this instance's biological mother; {@code null} if unknown to this instance (not the same thing as unknown to WikiTree).
     */

    public WikiTreePersonProfile getBiologicalMother() {

        return _biologicalMother;

    }

    /**
     Determine if the person's gender is male.

     @return the result of {@code getGender() == BiologicalGender.MALE}.
     */

    public boolean isGenderMale() {

        return getGender() == WikiTreeApiClient.BiologicalGender.MALE;

    }

    /**
     Determine if the person's gender is female.

     @return the result of {@code getGender() == BiologicalGender.FEMALE}.
     */

    public boolean isGenderFemale() {

        return getGender() == WikiTreeApiClient.BiologicalGender.FEMALE;

    }

    /**
     Determine if the person's gender is unknown.

     @return the result of {@code getGender() == BiologicalGender.UNKNOWN}.
     */

    public boolean isGenderUnknown() {

        return getGender() == WikiTreeApiClient.BiologicalGender.UNKNOWN;

    }

    /**
     Get the person's Person.Id.

     @return the person's Person.Id.
     */

    public long getPersonId() {

        if ( _personId == null ) {

            Object personIdObj = get( "Id" );
            if ( personIdObj == null ) {

                _personId = -1L;

            } else if ( personIdObj instanceof String ) {

                try {

                    _personId = Long.parseLong( (String)personIdObj );

                } catch ( NumberFormatException e ) {

                    throw new ReallyBadNewsError( "WikiTreePersonProfile.getPersonId:  Person.Id is not an integer" );

                }

            } else if ( personIdObj instanceof Number ) {

                _personId = ( (Number)personIdObj ).longValue();

            } else {

                throw new ReallyBadNewsError(
                        "WikiTreePersonProfile.getPersonId:  Person.Id is neither null, a String or a Number; it's a " +
                        personIdObj.getClass().getCanonicalName() );

            }

        }

        return _personId.longValue();

    }

    /**
     Get the person's WikiTree ID.

     @return the person's WikiTree ID or {@code "Id=" + getPersonId()} if the person's WikiTree ID is unavailable (not sure if that is even possible).
     */

    @NotNull
    public WikiTreeId getWikiTreeId() {

        Object wikiTreeIdObj = get( "Name" );
        if ( wikiTreeIdObj == null ) {

            return new WikiTreeId( "Id=" + getPersonId() );

        } else if ( wikiTreeIdObj instanceof String ) {

            return new WikiTreeId( (String)wikiTreeIdObj );

        } else {

            throw new ReallyBadNewsError( "WikiTreePersonProfile.getWikiTreeId:  WikiTreeId is neither null or a String; it's a " +
                                          wikiTreeIdObj.getClass().getCanonicalName() );

        }

    }

    public Optional<Object> getOptionalValue( @NotNull String jsonKey ) {

        Object obj = get( jsonKey );

        return Optional.ofNullable( obj );

    }

    @NotNull
    public Object getMandatoryValue( @NotNull String jsonKey ) {

        Object obj = get( jsonKey );

        return obj;

    }

    @NotNull
    public <T> T getValue( @NotNull String jsonKey, @NotNull Supplier<T> alternative ) {

        Optional<Object> optRval = getOptionalValue( jsonKey );

        return (T)optRval.orElseGet( alternative );

    }

    /**
     Get the person's short name.

     @return the person's short name. If the person has no short name then the person's WikiTree ID.
     */

    @NotNull
    public String getShortName() {

        String nameObj = getValue(
                "ShortName",
                () -> WikiTreePersonProfile.this.getWikiTreeId().getValueString()
        );

        return nameObj;

//        if ( nameObj instanceof String ) {
//
//            return (String)nameObj;
//
//        } else {
//
//            ReallyBadNewsError e = new ReallyBadNewsError(
//                    "WikiTreePersonProfile.getShortName:  short name is a " + nameObj.getClass().getCanonicalName() + " equal to " + nameObj
//            );
//
//            throw e;
//
//        }

    }

    /**
     Get the person's first name.

     @return an {@code Optional<String>} containing the person's first name if they have one or {@code Optional.empty()} if they don't.
     */

    @NotNull
    public String getFirstName() {

        String firstName = getValue(
                "FirstName",
                this::getShortName
        );

        return firstName;

//        if ( nameObj instanceof String ) {
//
//            return (String)nameObj;
//
//        } else {
//
//            throw new ReallyBadNewsError(
//                    "WikiTreePersonProfile.getShortName:  short name is a " + nameObj.getClass().getCanonicalName() + " equal to " + nameObj
//            );
//
//        }
//        Object nameObj = get( "ShortName" );
//        if ( nameObj == null ) {
//
//            return Optional.empty();
//
//        } else if ( nameObj instanceof String ) {
//
//            return Optional.of( (String)nameObj );
//
//        } else {
//
//            throw new ReallyBadNewsError( "WikiTreePersonProfile.getFirstName:  first name is neither null or a String; it's a " +
//                                          nameObj.getClass().getCanonicalName() + " equal to " + nameObj );
//        }

    }

    /**
     Get the person's gender.

     @return one of {@link WikiTreeApiClient.BiologicalGender#FEMALE}, {@link WikiTreeApiClient.BiologicalGender#MALE}, or {@link WikiTreeApiClient.BiologicalGender#UNKNOWN}.
     */

    @NotNull
    public WikiTreeApiClient.BiologicalGender getGender() {

        if ( _gender == null ) {

            _gender = WikiTreeApiClient.BiologicalGender.UNKNOWN;

            Object genderObj = get( "Gender" );
            if ( genderObj instanceof String ) {

                WikiTreeApiClient.BiologicalGender g = s_genderMap.get( genderObj );
                if ( g != null ) {

                    _gender = g;

                }

            } else if ( genderObj != null ) {

                System.err.println( "WikiTreePersonProfile.getGender:  gender value is not a String - returning unknown (gender value is \"" +
                                    genderObj +
                                    "\")" );

            }

        }

        return _gender;

    }

    public String toString() {

        StringBuilder sb = new StringBuilder( "WikiTreePersonProfile( " );

        String genderString;

        switch ( getGender() ) {

            case FEMALE:

                genderString = ( "F" );
                break;

            case MALE:

                genderString = ( "M" );
                break;

            default:

                genderString = ( "?" );
                break;

        }

        sb.
                  append( getShortName() ).
                  append( " ( gender:" ).
                  append( genderString ).
                  append( ", birthDate:" ).
                  append( WikiTreeApiUtilities.cleanupStringDate( get( "BirthDate" ) ) ).
                  append( ", deathDate:" ).
                  append( WikiTreeApiUtilities.cleanupStringDate( get( "DeathDate" ) ) ).
                  append( " )" ).
                  append( " )" );

        return sb.toString();

    }

}
