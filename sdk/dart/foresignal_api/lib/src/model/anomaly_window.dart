//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'anomaly_window.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnomalyWindow {
  /// Returns a new [AnomalyWindow] instance.
  AnomalyWindow({

    required  this.window,

     this.step,

     this.distinctBy = 'entity',

     this.groupBy,

     this.maxGroups = 500,

     this.bucket,

     this.slotMode,

     this.direction,

     this.sensitivity,

     this.minAbsoluteDelta = 3,

     this.minWeeks = 8,

     this.coverageMin = 0.8,
  });

      /// Window length, 1m to 7d. ISO-8601 duration or shorthand (30m, 6h, 7d).
  @JsonKey(
    
    name: r'window',
    required: true,
    includeIfNull: false,
  )


  final String window;



      /// Evaluation period; default min(window/4, 5m), minimum 30s. ISO-8601 duration or shorthand (30m, 6h, 7d).
  @JsonKey(
    
    name: r'step',
    required: false,
    includeIfNull: false,
  )


  final String? step;



      /// `entity` (098 entity, else descriptor entityKeys, else source+externalId), `externalId`, or a payload path. Versions are never counted.
  @JsonKey(
    defaultValue: 'entity',
    name: r'distinctBy',
    required: false,
    includeIfNull: false,
  )


  final String? distinctBy;



      /// null | `source` | `place` | `h3:<res 3..8>` | `admin1` | `country` | a payload path
  @JsonKey(
    
    name: r'groupBy',
    required: false,
    includeIfNull: false,
  )


  final String? groupBy;



          // minimum: 1
          // maximum: 500
  @JsonKey(
    defaultValue: 500,
    name: r'maxGroups',
    required: false,
    includeIfNull: false,
  )


  final int? maxGroups;



  @JsonKey(
    
    name: r'bucket',
    required: false,
    includeIfNull: false,
  )


  final AnomalyWindowBucketEnum? bucket;



  @JsonKey(
    
    name: r'slotMode',
    required: false,
    includeIfNull: false,
  )


  final AnomalyWindowSlotModeEnum? slotMode;



  @JsonKey(
    
    name: r'direction',
    required: false,
    includeIfNull: false,
  )


  final AnomalyWindowDirectionEnum? direction;



      /// α 1e-4 / 1e-3 / 1e-2
  @JsonKey(
    
    name: r'sensitivity',
    required: false,
    includeIfNull: false,
  )


  final AnomalyWindowSensitivityEnum? sensitivity;



          // minimum: 0
  @JsonKey(
    defaultValue: 3,
    name: r'minAbsoluteDelta',
    required: false,
    includeIfNull: false,
  )


  final num? minAbsoluteDelta;



          // minimum: 8
          // maximum: 12
  @JsonKey(
    defaultValue: 8,
    name: r'minWeeks',
    required: false,
    includeIfNull: false,
  )


  final int? minWeeks;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    defaultValue: 0.8,
    name: r'coverageMin',
    required: false,
    includeIfNull: false,
  )


  final num? coverageMin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnomalyWindow &&
      other.window == window &&
      other.step == step &&
      other.distinctBy == distinctBy &&
      other.groupBy == groupBy &&
      other.maxGroups == maxGroups &&
      other.bucket == bucket &&
      other.slotMode == slotMode &&
      other.direction == direction &&
      other.sensitivity == sensitivity &&
      other.minAbsoluteDelta == minAbsoluteDelta &&
      other.minWeeks == minWeeks &&
      other.coverageMin == coverageMin;

    @override
    int get hashCode =>
        window.hashCode +
        step.hashCode +
        distinctBy.hashCode +
        (groupBy == null ? 0 : groupBy.hashCode) +
        maxGroups.hashCode +
        bucket.hashCode +
        slotMode.hashCode +
        direction.hashCode +
        sensitivity.hashCode +
        minAbsoluteDelta.hashCode +
        minWeeks.hashCode +
        coverageMin.hashCode;

  factory AnomalyWindow.fromJson(Map<String, dynamic> json) => _$AnomalyWindowFromJson(json);

  Map<String, dynamic> toJson() => _$AnomalyWindowToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnomalyWindowBucketEnum {
@JsonValue(r'15m')
n15m(r'15m'),
@JsonValue(r'1h')
n1h(r'1h');

const AnomalyWindowBucketEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnomalyWindowSlotModeEnum {
@JsonValue(r'hour-of-week')
hourOfWeek(r'hour-of-week'),
@JsonValue(r'hour-of-day')
hourOfDay(r'hour-of-day');

const AnomalyWindowSlotModeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnomalyWindowDirectionEnum {
@JsonValue(r'up')
up(r'up'),
@JsonValue(r'down')
down(r'down'),
@JsonValue(r'both')
both(r'both');

const AnomalyWindowDirectionEnum(this.value);

final String value;

@override
String toString() => value;
}


/// α 1e-4 / 1e-3 / 1e-2
enum AnomalyWindowSensitivityEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'medium')
medium(r'medium'),
@JsonValue(r'high')
high(r'high');

const AnomalyWindowSensitivityEnum(this.value);

final String value;

@override
String toString() => value;
}


