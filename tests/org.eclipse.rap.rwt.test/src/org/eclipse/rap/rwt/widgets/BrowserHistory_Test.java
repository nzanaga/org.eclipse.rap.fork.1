/*******************************************************************************
 * Copyright (c) 2009, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.widgets;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.rap.rwt.IBrowserHistory;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.events.BrowserHistoryEvent;
import org.eclipse.rap.rwt.events.BrowserHistoryListener;
import org.eclipse.rap.rwt.internal.protocol.ProtocolTestUtil;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CallOperation;
import org.eclipse.swt.widgets.Display;
import org.json.JSONArray;
import org.json.JSONException;
import org.mockito.ArgumentCaptor;


public class BrowserHistory_Test extends TestCase {

  private static final String TYPE = "rwt.client.BrowserHistory";
  private Display display;

  @Override
  protected void setUp() throws Exception {
    Fixture.setUp();
    display = new Display();
    Fixture.fakeNewRequest( display );
  }

  @Override
  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  public void testCreateEntry() {
    IBrowserHistory history = RWT.getBrowserHistory();
    try {
      history.createEntry( null, "name" );
      fail( "BrowserHistory#mark must not allow id == null" );
    } catch( IllegalArgumentException e ) {
      // expected
    }
    try {
      history.createEntry( "", "name" );
      fail( "BrowserHistory#mark must not id to be an empty string" );
    } catch( IllegalArgumentException e ) {
      // expected
    }
    try {
      history.createEntry( null, null );
      fail( "BrowserHistory#mark must not allow null for name or title" );
    } catch( IllegalArgumentException e ) {
      // expected
    }
  }

  public void testAddBrowserHistoryListener() {
    final IBrowserHistory history = RWT.getBrowserHistory();
    try {
      history.addBrowserHistoryListener( null );
      fail( "BrowserHistory#addBrowserHistoryListener must not allow null" );
    } catch( IllegalArgumentException e ) {
      // expected
    }
  }

  public void testRemoveBrowserHistoryListener() {
    final IBrowserHistory history = RWT.getBrowserHistory();
    try {
      history.removeBrowserHistoryListener( null );
      fail( "BrowserHistory#removeBrowserHistoryListener must not allow null" );
    } catch( IllegalArgumentException e ) {
      // expected
    }
  }

  public void testFireNavigationEvent() {
    BrowserHistoryListener listener = mock( BrowserHistoryListener.class );
    IBrowserHistory history = RWT.getBrowserHistory();
    history.addBrowserHistoryListener( listener );

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put( "entryId", "foo" );
    Fixture.fakeNotifyOperation( TYPE, "historyNavigated", parameters  );
    Fixture.executeLifeCycleFromServerThread();

    ArgumentCaptor<BrowserHistoryEvent> captor
      = ArgumentCaptor.forClass( BrowserHistoryEvent.class );
    verify( listener, times( 1 ) ).navigated( captor.capture() );
    BrowserHistoryEvent event = captor.getValue();
    assertEquals( "foo", event.entryId );
  }

  public void testRenderCreate() {
    RWT.getBrowserHistory();

    Fixture.executeLifeCycleFromServerThread();

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findCreateOperation( TYPE ) );
  }

  public void testRenderCreate_OnlyOnce() {
    RWT.getBrowserHistory();

    Fixture.executeLifeCycleFromServerThread();
    Fixture.fakeNewRequest( display );
    Fixture.executeLifeCycleFromServerThread();

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findCreateOperation( TYPE ) );
  }

  public void testRenderAddNavigationListener() {
    Fixture.fakePhase( PhaseId.READ_DATA );
    ProcessActionRunner.add( new Runnable() {
      public void run() {
        RWT.getBrowserHistory().addBrowserHistoryListener( new BrowserHistoryListener() {
          public void navigated( BrowserHistoryEvent event ) {
          }
        } );
      }
    } );

    Fixture.executeLifeCycleFromServerThread();

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findListenProperty( TYPE, "navigation" ) );
  }

  public void testRenderRemoveNavigationListener() {
    final BrowserHistoryListener listener = new BrowserHistoryListener() {
      public void navigated( BrowserHistoryEvent event ) {
      }
    };
    RWT.getBrowserHistory().addBrowserHistoryListener( listener );
    Fixture.fakePhase( PhaseId.READ_DATA );
    ProcessActionRunner.add( new Runnable() {
      public void run() {
        RWT.getBrowserHistory().removeBrowserHistoryListener( listener );
      }
    } );

    Fixture.executeLifeCycleFromServerThread();

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findListenProperty( TYPE, "navigation" ) );
  }

  public void testRenderNavigationListenerUnchanged() {
    RWT.getBrowserHistory().addBrowserHistoryListener( new BrowserHistoryListener() {
      public void navigated( BrowserHistoryEvent event ) {
      }
    } );

    Fixture.executeLifeCycleFromServerThread();

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( TYPE, "navigation" ) );
  }

  public void testRenderAdd() throws JSONException {
    RWT.getBrowserHistory().createEntry( "testId", "testText" );

    Fixture.executeLifeCycleFromServerThread();

    Message message = Fixture.getProtocolMessage();
    CallOperation operation = message.findCallOperation( TYPE, "add" );
    JSONArray entries = ( JSONArray )operation.getProperty( "entries" );
    JSONArray actual1 = entries.getJSONArray( 0 );
    assertTrue( ProtocolTestUtil.jsonEquals( "[\"testId\",\"testText\"]", actual1 ) );
  }

  public void testRenderAdd_NoEntries() {
    RWT.getBrowserHistory().createEntry( "testId", "testText" );

    Fixture.executeLifeCycleFromServerThread();
    Fixture.fakeNewRequest( display );
    Fixture.executeLifeCycleFromServerThread();

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findCallOperation( TYPE, "add" ) );
  }

  public void testRenderAddOrder() throws JSONException {
    RWT.getBrowserHistory().createEntry( "testId1", "testText1" );
    RWT.getBrowserHistory().createEntry( "testId2", "testText2" );

    Fixture.executeLifeCycleFromServerThread();

    Message message = Fixture.getProtocolMessage();
    CallOperation operation = message.findCallOperation( TYPE, "add" );
    JSONArray entries = ( JSONArray )operation.getProperty( "entries" );
    JSONArray actual1 = entries.getJSONArray( 0 );
    assertTrue( ProtocolTestUtil.jsonEquals( "[\"testId1\",\"testText1\"]", actual1 ) );
    JSONArray actual2 = entries.getJSONArray( 1 );
    assertTrue( ProtocolTestUtil.jsonEquals( "[\"testId2\",\"testText2\"]", actual2 ) );
  }
}
