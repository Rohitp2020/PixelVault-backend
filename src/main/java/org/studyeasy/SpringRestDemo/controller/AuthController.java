package org.studyeasy.SpringRestDemo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.studyeasy.SpringRestDemo.model.Account;
import org.studyeasy.SpringRestDemo.payloads.auth.AccountDTO;
import org.studyeasy.SpringRestDemo.payloads.auth.AccountViewDTO;
import org.studyeasy.SpringRestDemo.payloads.auth.AuthoritiesDTO;
import org.studyeasy.SpringRestDemo.payloads.auth.PasswordDTO;
import org.studyeasy.SpringRestDemo.payloads.auth.ProfileDTO;
import org.studyeasy.SpringRestDemo.payloads.auth.TokenDTO;
import org.studyeasy.SpringRestDemo.payloads.auth.UserLoginDTO;
import org.studyeasy.SpringRestDemo.repository.AccountRepository;
import org.studyeasy.SpringRestDemo.service.AccountService;
import org.studyeasy.SpringRestDemo.service.TokenService;
import org.studyeasy.util.constants.AccountError;
import org.studyeasy.util.constants.AccountSuccess;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@RestController
@CrossOrigin(origins = "http://localhost:3000/", maxAge = 3600)
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth Controller", description = "Controller for Account Management.")
@Slf4j // this is for logger
public class AuthController {

    @Autowired
    private  AuthenticationManager authenticationManager;

    @Autowired
    private  TokenService tokenService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;
    

    // Just look at the front-end of our swagger UI, you will find the /token POST controller there.
    // ResponseEntity  is basically used to handle the exception like when you give wrong credentials.
    // This POST mapping is used to authenticate user and password first and then generating token.
    @PostMapping("/token")
    @ApiResponse(responseCode = "401", description = "Please enter a valid email and password length should be between 6 and 20 OR Account already exists")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TokenDTO> token(@Valid @RequestBody UserLoginDTO userLogin) throws AuthenticationException {
        try {
            System.out.println(userLogin.getEmail());
            System.out.println(userLogin.getPassword());
            Authentication authentication = authenticationManager
            .authenticate(new UsernamePasswordAuthenticationToken(userLogin.getEmail(),userLogin.getPassword()));
            return ResponseEntity.ok(new TokenDTO(tokenService.generateToken(authentication)));
        } catch (Exception e) {
            log.debug(AccountError.TOKEN_GENERATION_ERROR.toString()+" : "+e.getMessage());
            return new ResponseEntity<>(new TokenDTO(null),HttpStatus.BAD_REQUEST);
        }
        
    }

    // Creating an API to create neew user and add in DB
    @PostMapping(value="/users/add",  produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please enter a valid email and password length should be between 6 and 20 OR Account already exists")
    @ApiResponse(responseCode = "200", description = "User Account added successfully.")
    @Operation(summary = "Add a new user.")
    public ResponseEntity<String> addUser(@Valid @RequestBody AccountDTO accountDTO){
        try {
            Optional<Account> optionalAccount = accountRepository.findByEmail(accountDTO.getEmail());
            System.out.println(optionalAccount);
            if(!optionalAccount.isPresent()){
                Account account = new Account();
                account.setEmail(accountDTO.getEmail());
                account.setPassword(accountDTO.getPassword());
                accountService.save(account);
                return ResponseEntity.ok(AccountSuccess.ACCOUNT_ADDED.toString());
            }else{
                System.out.println("Account already exists: "+optionalAccount);
                //return ResponseEntity.badRequest().body(AccountError.ACCOUNT_ALREADY_EXISTS.toString());
                // Above code is commented as on React project it was giving error so, made the response as OK to work fine.
                return ResponseEntity.ok(AccountError.ACCOUNT_ALREADY_EXISTS.toString());
            }
        } catch (Exception e) {
            log.debug(AccountError.ADD_ACCOUNT_ERROR.toString()+" : "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // This GET API will fetch all the list of users
    @GetMapping(value="/users", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "List of users fetched.")
    @ApiResponse(responseCode = "401", description = "Token Missing or Use Admin user to see users list.")
    @ApiResponse(responseCode = "403", description = "Token Error.")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "List user API.")
    public List<AccountViewDTO> Users(){
        List<AccountViewDTO> accountViewDTOs = new ArrayList<>();
        for(Account account: accountService.findAll()){
            accountViewDTOs.add(new AccountViewDTO(account.getId(),account.getEmail(), account.getAuthorities()));
        }
        return accountViewDTOs;
    }

    @PutMapping(value="/users/{user_id}/update_authority", produces = "application/json", consumes = "application/json")
    @ApiResponse(responseCode = "200", description = "Authorities updated successfully.")
    @ApiResponse(responseCode = "401", description = "Token Missing.")
    @ApiResponse(responseCode = "403", description = "Token Error.")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "Update Authorities.")
    public ResponseEntity<AccountViewDTO> update_authority(@Valid @RequestBody AuthoritiesDTO authoritiesDTO, @PathVariable long user_id){
        Optional<Account> optionalAccount = accountService.findByID(user_id);
        if(optionalAccount.isPresent()){
            Account account = optionalAccount.get();
            account.setAuthorities(authoritiesDTO.getAuthorities());
            accountService.save(account);
            return ResponseEntity.ok(new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorities()));
        } 
        return new ResponseEntity<AccountViewDTO>(new AccountViewDTO(), HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value="/profile", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "Profile fetched successfully.")
    @ApiResponse(responseCode = "401", description = "Token Missing.")
    @ApiResponse(responseCode = "403", description = "Token Error.")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "View User Profile.")
    public ProfileDTO profile(Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        if(optionalAccount.isPresent()){
            Account account = optionalAccount.get();
            return new ProfileDTO(account.getId(), account.getEmail(), account.getAuthorities());
        } 
        return null;
    }

    @PutMapping(value="/profile/update_password", produces = "application/json", consumes = "application/json")
    @ApiResponse(responseCode = "200", description = "Password updated successfully.")
    @ApiResponse(responseCode = "401", description = "Token Missing.")
    @ApiResponse(responseCode = "403", description = "Token Error.")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "User Password Update.")
    public AccountViewDTO update_password(@Valid @RequestBody PasswordDTO passwordDTO,Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        if(optionalAccount.isPresent()){
            Account account = optionalAccount.get();
            account.setPassword(passwordDTO.getPassword());
            accountService.save(account);
            return new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorities());
        } 
        return null;
    }

    @DeleteMapping(value="/profile/delete")
    @ApiResponse(responseCode = "200", description = "User deleted successfully.")
    @ApiResponse(responseCode = "401", description = "Token Missing.")
    @ApiResponse(responseCode = "403", description = "Token Error.")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "Delete profile.")
    public ResponseEntity<String> delete_profile(Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        if(optionalAccount.isPresent()){
            accountService.deleteByID(optionalAccount.get().getId());
            return ResponseEntity.ok("User Deleted.");
        } 
        return new ResponseEntity<String>("Bad Request",HttpStatus.BAD_REQUEST);
    }

}
