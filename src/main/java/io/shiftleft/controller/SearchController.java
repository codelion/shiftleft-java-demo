package io.shiftleft.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
* Search login
*/
@Controller
public class SearchController {
  
  @RequestMapping(value = "/search/user", method = RequestMethod.GET)
  public String doGetSearch(@RequestParam String foo, HttpServletResponse response, HttpServletRequest request) {
    String message;
    try {
      if(isAlphanumeric(foo)) {
        message = foo;
      } else {
        throw new IllegalArgumentException("Unsafe input provided by user");
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      return ex.getMessage();
    }
    return message.toString();
  }

  private boolean isAlphanumeric(String foo) {
    return foo != null && foo.matches("[a-zA-Z0-9]+");
  }
}
}
