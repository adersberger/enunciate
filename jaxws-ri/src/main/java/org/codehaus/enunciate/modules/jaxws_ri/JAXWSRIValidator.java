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

package org.codehaus.enunciate.modules.jaxws_ri;

import org.codehaus.enunciate.contract.jaxws.EndpointImplementation;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.contract.jaxws.WebParam;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Validator for the jaxws-ri module.
 *
 * @author Ryan Heaton
 */
public class JAXWSRIValidator extends BaseValidator {

  private final HashMap<String, EndpointInterface> visitedEndpoints = new HashMap<String, EndpointInterface>();

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);

    EndpointInterface visited = visitedEndpoints.put(ei.getServiceName(), ei);
    if (visited != null) {
      if (visited.getTargetNamespace().equals(ei.getTargetNamespace())) {
        result.addError(ei, "Ummm... you already have a service named " + ei.getServiceName() + " at " +
          visited.getPosition() + ".  You need to disambiguate.");
      }
    }

    for (WebMethod webMethod : ei.getWebMethods()) {
      for (WebParam webParam : webMethod.getWebParameters()) {
        if ((webParam.isHeader()) && ("".equals(webParam.getAnnotation(javax.jws.WebParam.class).name()))) {
          //todo: lift this constraint by serializing the parameter names to some file you can load for metadata...
          result.addError(webParam, "For now, Enunciate requires you to specify a 'name' on the @WebParam annotation if it's a header.");
        }
      }
    }

    if (ei.getEndpointImplementations().size() > 1) {
      ArrayList<String> impls = new ArrayList<String>();
      for (EndpointImplementation impl : ei.getEndpointImplementations()) {
        impls.add(impl.getQualifiedName());
      }
      result.addError(ei, "Sorry, JAXWS-RI module doesn't support two endpoint implementations for interface '" + ei.getQualifiedName() +
        "'.  Found " + ei.getEndpointImplementations().size() + " implementations (" + impls.toString() + ").");
    }
    else if (ei.getEndpointImplementations().isEmpty()) {
      result.addError(ei, "JAXWS-RI module requires an implementation for each service interface.");
    }

    return result;
  }

}
