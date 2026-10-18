//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_notifications.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoNotifications {
  /// Returns a new [SystemInfoNotifications] instance.
  SystemInfoNotifications({

     this.channels,

     this.pendingDeliveries,

     this.failedLast24h,
  });

  @JsonKey(
    
    name: r'channels',
    required: false,
    includeIfNull: false,
  )


  final int? channels;



  @JsonKey(
    
    name: r'pendingDeliveries',
    required: false,
    includeIfNull: false,
  )


  final int? pendingDeliveries;



  @JsonKey(
    
    name: r'failedLast24h',
    required: false,
    includeIfNull: false,
  )


  final int? failedLast24h;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoNotifications &&
      other.channels == channels &&
      other.pendingDeliveries == pendingDeliveries &&
      other.failedLast24h == failedLast24h;

    @override
    int get hashCode =>
        channels.hashCode +
        pendingDeliveries.hashCode +
        failedLast24h.hashCode;

  factory SystemInfoNotifications.fromJson(Map<String, dynamic> json) => _$SystemInfoNotificationsFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoNotificationsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

