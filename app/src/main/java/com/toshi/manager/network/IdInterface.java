/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.toshi.manager.network;


import com.toshi.model.local.Report;
import com.toshi.model.local.User;
import com.toshi.model.network.SearchResult;
import com.toshi.model.network.ServerTime;
import com.toshi.model.network.UserDetails;
import com.toshi.model.network.UserSearchResults;
import com.toshi.model.network.user.UserV2;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Completable;
import rx.Single;

public interface IdInterface {

    @Headers("Cache-control: no-store")
    @GET("/v1/timestamp")
    Single<ServerTime> getTimestamp();

    @Headers("Cache-control: no-store")
    @POST("/v1/user")
    Single<User> registerUser(@Body UserDetails details,
                              @Query("timestamp") long timestamp);

    // Works for username or toshiId
    @GET("/v1/user/{id}")
    Single<User> getUser(@Path("id") String userId);

    @GET("/v1/search/user")
    Single<SearchResult<User>> getUsers(@Query("toshi_id") List<String> userIds);

    // Works for username or toshiId
    @Headers("Cache-control: no-cache")
    @GET("/v1/user/{id}")
    Single<User> forceGetUser(@Path("id") String userId);

    @Headers("Cache-control: no-store")
    @PUT("/v1/user/{id}")
    Single<User> updateUser(@Path("id") String userId,
                            @Body UserDetails details,
                            @Query("timestamp") long timestamp);

    @GET("/v1/search/user")
    Single<UserSearchResults> searchByUsername(@Query("query") String username);

    @GET("/v1/search/user?apps=false")
    Single<UserSearchResults> searchOnlyUsersByUsername(@Query("query") String username);

    @GET("/v1/search/user")
    Single<UserSearchResults> searchByPaymentAddress(@Query("payment_address") String paymentAddress);

    @Headers("Cache-control: no-store")
    @GET("/v1/login/{id}")
    Completable webLogin(@Path("id") String loginToken,
                         @Query("timestamp") long timestamp);

    @Multipart
    @PUT("v1/user")
    Single<User> uploadFile(@Part MultipartBody.Part file,
                            @Query("timestamp") long timestamp);

    @POST("v1/report")
    Single<Void> reportUser(@Body Report report,
                            @Query("timestamp") long timestamp);

    @GET("v1/search/user")
    Single<UserSearchResults> getUsers(@Query("public") boolean isPublic,
                                       @Query("top") boolean isTopRated,
                                       @Query("recent") boolean isRecent,
                                       @Query("limit") int limit);

    @GET("/v2/search/")
    Single<SearchResult<UserV2>> search(
            @Query("type") String type,
            @Query("query") String query
    );
}

