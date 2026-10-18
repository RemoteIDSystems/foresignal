//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'share_link_create.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ShareLinkCreate {
  /// Returns a new [ShareLinkCreate] instance.
  ShareLinkCreate({

    required  this.targetType,

    required  this.targetRef,

     this.fieldMask,

     this.precisionM,

     this.expiresAt,

     this.maxUses,
  });

  @JsonKey(
    
    name: r'targetType',
    required: true,
    includeIfNull: false,
  )


  final ShareLinkCreateTargetTypeEnum targetType;



      /// Target id: record `{sourceId}/{externalId}`, alert id, view id, track `{sourceId}/{externalId}`, `{briefingId}/{runId}`, or export job id
  @JsonKey(
    
    name: r'targetRef',
    required: true,
    includeIfNull: false,
  )


  final String targetRef;



      /// Payload allowlist applied after licence filtering
  @JsonKey(
    
    name: r'fieldMask',
    required: false,
    includeIfNull: false,
  )


  final List<String>? fieldMask;



      /// Coordinate precision cap in metres (default 1,000 for personal or private sources)
          // minimum: 1
  @JsonKey(
    
    name: r'precisionM',
    required: false,
    includeIfNull: false,
  )


  final int? precisionM;



      /// Default now + 7 d, maximum now + 30 d
  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;



          // minimum: 1
  @JsonKey(
    
    name: r'maxUses',
    required: false,
    includeIfNull: false,
  )


  final int? maxUses;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ShareLinkCreate &&
      other.targetType == targetType &&
      other.targetRef == targetRef &&
      other.fieldMask == fieldMask &&
      other.precisionM == precisionM &&
      other.expiresAt == expiresAt &&
      other.maxUses == maxUses;

    @override
    int get hashCode =>
        targetType.hashCode +
        targetRef.hashCode +
        (fieldMask == null ? 0 : fieldMask.hashCode) +
        (precisionM == null ? 0 : precisionM.hashCode) +
        (expiresAt == null ? 0 : expiresAt.hashCode) +
        (maxUses == null ? 0 : maxUses.hashCode);

  factory ShareLinkCreate.fromJson(Map<String, dynamic> json) => _$ShareLinkCreateFromJson(json);

  Map<String, dynamic> toJson() => _$ShareLinkCreateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ShareLinkCreateTargetTypeEnum {
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'view')
view(r'view'),
@JsonValue(r'track')
track(r'track'),
@JsonValue(r'briefing-run')
briefingRun(r'briefing-run'),
@JsonValue(r'export')
export_(r'export');

const ShareLinkCreateTargetTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


