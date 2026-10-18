//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/persist_reason.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'feature_properties.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FeatureProperties {
  /// Returns a new [FeatureProperties] instance.
  FeatureProperties({

     this.source_,

     this.externalId,

     this.kind,

     this.title,

     this.version,

     this.seq,

     this.observedAt,

     this.validUntil,

     this.live,

     this.distanceKm,

     this.payload,

     this.near,

     this.ingestedAt,

     this.persistReason,
  });

  @JsonKey(
    
    name: r'source',
    required: false,
    includeIfNull: false,
  )


  final String? source_;



  @JsonKey(
    
    name: r'externalId',
    required: false,
    includeIfNull: false,
  )


  final String? externalId;



  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;



  @JsonKey(
    
    name: r'seq',
    required: false,
    includeIfNull: false,
  )


  final int? seq;



  @JsonKey(
    
    name: r'observedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? observedAt;



  @JsonKey(
    
    name: r'validUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validUntil;



      /// record_live.dirty
  @JsonKey(
    
    name: r'live',
    required: false,
    includeIfNull: false,
  )


  final bool? live;



          // minimum: 0
  @JsonKey(
    
    name: r'distanceKm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceKm;



  @JsonKey(
    
    name: r'payload',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? payload;



      /// Region near text with include=region (040)
  @JsonKey(
    
    name: r'near',
    required: false,
    includeIfNull: false,
  )


  final String? near;



      /// Version-based features only (history and asOf): the batch time of the version
  @JsonKey(
    
    name: r'ingestedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? ingestedAt;



      /// History features only
  @JsonKey(
    
    name: r'persistReason',
    required: false,
    includeIfNull: false,
  )


  final PersistReason? persistReason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FeatureProperties &&
      other.source_ == source_ &&
      other.externalId == externalId &&
      other.kind == kind &&
      other.title == title &&
      other.version == version &&
      other.seq == seq &&
      other.observedAt == observedAt &&
      other.validUntil == validUntil &&
      other.live == live &&
      other.distanceKm == distanceKm &&
      other.payload == payload &&
      other.near == near &&
      other.ingestedAt == ingestedAt &&
      other.persistReason == persistReason;

    @override
    int get hashCode =>
        source_.hashCode +
        externalId.hashCode +
        kind.hashCode +
        (title == null ? 0 : title.hashCode) +
        version.hashCode +
        seq.hashCode +
        observedAt.hashCode +
        (validUntil == null ? 0 : validUntil.hashCode) +
        live.hashCode +
        distanceKm.hashCode +
        payload.hashCode +
        (near == null ? 0 : near.hashCode) +
        ingestedAt.hashCode +
        persistReason.hashCode;

  factory FeatureProperties.fromJson(Map<String, dynamic> json) => _$FeaturePropertiesFromJson(json);

  Map<String, dynamic> toJson() => _$FeaturePropertiesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

