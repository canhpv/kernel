/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.container;

import org.exoplatform.container.multitenancy.bridge.TenantContainerContext;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;

import java.util.ArrayList;
import java.util.List;

/**
 * TenantContainer separate generally used components from ones what should be instantiated,
 * started and stopped on per-tenant basis.<br>
 * It overrides component getters, {@link #registerComponent(ComponentAdapter)} and
 * {@link #unregisterComponent(Object)} methods to get components taking in account Current Tenant
 * context.<br>
 * The Current Tenant context it's an abstraction what will be set by actual Multitenancy implementation (for
 * versions currently in production it's based on JCR Current Repository, but this should be transparent for
 * Kernel level and implementation can be changed in future).
 * 
 * 
 */
public class TenantContainer extends CachingContainer
{

   private static final long serialVersionUID = 1945046643718969920L;

   protected TenantContainerContext tenantsContainerContext;

   public TenantContainer(ComponentAdapterFactory componentAdapterFactory, PicoContainer parent)
   {
      super(componentAdapterFactory, parent);
   }

   public TenantContainer(PicoContainer parent)
   {
      super(parent);
   }

   public TenantContainer(ComponentAdapterFactory componentAdapterFactory)
   {
      super(componentAdapterFactory);
   }

   public TenantContainer()
   {
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings({"rawtypes"})
   @Override
   public ComponentAdapter getComponentAdapterOfType(Class componentType)
   {
      if (tenantsContainerContext != null && tenantsContainerContext.accept(componentType))
      {
         return tenantsContainerContext.getComponentAdapterOfType(componentType);
      }
      return super.getComponentAdapterOfType(componentType);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getComponentInstance(Object componentKey) throws PicoException
   {
      if (tenantsContainerContext != null && tenantsContainerContext.accept(componentKey))
      {
         return tenantsContainerContext.getComponentInstance(componentKey);
      }
      return super.getComponentInstance(componentKey);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   @Override
   public List getComponentAdaptersOfType(Class componentType)
   {
      List result = new ArrayList();
      result.addAll(super.getComponentAdaptersOfType(componentType));

      if (tenantsContainerContext != null && tenantsContainerContext.accept(componentType))
      {
         List adapters = tenantsContainerContext.getComponentAdaptersOfType(componentType);
         if (adapters != null)
         {
            result.addAll(adapters);
         }
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   @Override
   public List getComponentInstancesOfType(Class componentType) throws PicoException
   {
      // XXX: order of components in the list broken as we taking it from two sources
      List result = new ArrayList();
      result.addAll(super.getComponentInstancesOfType(componentType));

      if (tenantsContainerContext != null && tenantsContainerContext.accept(componentType))
      {
         List instances = tenantsContainerContext.getComponentInstancesOfType(componentType);
         if (instances != null)
         {
            result.addAll(instances);
         }
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings({"rawtypes"})
   @Override
   public Object getComponentInstanceOfType(Class componentType)
   {
      if (tenantsContainerContext != null && tenantsContainerContext.accept(componentType))
      {
         return tenantsContainerContext.getComponentInstanceOfType(componentType);
      }
      return super.getComponentInstanceOfType(componentType);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ComponentAdapter registerComponent(ComponentAdapter componentAdapter)
      throws DuplicateComponentKeyRegistrationException
   {
      if (tenantsContainerContext != null && tenantsContainerContext.accept(componentAdapter))
      {
         ComponentAdapter contextAdapter = tenantsContainerContext.registerComponent(componentAdapter);
         // check if the same adapter returned, if not - register the new in the super also 
         if (contextAdapter != null)
         {
            return contextAdapter;
         }
         else
         {
            return super.registerComponent(contextAdapter);
         }
      }
      else
      {
         return super.registerComponent(componentAdapter);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ComponentAdapter unregisterComponent(Object componentKey)
   {
      ComponentAdapter adapter = getComponentAdapter(componentKey);
      if (tenantsContainerContext != null && tenantsContainerContext.accept(adapter))
      {
         adapter = tenantsContainerContext.unregisterComponent(componentKey);
         if (adapter != null)
         {
            return adapter;
         }
      }

      return super.unregisterComponent(componentKey);
   }

}
