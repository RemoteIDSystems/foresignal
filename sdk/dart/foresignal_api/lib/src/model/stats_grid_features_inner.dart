//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/stats_grid_features_inner_properties.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stats_grid_features_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StatsGridFeaturesInner {
  /// Returns a new [StatsGridFeaturesInner] instance.
  StatsGridFeaturesInner({

    required  this.type,

     this.id,

    required  this.geometry,

    required  this.properties,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final StatsGridFeaturesInnerTypeEnum type;



  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



  @JsonKey(
    
    name: r'geometry',
    required: true,
    includeIfNull: false,
  )


  final Geometry geometry;



  @JsonKey(
    
    name: r'properties',
    required: true,
    includeIfNull: false,
  )


  final StatsGridFeaturesInnerProperties properties;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StatsGridFeaturesInner &&
      other.type == type &&
      other.id == id &&
      other.geometry == geometry &&
      other.properties == properties;

    @override
    int get hashCode =>
        type.hashCode +
        id.hashCode +
        geometry.hashCode +
        properties.hashCode;

  factory StatsGridFeaturesInner.fromJson(Map<String, dynamic> json) => _$StatsGridFeaturesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$StatsGridFeaturesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StatsGridFeaturesInnerTypeEnum {
@JsonValue(r'Feature')
feature(r'Feature');

const StatsGridFeaturesInnerTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


