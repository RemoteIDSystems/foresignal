//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record_mapping_geometry_one_of2.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecordMappingGeometryOneOf2 {
  /// Returns a new [RecordMappingGeometryOneOf2] instance.
  RecordMappingGeometryOneOf2({

    required  this.const_,
  });

  @JsonKey(
    
    name: r'const',
    required: true,
    includeIfNull: false,
  )


  final List<num> const_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecordMappingGeometryOneOf2 &&
      other.const_ == const_;

    @override
    int get hashCode =>
        const_.hashCode;

  factory RecordMappingGeometryOneOf2.fromJson(Map<String, dynamic> json) => _$RecordMappingGeometryOneOf2FromJson(json);

  Map<String, dynamic> toJson() => _$RecordMappingGeometryOneOf2ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

