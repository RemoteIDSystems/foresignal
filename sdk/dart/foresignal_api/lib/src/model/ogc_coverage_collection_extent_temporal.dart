//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_coverage_collection_extent_temporal.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcCoverageCollectionExtentTemporal {
  /// Returns a new [OgcCoverageCollectionExtentTemporal] instance.
  OgcCoverageCollectionExtentTemporal({

     this.interval,

     this.values,

     this.trs,
  });

  @JsonKey(
    
    name: r'interval',
    required: false,
    includeIfNull: false,
  )


  final List<List<DateTime?>>? interval;



      /// Recent frame times
  @JsonKey(
    
    name: r'values',
    required: false,
    includeIfNull: false,
  )


  final List<String>? values;



  @JsonKey(
    
    name: r'trs',
    required: false,
    includeIfNull: false,
  )


  final String? trs;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcCoverageCollectionExtentTemporal &&
      other.interval == interval &&
      other.values == values &&
      other.trs == trs;

    @override
    int get hashCode =>
        interval.hashCode +
        values.hashCode +
        trs.hashCode;

  factory OgcCoverageCollectionExtentTemporal.fromJson(Map<String, dynamic> json) => _$OgcCoverageCollectionExtentTemporalFromJson(json);

  Map<String, dynamic> toJson() => _$OgcCoverageCollectionExtentTemporalToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

