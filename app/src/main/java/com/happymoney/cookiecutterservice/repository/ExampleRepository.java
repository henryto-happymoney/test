package com.happymoney.cookiecutterservice.repository;

import com.happymoney.cookiecutterservice.entity.dynamo.ExampleDynamo;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@EnableScan
public interface ExampleRepository extends CrudRepository<ExampleDynamo, String> {
}
