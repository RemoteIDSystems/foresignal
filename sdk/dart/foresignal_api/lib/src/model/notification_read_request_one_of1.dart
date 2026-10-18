//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_read_request_one_of1.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationReadRequestOneOf1 {
  /// Returns a new [NotificationReadRequestOneOf1] instance.
  NotificationReadRequestOneOf1({

    required  this.ids,
  });

  @JsonKey(
    
    name: r'ids',
    required: true,
    includeIfNull: false,
  )


  final List<int> ids;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationReadRequestOneOf1 &&
      other.ids == ids;

    @override
    int get hashCode =>
        ids.hashCode;

  factory NotificationReadRequestOneOf1.fromJson(Map<String, dynamic> json) => _$NotificationReadRequestOneOf1FromJson(json);

  Map<String, dynamic> toJson() => _$NotificationReadRequestOneOf1ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

