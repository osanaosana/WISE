package org.wise.portal.domain.portal;

import java.util.Date;

import org.json.JSONObject;
import org.wise.portal.domain.Persistable;

public interface PortalStatistics extends Persistable {

    public Date getTimestamp();

	public void setTimestamp(Date timestamp);
	
	public Long getTotalNumberStudents();
	
	public void setTotalNumberStudents(Long totalNumberStudents);

	public Long getTotalNumberStudentLogins();

	public void setTotalNumberStudentLogins(Long totalNumberStudentLogins);

	public Long getTotalNumberTeachers();

	public void setTotalNumberTeachers(Long totalNumberTeachers);

	public Long getTotalNumberTeacherLogins();

	public void setTotalNumberTeacherLogins(Long totalNumberTeacherLogins);

    public Long getTotalNumberProjects();

	public void setTotalNumberProjects(Long totalNumberProjects);
	
	public Long getTotalNumberRuns();

	public void setTotalNumberRuns(Long totalNumberRuns);

	public Long getTotalNumberProjectsRun();

	public void setTotalNumberProjectsRun(Long totalNumberProjectsRun);
	
	public JSONObject getJSONObject();
}
