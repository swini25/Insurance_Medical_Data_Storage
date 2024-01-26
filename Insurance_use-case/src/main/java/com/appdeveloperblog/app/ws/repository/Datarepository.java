package com.appdeveloperblog.app.ws.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.appdeveloperblog.app.ws.models.Data;

public interface Datarepository extends MongoRepository<Data, String> {
}