package usersmicroservice.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import usersmicroservice.entities.User;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter{
	private AuthenticationManager authenticationManager;
	public JWTAuthenticationFilter(AuthenticationManager authenticationManager)
	{
		super();
		this.authenticationManager = authenticationManager;
		}
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,HttpServletResponse response)
			throws AuthenticationException {
		User user =null;
		try {
			user = new ObjectMapper().readValue(request.getInputStream(),User.class);
			} catch (JsonParseException e) {
				e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
						}
		return authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(),user.getPassword()));
		}
	
	/*@Override
	protected void successfulAuthentication(HttpServletRequest request,HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException{
		 String userId = getCurrentUserId(authResult);

		org.springframework.security.core.userdetails.User springUser =
				(org.springframework.security.core.userdetails.User)authResult.getPrincipal();
		List<String> roles = new ArrayList<>();
		springUser.getAuthorities().forEach(au-> {
			roles.add(au.getAuthority());
			});
		String jwt = JWT.create().
				withSubject(springUser.getUsername())
				.withClaim("user_id",userId )
				.withArrayClaim("roles", roles.toArray(new String[roles.size()])).
				withExpiresAt(new Date(System.currentTimeMillis()+10*24*60*60*1000)).
				sign(Algorithm.HMAC256("mariem@gmail.com"));
		response.addHeader("Authorization", jwt);
		response.addHeader("user_id", userId);


		}*/
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
	                                        Authentication authResult) throws IOException, ServletException {
	    // Assuming that you have a method getCurrentUserId that extracts the user ID
	    String userId = getCurrentUserId(authResult);

	    UserDetails userDetails = (UserDetails) authResult.getPrincipal();
	    List<String> roles = new ArrayList<>();
	    userDetails.getAuthorities().forEach(authority -> roles.add(authority.getAuthority()));

	    // Create JWT token
	    String jwt = JWT.create()
	            .withSubject(userDetails.getUsername())
	            .withClaim("user_id", userId)
	            .withArrayClaim("roles", roles.toArray(new String[0]))
	            .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000)) // 10 days
	            .sign(Algorithm.HMAC256("YourSecretHere"));

	    // Prepare response object with the token and the user ID
	    Map<String, Object> tokenInfo = new HashMap<>();
	    tokenInfo.put("Authorization", "Bearer " + jwt); // It's common to prepend "Bearer" to the token
	    tokenInfo.put("user_id", userId);

	    // Convert map to JSON string
	    ObjectMapper objectMapper = new ObjectMapper();
	    String responseBody = objectMapper.writeValueAsString(tokenInfo);

	    // Modify response to return JSON body
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    response.getWriter().write(responseBody);
	    response.getWriter().flush();
	}
	


	private String getCurrentUserId(Authentication auth) {
	    UserDetails userDetails = (UserDetails) auth.getPrincipal();
	    if (userDetails instanceof User) {
	    	User myUserDetails = (User) userDetails;
	        return String.valueOf(myUserDetails.getUser_id());
	    } else {
	        // Handle different types of UserDetails if necessary
	    }
	    return null; // Return null or throw an exception if the user ID cannot be determined
	}
	
}
