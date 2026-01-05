package com.rafhi.controller;

// import com.rafhi.dto.LoginRequest;
import com.rafhi.dto.RegisterRequest;
import com.rafhi.entity.AppUser;
// import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
// import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// import java.time.Duration;
import java.util.Set;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    SecurityIdentity securityIdentity;

    @POST
    @Path("/register")
    @Transactional
    @PermitAll // Menandakan endpoint ini bisa diakses semua orang
    public Response register(RegisterRequest data) {
        if (AppUser.find("username", data.username).count() > 0) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Username sudah terdaftar.\"}")
                    .build();
        }
        
        // Asumsi pengguna baru selalu memiliki peran "USER".
        // Gunakan metode `add` yang sudah ada di AppUser.
        AppUser.add(data.username, data.password, "USER");
        
        return Response.status(Response.Status.CREATED)
                       .entity("{\"message\":\"Registrasi berhasil.\"}")
                       .build();
    }

    // @POST
    // @Path("/login")
    // @PermitAll
    // public Response login(LoginRequest credentials) {

    //     AppUser user = AppUser.find("username", credentials.username).firstResult();
        
    //     // Validate credentials using bcrypt to prevent token issuance without a correct password
    //     boolean valid = user != null && BcryptUtil.matches(credentials.password, user.password);
    //     if (valid) {
    //         String token = Jwt.issuer("https://10.1.51.104:8080/issuer")
    //                             .upn(user.username)
    //                             .groups(Set.of(user.role))
    //                             .expiresIn(Duration.ofHours(24))
    //                             .sign();
    //         return Response.ok("{\"token\":\"" + token + "\"}").build();
    //     }

    //     System.out.println("Login gagal untuk username: '" + credentials.username + "'");
    //     return Response.status(Response.Status.UNAUTHORIZED)
    //                    .entity("{\"error\":\"Username atau password salah.\"}")
    //                    .build();
    // }

    @GET
    @Path("/me")
    @Authenticated // Hanya untuk pengguna yang sudah login
    public Response me() {
        String username = securityIdentity.getPrincipal().getName();

        // Ambil role dari SecurityIdentity
        Set<String> roles = securityIdentity.getRoles();
        
        // Ambil role pertama, asumsi hanya ada satu role per user
        String role = roles.isEmpty() ? "USER" : roles.iterator().next(); 
        
        // Buat objek JSON yang lebih informatif untuk frontend
        String jsonResponse = String.format("{\"username\":\"%s\", \"role\":\"%s\"}", username, role);
        
        return Response.ok(jsonResponse).build();
    }
}
