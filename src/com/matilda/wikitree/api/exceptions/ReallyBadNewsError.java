/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.exceptions;

import org.jetbrains.annotations.Nullable;

/**
 Thrown if something truly unexpected happens.
 <p/>If you get one of these then you are almost certainly the 'proud' discoverer of a software defect in this facility.
 Please notify danny@matilda.com (please provide, at a minimum, the entire error message including the stack traceback that was
 thrown towards you when the "really bad news" happened).
 */

public class ReallyBadNewsError
        extends RuntimeException {

    /**
     The current oops catcher.
     If specified then the oops catcher is called from within the constructor which is creating this instance
     (needless to say, this is BEFORE this instance is thrown or otherwise even seen by whichever software
     invoked the constructor in question).
     Setting up an oops catcher is a good way to be sure to always receive notification within your software when
     "really bad news" happens.
     */

    private static OopsCatcher s_oopsCatcher = null;

    /**
     Describe how an oops catcher is informed that a {@link ReallyBadNewsError} instance has been created and is, presumably, about to be thrown.
     */

    public interface OopsCatcher {

        /**
         Notify someone about a {@link ReallyBadNewsError} instance which is, presumably, about to be thrown.

         @param e the {@link ReallyBadNewsError} instance which has just been created and is, presumably, about to be thrown.
         */

        void oops( ReallyBadNewsError e );

    }

    @SuppressWarnings("unused")
    public ReallyBadNewsError() {

        super();

        notifyOopsCatcher();

    }

    public ReallyBadNewsError( final String msg ) {

        super( msg );

        notifyOopsCatcher();

    }

    @SuppressWarnings("unused")
    public ReallyBadNewsError( final String msg, final Throwable e ) {

        super( msg, e );

        notifyOopsCatcher();

    }

    /**
     Call the oops catcher if one is defined.
     */

    private void notifyOopsCatcher() {

        if ( ReallyBadNewsError.s_oopsCatcher != null ) {

            ReallyBadNewsError.s_oopsCatcher.oops( this );

        }

    }

    /**
     Specify the oops catcher which is to be called whenever a {@link ReallyBadNewsError} instance is created and,
     presumably, is about to be thrown.

     @param oopsCatcher the oops catcher which is to catch the oops.
     @return the previously configured oops catcher
     (a weak but hopefully adequate way to handle multiple oops catchers if everyone cooperates).
     */

    @SuppressWarnings("UnusedDeclaration")
    public static OopsCatcher setOopsCatcher( @Nullable final OopsCatcher oopsCatcher ) {

        OopsCatcher oldOopsCatcher = ReallyBadNewsError.s_oopsCatcher;

        ReallyBadNewsError.s_oopsCatcher = oopsCatcher;

        return oldOopsCatcher;

    }

}