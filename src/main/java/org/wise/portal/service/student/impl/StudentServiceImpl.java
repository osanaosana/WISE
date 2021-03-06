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
package org.wise.portal.service.student.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.PeriodNotFoundException;
import org.wise.portal.domain.StudentUserAlreadyAssociatedWithRunException;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.project.impl.Projectcode;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.StudentRunInfo;
import org.wise.portal.domain.user.User;
import org.wise.portal.domain.workgroup.WISEWorkgroup;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.service.acl.AclService;
import org.wise.portal.service.group.GroupService;
import org.wise.portal.service.offering.RunService;
import org.wise.portal.service.student.StudentService;
import org.wise.portal.service.user.UserService;
import org.wise.portal.service.workgroup.WISEWorkgroupService;
import org.wise.portal.service.workgroup.WorkgroupService;

/**
 * @author Hiroki Terashima
 * @version $Id$
 */
public class StudentServiceImpl implements StudentService {

	private RunService runService;
	
	private GroupService groupService;
	
	private WorkgroupService workgroupService;
	
    private AclService<Run> aclService;
    
    private UserService userService;

	/**
	 * @see org.wise.portal.service.student.StudentService#addStudentToRun(net.sf.sail.webapp.domain.User, org.wise.portal.domain.impl.Projectcode)
	 */
	public void addStudentToRun(User studentUser, Projectcode projectcode) 
	    throws ObjectNotFoundException, PeriodNotFoundException, StudentUserAlreadyAssociatedWithRunException {
		// TODO HT: figure out if we need a Transactional annotation for this method
		// possible problem: groupService.addMembers is transactional
		// we probably need a rollback though
		String runcode = projectcode.getRuncode();
    	String periodName = projectcode.getRunPeriod();

		Run run = this.runService.retrieveRunByRuncode(runcode);
		if (!run.isStudentAssociatedToThisRun(studentUser)) {
			// this.aclService.addPermission(run, BasePermission.READ); TODO HT: uncomment when this is figured out
			Group period = run.getPeriodByName(periodName);
			Long groupId = period.getId();
			this.groupService.addMember(groupId, studentUser);
			
			//if teacher specified only one student/workgroup, create workgroup now
			if(run.getMaxWorkgroupSize()==1){
				String name = "Workgroup for user: " + studentUser.getUserDetails().getUsername();
				Set<User> members = new HashSet<User>();
				members.add(studentUser);
				WISEWorkgroup workgroup = ((WISEWorkgroupService)workgroupService).createWISEWorkgroup(name, members, run, period);
			}
		} else {
			throw new StudentUserAlreadyAssociatedWithRunException(studentUser, run);
		}
	}

	/**
	 * @see org.wise.portal.service.student.StudentService#getTeachersOfStudent(net.sf.sail.webapp.domain.User)
	 */
	public List<User> getTeachersOfStudent(User studentUser) {
		// get all runs that this student is associated with.
		List<Run> runList = runService.getRunList(studentUser);
		List<User> teachers = new ArrayList<User>();
		for (Run run : runList) {
			// add all of the owners of the run to the list.
			teachers.addAll(run.getOwners());
			teachers.addAll(run.getSharedowners());
		}
		return teachers;
	}
	

	/**
	 * @see org.wise.portal.service.student.StudentService#isStudentAssociatedWithTeacher(net.sf.sail.webapp.domain.User, net.sf.sail.webapp.domain.User)
	 */
	public boolean isStudentAssociatedWithTeacher(User studentUser,
			User teacherUser) {
		List<User> teachersOfStudent = getTeachersOfStudent(studentUser);
		return teachersOfStudent.contains(teacherUser);
	}
	
	/**
	 * @see org.wise.portal.service.student.StudentService#removeStudentFromRun(net.sf.sail.webapp.domain.User, org.wise.portal.domain.Run)
	 */
	public void removeStudentFromRun(User studentUser, Run run) {
		if (run.isStudentAssociatedToThisRun(studentUser)) {
    		StudentRunInfo studentRunInfo = getStudentRunInfo(studentUser, run);

			Group period = run.getPeriodOfStudent(studentUser);
			Set<User> membersToRemove = new HashSet<User>();
			membersToRemove.add(studentUser);
			this.groupService.removeMembers(period, membersToRemove);
			
        	Workgroup workgroup = studentRunInfo.getWorkgroup();
        	if (workgroup != null) {
        		Set<User> membersToRemoveFromWorkgroup = new HashSet<User>();
        		membersToRemoveFromWorkgroup.add(studentUser);
        		workgroupService.removeMembers(workgroup, membersToRemoveFromWorkgroup);
        	}
		}
	}

	/**
	 * @see org.wise.portal.service.student.StudentService#getStudentRunInfo(net.sf.sail.webapp.domain.User, org.wise.portal.domain.Run)
	 */
	public StudentRunInfo getStudentRunInfo(User studentUser, Run run) {
		StudentRunInfo studentRunInfo = new StudentRunInfo();
		studentRunInfo.setRun(run);
		studentRunInfo.setStudentUser(studentUser);
		studentRunInfo.setGroup(run.getPeriodOfStudent(studentUser));
		
		List<Workgroup> workgroupsForThisRun = 
			workgroupService.getWorkgroupListByOfferingAndUser(run, studentUser);
		if (workgroupsForThisRun.size() > 0) {
			WISEWorkgroup workgroupForThisRun = (WISEWorkgroup) workgroupsForThisRun.get(0);			
			studentRunInfo.setWorkgroup(workgroupForThisRun);
		} 
		
		return studentRunInfo;
	}

	/**
	 * @param runService the runService to set
	 */
	public void setRunService(RunService runService) {
		this.runService = runService;
	}

	/**
	 * @param groupService the groupService to set
	 */
	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}
	
	/**
	 * @param workgroupService the workgroupService to set
	 */
	public void setWorkgroupService(WorkgroupService workgroupService) {
		this.workgroupService = workgroupService;
	}

	/**
	 * @param aclService the aclService to set
	 */
	public void setAclService(AclService<Run> aclService) {
		this.aclService = aclService;
	}

	/**
	 * @param userService the userService to set
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}
