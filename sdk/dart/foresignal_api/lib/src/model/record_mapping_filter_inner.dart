//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record_mapping_filter_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecordMappingFilterInner {
  /// Returns a new [RecordMappingFilterInner] instance.
  RecordMappingFilterInner({

    required  this.path,

    required  this.op,

     this.value,
  });

  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final String path;



  @JsonKey(
    
    name: r'op',
    required: true,
    includeIfNull: false,
  )


  final RecordMappingFilterInnerOpEnum op;



  @JsonKey(
    
    name: r'value',
    required: false,
    includeIfNull: false,
  )


  final Object? value;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecordMappingFilterInner &&
      other.path == path &&
      other.op == op &&
      other.value == value;

    @override
    int get hashCode =>
        path.hashCode +
        op.hashCode +
        (value == null ? 0 : value.hashCode);

  factory RecordMappingFilterInner.fromJson(Map<String, dynamic> json) => _$RecordMappingFilterInnerFromJson(json);

  Map<String, dynamic> toJson() => _$RecordMappingFilterInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RecordMappingFilterInnerOpEnum {
@JsonValue(r'==')
equalEqual(r'=='),
@JsonValue(r'!=')
exclamationEqual(r'!='),
@JsonValue(r'exists')
exists(r'exists'),
@JsonValue(r'in')
in_(r'in');

const RecordMappingFilterInnerOpEnum(this.value);

final String value;

@override
String toString() => value;
}


