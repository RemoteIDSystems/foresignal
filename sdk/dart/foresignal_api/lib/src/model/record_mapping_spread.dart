//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record_mapping_spread.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecordMappingSpread {
  /// Returns a new [RecordMappingSpread] instance.
  RecordMappingSpread({

    required  this.path,

     this.type,

     this.max = 32,
  });

      /// JSONPath of the object
  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final String path;



      /// Type every copied value must have; others are skipped
  @JsonKey(
    
    name: r'type',
    required: false,
    includeIfNull: false,
  )


  final RecordMappingSpreadTypeEnum? type;



          // minimum: 1
          // maximum: 32
  @JsonKey(
    defaultValue: 32,
    name: r'max',
    required: false,
    includeIfNull: false,
  )


  final int? max;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecordMappingSpread &&
      other.path == path &&
      other.type == type &&
      other.max == max;

    @override
    int get hashCode =>
        path.hashCode +
        type.hashCode +
        max.hashCode;

  factory RecordMappingSpread.fromJson(Map<String, dynamic> json) => _$RecordMappingSpreadFromJson(json);

  Map<String, dynamic> toJson() => _$RecordMappingSpreadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Type every copied value must have; others are skipped
enum RecordMappingSpreadTypeEnum {
@JsonValue(r'number')
number(r'number'),
@JsonValue(r'integer')
integer(r'integer'),
@JsonValue(r'string')
string(r'string'),
@JsonValue(r'boolean')
boolean(r'boolean');

const RecordMappingSpreadTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


