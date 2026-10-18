//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'structure_diff_delimiter_changed.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StructureDiffDelimiterChanged {
  /// Returns a new [StructureDiffDelimiterChanged] instance.
  StructureDiffDelimiterChanged({

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
    bool operator ==(Object other) => identical(this, other) || other is StructureDiffDelimiterChanged &&
      other.from == from &&
      other.to == to;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode;

  factory StructureDiffDelimiterChanged.fromJson(Map<String, dynamic> json) => _$StructureDiffDelimiterChangedFromJson(json);

  Map<String, dynamic> toJson() => _$StructureDiffDelimiterChangedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

