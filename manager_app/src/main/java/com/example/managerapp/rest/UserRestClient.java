package com.example.managerapp.rest;


import com.example.managerapp.record.MyUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import java.security.Principal;

//@Slf4j
@RequiredArgsConstructor
public class UserRestClient {

    private PasswordEncoder passwordEncoder;

    private RestClient restClient;





    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       // return repository.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("User not found!"));
        return null;

    }

    public MyUser findByEmail(String email) throws UsernameNotFoundException {
        return null;// repository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User not found!"));

    }
   /* public MyUser findByPrincipal(Principal principal) {
        if(principal==null) return new MyUser();
        return findByEmail(principal.getName());
    }*/


    public boolean createUser(MyUser user) {
       /* String email = userDTO.getEmail();
        if(repository.findByEmail(email).isPresent()) return false;
        MyUser user = userMapper.toUser(userDTO);
        user.getRoles().add(Role.CLIENT);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        repository.save(user);
       // log.info("Create new user:{}",email);
        return true;*/ return true;

    }


    public void saveUser(MyUser user) {
       // repository.save(user);
    }


    public boolean updateProfile(MyUser user, Principal principal) {
       /*  boolean isChanged = false;

        MyUser user = repository.findByEmail(principal.getName()).orElseThrow(()-> new UsernameNotFoundException("User not found!"));

        if(userDTO.getPassword()!=null){
            if(!userDTO.getPassword().equals(userDTO.getMatchingPassword())) throw  new RuntimeException("Passwords do not match");
            String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
            if(!user.getPassword().equals(encodedPassword)){
            user.setPassword(encodedPassword);
            isChanged = true;
            }
        }

        if(userDTO.getEmail()!=null){
            if(!user.getEmail().equals(userDTO.getEmail())){
            user.setEmail(userDTO.getEmail());
            isChanged = true;
            }
        }
        if(userDTO.getName()!=null){
            if(!user.getUsername().equals(userDTO.getName())){
            user.setName(userDTO.getName());
            isChanged = true;
            }
        }
        if(!isChanged) throw  new RuntimeException("User did not change");


        repository.save(user);*/

        return true;

    }

}
