/*******************************************************************************
 * Copyright (c) 2011 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 ******************************************************************************/
package org.eclipse.rwt.internal.textsize;

import junit.framework.TestCase;

import org.eclipse.rwt.Fixture;
import org.eclipse.rwt.lifecycle.PhaseId;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;


public class TextSizeRecalculation_Test extends TestCase {

  private Shell shell;
  private Composite scrolledCompositeContent;
  private ResizeListener shellResizeListener;
  private ResizeListener scrolledCompositeContentResizeListener;
  
  private final class ResizeListener implements ControlListener {
    private int resizeCount;
    
    public void controlResized( ControlEvent e ) {
      resizeCount++;
    }

    public void controlMoved( ControlEvent e ) {
    }

    public int resizeCount() {
      return resizeCount;
    }
  }
 
  public void testExecute() {
    createWidgetTree();
    registerResizeListeners();
    turnOnImmediateResizeEventHandling();
    TextSizeRecalculation recalculation = new TextSizeRecalculation();
    
    recalculation.execute();
  
    checkResizeTookPlace();
  }
  
  protected void setUp() throws Exception {
    Fixture.setUp();
  }
  
  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }

  private void checkResizeTookPlace() {
    assertEquals( getInitialShellBounds(), shell.getBounds() );
    assertEquals( getInitialCompositeBounds(), scrolledCompositeContent.getBounds() );
    assertEquals( 2, shellResizeListener.resizeCount() );
    assertEquals( 2, scrolledCompositeContentResizeListener.resizeCount() );
  }

  private Rectangle getInitialCompositeBounds() {
    return new Composite( new Shell(), SWT.NONE ).getBounds();
  }

  private Rectangle getInitialShellBounds() {
    return new Shell().getBounds();
  }

  private void createWidgetTree() {
    Display display = new Display();
    shell = new Shell( display );
    shell.setLayout( new FillLayout() );
    ScrolledComposite scrolledComposite = new ScrolledComposite( shell, SWT.NONE );
    scrolledCompositeContent = new Composite( scrolledComposite, SWT.NONE );
    scrolledComposite.setContent( scrolledCompositeContent );
  }

  private void registerResizeListeners() {
    shellResizeListener = new ResizeListener();
    scrolledCompositeContentResizeListener = new ResizeListener();
    shell.addControlListener( shellResizeListener );
    scrolledCompositeContent.addControlListener( scrolledCompositeContentResizeListener );
  }

  private void turnOnImmediateResizeEventHandling() {
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
  }
}
