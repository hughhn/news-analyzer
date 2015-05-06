package services.semantria.serializer;

import services.semantria.interfaces.ISerializer;
import services.semantria.mapping.Batch;
import services.semantria.mapping.Collection;
import services.semantria.mapping.Document;
import services.semantria.mapping.configuration.stub.*;
import services.semantria.mapping.output.*;
import services.semantria.mapping.output.stub.CollsAnalyticData;
import services.semantria.mapping.output.stub.DocsAnalyticData;
import services.semantria.mapping.output.stub.FeaturesList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public final class XmlSerializer implements ISerializer
{
	public Object deserialize(String input, Class<?> type)
	{
		Object obj = null;
		if(input.length() > 0)
		{
			try
			{
				ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
				JAXBContext jc = JAXBContext.newInstance(
						Blacklists.class,
						Categories.class,
						SentimentPhrases.class,
                        Configurations.class,
						Queries.class,
						UserEntities.class,
						DocAnalyticData.class,
						DocsAnalyticData.class,
						CollAnalyticData.class,
                        CollsAnalyticData.class,
						ServiceStatus.class,
						Subscription.class,
                        FeaturesList.class,
						Statistics.class
				);
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				obj = unmarshaller.unmarshal(inputStream);
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
		return obj;
	}
	
	public String serialize(Object obj)
	{
		String res = null;
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			JAXBContext jc = JAXBContext.newInstance(
					Blacklists.class,
					Categories.class,
					Queries.class,
					SentimentPhrases.class,
					UserEntities.class,
					Configurations.class,
					Batch.class,
					Collection.class,
					Document.class,
					CategoriesDeleteReq.class,
					QueriesDeleteReq.class,
					SentimentPhrasesDeleteReq.class,
					UserEntitiesDeleteReq.class,
					ConfigurationsDeleteReq.class
			);
			Marshaller marshaller = jc.createMarshaller();

			marshaller.marshal(obj, os);
			res = os.toString();

		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		return res;
	}
	
	public String getType()
	{
		return "xml";
	}
}
