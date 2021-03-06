/**
 * 
 */
package org.wise.portal.presentation.web.controllers.teacher.management;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.wise.portal.presentation.web.controllers.ControllerUtil;

/**
 * @author MattFish
 * @version $Id:$
 */
public class ChangeTeacherPasswordSuccessController extends AbstractController{

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView modelAndView = new ModelAndView();
    	ControllerUtil.addUserToModelAndView(request, modelAndView);

        return modelAndView;
	}

}
