//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'structure_diff_content_type_changed.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StructureDiffContentTypeChanged {
  /// Returns a new [StructureDiffContentTypeChanged] instance.
  StructureDiffContentTypeChanged({

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
    bool operator ==(Object other) => identical(this, other) || other is StructureDiffContentTypeChanged &&
      other.from == from &&
      other.to == to;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode;

  factory StructureDiffContentTypeChanged.fromJson(Map<String, dynamic> json) => _$StructureDiffContentTypeChangedFromJson(json);

  Map<String, dynamic> toJson() => _$StructureDiffContentTypeChangedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

