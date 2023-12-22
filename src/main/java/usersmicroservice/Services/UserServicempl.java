package usersmicroservice.Services;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import usersmicroservice.Repositories.RoleRepository;
import usersmicroservice.Repositories.UserRepository;
import usersmicroservice.entities.Role;
import usersmicroservice.entities.User;

@Transactional
@Service
public class UserServicempl implements UserService{
@Autowired
UserRepository userRep;

@Autowired
RoleRepository roleRep;

@Autowired
BCryptPasswordEncoder bCryptPasswordEncoder;



@Override
public User CreateUser(User user) {
    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
    // Assigner le rôle par défaut "USER"
    Role userRole = roleRep.findByRole("USER"); // Assurez-vous d'avoir une méthode findByRoleName dans votre repository
    if (userRole != null) {
        user.setRoles(Collections.singletonList(userRole));
    }

    return userRep.save(user);
}

@Override
public User addRoleToUser(String username, String rolename) {
	User usr = userRep.findByUsername(username);
	Role r = roleRep.findByRole(rolename);
	usr.getRoles().add(r);
	return usr;
}

@Override
public Role addRole(Role role) {
	return roleRep.save(role);
}
@Override
public User findUserByUsername(String username) {
	return userRep.findByUsername(username);
	}
@Override
public User getUserByMailAndMatricule(String email, String matricule) {
    return userRep.findByEmailAndMatricule(email, matricule);

}
@Override
public List<User> getAllUser() {
	return userRep.findAll();

}
@Override
public User findUserById(Long user_id) {
	Optional<User> utOptional = userRep.findById(user_id); 
	
	if(utOptional.isEmpty() ) {
		return null;
	}else {
		return utOptional.get();
	}
}
@Override
public User updateUser(User user) {
	Optional<User> utOptional = userRep.findById(user.getUser_id()); 
	if(utOptional.isEmpty() ) {
		return null;
	}else {
		return userRep.save(user);
	}
}


    @Override
    public void deleteUserRole(Long user_id) {
        Optional<User> userOptional = userRep.findById(user_id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Supprimer l'utilisateur et les relations dans la table de jointure
            user.getRoles().clear(); // Supprime toutes les relations ManyToMany
            userRep.deleteById(user_id);
        }
        
        System.out.println("user deleted");
    

}

@Override
public User updateUserById(Long user_id, User updatedUser) {
	Optional<User> userOptional = userRep.findById(user_id);
    if (userOptional.isPresent()) {
        User existingUser = userOptional.get();
        

        if (updatedUser.getUsername() != null) {
            existingUser.setUsername(updatedUser.getUsername());
        }
        
        if (updatedUser.getLastName() != null) {
            existingUser.setLastName(updatedUser.getLastName());
        }
        
        if (updatedUser.getPassword() != null) {
            existingUser.setPassword(updatedUser.getPassword());
        }
        
        if (updatedUser.getTel() != null) {
            existingUser.setTel(updatedUser.getTel());
        }
        
        if (updatedUser.getSexe() != null) {
            existingUser.setSexe(updatedUser.getSexe());
        }
        
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        
        if (updatedUser.getDateNaissance() != null) {
            existingUser.setDateNaissance(updatedUser.getDateNaissance());
        }
        
        if (updatedUser.getMatricule() == null) {
            updatedUser.setMatricule(existingUser.getMatricule());
        }
        // Save the updated user
        return userRep.save(existingUser);
    } else {
        throw new NoSuchElementException("Utilisateur non trouvé avec l'ID : " + user_id);
    }
    
}
@Override
public User getUserByMailAndPassword(String email, String password) {
    return userRep.findByEmailAndPassword(email, password);

}
@Override
public User findUserByMatricule(String matricule) {
	return userRep.findByMatricule(matricule);

}



}
