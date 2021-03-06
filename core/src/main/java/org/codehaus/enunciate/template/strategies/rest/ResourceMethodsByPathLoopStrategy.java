/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.template.strategies.rest;

import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.doc.ExcludeFromDocumentation;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import org.codehaus.enunciate.util.ResourceMethodPathComparator;

import java.util.*;

/**
 * Strategy for looping through all sets of resource methods, grouped by path.
 *
 * @author Ryan Heaton
 */
public class ResourceMethodsByPathLoopStrategy extends EnunciateTemplateLoopStrategy<List<ResourceMethod>> {

  private String var = "resources";
  private boolean includeExcludedFromDocs = false;
  private boolean includeExcludedFromIDL = false;

  protected Iterator<List<ResourceMethod>> getLoop(TemplateModel model) throws TemplateException {
    TreeMap<String, List<ResourceMethod>> resourcesByPath = new TreeMap<String, List<ResourceMethod>>(new ResourceMethodPathComparator());

    for (RootResource rootResource : getModel().getRootResources()) {
      for (ResourceMethod resource : rootResource.getResourceMethods(true)) {
        ExcludeFromDocumentation excludeFromDocs = resource.getAnnotation(ExcludeFromDocumentation.class);
        if (excludeFromDocs == null || this.includeExcludedFromDocs) {
          if (excludeFromDocs != null && excludeFromDocs.excludeFromIDL() && !this.includeExcludedFromIDL) {
            continue;
          }

          String path = resource.getFullpath();
          List<ResourceMethod> resourceList = resourcesByPath.get(path);
          if (resourceList == null) {
            resourceList = new ArrayList<ResourceMethod>();
            resourcesByPath.put(path, resourceList);
          }

          resourceList.add(resource);
        }
      }
    }

    return resourcesByPath.values().iterator();
  }

  @Override
  protected void setupModelForLoop(TemplateModel model, List<ResourceMethod> resources, int index) throws TemplateException {
    super.setupModelForLoop(model, resources, index);

    if (this.var != null) {
      getModel().setVariable(var, resources);
    }
  }

  /**
   * The variable into which to store the current REST endpoint.
   *
   * @return The variable into which to store the current REST endpoint.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable into which to store the current REST endpoint.
   *
   * @param var The variable into which to store the current REST endpoint.
   */
  public void setVar(String var) {
    this.var = var;
  }

  /**
   * Whether to include resource methods that have been excluded from the docs.
   *
   * @return Whether to include resource methods that have been excluded from the docs.
   * @see org.codehaus.enunciate.doc.ExcludeFromDocumentation
   */
  public boolean isIncludeExcludedFromDocs() {
    return includeExcludedFromDocs;
  }

  /**
   * Whether to include resource methods that have been excluded from the docs.
   *
   * @param includeExcludedFromDocs Whether to include resource methods that have been excluded from the docs.
   */
  public void setIncludeExcludedFromDocs(boolean includeExcludedFromDocs) {
    this.includeExcludedFromDocs = includeExcludedFromDocs;
  }

  /**
   * Whether to include resource methods that have been excluded from the IDL.
   *
   * @return Whether to include resource methods that have been excluded from the IDL.
   * @see org.codehaus.enunciate.doc.ExcludeFromDocumentation#excludeFromIDL() 
   */
  public boolean isIncludeExcludedFromIDL() {
    return includeExcludedFromIDL;
  }

  /**
   * Whether to include resource methods that have been excluded from the IDL.
   *
   * @param includeExcludedFromIDL Whether to include resource methods that have been excluded from the IDL.
   */
  public void setIncludeExcludedFromIDL(boolean includeExcludedFromIDL) {
    this.includeExcludedFromIDL = includeExcludedFromIDL;
  }
}