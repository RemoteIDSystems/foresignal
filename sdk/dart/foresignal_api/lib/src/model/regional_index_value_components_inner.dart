//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'regional_index_value_components_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegionalIndexValueComponentsInner {
  /// Returns a new [RegionalIndexValueComponentsInner] instance.
  RegionalIndexValueComponentsInner({

    required  this.key,

     this.raw,

    required  this.normalised,

    required  this.weight,

    required  this.contribution,

     this.missing,
  });

  @JsonKey(
    
    name: r'key',
    required: true,
    includeIfNull: false,
  )


  final String key;



  @JsonKey(
    
    name: r'raw',
    required: false,
    includeIfNull: false,
  )


  final num? raw;



          // minimum: 0
          // maximum: 100
  @JsonKey(
    
    name: r'normalised',
    required: true,
    includeIfNull: false,
  )


  final num normalised;



  @JsonKey(
    
    name: r'weight',
    required: true,
    includeIfNull: false,
  )


  final num weight;



  @JsonKey(
    
    name: r'contribution',
    required: true,
    includeIfNull: false,
  )


  final num contribution;



      /// True when the component had no value; its weight went to the others
  @JsonKey(
    
    name: r'missing',
    required: false,
    includeIfNull: false,
  )


  final bool? missing;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegionalIndexValueComponentsInner &&
      other.key == key &&
      other.raw == raw &&
      other.normalised == normalised &&
      other.weight == weight &&
      other.contribution == contribution &&
      other.missing == missing;

    @override
    int get hashCode =>
        key.hashCode +
        (raw == null ? 0 : raw.hashCode) +
        normalised.hashCode +
        weight.hashCode +
        contribution.hashCode +
        missing.hashCode;

  factory RegionalIndexValueComponentsInner.fromJson(Map<String, dynamic> json) => _$RegionalIndexValueComponentsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$RegionalIndexValueComponentsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

