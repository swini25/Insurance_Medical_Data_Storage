package com.appdeveloperblog.app.ws.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.*;
import jakarta.validation.constraints.NotNull;



@Document(collection = "data")
public class Data {

    @Id
    private String id;

    @NotNull
    private Object object;
    
    public Data() {
    }

    public Data(Object object) {
        this.object = object;
    }
    

    public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public Object getObject() {
		return object;
	}


	public void setObject(Object object) {
		this.object = object;
	}


	public void updateWith(Data newData) {
        this.object = newData.getObject();
    }

	@Override
	public String toString() {
		return "Data [id=" + id + ", object=" + object + "]";
	}

    // Getters and setters...
}