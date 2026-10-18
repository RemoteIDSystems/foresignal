//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cpa_batch_request_filter.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CpaBatchRequestFilter {
  /// Returns a new [CpaBatchRequestFilter] instance.
  CpaBatchRequestFilter({

     this.kinds,

     this.sources,

     this.bbox,
  });

  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



      /// [minLon, minLat, maxLon, maxLat] (WGS84)
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CpaBatchRequestFilter &&
      other.kinds == kinds &&
      other.sources == sources &&
      other.bbox == bbox;

    @override
    int get hashCode =>
        kinds.hashCode +
        sources.hashCode +
        (bbox == null ? 0 : bbox.hashCode);

  factory CpaBatchRequestFilter.fromJson(Map<String, dynamic> json) => _$CpaBatchRequestFilterFromJson(json);

  Map<String, dynamic> toJson() => _$CpaBatchRequestFilterToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

