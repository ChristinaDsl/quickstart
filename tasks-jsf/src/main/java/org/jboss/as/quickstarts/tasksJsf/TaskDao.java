/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.as.quickstarts.tasksJsf;

import java.util.List;

import jakarta.ejb.Local;

/**
 * Basic operations for manipulation of tasks
 *
 * @author Lukas Fryc
 *
 */
@Local
public interface TaskDao {

    void createTask(User user, Task task);

    List<Task> getAll(User user);

    List<Task> getRange(User user, int offset, int count);

    List<Task> getForTitle(User user, String title);

    void deleteTask(Task task);
}
