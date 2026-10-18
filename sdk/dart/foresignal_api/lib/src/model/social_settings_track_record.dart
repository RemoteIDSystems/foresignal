//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_settings_track_record_overrides_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_settings_track_record.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialSettingsTrackRecord {
  /// Returns a new [SocialSettingsTrackRecord] instance.
  SocialSettingsTrackRecord({

     this.minAssessed = 10,

     this.gradeEShare = 0.7,

     this.gradeDShare = 0.85,

     this.overrides,
  });

          // minimum: 1
  @JsonKey(
    defaultValue: 10,
    name: r'minAssessed',
    required: false,
    includeIfNull: false,
  )


  final int? minAssessed;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    defaultValue: 0.7,
    name: r'gradeEShare',
    required: false,
    includeIfNull: false,
  )


  final num? gradeEShare;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    defaultValue: 0.85,
    name: r'gradeDShare',
    required: false,
    includeIfNull: false,
  )


  final num? gradeDShare;



  @JsonKey(
    
    name: r'overrides',
    required: false,
    includeIfNull: false,
  )


  final List<SocialSettingsTrackRecordOverridesInner>? overrides;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialSettingsTrackRecord &&
      other.minAssessed == minAssessed &&
      other.gradeEShare == gradeEShare &&
      other.gradeDShare == gradeDShare &&
      other.overrides == overrides;

    @override
    int get hashCode =>
        minAssessed.hashCode +
        gradeEShare.hashCode +
        gradeDShare.hashCode +
        overrides.hashCode;

  factory SocialSettingsTrackRecord.fromJson(Map<String, dynamic> json) => _$SocialSettingsTrackRecordFromJson(json);

  Map<String, dynamic> toJson() => _$SocialSettingsTrackRecordToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

