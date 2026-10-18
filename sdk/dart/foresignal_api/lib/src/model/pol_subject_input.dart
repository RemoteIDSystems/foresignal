//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/pol_subject_input_params.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pol_subject_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PolSubjectInput {
  /// Returns a new [PolSubjectInput] instance.
  PolSubjectInput({

    required  this.subjectType,

    required  this.ref,

     this.enabled = true,

     this.params,
  });

  @JsonKey(
    
    name: r'subjectType',
    required: true,
    includeIfNull: false,
  )


  final PolSubjectInputSubjectTypeEnum subjectType;



      /// record: `{sourceId}/{externalId}`; entity: 098 entity id; watchlist: 036 watchlist id (every member)
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final PolSubjectInputParams? params;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PolSubjectInput &&
      other.subjectType == subjectType &&
      other.ref == ref &&
      other.enabled == enabled &&
      other.params == params;

    @override
    int get hashCode =>
        subjectType.hashCode +
        ref.hashCode +
        enabled.hashCode +
        params.hashCode;

  factory PolSubjectInput.fromJson(Map<String, dynamic> json) => _$PolSubjectInputFromJson(json);

  Map<String, dynamic> toJson() => _$PolSubjectInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PolSubjectInputSubjectTypeEnum {
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'entity')
entity(r'entity'),
@JsonValue(r'watchlist')
watchlist(r'watchlist');

const PolSubjectInputSubjectTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


