package services.semantria.mapping.output.stub;

import services.semantria.mapping.output.CollAnalyticData;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="collections")
public final class CollsAnalyticData
{
	private List<CollAnalyticData> collAnalytics = new ArrayList<CollAnalyticData>();

	public CollsAnalyticData() {}

	public CollsAnalyticData(List<CollAnalyticData> collAnalytics)
	{
		this.collAnalytics = collAnalytics;
	}

	@XmlElement(name="collection")
	public List<CollAnalyticData> getDocuments() { return collAnalytics; }
	
	public void setDocuments(List<CollAnalyticData> collAnalytics) { collAnalytics = collAnalytics; }
}
