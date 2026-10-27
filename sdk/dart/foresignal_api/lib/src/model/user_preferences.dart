//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/user_preferences_notifications.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'user_preferences.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UserPreferences {
  /// Returns a new [UserPreferences] instance.
  UserPreferences({

     this.timezone,

     this.locale,

     this.theme,

     this.notifications,

     this.ui,
  });

      /// IANA zone; falls back to the server setting when null
  @JsonKey(
    
    name: r'timezone',
    required: false,
    includeIfNull: false,
  )


  final String? timezone;



  @JsonKey(
    
    name: r'locale',
    required: false,
    includeIfNull: false,
  )


  final String? locale;



  @JsonKey(
    
    name: r'theme',
    required: false,
    includeIfNull: false,
  )


  final String? theme;



  @JsonKey(
    
    name: r'notifications',
    required: false,
    includeIfNull: false,
  )


  final UserPreferencesNotifications? notifications;



      /// Free-form UI preferences
  @JsonKey(
    
    name: r'ui',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? ui;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UserPreferences &&
      other.timezone == timezone &&
      other.locale == locale &&
      other.theme == theme &&
      other.notifications == notifications &&
      other.ui == ui;

    @override
    int get hashCode =>
        (timezone == null ? 0 : timezone.hashCode) +
        (locale == null ? 0 : locale.hashCode) +
        (theme == null ? 0 : theme.hashCode) +
        notifications.hashCode +
        ui.hashCode;

  factory UserPreferences.fromJson(Map<String, dynamic> json) => _$UserPreferencesFromJson(json);

  Map<String, dynamic> toJson() => _$UserPreferencesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

