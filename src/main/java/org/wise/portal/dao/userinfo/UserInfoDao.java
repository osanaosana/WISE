/**
 * Copyright (c) 2008 Regents of the University of California (Regents). Created
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
package org.wise.portal.dao.userinfo;

import java.util.List;

import org.wise.portal.dao.SimpleDao;
import org.wise.vle.domain.user.UserInfo;



/**
 * @author h
 * @version $Id:$
 */
public interface UserInfoDao<T extends UserInfo> extends SimpleDao<T> {

	public UserInfo getUserInfoById(Long id);
	
	public void saveUserInfo(UserInfo userInfo);
	
	public UserInfo getUserInfoByWorkgroupId(Long workgroupId);
	
	public UserInfo getUserInfoOrCreateByWorkgroupId(Long workgroupId);
	
	public List<UserInfo> getUserInfoByWorkgroupIds(List<String> workgroupIds);
	
	public List<UserInfo> getUserInfosThatHaveWorkedToday(List<UserInfo> userInfos);
	
}
