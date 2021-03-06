package org.wise.portal.dao.statistics;

import java.util.List;

import org.wise.portal.dao.SimpleDao;
import org.wise.vle.domain.statistics.VLEStatistics;


public interface VLEStatisticsDao<T extends VLEStatistics> extends SimpleDao<T> {

	public VLEStatistics getVLEStatisticsById(Long id);
	
	public void saveVLEStatistics(VLEStatistics vleStatistics);
	
	public List<VLEStatistics> getVLEStatistics();
}
