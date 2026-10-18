//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'share_link.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ShareLink {
  /// Returns a new [ShareLink] instance.
  ShareLink({

    required  this.id,

    required  this.keyPrefix,

    required  this.targetType,

    required  this.targetRef,

     this.fieldMask,

     this.precisionM,

     this.masked,

    required  this.expiresAt,

     this.maxUses,

    required  this.uses,

    required  this.status,

     this.createdBy,

    required  this.createdAt,

     this.revokedAt,

     this.lastUsedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// First characters of the fss_ key
  @JsonKey(
    
    name: r'keyPrefix',
    required: true,
    includeIfNull: false,
  )


  final String keyPrefix;



  @JsonKey(
    
    name: r'targetType',
    required: true,
    includeIfNull: false,
  )


  final ShareLinkTargetTypeEnum targetType;



  @JsonKey(
    
    name: r'targetRef',
    required: true,
    includeIfNull: false,
  )


  final String targetRef;



  @JsonKey(
    
    name: r'fieldMask',
    required: false,
    includeIfNull: false,
  )


  final List<String>? fieldMask;



  @JsonKey(
    
    name: r'precisionM',
    required: false,
    includeIfNull: false,
  )


  final int? precisionM;



      /// Privacy mask applied (personal or private source)
  @JsonKey(
    
    name: r'masked',
    required: false,
    includeIfNull: false,
  )


  final bool? masked;



  @JsonKey(
    
    name: r'expiresAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime expiresAt;



  @JsonKey(
    
    name: r'maxUses',
    required: false,
    includeIfNull: false,
  )


  final int? maxUses;



  @JsonKey(
    
    name: r'uses',
    required: true,
    includeIfNull: false,
  )


  final int uses;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ShareLinkStatusEnum status;



  @JsonKey(
    
    name: r'createdBy',
    required: false,
    includeIfNull: false,
  )


  final String? createdBy;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'revokedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? revokedAt;



  @JsonKey(
    
    name: r'lastUsedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastUsedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ShareLink &&
      other.id == id &&
      other.keyPrefix == keyPrefix &&
      other.targetType == targetType &&
      other.targetRef == targetRef &&
      other.fieldMask == fieldMask &&
      other.precisionM == precisionM &&
      other.masked == masked &&
      other.expiresAt == expiresAt &&
      other.maxUses == maxUses &&
      other.uses == uses &&
      other.status == status &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt &&
      other.revokedAt == revokedAt &&
      other.lastUsedAt == lastUsedAt;

    @override
    int get hashCode =>
        id.hashCode +
        keyPrefix.hashCode +
        targetType.hashCode +
        targetRef.hashCode +
        (fieldMask == null ? 0 : fieldMask.hashCode) +
        (precisionM == null ? 0 : precisionM.hashCode) +
        masked.hashCode +
        expiresAt.hashCode +
        (maxUses == null ? 0 : maxUses.hashCode) +
        uses.hashCode +
        status.hashCode +
        createdBy.hashCode +
        createdAt.hashCode +
        (revokedAt == null ? 0 : revokedAt.hashCode) +
        (lastUsedAt == null ? 0 : lastUsedAt.hashCode);

  factory ShareLink.fromJson(Map<String, dynamic> json) => _$ShareLinkFromJson(json);

  Map<String, dynamic> toJson() => _$ShareLinkToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ShareLinkTargetTypeEnum {
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
export_(r'export'),
@JsonValue(r'workspace')
workspace(r'workspace'),
@JsonValue(r'report')
report(r'report');

const ShareLinkTargetTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ShareLinkStatusEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'expired')
expired(r'expired'),
@JsonValue(r'revoked')
revoked(r'revoked'),
@JsonValue(r'exhausted')
exhausted(r'exhausted');

const ShareLinkStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


