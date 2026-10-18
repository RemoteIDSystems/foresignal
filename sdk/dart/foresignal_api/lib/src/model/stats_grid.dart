//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/stats_grid_features_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stats_grid.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StatsGrid {
  /// Returns a new [StatsGrid] instance.
  StatsGrid({

    required  this.type,

    required  this.cellSystem,

     this.res,

     this.sizeKm,

     this.measure,

     this.backend,

    required  this.features,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final StatsGridTypeEnum type;



  @JsonKey(
    
    name: r'cellSystem',
    required: true,
    includeIfNull: false,
  )


  final StatsGridCellSystemEnum cellSystem;



      /// H3 resolution (rollup mode)
  @JsonKey(
    
    name: r'res',
    required: false,
    includeIfNull: false,
  )


  final int? res;



  @JsonKey(
    
    name: r'sizeKm',
    required: false,
    includeIfNull: false,
  )


  final num? sizeKm;



  @JsonKey(
    
    name: r'measure',
    required: false,
    includeIfNull: false,
  )


  final String? measure;



  @JsonKey(
    
    name: r'backend',
    required: false,
    includeIfNull: false,
  )


  final StatsGridBackendEnum? backend;



  @JsonKey(
    
    name: r'features',
    required: true,
    includeIfNull: false,
  )


  final List<StatsGridFeaturesInner> features;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StatsGrid &&
      other.type == type &&
      other.cellSystem == cellSystem &&
      other.res == res &&
      other.sizeKm == sizeKm &&
      other.measure == measure &&
      other.backend == backend &&
      other.features == features;

    @override
    int get hashCode =>
        type.hashCode +
        cellSystem.hashCode +
        (res == null ? 0 : res.hashCode) +
        (sizeKm == null ? 0 : sizeKm.hashCode) +
        measure.hashCode +
        backend.hashCode +
        features.hashCode;

  factory StatsGrid.fromJson(Map<String, dynamic> json) => _$StatsGridFromJson(json);

  Map<String, dynamic> toJson() => _$StatsGridToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StatsGridTypeEnum {
@JsonValue(r'FeatureCollection')
featureCollection(r'FeatureCollection');

const StatsGridTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum StatsGridCellSystemEnum {
@JsonValue(r'h3')
h3(r'h3'),
@JsonValue(r'hex3857')
hex3857(r'hex3857');

const StatsGridCellSystemEnum(this.value);

final String value;

@override
String toString() => value;
}


enum StatsGridBackendEnum {
@JsonValue(r'rollup')
rollup(r'rollup'),
@JsonValue(r'raw')
raw(r'raw');

const StatsGridBackendEnum(this.value);

final String value;

@override
String toString() => value;
}


