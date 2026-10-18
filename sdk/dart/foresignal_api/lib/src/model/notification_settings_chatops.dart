//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_settings_chatops.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationSettingsChatops {
  /// Returns a new [NotificationSettingsChatops] instance.
  NotificationSettingsChatops({

     this.enabled = true,
  });

      /// Global kill switch for all interactive channels
  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationSettingsChatops &&
      other.enabled == enabled;

    @override
    int get hashCode =>
        enabled.hashCode;

  factory NotificationSettingsChatops.fromJson(Map<String, dynamic> json) => _$NotificationSettingsChatopsFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationSettingsChatopsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

