//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_binding.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalBinding {
  /// Returns a new [ExternalBinding] instance.
  ExternalBinding({

    required  this.streamId,

    required  this.target,

     this.config,

     this.filter,

     this.enabled = true,

     this.backfill = true,

    required  this.id,

    required  this.sourceId,

     this.manifestSha256,

    required  this.state,

     this.applied,

     this.lastError,

     this.createdBy,

    required  this.createdAt,

     this.targetRef,

     this.updatedAt,

     this.lastAppliedAt,
  });

  @JsonKey(
    
    name: r'streamId',
    required: true,
    includeIfNull: false,
  )


  final String streamId;



  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final ExternalBindingTargetEnum target;



  @JsonKey(
    
    name: r'config',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? config;



      /// 017 conditions on item properties
  @JsonKey(
    
    name: r'filter',
    required: false,
    includeIfNull: false,
  )


  final List<Map<String, Object>>? filter;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



  @JsonKey(
    defaultValue: true,
    name: r'backfill',
    required: false,
    includeIfNull: false,
  )


  final bool? backfill;



      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'manifestSha256',
    required: false,
    includeIfNull: false,
  )


  final String? manifestSha256;



  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final ExternalBindingStateEnum state;



  @JsonKey(
    
    name: r'applied',
    required: false,
    includeIfNull: false,
  )


  final int? applied;



  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;



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



      /// What the binding set up once (the chronicle layer of a chronicle binding, chronicle:{id}/layers/{layerId})
  @JsonKey(
    
    name: r'targetRef',
    required: false,
    includeIfNull: false,
  )


  final String? targetRef;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;



  @JsonKey(
    
    name: r'lastAppliedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastAppliedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalBinding &&
      other.streamId == streamId &&
      other.target == target &&
      other.config == config &&
      other.filter == filter &&
      other.enabled == enabled &&
      other.backfill == backfill &&
      other.id == id &&
      other.sourceId == sourceId &&
      other.manifestSha256 == manifestSha256 &&
      other.state == state &&
      other.applied == applied &&
      other.lastError == lastError &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt &&
      other.targetRef == targetRef &&
      other.updatedAt == updatedAt &&
      other.lastAppliedAt == lastAppliedAt;

    @override
    int get hashCode =>
        streamId.hashCode +
        target.hashCode +
        config.hashCode +
        (filter == null ? 0 : filter.hashCode) +
        enabled.hashCode +
        backfill.hashCode +
        id.hashCode +
        sourceId.hashCode +
        manifestSha256.hashCode +
        state.hashCode +
        applied.hashCode +
        (lastError == null ? 0 : lastError.hashCode) +
        createdBy.hashCode +
        createdAt.hashCode +
        (targetRef == null ? 0 : targetRef.hashCode) +
        updatedAt.hashCode +
        (lastAppliedAt == null ? 0 : lastAppliedAt.hashCode);

  factory ExternalBinding.fromJson(Map<String, dynamic> json) => _$ExternalBindingFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalBindingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExternalBindingTargetEnum {
@JsonValue(r'series')
series(r'series'),
@JsonValue(r'canonical-family')
canonicalFamily(r'canonical-family'),
@JsonValue(r'social')
social(r'social'),
@JsonValue(r'news')
news(r'news'),
@JsonValue(r'evidence')
evidence(r'evidence'),
@JsonValue(r'indicator')
indicator(r'indicator'),
@JsonValue(r'forecast')
forecast(r'forecast'),
@JsonValue(r'place')
place(r'place'),
@JsonValue(r'situation')
situation(r'situation'),
@JsonValue(r'chronicle')
chronicle(r'chronicle');

const ExternalBindingTargetEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ExternalBindingStateEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'disabled')
disabled(r'disabled'),
@JsonValue(r'backfilling')
backfilling(r'backfilling'),
@JsonValue(r'error')
error(r'error');

const ExternalBindingStateEnum(this.value);

final String value;

@override
String toString() => value;
}


