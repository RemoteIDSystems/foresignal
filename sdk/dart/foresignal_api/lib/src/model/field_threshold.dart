//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'field_threshold.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FieldThreshold {
  /// Returns a new [FieldThreshold] instance.
  FieldThreshold({

    required  this.field,

    required  this.delta,

     this.angular = false,
  });

      /// Top-level payload key (never significant)
  @JsonKey(
    
    name: r'field',
    required: true,
    includeIfNull: false,
  )


  final String field;



      /// Minimum change
          // minimum: 0
  @JsonKey(
    
    name: r'delta',
    required: true,
    includeIfNull: false,
  )


  final num delta;



      /// Compare with 360° wrap (headings, courses)
  @JsonKey(
    defaultValue: false,
    name: r'angular',
    required: false,
    includeIfNull: false,
  )


  final bool? angular;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FieldThreshold &&
      other.field == field &&
      other.delta == delta &&
      other.angular == angular;

    @override
    int get hashCode =>
        field.hashCode +
        delta.hashCode +
        angular.hashCode;

  factory FieldThreshold.fromJson(Map<String, dynamic> json) => _$FieldThresholdFromJson(json);

  Map<String, dynamic> toJson() => _$FieldThresholdToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

