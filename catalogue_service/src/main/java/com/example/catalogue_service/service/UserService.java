package com.example.catalogue_service.service;


import com.example.artem.task1.marketplace.model.enums.Role;
import com.example.catalogue_service.dto.UserDTO;
import com.example.catalogue_service.entity.MyUser;
import com.example.catalogue_service.mapper.UserMapper;
import com.example.catalogue_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.security.Principal;

@Service

//@Slf4j
public class UserService{// implements com.example.catalogue_service.service.i.UserService {

    private UserRepository repository;

    private PasswordEncoder passwordEncoder;

    private UserMapper userMapper;

    @Autowired
    public void setRepository(UserRepository repository) {
        this.repository = repository;
    }

    //todo @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

   //todo @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

//    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("User not found!"));

    }

    public MyUser findByEmail(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User not found!"));

    }
    public MyUser findByPrincipal(Principal principal) {
        if(principal==null) return new MyUser();
        return findByEmail(principal.getName());
    }

 //   @Override
    public boolean createUser(UserDTO userDTO) {
        String email = userDTO.getEmail();
        if(repository.findByEmail(email).isPresent()) return false;
        MyUser user = userMapper.toUser(userDTO);
        user.getRoles().add(Role.CLIENT);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        repository.save(user);
       // log.info("Create new user:{}",email);
        return true;

    }

//    @Override
    public void saveUser(MyUser user) {
        repository.save(user);
    }

  //  @Override
    public boolean updateProfile(UserDTO userDTO, Principal principal) {
         boolean isChanged = false;

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


        repository.save(user);

        return true;

    }

}
