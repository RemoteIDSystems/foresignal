//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'user_preferences_notifications.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UserPreferencesNotifications {
  /// Returns a new [UserPreferencesNotifications] instance.
  UserPreferencesNotifications({

     this.emailEnabled = true,

     this.pushEnabled = true,

     this.quietHours,
  });

  @JsonKey(
    defaultValue: true,
    name: r'emailEnabled',
    required: false,
    includeIfNull: false,
  )


  final bool? emailEnabled;



  @JsonKey(
    defaultValue: true,
    name: r'pushEnabled',
    required: false,
    includeIfNull: false,
  )


  final bool? pushEnabled;



      /// e.g. `22:00-07:00` in the user's timezone
  @JsonKey(
    
    name: r'quietHours',
    required: false,
    includeIfNull: false,
  )


  final String? quietHours;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UserPreferencesNotifications &&
      other.emailEnabled == emailEnabled &&
      other.pushEnabled == pushEnabled &&
      other.quietHours == quietHours;

    @override
    int get hashCode =>
        emailEnabled.hashCode +
        pushEnabled.hashCode +
        (quietHours == null ? 0 : quietHours.hashCode);

  factory UserPreferencesNotifications.fromJson(Map<String, dynamic> json) => _$UserPreferencesNotificationsFromJson(json);

  Map<String, dynamic> toJson() => _$UserPreferencesNotificationsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

