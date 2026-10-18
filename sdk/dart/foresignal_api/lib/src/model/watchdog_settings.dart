//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/watchdog_settings_canary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'watchdog_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WatchdogSettings {
  /// Returns a new [WatchdogSettings] instance.
  WatchdogSettings({

     this.heartbeatUrl,

     this.failUrl,

     this.interval = '60s',

     this.method,

     this.canary,
  });

      /// Pinged every interval while readiness is UP (Healthchecks.io, Uptime Kuma push)
  @JsonKey(
    
    name: r'heartbeatUrl',
    required: false,
    includeIfNull: false,
  )


  final String? heartbeatUrl;



      /// Pinged instead while not ready (optional)
  @JsonKey(
    
    name: r'failUrl',
    required: false,
    includeIfNull: false,
  )


  final String? failUrl;



      /// Ping interval (≥ 30s). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    defaultValue: '60s',
    name: r'interval',
    required: false,
    includeIfNull: false,
  )


  final String? interval;



  @JsonKey(
    
    name: r'method',
    required: false,
    includeIfNull: false,
  )


  final WatchdogSettingsMethodEnum? method;



  @JsonKey(
    
    name: r'canary',
    required: false,
    includeIfNull: false,
  )


  final WatchdogSettingsCanary? canary;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WatchdogSettings &&
      other.heartbeatUrl == heartbeatUrl &&
      other.failUrl == failUrl &&
      other.interval == interval &&
      other.method == method &&
      other.canary == canary;

    @override
    int get hashCode =>
        (heartbeatUrl == null ? 0 : heartbeatUrl.hashCode) +
        (failUrl == null ? 0 : failUrl.hashCode) +
        interval.hashCode +
        method.hashCode +
        canary.hashCode;

  factory WatchdogSettings.fromJson(Map<String, dynamic> json) => _$WatchdogSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$WatchdogSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum WatchdogSettingsMethodEnum {
@JsonValue(r'GET')
GET(r'GET'),
@JsonValue(r'POST')
POST(r'POST');

const WatchdogSettingsMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


