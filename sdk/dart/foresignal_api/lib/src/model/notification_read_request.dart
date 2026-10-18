//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/notification_read_request_one_of1.dart';
import 'package:foresignal_api/src/model/notification_read_request_one_of.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_read_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationReadRequest {
  /// Returns a new [NotificationReadRequest] instance.
  NotificationReadRequest({

    required  this.upToId,

    required  this.ids,
  });

  @JsonKey(
    
    name: r'upToId',
    required: true,
    includeIfNull: false,
  )


  final int upToId;



  @JsonKey(
    
    name: r'ids',
    required: true,
    includeIfNull: false,
  )


  final List<int> ids;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationReadRequest &&
      other.upToId == upToId &&
      other.ids == ids;

    @override
    int get hashCode =>
        upToId.hashCode +
        ids.hashCode;

  factory NotificationReadRequest.fromJson(Map<String, dynamic> json) => _$NotificationReadRequestFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationReadRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

