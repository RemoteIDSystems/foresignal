//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_action_params.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationActionParams {
  /// Returns a new [NotificationActionParams] instance.
  NotificationActionParams({

     this.duration,
  });

      /// snooze 15m–24h (default 1h); mute 1h–7d (default 4h)
  @JsonKey(
    
    name: r'duration',
    required: false,
    includeIfNull: false,
  )


  final String? duration;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationActionParams &&
      other.duration == duration;

    @override
    int get hashCode =>
        duration.hashCode;

  factory NotificationActionParams.fromJson(Map<String, dynamic> json) => _$NotificationActionParamsFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationActionParamsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

