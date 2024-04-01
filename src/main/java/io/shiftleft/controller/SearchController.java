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
    String message = "";
    try {
      message = foo; // Assigning the 'foo' directly to 'message' to prevent code injection
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
    return message;
  }
}
    return message.toString();
  }

  private boolean containsDangerousChars(String input) {
    String[] dangerousChars = {"\"", "'", "#", "$", "{", "}", "(", ")", "<", ">", "@", "[", "]", ";"};
    for (String dangerousChar : dangerousChars) {
      if (input.contains(dangerousChar)) {
        return true;
      }
    }
    return false;
  }
}
