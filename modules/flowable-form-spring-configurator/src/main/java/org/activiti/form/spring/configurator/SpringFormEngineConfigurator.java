/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.form.spring.configurator;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.cfg.AbstractProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.form.engine.FormEngine;
import org.activiti.form.engine.deployer.FormDeployer;
import org.activiti.form.spring.SpringFormEngineConfiguration;
import org.activiti.spring.SpringProcessEngineConfiguration;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class SpringFormEngineConfigurator extends AbstractProcessEngineConfigurator {
  
  protected SpringFormEngineConfiguration formEngineConfiguration;
  
  @Override
  public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    
    // Custom deployers need to be added before the process engine boots
    List<Deployer> deployers = null;
    if (processEngineConfiguration.getCustomPostDeployers() != null) {
      deployers = processEngineConfiguration.getCustomPostDeployers();
    } else {
      deployers = new ArrayList<Deployer>();
    }
    deployers.add(new FormDeployer());
    processEngineConfiguration.setCustomPostDeployers(deployers);
    
  }
  
  @Override
  public void configure(ProcessEngineConfigurationImpl processEngineConfiguration) {
    if (formEngineConfiguration == null) {
      formEngineConfiguration = new SpringFormEngineConfiguration();
    }
    
    if (processEngineConfiguration.getDataSource() != null) {
      DataSource originalDatasource = processEngineConfiguration.getDataSource();
      formEngineConfiguration.setDataSource(originalDatasource);
      
    } else {
      throw new ActivitiException("A datasource is required for initializing the Form engine ");
    }
    
    formEngineConfiguration.setTransactionManager(((SpringProcessEngineConfiguration) processEngineConfiguration).getTransactionManager());
    
    formEngineConfiguration.setDatabaseCatalog(processEngineConfiguration.getDatabaseCatalog());
    formEngineConfiguration.setDatabaseSchema(processEngineConfiguration.getDatabaseSchema());
    formEngineConfiguration.setDatabaseSchemaUpdate(processEngineConfiguration.getDatabaseSchemaUpdate());
    
    FormEngine formEngine = initFormEngine();
    processEngineConfiguration.setFormEngineInitialized(true);
    processEngineConfiguration.setFormEngineRepositoryService(formEngine.getFormRepositoryService());
    processEngineConfiguration.setFormEngineFormService(formEngine.getFormService());
  }

  protected synchronized FormEngine initFormEngine() {
    if (formEngineConfiguration == null) {
      throw new ActivitiException("FormEngineConfiguration is required");
    }
    
    return formEngineConfiguration.buildFormEngine();
  }

  public SpringFormEngineConfiguration getFormEngineConfiguration() {
    return formEngineConfiguration;
  }

  public SpringFormEngineConfigurator setFormEngineConfiguration(SpringFormEngineConfiguration formEngineConfiguration) {
    this.formEngineConfiguration = formEngineConfiguration;
    return this;
  }

}