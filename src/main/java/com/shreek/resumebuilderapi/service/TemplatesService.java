package com.shreek.resumebuilderapi.service;

import com.shreek.resumebuilderapi.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shreek.resumebuilderapi.utils.AppConstants.PREMIUM;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplatesService {

    private final AuthService authService;

    public Map<String, Object> getTemplates(Object principal) {
        //step 1: Get the current user
        AuthResponse authResponse = authService.getProfile(principal);

        //step 2: Get the available templates based on subscription
        List<String> availableTemplates;

        Boolean isPremium = PREMIUM.equalsIgnoreCase(authResponse.getSubscriptionPlan());

        if(isPremium){
          availableTemplates = List.of("01","02","03");
        }else{
            availableTemplates = List.of("01");
        }

        //Step 3: add the data into map
        Map<String,Object> restrictions = new HashMap<>();
        restrictions.put("availableTemplates",availableTemplates);
        restrictions.put("allTemplates",List.of("01","02","03"));
        restrictions.put("subscriptionPlan",authResponse.getSubscriptionPlan());
        restrictions.put("isPremium",isPremium);

        //step 4:Return the Response
        return restrictions;
    }
}
