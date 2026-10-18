//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_source_info_originator.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpSourceInfoOriginator {
  /// Returns a new [EdpSourceInfoOriginator] instance.
  EdpSourceInfoOriginator({

    required  this.id,

    required  this.class_,

     this.aggregates,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'class',
    required: true,
    includeIfNull: false,
  )


  final EdpSourceInfoOriginatorClass_Enum class_;



  @JsonKey(
    
    name: r'aggregates',
    required: false,
    includeIfNull: false,
  )


  final List<String>? aggregates;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpSourceInfoOriginator &&
      other.id == id &&
      other.class_ == class_ &&
      other.aggregates == aggregates;

    @override
    int get hashCode =>
        id.hashCode +
        class_.hashCode +
        aggregates.hashCode;

  factory EdpSourceInfoOriginator.fromJson(Map<String, dynamic> json) => _$EdpSourceInfoOriginatorFromJson(json);

  Map<String, dynamic> toJson() => _$EdpSourceInfoOriginatorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpSourceInfoOriginatorClass_Enum {
@JsonValue(r'sensor-network')
sensorNetwork(r'sensor-network'),
@JsonValue(r'agency')
agency(r'agency'),
@JsonValue(r'aggregator')
aggregator(r'aggregator'),
@JsonValue(r'media')
media(r'media'),
@JsonValue(r'crowd')
crowd(r'crowd'),
@JsonValue(r'social')
social(r'social'),
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'derived')
derived(r'derived');

const EdpSourceInfoOriginatorClass_Enum(this.value);

final String value;

@override
String toString() => value;
}


