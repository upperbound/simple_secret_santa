package com.github.upperbound.secret_santa.web.mvc;

import com.github.upperbound.secret_santa.web.dto.AvailableMessageBundle;
import com.github.upperbound.secret_santa.util.StaticContext;
import com.github.upperbound.secret_santa.web.rest.CommonRestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class CommonController {
    protected final StaticContext staticContext;

    @ModelAttribute("availableMessageBundles")
    public List<AvailableMessageBundle> availableMessageBundles() {
        return staticContext.getAvailableMessageBundles();
    }
}
