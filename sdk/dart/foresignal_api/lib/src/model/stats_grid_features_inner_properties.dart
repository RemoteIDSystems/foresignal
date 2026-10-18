//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stats_grid_features_inner_properties.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StatsGridFeaturesInnerProperties {
  /// Returns a new [StatsGridFeaturesInnerProperties] instance.
  StatsGridFeaturesInnerProperties({

     this.cell,

    required  this.value,
  });

  @JsonKey(
    
    name: r'cell',
    required: false,
    includeIfNull: false,
  )


  final String? cell;



  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: false,
  )


  final int value;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StatsGridFeaturesInnerProperties &&
      other.cell == cell &&
      other.value == value;

    @override
    int get hashCode =>
        cell.hashCode +
        value.hashCode;

  factory StatsGridFeaturesInnerProperties.fromJson(Map<String, dynamic> json) => _$StatsGridFeaturesInnerPropertiesFromJson(json);

  Map<String, dynamic> toJson() => _$StatsGridFeaturesInnerPropertiesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

