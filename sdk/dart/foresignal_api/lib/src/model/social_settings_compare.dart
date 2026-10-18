//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_settings_compare_family_windows_value.dart';
import 'package:foresignal_api/src/model/social_settings_compare_default_window.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_settings_compare.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialSettingsCompare {
  /// Returns a new [SocialSettingsCompare] instance.
  SocialSettingsCompare({

     this.minGroups = 3,

     this.defaultWindow,

     this.familyWindows,
  });

          // minimum: 1
  @JsonKey(
    defaultValue: 3,
    name: r'minGroups',
    required: false,
    includeIfNull: false,
  )


  final int? minGroups;



  @JsonKey(
    
    name: r'defaultWindow',
    required: false,
    includeIfNull: false,
  )


  final SocialSettingsCompareDefaultWindow? defaultWindow;



      /// Signal family → window
  @JsonKey(
    
    name: r'familyWindows',
    required: false,
    includeIfNull: false,
  )


  final Map<String, SocialSettingsCompareFamilyWindowsValue>? familyWindows;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialSettingsCompare &&
      other.minGroups == minGroups &&
      other.defaultWindow == defaultWindow &&
      other.familyWindows == familyWindows;

    @override
    int get hashCode =>
        minGroups.hashCode +
        defaultWindow.hashCode +
        familyWindows.hashCode;

  factory SocialSettingsCompare.fromJson(Map<String, dynamic> json) => _$SocialSettingsCompareFromJson(json);

  Map<String, dynamic> toJson() => _$SocialSettingsCompareToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

