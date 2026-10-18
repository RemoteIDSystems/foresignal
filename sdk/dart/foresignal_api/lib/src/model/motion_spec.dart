//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'motion_spec.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MotionSpec {
  /// Returns a new [MotionSpec] instance.
  MotionSpec({

    required  this.speedField,

    required  this.speedUnit,

     this.courseField,
  });

      /// Top-level payload key holding speed
  @JsonKey(
    
    name: r'speedField',
    required: true,
    includeIfNull: false,
  )


  final String speedField;



      /// Unit of the speed field
  @JsonKey(
    
    name: r'speedUnit',
    required: true,
    includeIfNull: false,
  )


  final String speedUnit;



      /// Top-level payload key holding course over ground (degrees)
  @JsonKey(
    
    name: r'courseField',
    required: false,
    includeIfNull: false,
  )


  final String? courseField;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MotionSpec &&
      other.speedField == speedField &&
      other.speedUnit == speedUnit &&
      other.courseField == courseField;

    @override
    int get hashCode =>
        speedField.hashCode +
        speedUnit.hashCode +
        (courseField == null ? 0 : courseField.hashCode);

  factory MotionSpec.fromJson(Map<String, dynamic> json) => _$MotionSpecFromJson(json);

  Map<String, dynamic> toJson() => _$MotionSpecToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

