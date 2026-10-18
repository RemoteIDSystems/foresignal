//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'regional_index_def_components_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegionalIndexDefComponentsInner {
  /// Returns a new [RegionalIndexDefComponentsInner] instance.
  RegionalIndexDefComponentsInner({

    required  this.key,

    required  this.ref,

    required  this.transform,

    required  this.window,

    required  this.direction,

     this.floor,
  });

  @JsonKey(
    
    name: r'key',
    required: true,
    includeIfNull: false,
  )


  final String key;



      /// SeriesRef template with `{region}`
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'transform',
    required: true,
    includeIfNull: false,
  )


  final RegionalIndexDefComponentsInnerTransformEnum transform;



  @JsonKey(
    
    name: r'window',
    required: true,
    includeIfNull: false,
  )


  final RegionalIndexDefComponentsInnerWindowEnum window;



  @JsonKey(
    
    name: r'direction',
    required: true,
    includeIfNull: false,
  )


  final RegionalIndexDefComponentsInnerDirectionEnum direction;



  @JsonKey(
    
    name: r'floor',
    required: false,
    includeIfNull: false,
  )


  final num? floor;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegionalIndexDefComponentsInner &&
      other.key == key &&
      other.ref == ref &&
      other.transform == transform &&
      other.window == window &&
      other.direction == direction &&
      other.floor == floor;

    @override
    int get hashCode =>
        key.hashCode +
        ref.hashCode +
        transform.hashCode +
        window.hashCode +
        direction.hashCode +
        (floor == null ? 0 : floor.hashCode);

  factory RegionalIndexDefComponentsInner.fromJson(Map<String, dynamic> json) => _$RegionalIndexDefComponentsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$RegionalIndexDefComponentsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RegionalIndexDefComponentsInnerTransformEnum {
@JsonValue(r'log1p')
log1p(r'log1p'),
@JsonValue(r'zscore')
zscore(r'zscore'),
@JsonValue(r'pctrank')
pctrank(r'pctrank'),
@JsonValue(r'level')
level(r'level');

const RegionalIndexDefComponentsInnerTransformEnum(this.value);

final String value;

@override
String toString() => value;
}


enum RegionalIndexDefComponentsInnerWindowEnum {
@JsonValue(r'P7D')
P7D(r'P7D'),
@JsonValue(r'P28D')
P28D(r'P28D'),
@JsonValue(r'P90D')
P90D(r'P90D');

const RegionalIndexDefComponentsInnerWindowEnum(this.value);

final String value;

@override
String toString() => value;
}


enum RegionalIndexDefComponentsInnerDirectionEnum {
@JsonValue(1)
number1('1'),
@JsonValue(-1)
numberNegative1('-1');

const RegionalIndexDefComponentsInnerDirectionEnum(this.value);

final String value;

@override
String toString() => value;
}


