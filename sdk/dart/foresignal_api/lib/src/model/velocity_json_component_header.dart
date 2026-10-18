//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'velocity_json_component_header.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class VelocityJsonComponentHeader {
  /// Returns a new [VelocityJsonComponentHeader] instance.
  VelocityJsonComponentHeader({

    required  this.parameterCategory,

    required  this.parameterNumber,

    required  this.lo1,

    required  this.la1,

    required  this.lo2,

    required  this.la2,

    required  this.dx,

    required  this.dy,

    required  this.nx,

    required  this.ny,

    required  this.refTime,

    required  this.forecastTime,
  });

  @JsonKey(
    
    name: r'parameterCategory',
    required: true,
    includeIfNull: false,
  )


  final VelocityJsonComponentHeaderParameterCategoryEnum parameterCategory;



      /// 2 = U, 3 = V
  @JsonKey(
    
    name: r'parameterNumber',
    required: true,
    includeIfNull: false,
  )


  final VelocityJsonComponentHeaderParameterNumberEnum parameterNumber;



  @JsonKey(
    
    name: r'lo1',
    required: true,
    includeIfNull: false,
  )


  final num lo1;



  @JsonKey(
    
    name: r'la1',
    required: true,
    includeIfNull: false,
  )


  final num la1;



  @JsonKey(
    
    name: r'lo2',
    required: true,
    includeIfNull: false,
  )


  final num lo2;



  @JsonKey(
    
    name: r'la2',
    required: true,
    includeIfNull: false,
  )


  final num la2;



  @JsonKey(
    
    name: r'dx',
    required: true,
    includeIfNull: false,
  )


  final num dx;



  @JsonKey(
    
    name: r'dy',
    required: true,
    includeIfNull: false,
  )


  final num dy;



  @JsonKey(
    
    name: r'nx',
    required: true,
    includeIfNull: false,
  )


  final int nx;



  @JsonKey(
    
    name: r'ny',
    required: true,
    includeIfNull: false,
  )


  final int ny;



  @JsonKey(
    
    name: r'refTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime refTime;



      /// Hours after refTime
  @JsonKey(
    
    name: r'forecastTime',
    required: true,
    includeIfNull: false,
  )


  final int forecastTime;





    @override
    bool operator ==(Object other) => identical(this, other) || other is VelocityJsonComponentHeader &&
      other.parameterCategory == parameterCategory &&
      other.parameterNumber == parameterNumber &&
      other.lo1 == lo1 &&
      other.la1 == la1 &&
      other.lo2 == lo2 &&
      other.la2 == la2 &&
      other.dx == dx &&
      other.dy == dy &&
      other.nx == nx &&
      other.ny == ny &&
      other.refTime == refTime &&
      other.forecastTime == forecastTime;

    @override
    int get hashCode =>
        parameterCategory.hashCode +
        parameterNumber.hashCode +
        lo1.hashCode +
        la1.hashCode +
        lo2.hashCode +
        la2.hashCode +
        dx.hashCode +
        dy.hashCode +
        nx.hashCode +
        ny.hashCode +
        refTime.hashCode +
        forecastTime.hashCode;

  factory VelocityJsonComponentHeader.fromJson(Map<String, dynamic> json) => _$VelocityJsonComponentHeaderFromJson(json);

  Map<String, dynamic> toJson() => _$VelocityJsonComponentHeaderToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum VelocityJsonComponentHeaderParameterCategoryEnum {
@JsonValue(2)
number2('2');

const VelocityJsonComponentHeaderParameterCategoryEnum(this.value);

final String value;

@override
String toString() => value;
}


/// 2 = U, 3 = V
enum VelocityJsonComponentHeaderParameterNumberEnum {
@JsonValue(2)
number2('2'),
@JsonValue(3)
number3('3');

const VelocityJsonComponentHeaderParameterNumberEnum(this.value);

final String value;

@override
String toString() => value;
}


