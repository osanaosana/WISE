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
package org.wise.portal.presentation.google.charts.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.wise.portal.presentation.google.charts.AbstractGoogleChart;
import org.wise.portal.presentation.google.charts.LineChart;

/**
 * LineChart initializes to default type of XYLineChart.
 * 
 * @author patrick lawler
 * @version $Id:$
 */
public class LineChartImpl extends AbstractGoogleChart implements LineChart{
	
	private boolean xyType = true;

	/**
	 * @see org.wise.portal.presentation.google.charts.AbstractGoogleChart#getChartData()
	 */
	@Override
	protected String getChartData() {
		String dataString = AMP + "chd=t:";
		
		if(this.data.size() < 1){
			return "";
		}
		
		for(List<?> list : this.data){
			for(int x = 0; x < list.size(); x++){
				dataString = dataString + list.get(x) + ",";
			}
			dataString = dataString.substring(0, dataString.length()-1) + "|";
		}
		return dataString.substring(0, dataString.length()-1);
	}

	/**
	 * @see org.wise.portal.presentation.google.charts.AbstractGoogleChart#getChartType()
	 */
	@Override
	protected String getChartType() {
		String type = AMP + "cht=";
		if(xyType){
			type = type + "lxy";
		} else {
			type = type + "lc";
		}
		return type;
	}

	/**
	 * @see org.wise.portal.presentation.google.charts.LineChart#addXYData(java.util.Map)
	 */
	public void addXYData(Map<?, ?> data) {
		this.addData(new LinkedList(data.keySet()));
		this.addData(new LinkedList(data.values()));
	}

	/**
	 * @see org.wise.portal.presentation.google.charts.LineChart#setXYType(boolean)
	 */
	public void setXYType(boolean v) {
		this.xyType = v;
	}

}
