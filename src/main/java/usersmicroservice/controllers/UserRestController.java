package usersmicroservice.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import usersmicroservice.Repositories.UserRepository;
import usersmicroservice.Services.UserService;
import usersmicroservice.entities.User;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class UserRestController {
	
	@Autowired
	UserRepository userRep;
	

	@Autowired
	private UserService userService ;
	
	 @Autowired
	    private JavaMailSender javaMailSender;
	 
	 @Autowired
	 private BCryptPasswordEncoder bCryptPasswordEncoder;

	
	@RequestMapping(path = "all",method = RequestMethod.GET)
	public List<User> getAllUsers() {
		return userRep.findAll();
	 }
	
	
	//@GetMapping(path="/{user_id}")
	@RequestMapping(path = "find/{user_id}",method = RequestMethod.GET)
	public ResponseEntity<User> findUserById(@PathVariable Long user_id)
	{
		User user= userService.findUserById(user_id);
		
		if(user==null)
		{
			return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
		}else {
			return new ResponseEntity<User>(user,HttpStatus.OK);
		}
	}
	
	private void sendRegistrationEmail(User user, String plainTextPassword) {
	    MimeMessage message = javaMailSender.createMimeMessage();
	    MimeMessageHelper helper = new MimeMessageHelper(message);

	    try {
	        helper.setTo(user.getEmail());
	        helper.setSubject("Welcome to our platform!");
	        String emailContent = "Thank you for registering.\n\n";
	        emailContent += "Here are your login credentials:\n";
	        emailContent += "Email: " + user.getEmail() + "\n";
	        emailContent += "Password: " +  plainTextPassword + "\n";

	        helper.setText(emailContent);

	        javaMailSender.send(message);
	    } catch (MessagingException e) {
	        e.printStackTrace(); 
	    }
	}
	
	@PostMapping("/create")
	 public ResponseEntity<String> createUser(@RequestBody User user) {
        try {
            // Check if the user already exists
            if (userService.getUserByMailAndMatricule(user.getEmail(), user.getMatricule()) != null) {
                return ResponseEntity.badRequest().body("User with this email already exists.");
            }

            String plainTextPassword = user.getPassword();

            // Check if the password is empty
            if (plainTextPassword == null || plainTextPassword.isEmpty()) {
                return ResponseEntity.badRequest().body("Password cannot be empty.");
            }

            // Encrypt the password before saving it to the database
            user.setPassword(bCryptPasswordEncoder.encode(plainTextPassword));

            // Create the user in the database
            User createdUser = userService.CreateUser(user);

            if (createdUser != null) {
                // Send the registration email with the uncrypted email address
                sendRegistrationEmail(createdUser, plainTextPassword);
                return ResponseEntity.ok("User created successfully, and registration email sent.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create user.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during user creation.");
        }
    }

	   

	

	
	//@PostMapping
	/*@RequestMapping(path = "/create",method = RequestMethod.POST)
	public User CreateUser(@RequestBody User user)
	{
		User createdUser = userService.CreateUser(user);
        
     
        if (createdUser != null) {
            sendRegistrationEmail(createdUser); 
        }
        
        return createdUser;
	
	}	*/
	
	//@PutMapping
	@RequestMapping(path = "/update",method = RequestMethod.PUT)
	public User UpdateUser(@RequestBody User user)
	{
		return userService.updateUser(user);
	}	
	
	//@DeleteMapping(path="/supprimer/{user_id}")
	@RequestMapping(path = "/delete/{user_id}", method = RequestMethod.DELETE)
	public void deleteUser(@PathVariable Long user_id) {
	    userService.deleteUserRole(user_id);
	}
	
	
	// @PutMapping(path="/update/{user_id}")
	@RequestMapping(path = "/update/{user_id}",method = RequestMethod.PUT)
	    public ResponseEntity<User> updateUserById(@PathVariable Long user_id, @RequestBody User updatedUser) {
	        User updatedUtilisateur = userService.updateUserById(user_id, updatedUser);
	        return ResponseEntity.ok(updatedUtilisateur);
	    }
	 
	 
	 //@GetMapping("/recuperer/{email}/{matricule}")
	  @RequestMapping(path = "/recuperer/{email}/{matricule}",method = RequestMethod.GET)
		public ResponseEntity<?> getUserByMailAndMatricule(
		        @PathVariable(value = "email") String email ,
		        @PathVariable (value = "matricule")String matricule) {
		    try {
		        System.out.println("Received request with mail: " + email + " and matricule: " + matricule);
		        User user = userService.getUserByMailAndMatricule(email, matricule);

		        if (user != null) {
		            return new ResponseEntity<>(user, HttpStatus.OK);
		        } else {
		            System.out.println("User not found");
		            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
		        }
		    } catch (Exception e) {
		        System.out.println("An error occurred: " + e.getMessage());
		        return new ResponseEntity<>("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		    }
		}
	 
	 
	 //@GetMapping("/byMatricule/{matricule}")
	  @RequestMapping(path = "/findbyMatricule/{matricule}",method = RequestMethod.GET)

		public ResponseEntity<?> findUserByMatricule( @PathVariable (value = "matricule")String matricule) {
		    try {
		        
		        User user = userService.findUserByMatricule(matricule);

		        if (user != null) {
		            return new ResponseEntity<>(user, HttpStatus.OK);
		        } else {
		            System.out.println("User not found");
		            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
		        }
		    } catch (Exception e) {
		        System.out.println("An error occurred: " + e.getMessage());
		        return new ResponseEntity<>("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		    }
		}
	


}