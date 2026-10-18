//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'regional_index_def_floors_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegionalIndexDefFloorsInner {
  /// Returns a new [RegionalIndexDefFloorsInner] instance.
  RegionalIndexDefFloorsInner({

     this.id,

    required  this.description,

     this.condition,

    required  this.minValue,
  });

  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



  @JsonKey(
    
    name: r'description',
    required: true,
    includeIfNull: false,
  )


  final String description;



  @JsonKey(
    
    name: r'condition',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? condition;



          // minimum: 0
          // maximum: 100
  @JsonKey(
    
    name: r'minValue',
    required: true,
    includeIfNull: false,
  )


  final num minValue;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegionalIndexDefFloorsInner &&
      other.id == id &&
      other.description == description &&
      other.condition == condition &&
      other.minValue == minValue;

    @override
    int get hashCode =>
        id.hashCode +
        description.hashCode +
        condition.hashCode +
        minValue.hashCode;

  factory RegionalIndexDefFloorsInner.fromJson(Map<String, dynamic> json) => _$RegionalIndexDefFloorsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$RegionalIndexDefFloorsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

