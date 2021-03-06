/**
 * 
 */
package org.wise.vle.domain.annotation;

import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wise.vle.domain.PersistableDomain;
import org.wise.vle.domain.user.UserInfo;
import org.wise.vle.domain.work.StepWork;


/**
 * Domain representing Annotation.
 * Annotations are what users annotate on other
 * user's work, such as Comments, Scores, Flags.
 * @author hirokiterashima
 */
@Entity
@Table(name="annotation")
@Inheritance(strategy=InheritanceType.JOINED)
public class Annotation extends PersistableDomain {

	protected static String fromQuery = "from Annotation";
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id = null;

	@ManyToOne(cascade = {CascadeType.PERSIST})
	private UserInfo fromUser;  // who created this annotation

	@ManyToOne(cascade = {CascadeType.PERSIST})
	private UserInfo toUser;  // who this annotation is for
	
	@ManyToOne(cascade = {CascadeType.PERSIST})
	private StepWork stepWork;   // the work that is being annotated

	@Column(name="annotateTime")
	private Timestamp annotateTime;  // when the work was actually annotated

	@Column(name="postTime")
	private Timestamp postTime;  // when this annotation was saved

	@Column(name="runId")
	private Long runId = null;
	
	@Column(name="type")
	private String type = null;
	
	@Column(name="data", length=512000)
	private String data = null;

	public Long getId() {
        return id;
    }

    @SuppressWarnings("unused")
	private void setId(Long id) {
        this.id = id;
    }

	/**
	 * @return the fromUser
	 */
	public UserInfo getFromUser() {
		return fromUser;
	}

	/**
	 * @param fromUser the fromUser to set
	 */
	public void setFromUser(UserInfo fromUser) {
		this.fromUser = fromUser;
	}

	/**
	 * @return the toUser
	 */
	public UserInfo getToUser() {
		return toUser;
	}

	/**
	 * @param toUser the toUser to set
	 */
	public void setToUser(UserInfo toUser) {
		this.toUser = toUser;
	}

	/**
	 * @return the stepWork
	 */
	public StepWork getStepWork() {
		return stepWork;
	}

	/**
	 * @param stepWork the stepWork to set
	 */
	public void setStepWork(StepWork stepWork) {
		this.stepWork = stepWork;
	}

	/**
	 * @return the annotateTime
	 */
	public Timestamp getAnnotateTime() {
		return annotateTime;
	}

	/**
	 * @param annotateTime the annotateTime to set
	 */
	public void setAnnotateTime(Timestamp annotateTime) {
		this.annotateTime = annotateTime;
	}

	/**
	 * @return the postTime
	 */
	public Timestamp getPostTime() {
		return postTime;
	}

	/**
	 * @param postTime the postTime to set
	 */
	public void setPostTime(Timestamp postTime) {
		this.postTime = postTime;
	}
	
	/**
	 * @return the id of the run
	 */
    public Long getRunId() {
		return runId;
	}

    /**
     * @param runId the id of the run
     */
	public void setRunId(Long runId) {
		this.runId = runId;
	}

	/**
	 * @see org.wise.vle.domain.PersistableDomain#getObjectClass()
	 */
	@Override
	protected Class<?> getObjectClass() {
		return Annotation.class;
	}

	/**
	 * 
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * 
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * populates the data of the annotation
	 * @param nodeVisit
	 */
	public void setData(String data) {
		// to be overridden by subclass.
		this.data = data;
	}
	
	/**
	 * Returns the data associated with this stepWork
	 * @return
	 */
	public String getData() {
		return this.data;
	}
	
	/**
	 * The default constructor for Annotation
	 */
	public Annotation() {
	}
	
	/**
	 * The constructor for Annotation
	 * @param type the annotation type
	 */
	public Annotation(String type) {
		this.type = type;
	}
	
	/**
	 * Constructor for Annotation
	 * @param stepWork
	 * @param fromUser who this annotation is from
	 * @param toUser who this annotation is for
	 * @param runId
	 * @param postTime
	 * @param type
	 * @param data
	 */
	public Annotation(StepWork stepWork, UserInfo fromUser, UserInfo toUser, Long runId, Timestamp postTime, String type, String data) {
		setStepWork(stepWork);
		setFromUser(fromUser);
		setToUser(toUser);
		setRunId(runId);
		setPostTime(postTime);
		setType(type);
		setData(data);
	}

	public JSONObject getAnnotationForNodeStateId(Long nodeStateId) {
		try {
			JSONObject dataJSON = new JSONObject(this.data);
			if (dataJSON != null) {
				JSONArray valueArray = dataJSON.getJSONArray("value");
				if (valueArray != null) {
					for (int i=0; i<valueArray.length(); i++) {
						JSONObject nodeStateCRaterAnnotation = valueArray.getJSONObject(i);
						long nodeStateIdFromAnnotation = nodeStateCRaterAnnotation.getLong("nodeStateId");
						if (nodeStateId != null && nodeStateId.equals(nodeStateIdFromAnnotation)) {
							return nodeStateCRaterAnnotation;
						}
						
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void appendNodeStateAnnotation(JSONObject nodeStateAnnotation) {
		try {
			JSONObject dataJSON = new JSONObject(this.data);
			if (dataJSON != null) {
				JSONArray valueArray = dataJSON.getJSONArray("value");
				if (valueArray != null) {
					valueArray.put(nodeStateAnnotation);
					this.data = dataJSON.toString();
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static JSONObject createCRaterNodeStateAnnotation(Long nodeStateId, int score, String concepts, JSONObject studentResponse, String cRaterResponse) {
		JSONObject cRaterNodeStateAnnotation = new JSONObject();
		
		try {
			cRaterNodeStateAnnotation.put("nodeStateId", nodeStateId);
			cRaterNodeStateAnnotation.put("score", score);
			cRaterNodeStateAnnotation.put("concepts", concepts);
			cRaterNodeStateAnnotation.put("studentResponse", studentResponse);
			cRaterNodeStateAnnotation.put("cRaterResponse", cRaterResponse);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return cRaterNodeStateAnnotation;
	}
}
