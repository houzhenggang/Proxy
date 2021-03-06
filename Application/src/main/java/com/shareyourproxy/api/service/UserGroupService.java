package com.shareyourproxy.api.service;

import com.shareyourproxy.api.domain.model.Group;

import java.util.HashMap;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

/**
 * Group services for {@link Group}s.
 */
public interface UserGroupService {

    @PUT("/users/{userId}/groups.json")
    Observable<Group> updateUserGroups(
        @Path("userId") String userId, @Body HashMap<String, Group> group);

    @PUT("/users/{userId}/groups/{groupId}.json")
    Observable<Group> addUserGroup(
        @Path("userId") String userId, @Path("groupId") String groupId, @Body Group group);

    @DELETE("/users/{userId}/groups/{groupId}.json")
    Observable<Group> deleteUserGroup(
        @Path("userId") String userId, @Path("groupId") String groupId);
}
