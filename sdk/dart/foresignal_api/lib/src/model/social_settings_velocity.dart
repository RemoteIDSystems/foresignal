//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_settings_velocity.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialSettingsVelocity {
  /// Returns a new [SocialSettingsVelocity] instance.
  SocialSettingsVelocity({

     this.minRatio = 5,

     this.minUniqueAuthors = 20,

     this.dedupHours = 6,
  });

          // minimum: 1
  @JsonKey(
    defaultValue: 5,
    name: r'minRatio',
    required: false,
    includeIfNull: false,
  )


  final num? minRatio;



          // minimum: 1
  @JsonKey(
    defaultValue: 20,
    name: r'minUniqueAuthors',
    required: false,
    includeIfNull: false,
  )


  final int? minUniqueAuthors;



          // minimum: 1
  @JsonKey(
    defaultValue: 6,
    name: r'dedupHours',
    required: false,
    includeIfNull: false,
  )


  final int? dedupHours;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialSettingsVelocity &&
      other.minRatio == minRatio &&
      other.minUniqueAuthors == minUniqueAuthors &&
      other.dedupHours == dedupHours;

    @override
    int get hashCode =>
        minRatio.hashCode +
        minUniqueAuthors.hashCode +
        dedupHours.hashCode;

  factory SocialSettingsVelocity.fromJson(Map<String, dynamic> json) => _$SocialSettingsVelocityFromJson(json);

  Map<String, dynamic> toJson() => _$SocialSettingsVelocityToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

