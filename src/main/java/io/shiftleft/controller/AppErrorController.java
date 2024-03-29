package io.shiftleft.controller;

import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@SessionAttributes("csrfToken")
public class AppErrorController implements ErrorController{

  private ErrorAttributes errorAttributes;

  private final static String ERROR_PATH = "/error";

  @Autowired
  public AppErrorController(ErrorAttributes errorAttributes) {
    this.errorAttributes = errorAttributes;
  }

  @ModelAttribute("csrfToken")
  public CsrfToken csrfToken(HttpServletRequest request) {
    return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.setDisallowedFields("password");
  }

  @RequestMapping(value = ERROR_PATH, produces = "text/html", method = RequestMethod.GET)
  public ModelAndView errorHtml(HttpServletRequest request, @ModelAttribute("csrfToken") CsrfToken csrfToken) {
    ModelAndView modelAndView = new ModelAndView("/errors/error", getErrorAttributes(request, false));
    modelAndView.addObject("_csrf", csrfToken);
    return modelAndView;
  }

  @RequestMapping(value = ERROR_PATH, method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
    Map<String, Object> body = getErrorAttributes(request, getTraceParameter(request));
    HttpStatus status = getStatus(request);
    return new ResponseEntity<Map<String, Object>>(body, status);
  }

  @Override
  public String getErrorPath() {
    return ERROR_PATH;
  }

  private boolean getTraceParameter(HttpServletRequest request) {
    String parameter = request.getParameter("trace");
    if (parameter == null) {
      return false;
    }
    return !"false".equals(parameter.toLowerCase());
  }

  private Map<String, Object> getErrorAttributes(HttpServletRequest request,
                                                 boolean includeStackTrace) {
    RequestAttributes requestAttributes = new ServletRequestAttributes(request);
    return this.errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
  }

  private HttpStatus getStatus(HttpServletRequest request) {
    Integer statusCode = (Integer) request
        .getAttribute("javax.servlet.error.status_code");
    if (statusCode != null) {
      try {
        return HttpStatus.valueOf(statusCode);
      }
      catch (Exception ex) {
      }
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
