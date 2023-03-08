/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.quickstarts.microprofile.faulttolerance;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;

@RunWith(Arquillian.class)
public class MicroProfileFaultToleranceITest {

    private static final String APP_NAME = "microprofile-fault-tolerance";

    private AtomicInteger attempt = new AtomicInteger();

    @ArquillianResource
    private URL deploymentUrl;

    @Inject
    private CoffeeResource coffeeResource;

    private Client client;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, APP_NAME + ".war")
                .addPackage(CoffeeApplication.class.getPackage());
    }

    @Before
    public void before() {
        client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        client.close();
    }

    @Test
    public void testCoffeeList() {
        coffeeResource.setFailRatio(0f);
        coffeeResource.resetCounter();

        try (Response response = this.getResponse("/coffee")) {
            Assert.assertEquals(200, response.getStatus());

            List<Coffee> entity = response.readEntity(new GenericType<List<Coffee>>() {});
            Assert.assertNotNull(entity);
            Assert.assertEquals(3, entity.size());
        }
    }

    @Test
    public void testCoffeeListFailure() {
        coffeeResource.setFailRatio(1f);
        coffeeResource.resetCounter();

        try (Response response = this.getResponse("/coffee")) {
            Assert.assertEquals(500, response.getStatus());
            Assert.assertEquals(5, coffeeResource.getCounter().longValue());
        }
    }

    @Test
    public void testCoffeeDetail() {
        coffeeResource.setFailRatio(0f);

        try (Response response = this.getResponse("/coffee/1")) {
            Assert.assertEquals(200, response.getStatus());

            Coffee entity = response.readEntity(new GenericType<Coffee>() {});
            Assert.assertNotNull(entity);
            Assert.assertEquals("Colombia", entity.countryOfOrigin);
        }
    }

    @Test
    public void testCoffeeDetailFailure() {
        coffeeResource.setFailRatio(1f);

        try (Response response = this.getResponse("/coffee/1")) {
            Assert.assertEquals(500, response.getStatus());
        }
    }

    /*@Test
    public void testCoffeeOrders() {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        try (Response response = this.getResponse("order")) {
            Assert.assertEquals(200, response.getStatus());

            Callable<String> call = () -> callWithFallbackAndNoRetryOnBulkhead();
            List<Future<String>> futures = executorService.invokeAll(asList(call, call, call));

            List<String> results = collectResultsAssumingFailures(futures, 0);
            Assert.assertEquals(futures.size(),3);
            //Assert.assertThat(futures.get.get().contains("call0");
            //assertThat(results).as("second call failed and was expected to be successful").contains("call1");
            //assertThat(results).as("third call didn't fall back").anyMatch(t -> t.startsWith("fallback"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdownNow();
        }

    }

    @Retry(retryOn = TimeoutException.class, delay = 500)
    @Bulkhead(3)
    public String callWithFallbackAndNoRetryOnBulkhead() throws InterruptedException {
        int attempt = this.attempt.getAndIncrement();
        if (attempt < 3) {
            Thread.sleep(300L);
        }
        return "call" + attempt;
    }

    private List<String> collectResultsAssumingFailures(List<Future<String>> futures, int expectedFailureCount) {
        int failureCount = 0;
        List<String> resultList = new ArrayList<>();
        for (Future<String> future : futures) {
            try {
                resultList.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                failureCount++;
            }
        }

        Assert.assertEquals(failureCount,expectedFailureCount);
        return resultList;
    }*/


    private Response getResponse(String path) {
        return client.target(deploymentUrl.toString())
                .path(path)
                .request()
                .get();
    }
}
