//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stat_interval.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StatInterval {
  /// Returns a new [StatInterval] instance.
  StatInterval({

    required  this.lower,

    required  this.upper,

     this.level = 0.95,

    required  this.method,
  });

  @JsonKey(
    
    name: r'lower',
    required: true,
    includeIfNull: false,
  )


  final num lower;



  @JsonKey(
    
    name: r'upper',
    required: true,
    includeIfNull: false,
  )


  final num upper;



      /// Coverage level
          // minimum: 0
          // maximum: 1
  @JsonKey(
    defaultValue: 0.95,
    name: r'level',
    required: true,
    includeIfNull: false,
  )


  final num level;



      /// How it was computed
  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final StatIntervalMethodEnum method;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StatInterval &&
      other.lower == lower &&
      other.upper == upper &&
      other.level == level &&
      other.method == method;

    @override
    int get hashCode =>
        lower.hashCode +
        upper.hashCode +
        level.hashCode +
        method.hashCode;

  factory StatInterval.fromJson(Map<String, dynamic> json) => _$StatIntervalFromJson(json);

  Map<String, dynamic> toJson() => _$StatIntervalToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// How it was computed
enum StatIntervalMethodEnum {
@JsonValue(r'wilson')
wilson(r'wilson'),
@JsonValue(r'jeffreys')
jeffreys(r'jeffreys'),
@JsonValue(r'paired-bootstrap')
pairedBootstrap(r'paired-bootstrap'),
@JsonValue(r'empirical')
empirical(r'empirical');

const StatIntervalMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


