package gamepkg;

// $Id$

import java.applet.*;
import java.net.*;
import java.util.*;

public class MultiApplet extends Applet
{
  public Vector applets = new Vector();

  public void addApplet( Applet applet ) {
    // Create a new, "artificial" stub for the subapplet
    AppletStub stub = new MultiAppletStub( this );
    // Give it to the subapplet to use
    applet.setStub( stub );
    // Start up the subapplet
    applet.init();
    // Finally, store it in our list
    applets.addElement( applet );
  }

  public Applet addApplet( String className ) 
      throws ClassNotFoundException, IllegalAccessException,
             InstantiationException {
    // Create an instance of the named applet
    Class clas = Class.forName( className );
    Applet applet = (Applet)clas.newInstance();
    // Add it to the system
    addApplet( applet );
    // Return it, in case the caller wants to access the Applet
    // object directly
    return applet;
  }

  public void start() {
    for (Enumeration e=applets.elements(); e.hasMoreElements();) {
      ((Applet)e.nextElement()).start();
    }
  }

  public void stop() {
    for (Enumeration e=applets.elements(); e.hasMoreElements();) {
      ((Applet)e.nextElement()).stop();
    }
  }

  public void destroy() {
    for (Enumeration e=applets.elements(); e.hasMoreElements();) {
      ((Applet)e.nextElement()).destroy();
    }
  }

  public void appletResize( int width, int height ) {
    System.out.println( "ach" );
  }
}

class MultiAppletStub implements AppletStub
{
  private Applet applet;

  public MultiAppletStub( Applet applet ) {
    this.applet = applet;
  }

  public boolean isActive() {
    return applet.isActive();
  }

  public URL getDocumentBase() {
    return applet.getDocumentBase();
  }

  public URL getCodeBase() {
	return applet.getCodeBase();
  }

  public String getParameter( String name ) {
    return applet.getParameter( name );
  }

  public AppletContext getAppletContext() {
	return applet.getAppletContext();
  }

  public void appletResize( int width, int height ) {
  }
}