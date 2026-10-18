//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_read_request_one_of.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationReadRequestOneOf {
  /// Returns a new [NotificationReadRequestOneOf] instance.
  NotificationReadRequestOneOf({

    required  this.upToId,
  });

  @JsonKey(
    
    name: r'upToId',
    required: true,
    includeIfNull: false,
  )


  final int upToId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationReadRequestOneOf &&
      other.upToId == upToId;

    @override
    int get hashCode =>
        upToId.hashCode;

  factory NotificationReadRequestOneOf.fromJson(Map<String, dynamic> json) => _$NotificationReadRequestOneOfFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationReadRequestOneOfToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

