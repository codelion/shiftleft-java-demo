  @RequestMapping(value = ERROR_PATH, produces = "text/html", method = RequestMethod.GET)
  public ModelAndView errorHtml(HttpServletRequest request) {
    return new ModelAndView("/errors/error", getErrorAttributes(request, false));
  }

  @RequestMapping(value = ERROR_PATH, method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
    Map<String, Object> body = getErrorAttributes(request, getTraceParameter(request));
    HttpStatus status = getStatus(request);
    return new ResponseEntity<Map<String, Object>>(body, status);
  }
