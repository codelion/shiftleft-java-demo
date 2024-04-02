import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Rest of the imports

@Controller
public class AdminController {
  private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
  private String fail = "redirect:/";

  private boolean isAdmin(String auth)
  {
    try {
      ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(auth));
      ObjectInputStream objectInputStream = new ObjectInputStream(bis);
      Object authToken = objectInputStream.readObject();
      return ((AuthToken) authToken).isAdmin();
    } catch (Exception ex) {
      LOGGER.error("Cookie cannot be deserialized: ", ex);
      return false;
    }
  }

  // Rest of the code

  public String doPostPrintSecrets(HttpServletResponse response, HttpServletRequest request) {
    return fail;
  }

  // Rest of the code

  public String doGetPrintSecrets(@CookieValue(value = "auth", defaultValue = "notset") String auth, HttpServletResponse response, HttpServletRequest request) throws Exception {
   // Rest of the code
    try {
      byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
      response.getOutputStream().println(new String(bdata, StandardCharsets.UTF_8));
      return null;
    } catch (IOException ex) {
      LOGGER.error("Error while reading file: ", ex);
      return fail;
    }
  }

  // Rest of the code

  public String doPostLogin(@CookieValue(value = "auth", defaultValue = "notset") String auth, @RequestBody String password, HttpServletResponse response, HttpServletRequest request) throws Exception {
    String succ = "redirect:/admin/printSecrets";
    try {
      // Rest of the code
    } catch (Exception ex) {
      LOGGER.error("Error in doPostLogin: ", ex);
      return fail; 
    }
  }

  // Rest of the code
}
  }
}
  /**
   * Same as POST but just a redirect
   * @param response
   * @param request
   * @return redirect
   */
  @RequestMapping(value = "/admin/login", method = RequestMethod.GET)
  public String doGetLogin(HttpServletResponse response, HttpServletRequest request) {
    return "redirect:/";
  }
}
  }
}
