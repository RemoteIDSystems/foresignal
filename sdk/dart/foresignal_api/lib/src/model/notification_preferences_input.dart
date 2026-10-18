//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/notification_preference_input.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_preferences_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationPreferencesInput {
  /// Returns a new [NotificationPreferencesInput] instance.
  NotificationPreferencesInput({

    required  this.items,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<NotificationPreferenceInput> items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationPreferencesInput &&
      other.items == items;

    @override
    int get hashCode =>
        items.hashCode;

  factory NotificationPreferencesInput.fromJson(Map<String, dynamic> json) => _$NotificationPreferencesInputFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationPreferencesInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

