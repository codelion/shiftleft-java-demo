package io.shiftleft.controller;

import io.shiftleft.model.Account;
import io.shiftleft.model.Address;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import io.shiftleft.data.DataLoader;
import io.shiftleft.exception.CustomerNotFoundException;
import io.shiftleft.exception.InvalidCustomerRequestException;
import io.shiftleft.model.Customer;
import io.shiftleft.repository.CustomerRepository;
import io.shiftleft.util.PathUtil;

import org.springframework.web.util.HtmlUtils;

/**
 * Customer Controller exposes a series of RESTful endpoints
 */

@Configuration
@EnableEncryptableProperties
@PropertySource({ "classpath:config/application-sfdc.properties" })
@RestController
public class CustomerController {

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	Environment env;
	
	private static Logger log = LoggerFactory.getLogger(CustomerController.class);

	@PostConstruct
	public void init() {
		log.info("Start Loading SalesForce Properties");
		log.info("Url is {}", env.getProperty("sfdc.url"));
		log.info("UserName is {}", env.getProperty("sfdc.username"));
		log.info("Password is {}", env.getProperty("sfdc.password"));
		log.info("End Loading SalesForce Properties");
	}

	private void dispatchEventToSalesForce(String event)
			throws ClientProtocolException, IOException, AuthenticationException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(env.getProperty("sfdc.url"));
		httpPost.setEntity(new StringEntity(event));
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(env.getProperty("sfdc.username"),
				env.getProperty("sfdc.password"));
		httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));

		CloseableHttpResponse response = client.execute(httpPost);
		log.info("Response from SFDC is {}", response.getStatusLine().getStatusCode());
		client.close();
	}

	/**
	 * Get customer using id. Returns HTTP 404 if customer not found
	 *
	 * @param customerId
	 * @return retrieved customer
	 */
	@RequestMapping(value = "/customers/{customerId}", method = RequestMethod.GET)
	public Customer getCustomer(@PathVariable("customerId") Long customerId) {

		/* validate customer Id parameter */
      if (null == customerId) {
        throw new InvalidCustomerRequestException();
      }

      Customer customer = customerRepository.findOne(customerId);
		if (null == customer) {
		  throw new CustomerNotFoundException();
	  }

	  Account account = new Account(4242l,1234, "savings", 1, 0);
	  log.info("Account Data is {}", account);
	  log.info("Customer Data is {}", customer);

      try {
        dispatchEventToSalesForce(String.format(" Customer %s Logged into SalesForce", customer));
      } catch (Exception e) {
        log.error("Failed to Dispatch Event to SalesForce . Details {} ", e.getLocalizedMessage());

      }

      return customer;
    }

    /**
     * Handler for / loads the index.tpl
     * @param httpResponse
     * @param request
     * @return
     * @throws IOException
     */
      @RequestMapping(value = "/", method = RequestMethod.GET)
      public String index(HttpServletResponse httpResponse, WebRequest request) throws IOException {
	  	ClassPathResource cpr = new ClassPathResource("static/index.html");
	  	String ret = "";
		  try {
			  byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
			  ret= new String(bdata, StandardCharsets.UTF_8);
		  } catch (IOException e) {
			  //LOG.warn("IOException", e);
		  }
		  return ret;
      }

      /**
       * Check if settings= is present in cookie
       * @param request
       * @return
       */
      private boolean checkCookie(WebRequest request) throws Exception {
      	try {
			return request.getHeader("Cookie").startsWith("settings=");
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		return false;
      }

      /**
       * restores the preferences on the filesystem
       *
       * @param httpResponse
       * @param request
       * @throws Exception
       */
      @RequestMapping(value = "/loadSettings", method = RequestMethod.GET)
      public void loadSettings(HttpServletResponse httpResponse, WebRequest request) throws Exception {
        // get cookie values
        if (!checkCookie(request)) {
          httpResponse.getOutputStream().println("Error");
          throw new Exception("cookie is incorrect");
        }
        String md5sum = request.getHeader("Cookie").substring("settings=".length(), 41);
    	ClassPathResource cpr = new ClassPathResource("static");
    	File folder = new File(cpr.getPath());
		File[] listOfFiles = folder.listFiles();
        String filecontent = new String();
        for (File f : listOfFiles) {
          // not efficient, i know
          filecontent = new String();
          byte[] encoded = Files.readAllBytes(f.toPath());
          filecontent = new String(encoded, StandardCharsets.UTF_8);
          if (filecontent.contains(md5sum)) {
            // this will send me to the developer hell (if exists)

            // encode the file settings, md5sum is removed
            String s = new String(Base64.getEncoder().encode(filecontent.replace(md5sum, "").getBytes()));
            // setting the new cookie
            httpResponse.setHeader("Cookie", "settings=" + s + "," + md5sum);
            return;
          }
        }
      }


  /**
   * Saves the preferences (screen resolution, language..) on the filesystem
   *
   * @param httpResponse
   * @param request
   * @throws Exception
   */
  @RequestMapping(value = "/saveSettings", method = RequestMethod.GET)
  public