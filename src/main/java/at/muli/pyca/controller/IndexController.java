package at.muli.pyca.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class IndexController {

    @RequestMapping(path = { "/video/*" }, method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String subpageIndex() {
        return "forward:/";
    }
}
