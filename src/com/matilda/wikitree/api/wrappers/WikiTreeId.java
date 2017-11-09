/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.wrappers;

import com.matilda.wikitree.api.exceptions.ReallyBadNewsError;
import com.matilda.wikitree.api.util.WikiTreeApiUtilities;
import org.jetbrains.annotations.NotNull;

/**
 Represent a WikiTree ID as a class rather than as a string to make it harder to pass junk as WikiTree ID parameters.
 <p/>A WikiTree ID is defined as either:
 <ul>
 <li>a WikiTree ID Name consisting of sequence of one or more characters followed by a minus sign followed by one or more digits (for example, {@code "Churchill-4"})</li>
 <li>a WikiTree Space Name which starts with {@code Space:} and has at least one character after the colon (for example, {@code "Space:Allied_POW_camps"}</li>
 </ul>
 For example, each of the following are, according to the above definition, valid WikiTree IDs:
 <ul>
 <li>{@code "Churchill-4"} (Sir Winston S. Churchill)</li>
 <li>{@code "Baden-Powell-19"} (Son of Lord Baden-Powell who founded the Scout Movement)</li>
 <li>{@code "宋-1"} ( 靄齡 (Ai-ling) "Eling" Soong Kung formerly 宋 aka Soong )</li>
 <li>{@code "Space:Allied_POW_camps"} (the name of a WikiTree Space)</li>
 <li>{@code "tre98743,n54jlsdgo7dsi23hljkfdioufsdew-123"} (mostly random junk that satisfies the definition - see below for more info)</li>
 </ul>
 Alternatively, none of the following are, according to the above definition, valid WikiTree IDs:
 <ul>
 <li>{@code "Churchill"} (no minus sign followed by one or more digits)</li>
 <li>{@code "Churchill-"} (no digits after the minus sign)</li>
 <li>{@code "5589"} (numeric {@code Person.Id}s or {@code Space.Id}s are not WikiTree IDs)</li>
 <li>{@code "Space:"} (nothing after the colon)</li>
 </ul>
 Note that the goal of this exercise is to eliminate total junk and to classify every possible string into exactly one of
 <ul>
 <li>a WikiTree ID Name</li>
 <li>a WikiTree Space Name</li>
 <li>none of the above</li>
 </ul>
 We err on the side of accepting an invalid value subject to the constraint
 that we don't accept a value that one of our sibling methods also accepts.
 This allows us to let the WikiTree API server make the final determination of validity.
 */

@SuppressWarnings("WeakerAccess")
public class WikiTreeId implements Comparable<WikiTreeId> {

    @NotNull
    private final String _wikiTreeIdString;
    private final boolean _isIdName;

//    private final Kind _kind;

    public WikiTreeId( @NotNull final String wikiTreeIdString )
            throws IllegalArgumentException, ReallyBadNewsError {

        super();

        if ( wikiTreeIdString.isEmpty() ) {

            throw new IllegalArgumentException( "WikiTreeId:  invalid id \"" + wikiTreeIdString + "\" (must not be empty)" );

        }

        boolean isName = WikiTreeApiUtilities.isValidWikiTreeIdPersonName( wikiTreeIdString );
        boolean isSpaceName = WikiTreeApiUtilities.isValidWikiTreeSpaceName( wikiTreeIdString );

        if ( isName && isSpaceName ) {

            throw new ReallyBadNewsError(
                    "WikiTreeId:  " + WikiTreeApiUtilities.enquoteJavaString( wikiTreeIdString ) +
                    " looks like both a WikiTreeId Name and a Space name " +
                    "(this is a bug; please email this entire message to danny@matilda.com)"
            );

        }

        if ( !isName && !isSpaceName ) {

            throw new IllegalArgumentException(
                    "WikiTreeId:  " + WikiTreeApiUtilities.enquoteJavaString( wikiTreeIdString ) +
                    " is neither a WikiTreeId Name or a WikiTree Space Name"
            );

        }

        _isIdName = isName;

        _wikiTreeIdString = wikiTreeIdString;

    }

    @NotNull
    public String getName() {

        return _wikiTreeIdString;

    }

    /**
     Determine if this instance encapsulates a WikiTree ID name.
     <p/>A WikiTree ID Name consists of a sequence of one or more characters followed by a minus sign followed by one or more digits (for example, {@code "Churchill-4"}).
     <p/>ALWAYS yields the same result as a call to {@link #isPersonName()}.
     <p/>Since there are only two kinds of ids managed by this class, a call to this method always yields the opposite result as a call to
     {@link #isSpaceName()}.

     @return {@code true} if this instance encapsulates a WikiTreeId Name; {@code false} otherwise.
     */

    public boolean isIdName() {

        return _isIdName;

    }

    /**
     Determine if this instance encapsulates a WikiTree Person Name (another way of asking if this instance encapsulates a WikiTree ID name).
     <p/>ALWAYS yields the same result as a call to {@link #isIdName()}.
     <p/>Since there are only two kinds of ids managed by this class, a call to this method always yields the opposite result as a call to
     {@link #isSpaceName()}.

     @return {@code true} if this instance encapsulates a WikiTreeId Name; {@code false} otherwise.
     */

    public boolean isPersonName() {

        return isIdName();

    }

    /**
     Determine if this instance encapsulates a WikiTree Space Name.
     <p/>A WikiTree Space Name starts with {@code Space:} and has at least one character after the colon (for example, {@code "Space:Allied_POW_camps"}).
     <p/>Since there are only two kinds of ids managed by this class, a call to this method always yields the opposite result as a call to
     either {@link #isIdName()} or {@link #isPersonName()}.

     @return {@code true} if this instance encapsulates a WikiTree Space Name; {@code false} otherwise.
     */

    public boolean isSpaceName() {

        return !isIdName();

    }

    /**
     Get this instance's value string.

     @return this instance's value string (the string that was provided when this instance was created).
     */

    @NotNull
    public String getValueString() {

        return _wikiTreeIdString;

    }

    public boolean equals( final Object rhs ) {

        return rhs instanceof WikiTreeId && compareTo( (WikiTreeId)rhs ) == 0;

    }

    public int hashCode() {

        return _wikiTreeIdString.hashCode();

    }

    public String toString() {

        return _wikiTreeIdString;

    }

    @Override
    public int compareTo( @NotNull final WikiTreeId o ) {

        return getValueString().compareTo( o.getValueString() );

    }

}
