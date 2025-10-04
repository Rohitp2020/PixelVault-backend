package org.studyeasy.SpringRestDemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.studyeasy.SpringRestDemo.model.Account;
import org.studyeasy.SpringRestDemo.service.AccountService;
import org.studyeasy.util.constants.Authority;

@Component
public class SeedData implements CommandLineRunner{

    @Autowired
    private AccountService accountService;

    @Override
    public void run(String... args) throws Exception {
        Account account1 = new Account();
        Account account2 = new Account();

        account1.setEmail("rohit@gmail.com");
        account1.setPassword("rohit");
        account1.setAuthorities(Authority.USER.toString());
        accountService.save(account1);

        account2.setEmail("kavya@gmail.com");
        account2.setPassword("kavya");
        account2.setAuthorities(Authority.ADMIN.toString()+" "+Authority.USER.toString());
        accountService.save(account2);
        
    }
    
}
