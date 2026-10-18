//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analog_query.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalogQuery {
  /// Returns a new [AnalogQuery] instance.
  AnalogQuery({

    required  this.regionCode,

    required  this.from,

    required  this.to,

     this.k = 10,

     this.outcome,
  });

      /// 040 region code: ISO 3166-1 alpha-2 country, `CC-admin1`, or `ZZ` for untagged
  @JsonKey(
    
    name: r'regionCode',
    required: true,
    includeIfNull: false,
  )


  final String regionCode;



  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final DateTime to;



          // minimum: 1
          // maximum: 50
  @JsonKey(
    defaultValue: 10,
    name: r'k',
    required: false,
    includeIfNull: false,
  )


  final int? k;



      /// Target outcome: `critical` (situation reaches critical) or `{family}/P{n}D` (a family within n days), e.g. `conflict:battles/P30D`
  @JsonKey(
    
    name: r'outcome',
    required: false,
    includeIfNull: false,
  )


  final String? outcome;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalogQuery &&
      other.regionCode == regionCode &&
      other.from == from &&
      other.to == to &&
      other.k == k &&
      other.outcome == outcome;

    @override
    int get hashCode =>
        regionCode.hashCode +
        from.hashCode +
        to.hashCode +
        k.hashCode +
        outcome.hashCode;

  factory AnalogQuery.fromJson(Map<String, dynamic> json) => _$AnalogQueryFromJson(json);

  Map<String, dynamic> toJson() => _$AnalogQueryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

