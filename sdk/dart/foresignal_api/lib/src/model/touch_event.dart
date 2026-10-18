//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'touch_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TouchEvent {
  /// Returns a new [TouchEvent] instance.
  TouchEvent({

    required  this.sourceId,

    required  this.externalId,

    required  this.observedAt,

     this.validUntil,

     this.bbox,
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



      /// Bounding box [minLon, minLat, maxLon, maxLat] (WGS84)
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TouchEvent &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.observedAt == observedAt &&
      other.validUntil == validUntil &&
      other.bbox == bbox;

    @override
    int get hashCode =>
        sourceId.hashCode +
        externalId.hashCode +
        observedAt.hashCode +
        (validUntil == null ? 0 : validUntil.hashCode) +
        (bbox == null ? 0 : bbox.hashCode);

  factory TouchEvent.fromJson(Map<String, dynamic> json) => _$TouchEventFromJson(json);

  Map<String, dynamic> toJson() => _$TouchEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

