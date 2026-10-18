//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'indicator_signal_intel_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndicatorSignalIntelEvent {
  /// Returns a new [IndicatorSignalIntelEvent] instance.
  IndicatorSignalIntelEvent({

    required  this.type,

     this.regionCodes,

     this.placeId,

     this.bbox,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final String type;



      /// ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



      /// minLon,minLat,maxLon,maxLat
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final String? bbox;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IndicatorSignalIntelEvent &&
      other.type == type &&
      other.regionCodes == regionCodes &&
      other.placeId == placeId &&
      other.bbox == bbox;

    @override
    int get hashCode =>
        type.hashCode +
        regionCodes.hashCode +
        (placeId == null ? 0 : placeId.hashCode) +
        (bbox == null ? 0 : bbox.hashCode);

  factory IndicatorSignalIntelEvent.fromJson(Map<String, dynamic> json) => _$IndicatorSignalIntelEventFromJson(json);

  Map<String, dynamic> toJson() => _$IndicatorSignalIntelEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

