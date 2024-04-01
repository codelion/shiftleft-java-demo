import org.springframework.web.bind.annotation.RequestMethod;

...

@RequestMapping(value = ERROR_PATH, produces = "text/html", method = RequestMethod.GET)
public ModelAndView errorHtml(HttpServletRequest request) {
    return new ModelAndView("/errors/error", getErrorAttributes(request, false));
}
