/*******************************************************************************
 * Copyright (c) 2002, 2007 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.resources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.eclipse.rap.rwt.internal.resources.ResourceRegistry.ResourceRegistration;
import org.eclipse.rap.rwt.resources.IResourceManager;
import org.eclipse.rap.rwt.resources.ResourceLoader;


public class ResourceRegistry_Test extends TestCase {

  private IResourceManager resourceManager;
  private ResourceRegistry resourceRegistry;

  public void testAdd() {
    String resourceName = "name";
    ResourceLoader resourceLoader = mock( ResourceLoader.class );
    resourceRegistry.add( resourceName, resourceLoader );
    
    ResourceRegistration resourceRegistration = resourceRegistry.getResourceRegistrations()[ 0 ];
    
    assertEquals( resourceName, resourceRegistration.getResourceName() );
    assertEquals( resourceLoader, resourceRegistration.getResourceLoader() );
  }
  
  public void testClear() {
    resourceRegistry.add( "name", mock( ResourceLoader.class ) );

    resourceRegistry.clear();
    
    assertEquals( 0, resourceRegistry.getResourceRegistrations().length );
  }
  
  @SuppressWarnings( "resource" )
  public void testRegisterResources() throws IOException {
    String resourceName = "name";
    InputStream inputStream = mock( InputStream.class );
    ResourceLoader resourceLoader = mock( ResourceLoader.class );
    when( resourceLoader.getResourceAsStream( resourceName ) ).thenReturn( inputStream );
    resourceRegistry.add( resourceName, resourceLoader );

    resourceRegistry.registerResources();
    
    verify( resourceManager ).register( resourceName, inputStream );
    assertEquals( 0, resourceRegistry.getResourceRegistrations().length );
  }
  
  public void testRegisterResourcesWithCorruptResourceLoader() {
    String resourceName = "resource-name";
    ResourceLoader resourceLoader = mock( ResourceLoader.class );
    resourceRegistry.add( resourceName, resourceLoader );

    try {
      resourceRegistry.registerResources();
      fail();
    } catch( IllegalStateException expected ) {
      assertTrue( expected.getMessage().contains( resourceName ) );
    }
  }

  protected void setUp() throws Exception {
    resourceManager = mock( IResourceManager.class );
    resourceRegistry = new ResourceRegistry( resourceManager );
  }
}
