//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/severity.dart';
import 'package:foresignal_api/src/model/situation_theme.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationPatch {
  /// Returns a new [SituationPatch] instance.
  SituationPatch({

     this.title,

     this.theme,

     this.severityOverride,

     this.status,

     this.topicIds,

     this.entityIds,

     this.investigationIds,

     this.reason,

     this.version,
  });

  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'theme',
    required: false,
    includeIfNull: false,
  )


  final SituationTheme? theme;



      /// Set to override the derived severity; null clears the override
  @JsonKey(
    
    name: r'severityOverride',
    required: false,
    includeIfNull: false,
  )


  final Severity? severityOverride;



      /// `active` activates or reopens a dormant situation; `closed` closes it (final)
  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final SituationPatchStatusEnum? status;



  @JsonKey(
    
    name: r'topicIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? topicIds;



  @JsonKey(
    
    name: r'entityIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? entityIds;



  @JsonKey(
    
    name: r'investigationIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? investigationIds;



      /// Note stored in the situation log
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



      /// Expected current version; a mismatch returns 409 `SITUATION_VERSION_CONFLICT`
  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationPatch &&
      other.title == title &&
      other.theme == theme &&
      other.severityOverride == severityOverride &&
      other.status == status &&
      other.topicIds == topicIds &&
      other.entityIds == entityIds &&
      other.investigationIds == investigationIds &&
      other.reason == reason &&
      other.version == version;

    @override
    int get hashCode =>
        title.hashCode +
        theme.hashCode +
        (severityOverride == null ? 0 : severityOverride.hashCode) +
        status.hashCode +
        topicIds.hashCode +
        entityIds.hashCode +
        investigationIds.hashCode +
        (reason == null ? 0 : reason.hashCode) +
        version.hashCode;

  factory SituationPatch.fromJson(Map<String, dynamic> json) => _$SituationPatchFromJson(json);

  Map<String, dynamic> toJson() => _$SituationPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `active` activates or reopens a dormant situation; `closed` closes it (final)
enum SituationPatchStatusEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'closed')
closed(r'closed');

const SituationPatchStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


