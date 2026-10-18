//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'quality.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Quality {
  /// Returns a new [Quality] instance.
  Quality({

     this.accuracyMeters,

     this.confidence,

     this.positionSource,

     this.extra,
  });

      /// Horizontal accuracy in metres
          // minimum: 0
  @JsonKey(
    
    name: r'accuracyMeters',
    required: false,
    includeIfNull: false,
  )


  final num? accuracyMeters;



      /// Confidence 0–1
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'confidence',
    required: false,
    includeIfNull: false,
  )


  final num? confidence;



      /// How the position was obtained (adapter-defined `PositionSource`)
  @JsonKey(
    
    name: r'positionSource',
    required: false,
    includeIfNull: false,
  )


  final String? positionSource;



      /// Namespaced scalar extras, e.g. `adsb.seen_pos`
  @JsonKey(
    
    name: r'extra',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? extra;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Quality &&
      other.accuracyMeters == accuracyMeters &&
      other.confidence == confidence &&
      other.positionSource == positionSource &&
      other.extra == extra;

    @override
    int get hashCode =>
        (accuracyMeters == null ? 0 : accuracyMeters.hashCode) +
        (confidence == null ? 0 : confidence.hashCode) +
        (positionSource == null ? 0 : positionSource.hashCode) +
        extra.hashCode;

  factory Quality.fromJson(Map<String, dynamic> json) => _$QualityFromJson(json);

  Map<String, dynamic> toJson() => _$QualityToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

