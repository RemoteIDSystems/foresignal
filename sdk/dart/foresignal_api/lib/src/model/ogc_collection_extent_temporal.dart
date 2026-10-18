//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_collection_extent_temporal.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcCollectionExtentTemporal {
  /// Returns a new [OgcCollectionExtentTemporal] instance.
  OgcCollectionExtentTemporal({

     this.interval,

     this.trs,
  });

  @JsonKey(
    
    name: r'interval',
    required: false,
    includeIfNull: false,
  )


  final List<List<DateTime?>>? interval;



  @JsonKey(
    
    name: r'trs',
    required: false,
    includeIfNull: false,
  )


  final String? trs;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcCollectionExtentTemporal &&
      other.interval == interval &&
      other.trs == trs;

    @override
    int get hashCode =>
        interval.hashCode +
        trs.hashCode;

  factory OgcCollectionExtentTemporal.fromJson(Map<String, dynamic> json) => _$OgcCollectionExtentTemporalFromJson(json);

  Map<String, dynamic> toJson() => _$OgcCollectionExtentTemporalToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

