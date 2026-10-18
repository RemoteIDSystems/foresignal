//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_area.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleArea {
  /// Returns a new [ChronicleArea] instance.
  ChronicleArea({

     this.geometry,

     this.placeIds,

     this.regionCodes,

     this.bufferKm,
  });

  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



  @JsonKey(
    
    name: r'placeIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? placeIds;



      /// 040 region codes (ISO 3166)
  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



          // minimum: 0
  @JsonKey(
    
    name: r'bufferKm',
    required: false,
    includeIfNull: false,
  )


  final num? bufferKm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleArea &&
      other.geometry == geometry &&
      other.placeIds == placeIds &&
      other.regionCodes == regionCodes &&
      other.bufferKm == bufferKm;

    @override
    int get hashCode =>
        (geometry == null ? 0 : geometry.hashCode) +
        placeIds.hashCode +
        regionCodes.hashCode +
        (bufferKm == null ? 0 : bufferKm.hashCode);

  factory ChronicleArea.fromJson(Map<String, dynamic> json) => _$ChronicleAreaFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleAreaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

