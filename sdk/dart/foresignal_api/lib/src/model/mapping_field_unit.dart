//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_field_unit.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingFieldUnit {
  /// Returns a new [MappingFieldUnit] instance.
  MappingFieldUnit({

    required  this.from,

    required  this.to,
  });

  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final String from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final String to;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingFieldUnit &&
      other.from == from &&
      other.to == to;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode;

  factory MappingFieldUnit.fromJson(Map<String, dynamic> json) => _$MappingFieldUnitFromJson(json);

  Map<String, dynamic> toJson() => _$MappingFieldUnitToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

