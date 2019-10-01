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

@SuppressWarnings({ "unchecked", "unused" })
public class WikiTreePersonProfile extends WikiTreeProfile {

    public static final String BIRTH_DATE = "BirthDate";
    public static final String BIRTH_DATE_DECADE = "BirthDateDecade";
    public static final String BIRTH_LOCATION = "BirthLocation";
    public static final String BIRTH_NAME = "BirthName";
    public static final String BIRTH_NAME_PRIVATE = "BirthNamePrivate";
    public static final String CHILDREN = "Children";
    public static final String DEATH_DATE = "DeathDate";
    public static final String DEATH_LOCATION = "DeathLocation";
    public static final String FATHER = "Father";
    public static final String FIRST_NAME = "FirstName";
    public static final String GENDER = "Gender";
    public static final String HAS_CHILDREN = "HasChildren";
    public static final String ID = "Id";
    public static final String IS_LIVING = "IsLiving";
    public static final String LAST_NAME_AT_BIRTH = "LastNameAtBirth";
    public static final String LAST_NAME_CURRENT = "LastNameCurrent";
    public static final String LAST_NAME_OTHER = "LastNameOther";
    public static final String LONG_NAME = "LongName";
    public static final String LONG_NAME_PRIVATE = "LongNamePrivate";
    public static final String MANAGER = "MANAGER";
    public static final String MARRIAGE_DATE = "marriage_date";
    public static final String MARRIAGE_LOCATION = "marriage_location";
    public static final String MIDDLE_NAME = "MiddleName";
    public static final String MOTHER = "Mother";
    public static final String NAME = "Name";
    public static final String NICKNAMES = "Nicknames";
    public static final String NO_CHILDREN = "1";
    public static final String PARENTS = "Parents";
    public static final String PHOTO = "Photo";
    public static final String PREFIX = "Prefix";
    public static final String PRIVACY_IS_AT_LEAST_PUBLIC = "Privacy_IsAtLeastPublic";
    public static final String PRIVACY_IS_OPEN = "Privacy_IsOpen";
    public static final String PRIVACY_IS_PRIVATE = "Privacy_IsPrivate";
    public static final String PRIVACY_IS_SEMI_PRIVATE_BIO = "Privacy_IsSemiPrivateBio";
    public static final String PRIVACY_IS_SEMI_PRIVATE = "Privacy_IsSemiPrivate";
    public static final String PRIVACY = "Privacy";
    public static final String REAL_NAME = "RealName";
    public static final String SHORT_NAME = "ShortName";
    public static final String SIBLINGS = "Siblings";
    public static final String SPOUSES = "Spouses";
    public static final String SUFFIX = "Suffix";
    private final boolean _isLiving;

    /**
     Specify what type of request yielded this profile.
     */

    public enum ProfileType {

        /**
         The target person of a {@code getPerson} request that asked for all fields ("*").
         */

        PRIMARY_PERSON,

        /**
         The target person of a {@code getRelatives} request that asked for all of the target person's relatives
         (all of the relative type selection booleans in the
         {@link WikiTreeApiWrappersSession#getRelatives(String, boolean, boolean, boolean, boolean)}
         call were {@code true}.
         */

        RELATIVES_COMPLETE,

        /**
         The target person of a {@code getRelatives} request that did not ask for all of the target person's relatives
         (at least one of the relative type selection booleans in the
         {@link WikiTreeApiWrappersSession#getRelatives(String, boolean, boolean, boolean, boolean)}
         call was {@code false}.
         */

        RELATIVES_INCOMPLETE,

        /**
         A relative of the target person obtained via a {@code getPerson} or a {@code getRelatives} request.
         */

        RELATIVE,

        /**
         A person profile returned by a {@code getProfile} or a {@code getPersonProfile} request that did not ask for all fields ("*").
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
    private final Collection<WikiTreePersonProfile> _parents = new LinkedList<>();
    private final Collection<WikiTreePersonProfile> _spouses = new LinkedList<>();
    private final Collection<WikiTreePersonProfile> _children = new LinkedList<>();

    private final Collection<WikiTreePersonProfile> _siblings = new LinkedList<>();

    private Long _personId;

    private final ProfileType _profileType;

    /**
     Create a person profile for the person described by the specified JSON object.

     @param requestType     the type of request to the WikiTree API server that got us this profile.
     If not {@code null} then this generally means that the profile was part of a larger response (for example, it might be one of the potentially
     many profiles returned by a {@link WikiTreeApiWrappersSession#getWatchlist(Boolean, Boolean, Boolean, Boolean, String, Integer, Integer, String)} call).
     If {@code null} then this is generally the entire response from a request to the WikiTree API server (for example, the result of calling
     {@link WikiTreeApiWrappersSession#getProfile(WikiTreeId)} for a person's profile).
     @param jsonObject      the specified JSON object.
     @param profileType     what kind of profile is this
     ({@link ProfileType#PRIMARY_PERSON}, {@link ProfileType#RELATIVES_COMPLETE}, {@link ProfileType#RELATIVES_INCOMPLETE},
     {@link ProfileType#RELATIVE}, {@link ProfileType#PROFILE}, {@link ProfileType#OTHER}).
     @param profileLocation a varargs series of {@link String} values (or an array of {@link String} values) indicating the path to where
     in {@code jsonObject} we should expect to find the actual person profile. If no vararg values are provided (or {@code profileLocation} is an empty array)
     then {@code jsonObject} is expected to be the person profile object.
     Note that you will be the not-so-proud recipient of
     a {@link ReallyBadNewsError} unchecked exception if the actual person profile is not where you said it was.
     @throws ReallyBadNewsError if any of the following are true:
     <ol>
     <li>no vararg values are provided (or {@code profileLocation} is an empty array) and {@code jsonObject}
     is not the actual person profile object</li><li>there is nothing in {@code jsonObject} at the specified path location</li>
     <li>the search down the specified path location yields a {@code null} value before we get to the end of the path.</li>
     </ol>
     */

    public WikiTreePersonProfile(
            @Nullable final WikiTreeRequestType requestType,
            final @NotNull JSONObject jsonObject,
            final @NotNull ProfileType profileType,
            final String... profileLocation
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

        if ( get( ID ) == null ) {

            throw new ReallyBadNewsError( "WTPP does not have a Person.Id:  " + this );

        }

        if ( get( NAME ) == null ) {

            throw new ReallyBadNewsError( "WTPP does not have a Name:  " + this );

        }

        // Make sure that what we saved is actually a profile.

        Object isLiving = get( IS_LIVING );
        if ( isLiving == null ) {

            //noinspection unchecked
            throw new ReallyBadNewsError(
                    "WikiTreePersonProfile:  we did not handle (" + requestType + ", " + jsonObject + ", " +
                    WikiTreeApiUtilities.formatPath( profileLocation ) + " ) correctly; we saved {" +
                    new HashMap( this ) +
                    "}"
            );

        }

        // Figure out the type of the relative profiles (I'm not sure that this is ever anything but ProfileType.RELATIVE).

        ProfileType relativeProfileType =
                ( profileType == ProfileType.PRIMARY_PERSON || profileType == ProfileType.RELATIVES_COMPLETE || profileType == ProfileType.RELATIVES_INCOMPLETE )
                        ?
                        ProfileType.RELATIVE
                        :
                        ProfileType.OTHER;
        _parents.addAll( getPeople( this, relativeProfileType, PARENTS ) );
        _children.addAll( getPeople( this, relativeProfileType, CHILDREN ) );
        _spouses.addAll( getPeople( this, relativeProfileType, SPOUSES ) );
        _siblings.addAll( getPeople( this, relativeProfileType, SIBLINGS ) );

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

        if ( isLiving instanceof Number ) {

            _isLiving = 1 == ( (Number)isLiving ).intValue();
            if ( _isLiving && hasDeathDate() ) {

                System.err.println( "warning:  profile for " +
                                    getWikiTreeId() +
                                    " is marked as living but has a death date of " +
                                    WikiTreeApiUtilities.formatDate( getDeathDate() ) );

            }

        } else {

            throw new ReallyBadNewsError(
                    "WikiTreePersonProfile:  profile's IS_LIVING value is not numeric (it is a " + isLiving.getClass().getCanonicalName() + ")"
            );

        }

    }

    /**
     Determine if this person is, according to the WikiTree database, currently alive.
     */

    public boolean isLiving() {

        return _isLiving;

    }

    /**
     Get this person's parents if they are available.

     @return if this is a {@link #isFullProfile()} then an {@link Optional} enclosing an unmodifiable collection containing
     this person's parents (to the extent that they are known).
     Otherwise, an {@link Optional#empty()}.
     */

    public Optional<Collection<WikiTreePersonProfile>> getParents() {

        if ( isFullProfile() ) {

            return Optional.of( Collections.unmodifiableCollection( _parents ) );

        } else {

            return Optional.empty();

        }

    }

    public int getParentCount() {

        return _parents.size();

    }

    public boolean containsSomeParents() {

        return !_parents.isEmpty();
    }

    /**
     Get this person's spouses.

     @return if this is a {@link #isFullProfile()} then an {@link Optional} enclosing an unmodifiable collection containing
     this person's spouses (to the extent that they are known).
     Otherwise, an {@link Optional#empty()}.
     */

    public Optional<Collection<WikiTreePersonProfile>> getSpouses() {

        if ( isFullProfile() ) {

            return Optional.of( Collections.unmodifiableCollection( _spouses ) );

        } else {

            return Optional.empty();

        }

    }

    public int getSpouseCount() {

        return _spouses.size();

    }

    public boolean containsSomeSpouses() {

        return !_spouses.isEmpty();
    }

    /**
     Get this person's children.

     @return if this is a {@link #isFullProfile()} then an {@link Optional} enclosing an unmodifiable collection containing
     this person's children (to the extent that they are known).
     Otherwise, an {@link Optional#empty()}.
     */

    public Optional<Collection<WikiTreePersonProfile>> getChildren() {

        if ( isFullProfile() ) {

            return Optional.of( Collections.unmodifiableCollection( _children ) );

        } else {

            return Optional.empty();

        }

    }

    public int getChildCount() {

        return _children.size();

    }

    public boolean containsSomeChildren() {

        return !_children.isEmpty();
    }

    /**
     Get this person's siblings.

     @return if this is a {@link #isFullProfile()} then an {@link Optional} enclosing an unmodifiable collection containing
     this person's siblings (to the extent that they are known).
     Otherwise, an {@link Optional#empty()}.
     */

    public Optional<Collection<WikiTreePersonProfile>> getSiblings() {

        if ( isFullProfile() ) {

            return Optional.of( Collections.unmodifiableCollection( _siblings ) );

        } else {

            return Optional.empty();

        }

    }

    public int getSiblingCount() {

        return _siblings.size();

    }

    public boolean containsSomeSiblings() {

        return !_siblings.isEmpty();

    }

    public ProfileType getProfileType() {

        return _profileType;

    }

    /**
     Determine if this is this a full (i.e. primary) profile.

     @return {@code true} if this profile was obtained
     via a call to {@link WikiTreeApiWrappersSession#getPerson(String, String)} with {@code "*"} as the second parameter
     or is the target profile of a {@link WikiTreeApiWrappersSession#getRelatives(String, boolean, boolean, boolean, boolean)} request.
     Note that
     {@link WikiTreeApiWrappersSession#getPerson(WikiTreeId)} and {@link WikiTreeApiWrappersSession#getPerson(long)} return full / primary
     profiles as they are are really just wrappers which call
     {@link WikiTreeApiWrappersSession#getPerson(String, String)} with {@code "*"} as the second parameter).
     Only full / primary profiles can be counted on to include the person's parents, spouses, siblings, and/or children (there may be other differences - use {@code getPerson()} get a full profile if you want everything).
     */

    public boolean isFullProfile() {

        return _profileType == ProfileType.PRIMARY_PERSON ||
               _profileType == ProfileType.RELATIVES_COMPLETE ||
               _profileType == ProfileType.RELATIVES_INCOMPLETE;

    }

    /**
     Determine if this is this a relative profile (a child, parent, sibling or spouse of a primary profile or a profile obtained
     via {@code getRelatives}.
     @return {@code true} if this profile's profile type is {@link ProfileType#RELATIVE}; {@code false} otherwise.
     */

    public boolean isRelativeProfile() {

        return _profileType == ProfileType.PRIMARY_PERSON;

    }

    /**
     Get the person profiles of all relatives of the primary person within a particular class.

     @param relationship the class of people of interest. Also, the key which specifies which field in the
     primary person's profile object lists the relatives in the specified class.
     @return a collection of the {@link WikiTreePersonProfile} instances for the people in the specified class/relationship.
     */

    @NotNull
    private static Collection<WikiTreePersonProfile> getPeople(
            final JSONObject profileObject,
            final ProfileType profileType,
            final String relationship
    )
            throws WikiTreeRequestFailedException {

        Collection<WikiTreePersonProfile> rval = new LinkedList<>();

        Object relativesObj = profileObject.get( relationship );
        Collection values;
        if ( relativesObj instanceof JSONObject ) {

            values = ( (JSONObject)relativesObj ).values();

        } else if ( relativesObj instanceof JSONArray ) {

            values = ( (JSONArray)relativesObj );

        } else if ( relativesObj == null ) {

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

    public void setBiologicalFather( final WikiTreePersonProfile biologicalFather ) {

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

     @return this instance's biological father if it is known to this instance (not the same thing as known to WikiTree).
     */

    @NotNull
    public Optional<WikiTreePersonProfile> getBiologicalFather() {

        return Optional.ofNullable( _biologicalFather );

    }

    /**
     Augment this instance with person's biological mother.
     <p/>Filled in by this class's constructor.
     If this profile is part of a return result for a {@link WikiTreeAncestors} request then this value is
     replaced when WikiTreeAncestors constructs its base person's ancestral tree.
     See {@link WikiTreeAncestors} for more info.

     @param biologicalMother this person's biological mother.
     */

    public void setBiologicalMother( final WikiTreePersonProfile biologicalMother ) {

        _biologicalMother = biologicalMother;

    }

    /**
     Get this instance's biological mother.
     <p/>This information

     @return this instance's biological mother if it is known to this instance (not the same thing as known to WikiTree).
     */

    @NotNull
    public Optional<WikiTreePersonProfile> getBiologicalMother() {

        return Optional.ofNullable( _biologicalMother );

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

            Object personIdObj = get( ID );
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

        return WikiTreeId.getWikiTreeId( this );

    }

    @NotNull
    public Optional<Object> getOptionalValue( final @NotNull String jsonKey ) {

        Object obj = get( jsonKey );

        return Optional.ofNullable( obj );

    }

    @NotNull
    public Object getMandatoryValue( final @NotNull String jsonKey ) {

        Object obj = get( jsonKey );

        return obj;

    }

    @NotNull
    public <T> T getValue( final @NotNull String jsonKey, final @NotNull Supplier<T> alternative ) {

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
                SHORT_NAME,
                () -> WikiTreePersonProfile.this.getWikiTreeId().getValueString()
        );

        return nameObj;

    }

    /**
     Get the person's long name.

     @return the person's long name. If the person has no long name then the person's short name via {@link #getShortName()}.
     */

    @NotNull
    public String getLongName() {

        String nameObj = getValue(
                LONG_NAME,
                this::getShortName
        );

        return nameObj;

    }

    /**
     Does this person actually have a birth name?

     @return {@code true} if they do; {@code false} if they don't.
     For what it is worth, I (Daniel Boulet) suspect that all profile's have a birth name entry as the alternative just doesn't make sense to me.
     */

    public boolean hasBirthName() {

        Object s = get( BIRTH_NAME );
        return s != null && !( (String)s ).trim().isEmpty();

    }

    /**
     Get the person's birth name.

     @return the person's birth name.
     @throws IllegalArgumentException if the person has no birth name (see {@link #hasBirthName()} for a way to avoid this).
     For what it is worth, I (Daniel Boulet) suspect that all profile's have a birth name entry as the alternative just doesn't make sense to me.
     */

    @NotNull
    public String getBirthName() {

        String birthName = (String)get( BIRTH_NAME );
        if ( birthName == null || birthName.trim().isEmpty() ) {

            birthName = (String)get( BIRTH_NAME_PRIVATE );

            if ( birthName == null || birthName.trim().isEmpty() ) {

                // Seriously??? How can a profile not have a birth name?

                throw new IllegalArgumentException( "WikiTreePersonProfile.getBirthName():  \"" +
                                                    getShortName() +
                                                    "\" (" + getWikiTreeId() + ") has no birth name (please notify danny@matilda.com)" );

            }

        }

        return birthName.trim();

    }

    /**
     Does this person actually have a first name?

     @return {@code true} if they do; {@code false} if they don't.
     */

    public boolean hasFirstName() {

        Object s = get( FIRST_NAME );
        return s != null && !( (String)s ).trim().isEmpty();

    }

    /**
     Get the person's first name.

     @return the person's first name if they have one; their short name otherwise.
     */

    @NotNull
    public String getFirstName() {

        String firstName = getValue(
                FIRST_NAME,
                this::getShortName
        );

        return firstName;

    }

    /**
     Does this person have a marriage date (probably only happens in spouse records that hang off person records)?

     @return {@code true} if they do; {@code false} if they don't.
     */

    public boolean hasMarriageDate() {

        Object s = get( MARRIAGE_DATE );
        return s != null && !( (String)s ).trim().isEmpty() && !"0000-00-00".equals( s );

    }

    /**
     Get the person's marriage date.

     @return the date that this person was married (probably only happens in spouse records that hang off person records).
     @throws IllegalArgumentException if there is no marriage date (see {@link #hasMarriageDate()} for a way to avoid this).
     */

    @NotNull
    public String getMarriageDate() {

        String marriageDate = (String)getMandatoryValue( MARRIAGE_DATE );

        return marriageDate;

    }

    /**
     Does this person have a marriage location (probably only happens in spouse records that hang off person records)?

     @return {@code true} if they do; {@code false} if they don't.
     */

    public boolean hasMarriageLocation() {

        Object s = get( MARRIAGE_LOCATION );
        return s != null && !( (String)s ).trim().isEmpty();

    }

    /**
     Get the person's marriage location.

     @return the location where this person was married (probably only happens in spouse records that hang off person records).
     @throws IllegalArgumentException if there is no marriage location (see {@link #hasMarriageDate()} for a way to avoid this).
     */

    @NotNull
    public String getMarriageLocation() {

        String marriageDate = (String)getMandatoryValue( MARRIAGE_LOCATION );

        return marriageDate;

    }

    /**
     Get the person's last name.

     @return the person's last name at birth.
     */

    @NotNull
    public String getLastNameAtBirth() {

        String lastNameAtBirth = (String)getMandatoryValue( LAST_NAME_AT_BIRTH );

        return lastNameAtBirth;

    }

    /**
     Get the person's gender.

     @return one of {@link WikiTreeApiClient.BiologicalGender#FEMALE}, {@link WikiTreeApiClient.BiologicalGender#MALE}, or {@link WikiTreeApiClient.BiologicalGender#UNKNOWN}.
     */

    @NotNull
    public WikiTreeApiClient.BiologicalGender getGender() {

        if ( _gender == null ) {

            _gender = WikiTreeApiClient.BiologicalGender.UNKNOWN;

            Object genderObj = get( GENDER );
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

    /**
     Does this person have a birth location?

     @return {@code true} if they do; {@code false} if they don't.
     */

    public boolean hasBirthLocation() {

        Object s = get( BIRTH_LOCATION );
        return s != null && !( (String)s ).trim().isEmpty();

    }

    /**
     Get the person's birth location.

     @return the person's birth location.
     @throws IllegalArgumentException if they don't have a birth location (see {@link #hasBirthLocation} for a way around this).
     */

    @NotNull
    public String getBirthLocation() {

        String birthLocation = (String)getMandatoryValue( BIRTH_LOCATION );

        return birthLocation;

    }

    /**
     Does this person have a birth date?

     @return {@code true} if they do; {@code false} if they don't.
     */

    public boolean hasBirthDate() {

        Object s = get( BIRTH_DATE );
        return s != null && !( (String)s ).trim().isEmpty() && !"0000-00-00".equals( s );

    }

    /**
     Get the person's birth date.

     @return whatever happens to be in the database (obviously not good enough but this is method is a work in progress right now).
     @throws IllegalArgumentException if they don't have a birth date (see {@link #hasBirthDate} for a way around this).
     */

    public String getBirthDate() {

        String birthDate = (String)getMandatoryValue( BIRTH_DATE );

        return birthDate;

    }

    /**
     Does this person have a death date?

     @return {@code true} if they do; {@code false} if they don't.
     */

    public boolean hasDeathDate() {

        Object s = get( DEATH_DATE );
        return s != null && !( (String)s ).trim().isEmpty() && !"0000-00-00".equals( s );

    }

    /**
     Get the person's death date.

     @return whatever happens to be in the database (obviously not good enough but this is method is a work in progress right now).
     @throws IllegalArgumentException if they don't have a birth date (see {@link #hasBirthDate} for a way around this).
     */

    public String getDeathDate() {

        String deathDate = (String)getMandatoryValue( DEATH_DATE );

        return deathDate;

    }

    /**
     Does this person have a death location?

     @return {@code true} if they do; {@code false} if they don't.
     */

    public boolean hasDeathLocation() {

        Object s = get( DEATH_LOCATION );
        return s != null && !( (String)s ).trim().isEmpty();

    }

    /**
     Get the person's death location.

     @return the person's death location.
     @throws IllegalArgumentException if they don't have a death location (see {@link #hasDeathLocation} for a way around this).
     */

    @NotNull
    public String getDeathLocation() {

        String deathLocation = (String)getMandatoryValue( DEATH_LOCATION );

        return deathLocation;

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
                  append( WikiTreeApiUtilities.cleanupStringDate( get( BIRTH_DATE ) ) ).
                  append( ", deathDate:" ).
                  append( WikiTreeApiUtilities.cleanupStringDate( get( DEATH_DATE ) ) ).
                  append( " )" ).
                  append( " )" );

        return sb.toString();

    }

}
