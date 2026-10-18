//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record_mapping_tombstone.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecordMappingTombstone {
  /// Returns a new [RecordMappingTombstone] instance.
  RecordMappingTombstone({

    required  this.path,

     this.equals,

     this.in_,
  });

  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final String path;



  @JsonKey(
    
    name: r'equals',
    required: false,
    includeIfNull: false,
  )


  final Object? equals;



  @JsonKey(
    
    name: r'in',
    required: false,
    includeIfNull: false,
  )


  final List<Object>? in_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecordMappingTombstone &&
      other.path == path &&
      other.equals == equals &&
      other.in_ == in_;

    @override
    int get hashCode =>
        path.hashCode +
        (equals == null ? 0 : equals.hashCode) +
        in_.hashCode;

  factory RecordMappingTombstone.fromJson(Map<String, dynamic> json) => _$RecordMappingTombstoneFromJson(json);

  Map<String, dynamic> toJson() => _$RecordMappingTombstoneToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

