   package example.phone;


import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		//unregisterReceiver(PL);
		super.onStop();		
	}
	// Call active notify vibrate  
	private Vibrator mVibrator;
	// Default vibrate time  
	private static final int VIBRATE_LENGTH = 100;

	private static final int PICK_CONTACT_SUBACTIVITY = 2; 
	//PhoneListener PL;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data); 
		switch (requestCode)
		{
		case PICK_CONTACT_SUBACTIVITY:
			Uri uriRet=null;
			try
			{
				uriRet=data.getData();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			if(uriRet!=null)
			{
				try
				{
					Cursor c = getContentResolver().query(uriRet, null, null, null, null); 
					
					c.moveToFirst();
					String strName="";
					String strPhone="";
					int contactId=c.getInt(c.getColumnIndex(ContactsContract.Contacts._ID));
					
					Cursor curContacts=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+contactId,null,null);
					if(curContacts.getCount()>0)
					{
						curContacts.moveToFirst();
						strName=curContacts.getString(curContacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
						strPhone=curContacts.getString(curContacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					}
					else
					{
						
					}
					TextView tv1=(TextView)findViewById(R.id.textView1);
					tv1.setText(strName +":"+ strPhone);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId())
		{
		case 0:
			openOptionDialog();
			break;
		case 1:
			exitOptionDialog();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button mButton1=(Button)findViewById(R.id.button1);
		Button mButton2=(Button)findViewById(R.id.button2);		
		
		OnClickListener a=new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Uri uri=Uri.parse("content://contacts/people");
				Intent intent =new Intent(Intent.ACTION_PICK,uri);
				startActivityForResult(intent,PICK_CONTACT_SUBACTIVITY);
			}
		};
		OnClickListener b=new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try
				{
					TextView tv=(TextView)findViewById(R.id.textView1);
					
					if(tv.getText().toString().length()>0)
					{

						//新建自己实现的PhoneStateListener
						exPhoneCallListener myPCL=new exPhoneCallListener();
						//取得电话服务
						TelephonyManager tm=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
						//注册Listener
						tm.listen(myPCL, PhoneStateListener.LISTEN_CALL_STATE);
						
						String strInput=tv.getText().toString().split(":")[1];
						//打电话开始
						Intent IntentDial=new Intent("android.intent.action.CALL",Uri.parse("tel:"+strInput));
						startActivity(IntentDial);
						tv.setText("");
					}
					else
					{
						tv.setText("");
						Toast.makeText(MainActivity.this, "请选择电话号码", Toast.LENGTH_LONG).show();
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		mButton1.setOnClickListener(a);
		mButton2.setOnClickListener(b);
	}
	public class exPhoneCallListener extends PhoneStateListener
	{

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			switch(state)
			{
			//可以通过查询通话记录中通话时长来判断是否接通。 
			case TelephonyManager.CALL_STATE_OFFHOOK:
				int oldnum=0;
				int newnum=0;
				ContentResolver cr = getContentResolver();
				Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI,
                         new String[]{CallLog.Calls.NUMBER,CallLog.Calls.DURATION},
                         null,null,CallLog.Calls.DEFAULT_SORT_ORDER);
				oldnum=cursor.getCount();
				while(true)
				{
					cursor = cr.query(CallLog.Calls.CONTENT_URI,
                             new String[]{CallLog.Calls.NUMBER,CallLog.Calls.DURATION},
                             null,null,CallLog.Calls.DEFAULT_SORT_ORDER);
					newnum=cursor.getCount();
					if(newnum>oldnum)
					{
						mVibrator=(Vibrator)getApplication().getSystemService(Service.VIBRATOR_SERVICE);
						mVibrator.vibrate(VIBRATE_LENGTH);
						break;
					}
				}
				break;
			case -1:
				exitOptionDialog();
				break;
				default:
					exitOptionDialog();
					break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.		
		menu.add(0,0,0,R.string.app_about);
		menu.add(0,1,1,R.string.str_exit);
		return super.onCreateOptionsMenu(menu);
	}
	
	private void openOptionDialog()
	{
		new AlertDialog.Builder(this).setTitle(R.string.app_about)
		.setMessage(R.string.app_about_msg)
		.setPositiveButton(R.string.str_OK, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		})
		.show();
	}
	private void exitOptionDialog()
	{
		new AlertDialog.Builder(this).setTitle(R.string.app_exit)
		.setMessage(R.string.app_exit_msg)
		.setNegativeButton(R.string.str_NO,  new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		})
		.setPositiveButton(R.string.str_OK, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				finish();
			}
		})
		.show();
	}
	
}
