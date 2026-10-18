//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'watchdog_settings_canary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WatchdogSettingsCanary {
  /// Returns a new [WatchdogSettingsCanary] instance.
  WatchdogSettingsCanary({

     this.enabled = false,

     this.interval = '1h',
  });

      /// Submit a synthetic `system-canary` record and require its change within 30 s
  @JsonKey(
    defaultValue: false,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    defaultValue: '1h',
    name: r'interval',
    required: false,
    includeIfNull: false,
  )


  final String? interval;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WatchdogSettingsCanary &&
      other.enabled == enabled &&
      other.interval == interval;

    @override
    int get hashCode =>
        enabled.hashCode +
        interval.hashCode;

  factory WatchdogSettingsCanary.fromJson(Map<String, dynamic> json) => _$WatchdogSettingsCanaryFromJson(json);

  Map<String, dynamic> toJson() => _$WatchdogSettingsCanaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

