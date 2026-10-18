//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'originator.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Originator {
  /// Returns a new [Originator] instance.
  Originator({

    required  this.id,

    required  this.class_,

     this.independenceGroup,

     this.aggregates,

     this.notes,
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


  final OriginatorClass_Enum class_;



      /// Sources in the same group are never independent of each other
  @JsonKey(
    
    name: r'independenceGroup',
    required: false,
    includeIfNull: false,
  )


  final String? independenceGroup;



      /// Originator ids this originator republishes
  @JsonKey(
    
    name: r'aggregates',
    required: false,
    includeIfNull: false,
  )


  final List<String>? aggregates;



  @JsonKey(
    
    name: r'notes',
    required: false,
    includeIfNull: false,
  )


  final String? notes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Originator &&
      other.id == id &&
      other.class_ == class_ &&
      other.independenceGroup == independenceGroup &&
      other.aggregates == aggregates &&
      other.notes == notes;

    @override
    int get hashCode =>
        id.hashCode +
        class_.hashCode +
        (independenceGroup == null ? 0 : independenceGroup.hashCode) +
        aggregates.hashCode +
        (notes == null ? 0 : notes.hashCode);

  factory Originator.fromJson(Map<String, dynamic> json) => _$OriginatorFromJson(json);

  Map<String, dynamic> toJson() => _$OriginatorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OriginatorClass_Enum {
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
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'derived')
derived(r'derived'),
@JsonValue(r'social')
social(r'social');

const OriginatorClass_Enum(this.value);

final String value;

@override
String toString() => value;
}


