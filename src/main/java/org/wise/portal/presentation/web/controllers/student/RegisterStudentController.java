/**
 * Copyright (c) 2007 Regents of the University of California (Regents). Created
 * by TELS, Graduate School of Education, University of California at Berkeley.
 *
 * This software is distributed under the GNU Lesser General Public License, v2.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE SOFTWAREAND ACCOMPANYING DOCUMENTATION, IF ANY, PROVIDED
 * HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.wise.portal.presentation.web.controllers.student;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.StaleObjectStateException;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.AccountQuestion;
import org.wise.portal.domain.PeriodNotFoundException;
import org.wise.portal.domain.authentication.Gender;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.project.impl.Projectcode;
import org.wise.portal.domain.user.User;
import org.wise.portal.presentation.web.StudentAccountForm;
import org.wise.portal.service.authentication.DuplicateUsernameException;
import org.wise.portal.service.student.StudentService;
import org.wise.portal.service.user.UserService;

/**
 * Signup controller for TELS student user
 *
 * @author Hiroki Terashima
 * @version $Id: RegisterStudentController.java 955 2007-08-21 22:47:13Z hiroki $
 */
public class RegisterStudentController extends SimpleFormController {
	
	private StudentService studentService;

	private UserService userService;
	
	protected static final String USERNAME_KEY = "username";
	
	public RegisterStudentController() {
		setValidateOnBinding(false);
	}
	
	/**
	 * On submission of the signup form, a user is created and saved to the data
	 * store.
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@Override
	@Transactional(rollbackFor = { 
			DuplicateUsernameException.class, ObjectNotFoundException.class, 
			PeriodNotFoundException.class, HibernateOptimisticLockingFailureException.class,
			StaleObjectStateException.class})
	protected synchronized ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
	throws Exception {
		String domain =  "http://" + request.getServerName();
		String domainWithPort = domain + ":" + request.getLocalPort();
		String referrer = request.getHeader("referer");

		//get the context path e.g. /wise
		String contextPath = request.getContextPath();
		
		String registerUrl = contextPath + "/student/registerstudent.html";
		String updateAccountInfoUrl = contextPath + "/student/updatestudentaccount.html";
		
		if(referrer != null && 
				(referrer.contains(domain + registerUrl) || 
				 referrer.contains(domainWithPort + registerUrl) ||
  				 referrer.contains(domain + updateAccountInfoUrl) ||
				 referrer.contains(domainWithPort + updateAccountInfoUrl))){
			StudentAccountForm accountForm = (StudentAccountForm) command;
			StudentUserDetails userDetails = (StudentUserDetails) accountForm.getUserDetails();
	
			if (accountForm.isNewAccount()) {
				try {
					//get the first name and last name
					String firstName = userDetails.getFirstname();
					String lastName = userDetails.getLastname();

					//check if first name and last name only contain letters
					Pattern pattern = Pattern.compile("[a-zA-Z]*");
					Matcher firstNameMatcher = pattern.matcher(firstName);
					Matcher lastNameMatcher = pattern.matcher(lastName);
					
					if(!firstNameMatcher.matches()) {
						//first name contains non letter characters
			    		errors.rejectValue("userDetails.firstname", "error.firstname-illegal-characters");
			    		return showForm(request, response, errors);
					}
					
					if(!lastNameMatcher.matches()) {
						//last name contains non letter characters
						errors.rejectValue("userDetails.lastname", "error.lastname-illegal-characters");
			    		return showForm(request, response, errors);						
					}
					
					User user = userService.createUser(userDetails);
					Projectcode projectcode = new Projectcode(accountForm.getProjectCode());
					
					int maxLoop = 100;  // to ensure that the following while loop gets run at most this many times.
					int currentLoopIndex = 0;
					while (currentLoopIndex < maxLoop) {
						try {
							studentService.addStudentToRun(user, projectcode);  // add student to period
						} catch (HibernateOptimisticLockingFailureException holfe) {
							// multiple students tried to create an account at the same time, resulting in this exception. try saving again.
							currentLoopIndex++;
							continue;
						} catch (StaleObjectStateException sose) {
							// multiple students tried to create an account at the same time, resulting in this exception. try saving again.
							currentLoopIndex++;
							continue;
						}
						// if it reaches here, it means that hibernate optimisitic locking exception was not thrown, so we can exit the loop.
						break;
					}
				} catch (DuplicateUsernameException e) {
					errors.rejectValue("userDetails.username", "error.duplicate-username",
							new Object[] { userDetails.getUsername() }, "Duplicate Username.");
					return showForm(request, response, errors);
				} catch (ObjectNotFoundException e) {
		    		errors.rejectValue("projectCode", "error.illegal-projectcode");
		    		return showForm(request, response, errors);
		    	} catch (PeriodNotFoundException e) {
		    		errors.rejectValue("projectCode", "error.illegal-projectcode");
		    		return showForm(request, response, errors);
		    	}
			} else {
				User user = userService.retrieveUserByUsername(userDetails.getUsername());
				StudentUserDetails studentUserDetails = (StudentUserDetails) user.getUserDetails();
				studentUserDetails.setLanguage(userDetails.getLanguage());
		        String userLanguage = userDetails.getLanguage();
				Locale locale = new Locale(userLanguage);
		        request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);

				userService.updateUser(user);
				// update user in session
				request.getSession().setAttribute(
						User.CURRENT_USER_SESSION_KEY, user);
			}
	
			ModelAndView modelAndView = new ModelAndView(getSuccessView());
	
			modelAndView.addObject(USERNAME_KEY, userDetails.getUsername());
			return modelAndView;
		} else {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
	}
	
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		return new StudentAccountForm();
	}
	
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("genders", Gender.values());
		model.put("accountQuestions",AccountQuestion.values());
		model.put("languages", new String[]{"en", "zh_TW", "zh_CN", "nl", "he", "ja", "ko", "es"});
		return model;
	}
	
	@Override
	protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) {

		StudentAccountForm accountForm = (StudentAccountForm) command;
		StudentUserDetails userDetails = (StudentUserDetails) accountForm.getUserDetails();
		if (accountForm.isNewAccount()) {
			userDetails.setSignupdate(Calendar.getInstance().getTime());
			Calendar birthday       = Calendar.getInstance();
			int birthmonth = Integer.parseInt(accountForm.getBirthmonth());
			int birthdate = Integer.parseInt(accountForm.getBirthdate());
			birthday.set(Calendar.MONTH, birthmonth-1);  // month is 0-based
			birthday.set(Calendar.DATE, birthdate);
			userDetails.setBirthday(birthday.getTime());
		}

		getValidator().validate(accountForm, errors);
	}

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
	{
	  binder.registerCustomEditor(Date.class,
	    new CustomDateEditor(new SimpleDateFormat("MM/dd"), false)
	  );
	}

	/**
	 * @param studentService the studentService to set
	 */
	public void setStudentService(StudentService studentService) {
		this.studentService = studentService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}


}
