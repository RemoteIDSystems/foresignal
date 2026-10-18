//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'retention_settings_stats.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RetentionSettingsStats {
  /// Returns a new [RetentionSettingsStats] instance.
  RetentionSettingsStats({

     this.hourly = '35d',

     this.daily = '1825d',

     this.regionHourly = '1095d',

     this.regionDaily = '3650d',
  });

      /// H3 hourly rollups (038)
  @JsonKey(
    defaultValue: '35d',
    name: r'hourly',
    required: false,
    includeIfNull: false,
  )


  final String? hourly;



      /// H3 daily rollups (038)
  @JsonKey(
    defaultValue: '1825d',
    name: r'daily',
    required: false,
    includeIfNull: false,
  )


  final String? daily;



      /// Region-hour rollups (resolution 2)
  @JsonKey(
    defaultValue: '1095d',
    name: r'regionHourly',
    required: false,
    includeIfNull: false,
  )


  final String? regionHourly;



      /// Region-day rollups (resolution 3)
  @JsonKey(
    defaultValue: '3650d',
    name: r'regionDaily',
    required: false,
    includeIfNull: false,
  )


  final String? regionDaily;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RetentionSettingsStats &&
      other.hourly == hourly &&
      other.daily == daily &&
      other.regionHourly == regionHourly &&
      other.regionDaily == regionDaily;

    @override
    int get hashCode =>
        hourly.hashCode +
        daily.hashCode +
        regionHourly.hashCode +
        regionDaily.hashCode;

  factory RetentionSettingsStats.fromJson(Map<String, dynamic> json) => _$RetentionSettingsStatsFromJson(json);

  Map<String, dynamic> toJson() => _$RetentionSettingsStatsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

