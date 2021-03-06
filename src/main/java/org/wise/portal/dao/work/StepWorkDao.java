package org.wise.portal.dao.work;

import java.util.List;

import org.wise.portal.dao.SimpleDao;
import org.wise.vle.domain.node.Node;
import org.wise.vle.domain.user.UserInfo;
import org.wise.vle.domain.work.StepWork;


public interface StepWorkDao<T extends StepWork> extends SimpleDao<T> {

	public StepWork getStepWorkById(Long id);
	
	public void saveStepWork(StepWork stepWork);
	
	public List<StepWork> getStepWorksByUserInfo(UserInfo userInfo);
	
	public StepWork getLatestStepWorkByUserInfo(UserInfo userInfo);
	
	public StepWork getLatestStepWorkByUserInfoAndNode(UserInfo userInfo,Node node);
	
	public List<StepWork> getStepWorksByUserInfoAndNode(UserInfo userInfo,Node node);
	
	public List<StepWork> getStepWorksByUserInfoAndNodeList(UserInfo userInfo, List<Node> nodeList);
	
	public List<StepWork> getStepWorksByUserInfosAndNode(List<UserInfo> userInfos, Node node);
	
	public List<StepWork> getStepWorksByNode(Node node);
	
	public StepWork getStepWorkByStepWorkId(Long id);
	
	public StepWork getStepWorkByUserIdAndData(UserInfo userInfo,String data);
	
}
