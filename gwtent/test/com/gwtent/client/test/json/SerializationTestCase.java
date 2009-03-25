package com.gwtent.client.test.json;

import java.util.ArrayList;
import java.util.List;

import com.gwtent.client.serialization.json.JsonSerializer;
import com.gwtent.client.test.GwtEntTestCase;

public class SerializationTestCase extends GwtEntTestCase{
	public void testJson(){
		Person p = new Person();
		p.setName("A");
		p.setAge(50);
		p.setSex(Sex.FEMALE);
		JsonSerializer serializer = new JsonSerializer();
		String jsonPerson = serializer.serializeObject(p);
		System.out.println(jsonPerson);
		
		serializer = new JsonSerializer();
		Person p1 = serializer.deserializeObject(jsonPerson, Person.class, null);
		assertTrue(p1.getName().equals(p.getName()));
		assertTrue(p1.getAge() == p.getAge());
		
		
		List<Person> people = new ArrayList<Person>();
		people.add(p);
		serializer = new JsonSerializer();
		System.out.println(serializer.serializeObject(people));
	}
}
