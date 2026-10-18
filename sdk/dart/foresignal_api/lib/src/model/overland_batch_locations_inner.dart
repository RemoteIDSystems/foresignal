//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/overland_batch_locations_inner_properties.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'overland_batch_locations_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OverlandBatchLocationsInner {
  /// Returns a new [OverlandBatchLocationsInner] instance.
  OverlandBatchLocationsInner({

     this.type,

    required  this.geometry,

    required  this.properties,
  });

  @JsonKey(
    
    name: r'type',
    required: false,
    includeIfNull: false,
  )


  final OverlandBatchLocationsInnerTypeEnum? type;



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


  final OverlandBatchLocationsInnerProperties properties;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OverlandBatchLocationsInner &&
      other.type == type &&
      other.geometry == geometry &&
      other.properties == properties;

    @override
    int get hashCode =>
        type.hashCode +
        geometry.hashCode +
        properties.hashCode;

  factory OverlandBatchLocationsInner.fromJson(Map<String, dynamic> json) => _$OverlandBatchLocationsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$OverlandBatchLocationsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OverlandBatchLocationsInnerTypeEnum {
@JsonValue(r'Feature')
feature(r'Feature');

const OverlandBatchLocationsInnerTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


