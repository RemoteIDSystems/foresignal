//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record_mapping_pairs.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecordMappingPairs {
  /// Returns a new [RecordMappingPairs] instance.
  RecordMappingPairs({

    required  this.path,

    required  this.key,

    required  this.value,
  });

  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final String path;



      /// Member holding a pair's name
  @JsonKey(
    
    name: r'key',
    required: true,
    includeIfNull: false,
  )


  final String key;



      /// Member holding a pair's value
  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: false,
  )


  final String value;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecordMappingPairs &&
      other.path == path &&
      other.key == key &&
      other.value == value;

    @override
    int get hashCode =>
        path.hashCode +
        key.hashCode +
        value.hashCode;

  factory RecordMappingPairs.fromJson(Map<String, dynamic> json) => _$RecordMappingPairsFromJson(json);

  Map<String, dynamic> toJson() => _$RecordMappingPairsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

