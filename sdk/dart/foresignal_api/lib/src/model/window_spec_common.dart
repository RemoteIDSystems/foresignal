//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'window_spec_common.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WindowSpecCommon {
  /// Returns a new [WindowSpecCommon] instance.
  WindowSpecCommon({

    required  this.window,

     this.step,

     this.distinctBy = 'entity',

     this.groupBy,

     this.maxGroups = 500,
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





    @override
    bool operator ==(Object other) => identical(this, other) || other is WindowSpecCommon &&
      other.window == window &&
      other.step == step &&
      other.distinctBy == distinctBy &&
      other.groupBy == groupBy &&
      other.maxGroups == maxGroups;

    @override
    int get hashCode =>
        window.hashCode +
        step.hashCode +
        distinctBy.hashCode +
        (groupBy == null ? 0 : groupBy.hashCode) +
        maxGroups.hashCode;

  factory WindowSpecCommon.fromJson(Map<String, dynamic> json) => _$WindowSpecCommonFromJson(json);

  Map<String, dynamic> toJson() => _$WindowSpecCommonToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

