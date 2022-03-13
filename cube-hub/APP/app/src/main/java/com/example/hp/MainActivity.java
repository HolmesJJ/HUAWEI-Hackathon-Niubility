package com.example.hp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private BluetoothAdapter bluetoothAdapter;//蓝牙定义
    private final UUID MY_UUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket clientsocket;
    private BluetoothDevice device;
    private OutputStream os;
    private InputStream is;
    String address="20:18:12:17:16:31";
//    String address="20:18:12:17:18:18";
    Boolean isRun=true;

    private static MediaPlayer mp_succeed = new MediaPlayer();
    private static MediaPlayer mp_defeated = new MediaPlayer();

    SeekBar seekBar;

    Spinner spinner;
    TextView textView;
    TextView textViewTime;
    Button button;

    int choose;
    int time;
    String[] word={"Apple","Fly","Cry","Sky","Sign","Away","Okay","Flow","Crow","Allow"};
    String[] wordNum={"122145","273314","254614","292514","291442","104813","202513","274520","254620","104519"};
    String str="";


    Handler handlerUI = new Handler()
    {
        public void handleMessage(Message paramMessage)
        {
            int i= (int) paramMessage.obj;
            if(i==1){
                showSucceedDialog("");
            }
            if(i==2){
                showDefeatedBuilder("");
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = (Spinner)findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        textView = (TextView)findViewById(R.id.textView);
        textView.setText("");

        textViewTime = (TextView)findViewById(R.id.textView2);
        textViewTime.setText(""+0);

        button = (Button)findViewById(R.id.button2);
        button.setOnClickListener(this);



        seekBar= (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        textViewTime.setText("time："+0+"s");

        seekBar.setProgress(30);



        try {
            //R.raw.notice 是ogg格式的音频 放在res/raw/下
            AssetFileDescriptor afd = getApplicationContext().getResources().openRawResourceFd(R.raw.succeed);
            mp_succeed.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp_succeed.setAudioStreamType(AudioManager.STREAM_RING);
            afd.close();
            mp_succeed.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //R.raw.notice 是ogg格式的音频 放在res/raw/下
            AssetFileDescriptor afd = getApplicationContext().getResources().openRawResourceFd(R.raw.defeated);
            mp_defeated.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp_defeated.setAudioStreamType(AudioManager.STREAM_RING);
            afd.close();
            mp_defeated.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }




        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.enable();//静默打开蓝牙

        xiancheng xc=new xiancheng();
        Thread the = new Thread(xc); // Thread（线程） 此处用于创建线程
        the.start(); // 启动线程
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRun = false;
    }

    protected void onPause() {
        super.onPause();

    }


    public void showSucceedDialog(String message){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("");
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View myLoginView = layoutInflater.inflate(
                R.layout.dialog_succeed, null);
        dialog.setView(myLoginView);
        dialog.show();

        if(mp_succeed.isPlaying())
            mp_succeed.pause();
        mp_succeed.seekTo(0);
        mp_succeed.setVolume(1000, 1000);//设置声音
        mp_succeed.start();
    }

    public void showDefeatedBuilder(String message){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("");
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View myLoginView = layoutInflater.inflate(
                R.layout.dialog_defeated, null);
        dialog.setView(myLoginView);
        dialog.show();

        if(mp_defeated.isPlaying())
            mp_defeated.pause();
        mp_defeated.seekTo(0);
        mp_defeated.setVolume(1000, 1000);//设置声音
        mp_defeated.start();
    }

    void send(String str)
    {
        if(os!=null)
        {
            try
            {
                os.write(str.getBytes("GB2312"));
            }
            catch (UnsupportedEncodingException e)
            {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
            }
        }
    }






    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        textView.setText(word[position]);
        choose=position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.button2)
        {
           long timeStamp = System.currentTimeMillis()%6;
            send("A"+  timeStamp  +"B"+  (time>9? "":"0")+time  +"C"+  wordNum[choose] +  "D" );
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        textViewTime.setText("time："+progress+"s");
        time=progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    public static String bytesToHexString(int ChangDu,byte[] bytes) {
        String result = "";
        for (int i = 0; i < ChangDu; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }

    byte lastData=0x00;
    class xiancheng implements Runnable//蓝牙连接
    {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            try{
                if(bluetoothAdapter.isDiscovering()==false)//如果不搜索则执行连接
                {
                    try
                    {
                        device=bluetoothAdapter.getRemoteDevice(address);
                        if(clientsocket==null)
                        {
                            clientsocket=device.createRfcommSocketToServiceRecord(MY_UUID);
                        }
                        if(clientsocket.isConnected()==false)//未连接
                        {
                            clientsocket=device.createRfcommSocketToServiceRecord(MY_UUID);
                            clientsocket.connect();
//                            isconnected = true;

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_SHORT).show();
                                }
                            });
                            os=clientsocket.getOutputStream();
                            is=clientsocket.getInputStream();
                            isRun=true;


                            while(isRun){
                                String smsg="";

//                                str="";
                                byte[] buffer_z= new byte[1024];
                                byte[] buffer_z_new= new byte[1024];
                                try {

                                    int num = is.read(buffer_z);//buffer_z和buffer_z_new都是1024
                                    int n=0;
                                    for(int i=0;i<num;i++){
                                        buffer_z_new[n] = buffer_z[i];
                                        n++;
                                    }
                                    if((lastData&0xff) == 0x30 && (buffer_z_new[0]&0xff)==0x31)
                                    {
                                        Message msg = Message.obtain();
                                        msg.obj = 1;   //从这里把你想传递的数据放进去就行了
                                        handlerUI.sendMessage(msg);

                                    }
                                    if((lastData&0xff) == 0x30 && (buffer_z_new[0]&0xff)==0x32)
                                    {
                                        Message msg = Message.obtain();
                                        msg.obj = 2;   //从这里把你想传递的数据放进去就行了
                                        handlerUI.sendMessage(msg);
                                    }
                                    lastData=buffer_z_new[0];
                                    String s =bytesToHexString(n,buffer_z_new);//因为单片机发出的是16进制的，直接读取会是乱码，于是进行了转换。
                                    str+=s.trim()+"\n";
                                    Thread.sleep(1);
//                                    if(is.available()==0)break;//有时候会因为某些原因，会出现有数据但是位0的问题，可以尝试添加延时；
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                    catch(Exception e){}
                }
                if(clientsocket.isConnected()==false)//未连接
                {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Cannot Connect",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            catch(Exception e){
            }
        }
    }


}
