//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_badge.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationBadge {
  /// Returns a new [NotificationBadge] instance.
  NotificationBadge({

    required  this.unread,

    required  this.unreadCritical,

     this.latestId,
  });

  @JsonKey(
    
    name: r'unread',
    required: true,
    includeIfNull: false,
  )


  final int unread;



  @JsonKey(
    
    name: r'unreadCritical',
    required: true,
    includeIfNull: false,
  )


  final int unreadCritical;



  @JsonKey(
    
    name: r'latestId',
    required: false,
    includeIfNull: false,
  )


  final int? latestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationBadge &&
      other.unread == unread &&
      other.unreadCritical == unreadCritical &&
      other.latestId == latestId;

    @override
    int get hashCode =>
        unread.hashCode +
        unreadCritical.hashCode +
        (latestId == null ? 0 : latestId.hashCode);

  factory NotificationBadge.fromJson(Map<String, dynamic> json) => _$NotificationBadgeFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationBadgeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

