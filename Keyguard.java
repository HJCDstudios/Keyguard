package com.hjcd.keyguard.plugins.user;

import android.app.*;
import android.content.*;
import android.os.*;
import android.security.keystore.*;
import android.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.security.*;
import java.security.cert.*;
import javax.crypto.*;
import android.widget.*;

import android.view.*;
import javax.net.ssl.*;

public class Keyguard {
  public static String TAG = "Keyguard"; 
  public static float VERSION = 1.0f;   
  private Method m_callscript;
  private Method m_execscript;
  private Method m_getObject;
  private Object m_parent;
  private Context m_ctx;
  private String m_plugDir;
  private String m_filesDir;
  private KeyguardManager m_keyguard;
  private Activity m_act;
  private String[] cb = new String[10];
  private int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 101;
  public Keyguard() {
    Log.d(TAG,"Creating plugin object");
  }
  public void Init(Context ctx,Object parent) {
    try {
      m_ctx = ctx;
      m_act = (Activity) ctx;
      m_parent = parent;
      m_callscript = parent.getClass().getMethod( "CallScript", Bundle.class );
      m_execscript = parent.getClass().getMethod( "ExecScript", String.class );
      m_getObject = parent.getClass().getMethod( "GetObject", String.class );
      m_plugDir = m_ctx.getDir( "Plugins", 0 ).getAbsolutePath() + "/MyPlugin";
      m_filesDir = m_ctx.getExternalFilesDir(null).getPath();
      m_keyguard = (KeyguardManager) m_ctx.getSystemService(Context.KEYGUARD_SERVICE);
    } catch (Exception e) {
      Log.e(TAG,"Failed to Initialise plugin!",e);
    }
  }
  public void OnResume() { }
  public void OnPause() { }
  public void OnConfig() { }
  public void OnNewIntent( Intent intent ) {
    showPopup("Oh no, the intent went the wrong way");
  }
  public void OnActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode >= REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
      Bundle a = new Bundle();
      if (resultCode == m_act.RESULT_OK) {
        try {
          a.putString("cmd",cb[0]);
          CallScript(a);
        } catch (Exception e) {
          showPopup(e.toString());
        }
      } else {
        try {
          a.putString("cmd",cb[1]);
          CallScript(a);
        } catch (Exception e) {
          showPopup(e.toString());
        }
      }
    }
  }
  private void CallScript( Bundle b ) throws Exception {
    m_callscript.invoke( m_parent, b );
  }
  public void ExecScript( final String code )  throws Exception {
    m_execscript.invoke( m_parent, code );
  }
  public Object GetObject( final String id )  throws Exception {
    return m_getObject.invoke( m_parent, id );
  }
  public String CallPlugin(Bundle b) {
    String cmd = b.getString("cmd");
    String ret = null;
    switch(cmd) {
      case "Authenticate":
        cb[0] = b.getString("p3","");
        cb[1] = b.getString("p4","");
        showAuthenticationScreen(b.getString("p1",null),b.getString("p2",null));
        break;
      case "InKeyguardRestrictedInputMode":
        if (android.os.Build.VERSION.SDK_INT >= 28) {
          ret = ""+m_keyguard.isKeyguardLocked();
        } else {
          ret = ""+m_keyguard.inKeyguardRestrictedInputMode();
        }
        break;
      case "IsDeviceLocked":
        ret = ""+m_keyguard.isDeviceLocked();
        break;
      case "IsDeviceSecure":
        ret = ""+m_keyguard.isDeviceSecure();
        break;
      case "IsKeyguardLocked":
        ret = ""+m_keyguard.isKeyguardLocked();
        break;
      case "IsKeyguardSecure":
        ret = ""+m_keyguard.isKeyguardSecure();
        break;
      case "EnableScreenCapture":
        if (b.getString("p1","true").equals("false")) {
          m_act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        } else {
          m_act.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        break;
      default:
    }
    return ret;
  }
  private void showAuthenticationScreen(String title,String desc) {
    Intent intent = m_keyguard.createConfirmDeviceCredentialIntent(title, desc);
    if (intent != null) {
      m_act.startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
    }
  }
  private void showPopup(String txt) {
    Toast.makeText(m_ctx,txt,Toast.LENGTH_LONG).show();
  }
}
