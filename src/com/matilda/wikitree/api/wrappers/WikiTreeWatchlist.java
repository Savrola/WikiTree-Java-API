/*
 * Copyright © 2017 Daniel Boulet
 */

package com.matilda.wikitree.api.wrappers;

import com.matilda.wikitree.api.exceptions.ReallyBadNewsError;
import com.matilda.wikitree.api.exceptions.WikiTreeRequestFailedException;
import com.matilda.wikitree.api.util.WikiTreeApiUtilities;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

/**
 Wrap a user's watchlist.
 */

@SuppressWarnings("WeakerAccess")
public class WikiTreeWatchlist extends WikiTreeWrapper {

    private final Boolean _getPersonParam;

    private final Boolean _getSpaceParam;

    private final Boolean _onlyLivingParam;

    private final Boolean _excludeLivingParam;

    private final String _fieldsParam;

    private final Integer _limitParam;

    private final Integer _offsetParam;

    private final String _orderParam;

    private final int _watchlistCount;

    private final Vector<WikiTreePersonProfile> _thisBatch = new Vector<>();

    public WikiTreeWatchlist(
            final Boolean getPersonParam,
            final Boolean getSpaceParam,
            final Boolean onlyLivingParam,
            final Boolean excludeLivingParam,
            final String fieldsParam,
            final Integer limitParam,
            final Integer offsetParam,
            final String orderParam,
            final @NotNull JSONObject resultObject
    ) throws WikiTreeRequestFailedException {

        super( resultObject );

        _getPersonParam = getPersonParam;
        _getSpaceParam = getSpaceParam;
        _onlyLivingParam = onlyLivingParam;
        _excludeLivingParam = excludeLivingParam;
        _fieldsParam = fieldsParam;
        _limitParam = limitParam;
        _offsetParam = offsetParam;
        _orderParam = orderParam;


        JSONArray watchlist = ( (JSONArray)WikiTreeApiUtilities.getMandatoryJsonValue( JSONArray.class, resultObject, "watchlist" ) );
        if ( watchlist.isEmpty() ) {

            _watchlistCount = 0;

        } else {

            _watchlistCount = (
                    (Number)WikiTreeApiUtilities.getMandatoryJsonValue(
                            Number.class,
                            resultObject,
                            "watchlistCount"
                    )
            ).intValue();

        }

        int ix = 0;
        for ( Object profileObject : watchlist ) {

            if ( profileObject instanceof JSONObject ) {

                JSONObject profileJsonObject = (JSONObject)profileObject;
                WikiTreePersonProfile profile =
                        new WikiTreePersonProfile( WikiTreeRequestType.UNKNOWN, profileJsonObject, WikiTreePersonProfile.ProfileType.PROFILE );

                _thisBatch.add( profile );

            } else {

                throw new ReallyBadNewsError(
                        "WikiTreeWatchlist:  profile at offset " + ( offsetParam == null ? 0 : offsetParam.intValue() ) +
                        "+" + ix + " is not a JSONObject (it is a " + profileObject.getClass().getCanonicalName() + ")"
                );

            }

            ix += 1;

        }

    }

    public Boolean getGetSpaceParam() {

        return _getSpaceParam;

    }

    public Boolean getOnlyLivingParam() {

        return _onlyLivingParam;

    }

    public Boolean getExcludeLivingParam() {

        return _excludeLivingParam;

    }

    public String getFieldsParam() {

        return _fieldsParam;

    }

    public Integer getLimitParam() {

        return _limitParam;

    }

    public Integer getOffsetParam() {

        return _offsetParam;

    }

    public String getOrderParam() {

        return _orderParam;

    }

    public Boolean getGetPersonParam() {

        return _getPersonParam;

    }

    /**
     Get the size of the authenticated owner of the session used to fetch this watchlist.

     @return the size of the authenticated owner of the session used to fetch this watchlist. {@code 0} if the session is not authenticated.
     */

    public int getWatchlistCount() {

        return _watchlistCount;

    }

    public int getThisBatchSize() {

        return _thisBatch.size();

    }

    public Collection<WikiTreePersonProfile> getWatchlist() {

        return Collections.unmodifiableCollection( _thisBatch );

    }

    public String toString() {

        return "WikiTreeWatchlist( " +
               "getPerson=" + getGetPersonParam() + ", " +
               "getSpace=" + getGetSpaceParam() + ", " +
               "onlyLiving=" + getOnlyLivingParam() + ", " +
               "excludeLiving=" + getExcludeLivingParam() + ", " +
               "fields=" + getFieldsParam() + ", " +
               "limit=" + getLimitParam() + ", " +
               "offset=" + getOffsetParam() + ", " +
               "order=" + getOrderParam() + ", " +
               "watchlistCount=" + getWatchlistCount() + ", " +
               "thisBlockSize=" + getThisBatchSize() +
               " )";

    }

}
