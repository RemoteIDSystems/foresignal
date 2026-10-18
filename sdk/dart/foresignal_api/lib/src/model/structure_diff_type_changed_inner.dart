//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'structure_diff_type_changed_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StructureDiffTypeChangedInner {
  /// Returns a new [StructureDiffTypeChangedInner] instance.
  StructureDiffTypeChangedInner({

    required  this.path,

    required  this.from,

    required  this.to,
  });

  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final String path;



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
    bool operator ==(Object other) => identical(this, other) || other is StructureDiffTypeChangedInner &&
      other.path == path &&
      other.from == from &&
      other.to == to;

    @override
    int get hashCode =>
        path.hashCode +
        from.hashCode +
        to.hashCode;

  factory StructureDiffTypeChangedInner.fromJson(Map<String, dynamic> json) => _$StructureDiffTypeChangedInnerFromJson(json);

  Map<String, dynamic> toJson() => _$StructureDiffTypeChangedInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

