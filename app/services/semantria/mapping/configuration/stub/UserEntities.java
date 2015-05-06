package services.semantria.mapping.configuration.stub;

import services.semantria.mapping.configuration.UserEntity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="entities")
public final class UserEntities
{
	private List<UserEntity> entities = new ArrayList<UserEntity>();

	public UserEntities() {}

	public UserEntities(List<UserEntity> entities)
	{
		this.entities = entities;
	}

	@XmlElement(name="entity")
	public List<UserEntity> getEntities() { return  entities; }
	
	public void setEntities(List<UserEntity> entities) { this.entities = entities; }
}
