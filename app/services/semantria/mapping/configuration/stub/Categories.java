package services.semantria.mapping.configuration.stub;

import services.semantria.mapping.configuration.Category;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="categories")
public final class Categories
{
	private List<Category> categories = new ArrayList<Category>();

	public Categories() { }

	public Categories(List<Category> categories)
	{
		this.categories = categories;
	}

	@XmlElement(name="category")
	public List<Category> getCategories() { return categories; }
	
	public void setCategories(List<Category> categories) { this.categories = categories; }
}
