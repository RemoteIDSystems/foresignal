//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mesh_channel_common_config_airtime_custom.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MeshChannelCommonConfigAirtimeCustom {
  /// Returns a new [MeshChannelCommonConfigAirtimeCustom] instance.
  MeshChannelCommonConfigAirtimeCustom({

     this.spreadingFactor,

     this.bandwidthKhz,

     this.codingRate,

     this.preambleSymbols,
  });

          // minimum: 5
          // maximum: 12
  @JsonKey(
    
    name: r'spreadingFactor',
    required: false,
    includeIfNull: false,
  )


  final int? spreadingFactor;



  @JsonKey(
    
    name: r'bandwidthKhz',
    required: false,
    includeIfNull: false,
  )


  final num? bandwidthKhz;



      /// Denominator of 4/x
          // minimum: 5
          // maximum: 8
  @JsonKey(
    
    name: r'codingRate',
    required: false,
    includeIfNull: false,
  )


  final int? codingRate;



          // minimum: 1
  @JsonKey(
    
    name: r'preambleSymbols',
    required: false,
    includeIfNull: false,
  )


  final int? preambleSymbols;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MeshChannelCommonConfigAirtimeCustom &&
      other.spreadingFactor == spreadingFactor &&
      other.bandwidthKhz == bandwidthKhz &&
      other.codingRate == codingRate &&
      other.preambleSymbols == preambleSymbols;

    @override
    int get hashCode =>
        spreadingFactor.hashCode +
        bandwidthKhz.hashCode +
        codingRate.hashCode +
        preambleSymbols.hashCode;

  factory MeshChannelCommonConfigAirtimeCustom.fromJson(Map<String, dynamic> json) => _$MeshChannelCommonConfigAirtimeCustomFromJson(json);

  Map<String, dynamic> toJson() => _$MeshChannelCommonConfigAirtimeCustomToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

