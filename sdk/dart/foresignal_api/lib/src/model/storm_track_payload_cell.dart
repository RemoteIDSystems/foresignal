//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'storm_track_payload_cell.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StormTrackPayloadCell {
  /// Returns a new [StormTrackPayloadCell] instance.
  StormTrackPayloadCell({

     this.maxDbz,

     this.areaKm2,

     this.meshMaxMm,

     this.flashRate,
  });

  @JsonKey(
    
    name: r'maxDbz',
    required: false,
    includeIfNull: false,
  )


  final num? maxDbz;



  @JsonKey(
    
    name: r'areaKm2',
    required: false,
    includeIfNull: false,
  )


  final num? areaKm2;



  @JsonKey(
    
    name: r'meshMaxMm',
    required: false,
    includeIfNull: false,
  )


  final num? meshMaxMm;



  @JsonKey(
    
    name: r'flashRate',
    required: false,
    includeIfNull: false,
  )


  final num? flashRate;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StormTrackPayloadCell &&
      other.maxDbz == maxDbz &&
      other.areaKm2 == areaKm2 &&
      other.meshMaxMm == meshMaxMm &&
      other.flashRate == flashRate;

    @override
    int get hashCode =>
        maxDbz.hashCode +
        areaKm2.hashCode +
        (meshMaxMm == null ? 0 : meshMaxMm.hashCode) +
        (flashRate == null ? 0 : flashRate.hashCode);

  factory StormTrackPayloadCell.fromJson(Map<String, dynamic> json) => _$StormTrackPayloadCellFromJson(json);

  Map<String, dynamic> toJson() => _$StormTrackPayloadCellToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

