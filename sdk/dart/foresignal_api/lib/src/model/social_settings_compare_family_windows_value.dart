//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_settings_compare_family_windows_value.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialSettingsCompareFamilyWindowsValue {
  /// Returns a new [SocialSettingsCompareFamilyWindowsValue] instance.
  SocialSettingsCompareFamilyWindowsValue({

     this.before,

     this.after,

     this.toleranceKm,

     this.publicationLag,
  });

      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'before',
    required: false,
    includeIfNull: false,
  )


  final String? before;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'after',
    required: false,
    includeIfNull: false,
  )


  final String? after;



          // minimum: 0
  @JsonKey(
    
    name: r'toleranceKm',
    required: false,
    includeIfNull: false,
  )


  final num? toleranceKm;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'publicationLag',
    required: false,
    includeIfNull: false,
  )


  final String? publicationLag;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialSettingsCompareFamilyWindowsValue &&
      other.before == before &&
      other.after == after &&
      other.toleranceKm == toleranceKm &&
      other.publicationLag == publicationLag;

    @override
    int get hashCode =>
        before.hashCode +
        after.hashCode +
        toleranceKm.hashCode +
        publicationLag.hashCode;

  factory SocialSettingsCompareFamilyWindowsValue.fromJson(Map<String, dynamic> json) => _$SocialSettingsCompareFamilyWindowsValueFromJson(json);

  Map<String, dynamic> toJson() => _$SocialSettingsCompareFamilyWindowsValueToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

