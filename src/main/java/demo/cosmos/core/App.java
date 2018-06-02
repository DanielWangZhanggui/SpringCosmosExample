package demo.cosmos.core;

import demo.cosmos.model.ContentRecord;
import demo.cosmos.model.ContentRecordSummer;
import demo.cosmos.model.PsCMSContentRecordImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import org.springframework.context.support.GenericXmlApplicationContext;

public class App {

	public static void main(String[] args) {

		// For XML
		ApplicationContext ctx = new GenericXmlApplicationContext("SpringConfig.xml");

		// For Annotation
//		 ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
//		MongoOperations mongoOperation = (MongoOperations) ctx.getBean("mongoTemplate");
		PsCMSContentRecordImpl psCMSContentRecord = (PsCMSContentRecordImpl) ctx.getBean(PsCMSContentRecordImpl.class);

		List<String> userList = new ArrayList<String>();
		List<String> contentList = new ArrayList<String>();

		for(int i=0; i<10001; i++)
		{
			userList.add(i+"");
		}

		ContentRecordSummer contentRecord = new ContentRecordSummer();

		for(int i=0; i<1001; i++)
		{
			for(int j=0; j<10; j++) {
				contentRecord.setContentId("99999");
				contentRecord.setUserId(userList.get(i));
				contentRecord.setAmount(1);
				contentRecord.setCreateTime(i);
				psCMSContentRecord.saveSum(contentRecord);
			}

			System.out.println(i);
		}

	}

}