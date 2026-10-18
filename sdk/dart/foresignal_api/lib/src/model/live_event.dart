//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/quality.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'live_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LiveEvent {
  /// Returns a new [LiveEvent] instance.
  LiveEvent({

    required  this.sourceId,

    required  this.externalId,

    required  this.kind,

    required  this.observedAt,

     this.validUntil,

    required  this.version,

     this.geometry,

     this.bbox,

     this.prevBbox,

     this.properties,

     this.quality,

     this.leftFilter = false,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



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
    
    name: r'validUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validUntil;



      /// Persisted version this live state builds on
  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



      /// Current bounding box
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



      /// Bounding box before this update (used by the spatial filter)
  @JsonKey(
    
    name: r'prevBbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? prevBbox;



      /// Kinematic (non-significant) properties after this update, over the persisted payload; a JSON null value means the key was removed
  @JsonKey(
    
    name: r'properties',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? properties;



  @JsonKey(
    
    name: r'quality',
    required: false,
    includeIfNull: false,
  )


  final Quality? quality;



      /// True when only `prevBbox` matched the spatial filter: remove the feature
  @JsonKey(
    defaultValue: false,
    name: r'leftFilter',
    required: false,
    includeIfNull: false,
  )


  final bool? leftFilter;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LiveEvent &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.kind == kind &&
      other.observedAt == observedAt &&
      other.validUntil == validUntil &&
      other.version == version &&
      other.geometry == geometry &&
      other.bbox == bbox &&
      other.prevBbox == prevBbox &&
      other.properties == properties &&
      other.quality == quality &&
      other.leftFilter == leftFilter;

    @override
    int get hashCode =>
        sourceId.hashCode +
        externalId.hashCode +
        kind.hashCode +
        observedAt.hashCode +
        (validUntil == null ? 0 : validUntil.hashCode) +
        version.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        (bbox == null ? 0 : bbox.hashCode) +
        (prevBbox == null ? 0 : prevBbox.hashCode) +
        properties.hashCode +
        (quality == null ? 0 : quality.hashCode) +
        leftFilter.hashCode;

  factory LiveEvent.fromJson(Map<String, dynamic> json) => _$LiveEventFromJson(json);

  Map<String, dynamic> toJson() => _$LiveEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

