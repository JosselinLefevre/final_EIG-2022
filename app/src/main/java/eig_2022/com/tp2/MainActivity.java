package eig_2022.com.tp2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.UUID;

import static android.R.attr.radius;
import static java.lang.Integer.parseInt;
import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private ConnectThread connectThread;
    private final static int REQUEST_CODE_ENABLE_BT = 1;
    private final Context context = this;
    BTManagerTask boundedDevicesTask;
    Menu mMenu;
    float initX, initY;
    MySurfaceView mySurfaceView;
    CircularProgressBar progressBar;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mySurfaceView = new MySurfaceView(this);

        LinearLayout layout = (LinearLayout) findViewById(R.id.tada);
        //layout.addView(mySurfaceView);

        Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBlueTooth, REQUEST_CODE_ENABLE_BT);

        SeekBar speedProgress = (SeekBar) findViewById(R.id.speed_progress);
        speedProgress.setMax(9);
        speedProgress.setProgress(speedProgress.getMax() / 2);

        speedProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sendFrequencyToThread(progress * 10);
                Log.wtf("progressBar", progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sendFrequencyToThread(speedProgress.getMax() / 2);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        boundedDevicesTask = new BTManagerTask(context, spinner, MainActivity.this);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        View[] v = {findViewById(R.id.upp), findViewById(R.id.down), findViewById(R.id.left), findViewById(R.id.right)};
        instantiateDirections(v);

        SurfaceView touchpad = (SurfaceView) findViewById(R.id.touchpad);
        touchpad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getActionMasked();

                switch (action) {

                    case MotionEvent.ACTION_DOWN:
                        initX = event.getX();
                        initY = event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:

                        float finalX = event.getX();
                        float finalY = event.getY();

                        if (finalX >= (initX + 100)) {
                            Log.wtf("Right true", initX + "=>" + finalX);

                            displaySpeedPercent(initX, finalX);
                            v.setTag(2);
                            sendDirectionToThread(v);
                        }

                        if (finalX <= (initX - 100)) {
                            Log.wtf("Left true", initX + "=>" + finalX);

                            displaySpeedPercent(initX, finalX);
                            v.setTag(1);
                            sendDirectionToThread(v);
                        }

                        if (finalY >= (initY + 100)) {
                            Log.wtf("Down true", initY + "=>" + finalY);

                            displaySpeedPercent(initY, finalY);
                            v.setTag(3);
                            sendDirectionToThread(v);
                        }

                        if (finalY <= (initY - 100)) {
                            Log.wtf("Up true", initY + "=>" + finalY);

                            displaySpeedPercent(initY, finalY);
                            v.setTag(0);
                            sendDirectionToThread(v);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        sendDirectionToThread(null);
                        progressBar.setProgress(0);
                        textView.setText(0+"%");
                        break;
                }
                return true;
            }
        });

        progressBar = (CircularProgressBar) findViewById(R.id.speed_level);

        textView = (TextView) findViewById(R.id.percent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_buttons, menu);
        findViewById(R.id.touchpad).setVisibility(View.GONE);
        findViewById(R.id.speed_layout).setVisibility(View.GONE);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                createBoundedDevicesTask((Spinner) findViewById(R.id.spinner));
                if (connectThread != null) {
                    connectThread.cancel();
                }
                return true;

            case R.id.action_touch:

                mMenu.clear();
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.menu_touch, mMenu);
                findViewById(R.id.gamepad).setVisibility(View.GONE);
                findViewById(R.id.touchpad).setVisibility(View.VISIBLE);
                findViewById(R.id.speed_progress).setVisibility(View.GONE);
                findViewById(R.id.speed_layout).setVisibility(View.VISIBLE);
                return true;

            case R.id.action_button:

                mMenu.clear();
                inflater = getMenuInflater();
                inflater.inflate(R.menu.menu_buttons, mMenu);
                findViewById(R.id.gamepad).setVisibility(View.VISIBLE);
                findViewById(R.id.touchpad).setVisibility(View.GONE);
                findViewById(R.id.speed_progress).setVisibility(View.VISIBLE);
                findViewById(R.id.speed_layout).setVisibility(View.GONE);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE_ENABLE_BT == requestCode) {
            if (resultCode == RESULT_OK) {

                final Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), R.string.bt_enable, Snackbar.LENGTH_LONG);
                snackbar.show();
                createBoundedDevicesTask((Spinner) findViewById(R.id.spinner));
            } else {
                Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBlueTooth, REQUEST_CODE_ENABLE_BT);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.wtf("OTS", parent.getAdapter().getItem(position).toString());
        retrieveDeviceFromSpinner(parent.getAdapter().getItem(position).toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void retrieveDeviceFromSpinner(String _name) {
        for (BluetoothDevice boundedDevice : boundedDevicesTask.getBoundedDevice()) {
            if (_name.equals(boundedDevice.getName())) {
                createThread(boundedDevice);
            }
        }
    }

    public void displaySpeedPercent(float _init, float _final){

        CircularProgressBar progressBar = (CircularProgressBar) findViewById(R.id.speed_level);
        TextView textView = (TextView) findViewById(R.id.percent);

        int percent = Math.round((abs(_final - _init) - 100) / 3);
        progressBar.setProgress(percent);
        if (percent <= 100) {
            textView.setText(percent + "%");
            sendFrequencyToThread(Math.abs((9-(Math.abs((percent/10)-10))))*10);
        }
        else {
            textView.setText(100 + "%");
        }
    }

    public void sendDirectionToThread(View v) {
        if (connectThread != null) {
            if (v == null) {
                connectThread.setDirection(4);
                return;
            }
            connectThread.setDirection(parseInt(v.getTag().toString()));
        }
    }

    public void sendFrequencyToThread(int _frequency) {
        if (connectThread != null) {
            connectThread.setFrequency((_frequency));
        }
    }

    public void createThread(BluetoothDevice _selectedDevice) {
        if (_selectedDevice != null) {
            connectThread = new ConnectThread(_selectedDevice, UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            connectThread.start();
        }
    }

    public void createBoundedDevicesTask(Spinner _spinner) {
        boundedDevicesTask = new BTManagerTask(context, _spinner, MainActivity.this);
        boundedDevicesTask.execute();
    }

    public void showProgress(final boolean show, final Spinner _spinner) {
        int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.spinner_progress);

        _spinner.setVisibility(show ? View.GONE : View.VISIBLE);

        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void instantiateDirections(View[] views) {
        for (View view : views) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.setBackgroundResource(R.drawable.ic_gamepadtouch_black_24dp);
                        sendDirectionToThread(v);

                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        v.setBackgroundResource(R.drawable.ic_empty_gamepadtouch_black_24dp);
                        sendDirectionToThread(null);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mySurfaceView.onResumeMySurfaceView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mySurfaceView.onPauseMySurfaceView();
    }
}

class MySurfaceView extends SurfaceView implements Runnable {

    Thread thread = null;
    SurfaceHolder surfaceHolder;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    volatile boolean touched = false;
    volatile float touched_x = 0, touched_y = 0;

    public MySurfaceView(Context context) {
        super(context);
        surfaceHolder = getHolder();
    }

    public void onResumeMySurfaceView() {
        thread = new Thread(this);
        thread.start();
    }

    public void onPauseMySurfaceView() {
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void onDraw(Canvas canvas) {
        paint.setARGB(255, 100, 200, 0);
        canvas.drawCircle(touched_x, touched_y, radius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        touched_x = event.getX();
        touched_y = event.getY();

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (surfaceHolder.getSurface().isValid()) {
                    //... actual drawing on canvas


                    Canvas canvas = surfaceHolder.lockCanvas();

                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(ContextCompat.getColor(super.getContext(), R.color.colorPrimaryExtremDark));
                    canvas.drawCircle(touched_x, touched_y, 40, paint);

                    paint.setColor(ContextCompat.getColor(super.getContext(), R.color.colorPrimary));
                    canvas.drawCircle(touched_x, touched_y, 30, paint);

                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
                touched = true;
                break;
            /*case MotionEvent.ACTION_MOVE:
                touched = true;
                break;*/
            case MotionEvent.ACTION_UP:

                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(ContextCompat.getColor(super.getContext(), R.color.colorAccent));
                surfaceHolder.unlockCanvasAndPost(canvas);

                touched = false;
                break;
            default:
        }
        return true;
    }

    @Override
    public void run() {
    }
}
