package io.shiftleft.controller;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;
import org.springframework.expression.spel.support.StandardEvaluationContext;
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
    java.lang.Object message = new Object();
    try {
      ExpressionParser parser = new SpelExpressionParser();
      parser.setMethodResolvers(new ReflectiveMethodResolver(){
        @Override
        public Method resolveMethod(EvaluationContext context, Object targetObject, String name, Class<?>[] argumentTypes) throws SpelEvaluationException {
          if (targetObject.getClass().equals(String.class) && name.equals("substring")) {
            return super.resolveMethod(context, targetObject, name, argumentTypes);
          }
          throw new SpelEvaluationException(SpelMessage.NOT_ALLOWED, name);
        }
      });
      Expression exp = parser.parseExpression(foo);
      EvaluationContext ctx = new StandardEvaluationContext();
      ctx.setVariable("foo", foo);
      message = (Object) exp.getValue(ctx);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
    return message.toString();
  }
}

  }

  boolean isValid(String foo) {
    // Do validation
    return true;
  }
}

