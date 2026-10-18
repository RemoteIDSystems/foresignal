//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/quality.dart';
import 'package:foresignal_api/src/model/normalized_record_preview_geometry.dart';
import 'package:foresignal_api/src/model/observer.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'normalized_record_preview.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NormalizedRecordPreview {
  /// Returns a new [NormalizedRecordPreview] instance.
  NormalizedRecordPreview({

    required  this.externalId,

    required  this.kind,

    required  this.observedAt,

     this.geometry,

     this.validUntil,

     this.tombstone,

    required  this.payload,

     this.eventTime,

     this.validFrom,

     this.quality,

     this.observers,
  });

  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final String kind;



  @JsonKey(
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final NormalizedRecordPreviewGeometry? geometry;



  @JsonKey(
    
    name: r'validUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validUntil;



  @JsonKey(
    
    name: r'tombstone',
    required: false,
    includeIfNull: false,
  )


  final bool? tombstone;



  @JsonKey(
    
    name: r'payload',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> payload;



  @JsonKey(
    
    name: r'eventTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? eventTime;



  @JsonKey(
    
    name: r'validFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validFrom;



  @JsonKey(
    
    name: r'quality',
    required: false,
    includeIfNull: false,
  )


  final Quality? quality;



  @JsonKey(
    
    name: r'observers',
    required: false,
    includeIfNull: false,
  )


  final List<Observer>? observers;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NormalizedRecordPreview &&
      other.externalId == externalId &&
      other.kind == kind &&
      other.observedAt == observedAt &&
      other.geometry == geometry &&
      other.validUntil == validUntil &&
      other.tombstone == tombstone &&
      other.payload == payload &&
      other.eventTime == eventTime &&
      other.validFrom == validFrom &&
      other.quality == quality &&
      other.observers == observers;

    @override
    int get hashCode =>
        externalId.hashCode +
        kind.hashCode +
        observedAt.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        (validUntil == null ? 0 : validUntil.hashCode) +
        tombstone.hashCode +
        payload.hashCode +
        (eventTime == null ? 0 : eventTime.hashCode) +
        (validFrom == null ? 0 : validFrom.hashCode) +
        (quality == null ? 0 : quality.hashCode) +
        observers.hashCode;

  factory NormalizedRecordPreview.fromJson(Map<String, dynamic> json) => _$NormalizedRecordPreviewFromJson(json);

  Map<String, dynamic> toJson() => _$NormalizedRecordPreviewToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

