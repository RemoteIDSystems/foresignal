//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'absence_window.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AbsenceWindow {
  /// Returns a new [AbsenceWindow] instance.
  AbsenceWindow({

    required  this.window,

     this.step,

     this.distinctBy = 'entity',

     this.groupBy,

     this.maxGroups = 500,

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



      /// Minimum OK/NOT_MODIFIED fetch share in the window
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
    bool operator ==(Object other) => identical(this, other) || other is AbsenceWindow &&
      other.window == window &&
      other.step == step &&
      other.distinctBy == distinctBy &&
      other.groupBy == groupBy &&
      other.maxGroups == maxGroups &&
      other.coverageMin == coverageMin;

    @override
    int get hashCode =>
        window.hashCode +
        step.hashCode +
        distinctBy.hashCode +
        (groupBy == null ? 0 : groupBy.hashCode) +
        maxGroups.hashCode +
        coverageMin.hashCode;

  factory AbsenceWindow.fromJson(Map<String, dynamic> json) => _$AbsenceWindowFromJson(json);

  Map<String, dynamic> toJson() => _$AbsenceWindowToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

