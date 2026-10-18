//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/severity.dart';
import 'package:foresignal_api/src/model/situation_member_input.dart';
import 'package:foresignal_api/src/model/situation_theme.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationInput {
  /// Returns a new [SituationInput] instance.
  SituationInput({

    required  this.title,

    required  this.theme,

     this.status,

     this.severityOverride,

     this.regionCodes,

     this.topicIds,

     this.entityIds,

     this.investigationIds,

     this.members,
  });

  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'theme',
    required: true,
    includeIfNull: false,
  )


  final SituationTheme theme;



      /// Initial status (operators may set `active` directly)
  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final SituationInputStatusEnum? status;



  @JsonKey(
    
    name: r'severityOverride',
    required: false,
    includeIfNull: false,
  )


  final Severity? severityOverride;



  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



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



  @JsonKey(
    
    name: r'members',
    required: false,
    includeIfNull: false,
  )


  final List<SituationMemberInput>? members;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationInput &&
      other.title == title &&
      other.theme == theme &&
      other.status == status &&
      other.severityOverride == severityOverride &&
      other.regionCodes == regionCodes &&
      other.topicIds == topicIds &&
      other.entityIds == entityIds &&
      other.investigationIds == investigationIds &&
      other.members == members;

    @override
    int get hashCode =>
        title.hashCode +
        theme.hashCode +
        status.hashCode +
        (severityOverride == null ? 0 : severityOverride.hashCode) +
        regionCodes.hashCode +
        topicIds.hashCode +
        entityIds.hashCode +
        investigationIds.hashCode +
        members.hashCode;

  factory SituationInput.fromJson(Map<String, dynamic> json) => _$SituationInputFromJson(json);

  Map<String, dynamic> toJson() => _$SituationInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Initial status (operators may set `active` directly)
enum SituationInputStatusEnum {
@JsonValue(r'emerging')
emerging(r'emerging'),
@JsonValue(r'active')
active(r'active');

const SituationInputStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


