package com.dassion.v2.pay_plugin;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import com.alipay.sdk.app.PayTask;

import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * PayPlugin
 */
public class PayPlugin implements MethodCallHandler {

    private static final int PAY_REQUEST_ID = 0x801;

    private Registrar mRegistrar;
    private Runnable alipayRunnable;


    private PayPlugin(Registrar registrar) {
        mRegistrar = registrar;
        mRegistrar.addRequestPermissionsResultListener(new PayRequestPermissionsListener());
    }

    private class PayRequestPermissionsListener
            implements PluginRegistry.RequestPermissionsResultListener {
        @Override
        public boolean onRequestPermissionsResult(int id, String[] permissions, int[] grantResults) {
            if (id == PAY_REQUEST_ID) {
                startAlipay();
                return true;
            }
            return false;
        }
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "pay_plugin");
        channel.setMethodCallHandler(new PayPlugin(registrar));
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equalsIgnoreCase("alipay")) {
            alipay(mRegistrar.activity(), (String) call.argument("info"), result);
        } else {
            result.notImplemented();
        }
    }

    public void alipay(final Activity currentActivity, final String payInfo, final Result callback) {
        alipayRunnable = new Runnable() {
            @Override
            public void run() {
                alipayRunnable = null;
                try {
                    PayTask alipay = new PayTask(currentActivity);
                    Map<String, String> result = alipay.payV2(payInfo, true);

                    callback.success(result);
                } catch (Exception e) {
                    callback.error(e.getMessage(), "支付发生错误", e);
                }
            }
        };
        if (hasPayPermission()) {
            startAlipay();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mRegistrar
                        .activity()
                        .requestPermissions(
                                new String[]{Manifest.permission.READ_PHONE_STATE},
                                PAY_REQUEST_ID);
            }
        }

    }

    private void startAlipay() {
        if (alipayRunnable == null) {
            return;
        }
        Thread payThread = new Thread(alipayRunnable);
        payThread.start();
    }

    private boolean hasPayPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || mRegistrar.activity().checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED;
    }
}
