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
package org.wise.portal.domain.general.contactwise;

import java.util.Properties;

/**
 * @author Hiroki Terashima
 * @author Geoffrey Kwan
 *
 * @version $Id$
 */
public enum WebBrowser {

	FIREFOX, IE, SAFARI, OPERA, NETSCAPE, OTHER;
	
	//the properties file that contains the user friendly values
	private static Properties i18nProperties;
	
	/**
	 * @param properties the properties file that contains the user friendly
	 * (regular casing) values i.e. FIREFOX would be resolved to Firefox
	 */
	public static void setProperties(Properties properties) {
		i18nProperties = properties;
	}


	/**
	 * Returns the user-friendly string representation of
	 * the IssueType
	 * @return String
	 */
	@Override
	public String toString() {
		return i18nProperties.getProperty("webbrowsers." + this.name());
	}

	/*
	 * This is required because the jsp needs to retrieve the declared
	 * value and not the user friendly value
	 * @return the value of the enum as declared i.e. FIREFOX
	 */
	public String getName() {
		return name();
	}
	
}
