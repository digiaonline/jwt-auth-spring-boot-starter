# Instruction to use the JWT authentication in a spring boot project.

## Description

This starter allows to generate JWT token which would be used to authenticate any request containing the following header:
```
X-Authorization: Bearer ${jwt}
```

The token can be created from the user using JwtAuthorizationProvider.produceJwt(User).

The user id should be a string, and defined as "principal" on the user model.
A class implementing RoleProviderI should define the roles to attach to the user.
*Note that it is not required to have a user object in the db corresponding to the principal.*
This case should be handled by the role provider where `user=null`. The application should
define the Role that applies to such a user if any.


Currently, this starter requires some boilerplate code to be used (defined
below). Hopefully this will be cleaned up in a future version.

## Configuration

* starcut.auth.jwt.secret: the secret used to sign tokens
* starcut.auth.jwt.lifetime-in-hours: the validity of the token in hours

## Instructions

Include the migration file '001-add-revoked-token' into the project.

* The user entity must implement UserI, define the principal used for jwt. This can be existing column as below.

```

@Column(name = "phoneNumber", insertable = false, updatable = false)
private String principal;

```


* The userRepository must implement UserRepositoryI<User>

* Define a class implementing RoleProviderI<User>
This require to define one function: getGrantedAuthoritiesFor(User) which returns a collection of granted authorities for the user. *User* can be *null* so the class must handle that accordingly. This happens when the jwt refers to a non existing user.

* Extend the JwtAuthenticationFilter

```

public class MyJwtAuthenticationFilter extends JwtAuthenticationFilter<UserRepository, User> {

    public MyJwtAuthenticationFilter(JwtAuthorizationProvider<UserRepository, User> jwtAuthorizationProvider,
            UserRepository userRepository) throws InstantiationException, IllegalAccessException {
        super(jwtAuthorizationProvider, userRepository, new MyRoleProvider());
    }

}
```


Add the filter on HttpSecurity http

```
@Autowired
JwtAuthorizationProvider jwtProvider;
    
@Autowired
UserRepository userRepository;

protected void configure(HttpSecurity http) throws Exception {
    http.addFilterBefore(new MyJwtAuthenticationFilter(jwtProvider, userRepository), BasicAuthenticationFilter.class);
}
```
