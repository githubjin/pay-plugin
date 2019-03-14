import 'dart:async';

import 'package:flutter/services.dart';

class PayPlugin {
  static const MethodChannel _channel = const MethodChannel('pay_plugin');

  /// 阿里支付
  static Future<dynamic> alipay(String payinfo) {
    return _channel.invokeMethod("alipay", {"info": payinfo});
  }

}
