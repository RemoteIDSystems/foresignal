//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/index_fit_result.dart';
import 'package:foresignal_api/src/model/regional_index_def.dart';
import 'package:foresignal_api/src/model/regional_index_value.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'regional_index_detail.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegionalIndexDetail {
  /// Returns a new [RegionalIndexDetail] instance.
  RegionalIndexDetail({

    required  this.index,

     this.region,

     this.from,

     this.to,

    required  this.values,

     this.latestFit,
  });

  @JsonKey(
    
    name: r'index',
    required: true,
    includeIfNull: false,
  )


  final RegionalIndexDef index;



  @JsonKey(
    
    name: r'region',
    required: false,
    includeIfNull: false,
  )


  final String? region;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



  @JsonKey(
    
    name: r'values',
    required: true,
    includeIfNull: false,
  )


  final List<RegionalIndexValue> values;



  @JsonKey(
    
    name: r'latestFit',
    required: false,
    includeIfNull: false,
  )


  final IndexFitResult? latestFit;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegionalIndexDetail &&
      other.index == index &&
      other.region == region &&
      other.from == from &&
      other.to == to &&
      other.values == values &&
      other.latestFit == latestFit;

    @override
    int get hashCode =>
        index.hashCode +
        (region == null ? 0 : region.hashCode) +
        (from == null ? 0 : from.hashCode) +
        (to == null ? 0 : to.hashCode) +
        values.hashCode +
        (latestFit == null ? 0 : latestFit.hashCode);

  factory RegionalIndexDetail.fromJson(Map<String, dynamic> json) => _$RegionalIndexDetailFromJson(json);

  Map<String, dynamic> toJson() => _$RegionalIndexDetailToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

