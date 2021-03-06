package org.wise.portal.domain.admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.wise.portal.dao.crater.CRaterRequestDao;
import org.wise.portal.dao.offering.RunDao;
import org.wise.portal.dao.portal.PortalStatisticsDao;
import org.wise.portal.dao.project.ProjectDao;
import org.wise.portal.dao.user.UserDao;
import org.wise.portal.domain.authentication.MutableUserDetails;
import org.wise.portal.domain.authentication.impl.StudentUserDetails;
import org.wise.portal.domain.authentication.impl.TeacherUserDetails;
import org.wise.portal.domain.portal.PortalStatistics;
import org.wise.portal.domain.portal.impl.PortalStatisticsImpl;
import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;
import org.wise.portal.service.vle.VLEService;
import org.wise.vle.domain.cRater.CRaterRequest;
import org.wise.vle.domain.statistics.VLEStatistics;
import org.wise.vle.domain.work.StepWork;
import org.wise.vle.web.VLEAnnotationController;

public class DailyAdminJob extends QuartzJobBean {

	private RunDao<Run> runDao;
	
	private UserDao<User> userDao;
	
	private ProjectDao<Project> projectDao;
	
	private PortalStatisticsDao<PortalStatistics> portalStatisticsDao;
	
	private VLEService vleService;
	
	private Properties wiseProperties;
	
	private CRaterRequestDao<CRaterRequest> cRaterRequestDao;
	
	private boolean DEBUG = false;
	
	/**
	 * 
	 * @see org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org.quartz.JobExecutionContext)
	 */
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		//query for the portal statistics and save a new row in the portalStatistics table
		gatherPortalStatistics();
		
		//query the vle tables and save a new row in the vleStatistics table
		gatherVLEStatistics();
		
		//try to score the CRater student work that previously failed to be scored
		handleIncompleteCRaterRequests();
	}
	
	/**
	 * query for the portal statistics and save a new row in the portalStatistics table
	 */
	private void gatherPortalStatistics() {
		debugOutput("gatherPortalStatistics start");
		
		//get all the students
		List<User> allStudents = userDao.retrieveByField(null, null, null, "studentUserDetails");
		long totalNumberStudents = allStudents.size();
		debugOutput("Number of students: " + totalNumberStudents);
		
		//get all the teachers
		List<User> allTeachers = userDao.retrieveByField(null, null, null, "teacherUserDetails");
		long totalNumberTeachers = allTeachers.size();
		debugOutput("Number of teachers: " + totalNumberTeachers);
		
		//get the number of student logins
		long totalNumberStudentLogins = 0;
		for(int x=0; x<allStudents.size(); x++) {
			User user = allStudents.get(x);
			MutableUserDetails userDetails = user.getUserDetails();
			totalNumberStudentLogins += ((StudentUserDetails) userDetails).getNumberOfLogins();
		}
		debugOutput("Number of student logins: " + totalNumberStudentLogins);
		
		//get the number of teacher logins
		long totalNumberTeacherLogins = 0;
		for(int x=0; x<allTeachers.size(); x++) {
			User user = allTeachers.get(x);
			MutableUserDetails userDetails = user.getUserDetails();
			totalNumberTeacherLogins += ((TeacherUserDetails) userDetails).getNumberOfLogins();
		}
		debugOutput("Number of teacher logins: " + totalNumberTeacherLogins);
		
		//get the number of projects
		List<Project> projectList = projectDao.getList();
		long totalNumberProjects = projectList.size();
		debugOutput("Number of projects: " + totalNumberProjects);
		
		//get the number of runs
		List<Run> runList = runDao.getList();
		long totalNumberRuns = runList.size();
		debugOutput("Number of runs: " + totalNumberRuns);
		
		//get the number of projects run (how many times students have clicked on the "Run Project" button)
		long totalNumberProjectsRun = 0;
		for(int x=0; x<runList.size(); x++) {
			Run run = runList.get(x);
			Integer timesRun = run.getTimesRun();
			
			if(timesRun != null) {
				totalNumberProjectsRun += timesRun;				
			}
		}
		debugOutput("Number of projects run: " + totalNumberProjectsRun);
		
		//create a new portal statistics object and populate it
		PortalStatisticsImpl newPortalStatistics = new PortalStatisticsImpl();
		newPortalStatistics.setTimestamp(new Date());
		newPortalStatistics.setTotalNumberStudents(totalNumberStudents);
		newPortalStatistics.setTotalNumberStudentLogins(totalNumberStudentLogins);
		newPortalStatistics.setTotalNumberTeachers(totalNumberTeachers);
		newPortalStatistics.setTotalNumberTeacherLogins(totalNumberTeacherLogins);
		newPortalStatistics.setTotalNumberProjects(totalNumberProjects);
		newPortalStatistics.setTotalNumberRuns(totalNumberRuns);
		newPortalStatistics.setTotalNumberProjectsRun(totalNumberProjectsRun);
		
		//save the new portal statistics
		portalStatisticsDao.save(newPortalStatistics);
		
		debugOutput("gatherPortalStatistics end");
	}
	
	/**
	 * Get the VLE statistics from the vle tables
	 * @param context
	 * @throws JobExecutionException
	 */
	public void gatherVLEStatistics() throws JobExecutionException {
		try {
			//get the user name, password, and url for the db
			String userName = this.wiseProperties.getProperty("hibernate.connection.username");
			String password = this.wiseProperties.getProperty("hibernate.connection.password");
			String url = this.wiseProperties.getProperty("hibernate.connection.url");
			
			//create a connection to the mysql db
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection conn = DriverManager.getConnection(url, userName, password);

			//create a statement to run db queries
			Statement statement = conn.createStatement();

			//the JSONObject that we will store all the statistics in and then store in the db
			JSONObject vleStatistics = new JSONObject();
			
			//gather the StepWork statistics
			gatherStepWorkStatistics(statement, vleStatistics);
			
			//gather the Node statistics
			gatherNodeStatistics(statement, vleStatistics);
			
			//gather the Annotation statistics
			gatherAnnotationStatistics(statement, vleStatistics);
			
			//gather the Hint statistics
			gatherHintStatistics(statement, vleStatistics);
			
	    	//get the current timestamp
	    	Date date = new Date();
	    	Timestamp timestamp = new Timestamp(date.getTime());
	    	
	    	//set the timestamp in milliseconds into the JSONObject
	    	vleStatistics.put("timestamp", timestamp.getTime());
	    	
	    	//save the vle statistics row
	    	VLEStatistics vleStatisticsObject = new VLEStatistics();
	    	vleStatisticsObject.setTimestamp(timestamp);
	    	vleStatisticsObject.setData(vleStatistics.toString());
	    	this.vleService.saveVLEStatistics(vleStatisticsObject);

	    	//close the db connection
	    	conn.close();
		} catch (Exception ex) {
			LoggerFactory.getLogger(getClass()).error(ex.getMessage());
		}
	}
	
	/**
	 * Gather the StepWork statistics. This includes the total number of StepWork
	 * rows as well as how many StepWork rows for each step type.
	 * @param statement the object to execute queries
	 * @param vleStatistics the JSONObject to store the statistics in
	 */
	private void gatherStepWorkStatistics(Statement statement, JSONObject vleStatistics) {
		try {
			//counter for total step work rows
			long stepWorkCount = 0;
			
			//array to hold the counts for each node type
			JSONArray stepWorkNodeTypeCounts = new JSONArray();
			
			/*
			 * the query to get the total step work rows for each node type
			 * e.g.
			 * 
			 * nodeType           | count(*)
			 * ------------------------------
			 * AssessmentListNode | 331053
			 * BrainstormNode     | 10936
			 * CarGraphNode       | 9
			 * etc.
			 * 
			 */
			ResultSet stepWorkNodeTypeCountQuery = statement.executeQuery("select node.nodeType, count(*) from stepwork, node where stepwork.node_id=node.id group by nodeType");
			
			//loop through all the rows from the query
			while(stepWorkNodeTypeCountQuery.next()) {
				//get the nodeType
				String nodeType = stepWorkNodeTypeCountQuery.getString(1);
				
				//get the count
				long nodeTypeCount = stepWorkNodeTypeCountQuery.getLong(2);
				
				try {
					if(nodeType != null && !nodeType.toLowerCase().equals("null")) {
						//create the object that will store the nodeType and count
						JSONObject stepWorkNodeTypeObject = new JSONObject();
						stepWorkNodeTypeObject.put("nodeType", nodeType);
						stepWorkNodeTypeObject.put("count", nodeTypeCount);
						
						//put the object into our array
						stepWorkNodeTypeCounts.put(stepWorkNodeTypeObject);
						
						//update the total count
						stepWorkCount += nodeTypeCount;
					}
				} catch(JSONException e) {
					e.printStackTrace();
				}
			}
			
			//add the step work statistics to the vleStatistics object
			vleStatistics.put("individualStepWorkNodeTypeCounts", stepWorkNodeTypeCounts);
			vleStatistics.put("totalStepWorkCount", stepWorkCount);
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gather the Annotation statistics. This includes the total number of Annotation
	 * rows as well as how many Annotation nodes for each annotation type.
	 * @param statement the object to execute queries
	 * @param vleStatistics the JSONObject to store the statistics in
	 */
	private void gatherAnnotationStatistics(Statement statement, JSONObject vleStatistics) {
		try {
			//get the total number of annotations
			ResultSet annotationCountQuery = statement.executeQuery("select count(*) from annotation");
			
			if(annotationCountQuery.first()) {
				//get the total number of annotations
				long annotationCount = annotationCountQuery.getLong(1);
				
				try {
					//add the total annotation count to the vle statistics
					vleStatistics.put("totalAnnotationCount", annotationCount);
				} catch(JSONException e) {
					e.printStackTrace();
				}
			}
			
			//this will hold all the annotation types e.g. "comment", "score", "flag", "cRater"
			Vector<String> annotationTypes = new Vector<String>();
			
			//get all the different types of annotations
			ResultSet annotationTypeQuery = statement.executeQuery("select distinct type from annotation");
			
			while(annotationTypeQuery.next()) {
				String annotationType = annotationTypeQuery.getString(1);
				annotationTypes.add(annotationType);
			}
			
			//the array to store the counts for each annotation type
			JSONArray annotationCounts = new JSONArray();
			
			//loop through all the annotation types
			for(String annotationType : annotationTypes) {
				if(annotationType != null && !annotationType.equals("")
						&& !annotationType.equals("null") && !annotationType.equals("NULL")) {
					//get the total number of annotations for the current annotation type
					ResultSet annotationTypeCountQuery = statement.executeQuery("select count(*) from annotation where type='" + annotationType + "'");
					
					if(annotationTypeCountQuery.first()) {
						//get the count for the current annotation type
						long annotationTypeCount = annotationTypeCountQuery.getLong(1);
						
						try {
							//create an object to store the type and count in
							JSONObject annotationObject = new JSONObject();
							annotationObject.put("annotationType", annotationType);
							annotationObject.put("count", annotationTypeCount);
							
							annotationCounts.put(annotationObject);
						} catch(JSONException e) {
							e.printStackTrace();
						}
					}					
				}
			}
			
			//add the annotation statistics to the vle statistics
			vleStatistics.put("individualAnnotationCounts", annotationCounts);			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the node statistics. This includes the total number of step nodes as well
	 * as how many step nodes for each node type.
	 * @param statement the object to execute queries
	 * @param vleStatistics the JSONObject to store the statistics in
	 */
	private void gatherNodeStatistics(Statement statement, JSONObject vleStatistics) {
		try {
			//counter for the total number of nodes
			long nodeCount = 0;
			
			//array to hold all the counts for each node type
			JSONArray nodeTypeCounts = new JSONArray();
			
			/*
			 * the query to get the total number of nodes for each node type
			 * e.g.
			 * 
			 * nodeType           | count(*)
			 * ------------------------------
			 * AssessmentListNode | 3408
			 * BrainstormNode     | 98
			 * CarGraphNode       | 9
			 * etc.
			 * 
			 */ 
			ResultSet nodeTypeCountQuery = statement.executeQuery("select nodeType, count(*) from node group by nodeType");
			
			//loop through all the rows
			while(nodeTypeCountQuery.next()) {
				//get a node type and the count
				String nodeType = nodeTypeCountQuery.getString(1);
				long nodeTypeCount = nodeTypeCountQuery.getLong(2);
				
				if(nodeType != null && !nodeType.toLowerCase().equals("null")) {
					try {
						//create an object to hold the node type and count
						JSONObject nodeTypeObject = new JSONObject();
						nodeTypeObject.put("nodeType", nodeType);
						nodeTypeObject.put("count", nodeTypeCount);
						
						//add the object to our array
						nodeTypeCounts.put(nodeTypeObject);
						
						//update the total count
						nodeCount += nodeTypeCount;
					} catch(JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			//add the counts to the vle statistics
			vleStatistics.put("individualNodeTypeCounts", nodeTypeCounts);
			vleStatistics.put("totalNodeCount", nodeCount);			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the number of times hints were viewed by a student
	 * @param statement the object to execute queries
	 * @param vleStatistics the JSONObject to store the statistics in
	 */
	private void gatherHintStatistics(Statement statement, JSONObject vleStatistics) {
		try {
			//get the total number of times a hint was viewed by a student
			ResultSet hintCountQuery = statement.executeQuery("select count(*) from stepwork where data like '%hintStates\":[{%]%'");
			
			if(hintCountQuery.first()) {
				//add the count to the vle statistics
				long hintCount = hintCountQuery.getLong(1);
				vleStatistics.put("totalHintViewCount", hintCount);
			}			
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Try to score the CRater student work that previously failed to be scored
	 */
	private void handleIncompleteCRaterRequests() {
		//get the CRater url and client id
		String cRaterScoringUrl = this.wiseProperties.getProperty("cRater_scoring_url");
		String cRaterClientId = this.wiseProperties.getProperty("cRater_client_id");
		
		//get the Henry url and client id
		String henryScoringUrl = this.wiseProperties.getProperty("henry_scoring_url");
		String henryClientId = this.wiseProperties.getProperty("henry_client_id");
		
		if(cRaterScoringUrl != null || henryScoringUrl != null) {
			//get all the incomplete CRater requests
			List<CRaterRequest> incompleteCRaterRequests = this.cRaterRequestDao.getIncompleteCRaterRequests();
			
			//loop through all the incomplete requests
			for(int x=0; x<incompleteCRaterRequests.size(); x++) {
				//get a CRater request that needs to be scored
				CRaterRequest cRaterRequest = incompleteCRaterRequests.get(x);
				String cRaterItemType = cRaterRequest.getcRaterItemType();
				
				String scoringUrl = "";
				String clientId = "";
				
				//get the appropriate scoring url and client id
				if(cRaterItemType == null) {
					scoringUrl = cRaterScoringUrl;
					clientId = cRaterClientId;
				} else if(cRaterItemType.equals("CRATER")) {
					scoringUrl = cRaterScoringUrl;
					clientId = cRaterClientId;
				} else if(cRaterItemType.equals("HENRY")) {
					scoringUrl = henryScoringUrl;
					clientId = henryClientId;
				}
				
				StepWork stepWork = cRaterRequest.getStepWork();
				Long stepWorkId = stepWork.getId();
				Long nodeStateId = cRaterRequest.getNodeStateId();
				String runId = cRaterRequest.getRunId().toString();
				String annotationType = "cRater";
				
				//make the request to score the student CRater work
				VLEAnnotationController.getCRaterAnnotation(this.vleService, nodeStateId, runId, stepWorkId, annotationType, scoringUrl, clientId);
				
				//sleep for 10 seconds between each request
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}			
		}
	}
	
	/**
	 * A function that outputs the string to System.out if DEBUG is true
	 * @param output a String to output to System.out
	 */
	private void debugOutput(String output) {
		if(DEBUG) {
			System.out.println(output);
		}
	}

	/**
	 * @param runDao the runDao to set
	 */
	public void setRunDao(RunDao<Run> runDao) {
		this.runDao = runDao;
	}
	
	/**
	 * @param userDao the userDao to set
	 */
	public void setUserDao(UserDao<User> userDao) {
		this.userDao = userDao;
	}
	
	/**
	 * 
	 * @param projectDao the projectDao to set
	 */
	public void setProjectDao(ProjectDao<Project> projectDao) {
		this.projectDao = projectDao;
	}
	
	/**
	 * 
	 * @param portalStatisticsDao
	 */
	public void setPortalStatisticsDao(
			PortalStatisticsDao<PortalStatistics> portalStatisticsDao) {
		this.portalStatisticsDao = portalStatisticsDao;
	}

	public VLEService getVleService() {
		return vleService;
	}

	public void setVleService(VLEService vleService) {
		this.vleService = vleService;
	}

	public Properties getWiseProperties() {
		return wiseProperties;
	}

	public void setWiseProperties(Properties wiseProperties) {
		this.wiseProperties = wiseProperties;
	}

	public CRaterRequestDao<CRaterRequest> getcRaterRequestDao() {
		return cRaterRequestDao;
	}

	public void setcRaterRequestDao(CRaterRequestDao<CRaterRequest> cRaterRequestDao) {
		this.cRaterRequestDao = cRaterRequestDao;
	}
}
