//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/system_info_history_tiers_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_history.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoHistory {
  /// Returns a new [SystemInfoHistory] instance.
  SystemInfoHistory({

     this.tiers,

     this.versionsPerMinute,

     this.projectedBytes,
  });

  @JsonKey(
    
    name: r'tiers',
    required: false,
    includeIfNull: false,
  )


  final List<SystemInfoHistoryTiersInner>? tiers;



  @JsonKey(
    
    name: r'versionsPerMinute',
    required: false,
    includeIfNull: false,
  )


  final num? versionsPerMinute;



  @JsonKey(
    
    name: r'projectedBytes',
    required: false,
    includeIfNull: false,
  )


  final int? projectedBytes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoHistory &&
      other.tiers == tiers &&
      other.versionsPerMinute == versionsPerMinute &&
      other.projectedBytes == projectedBytes;

    @override
    int get hashCode =>
        tiers.hashCode +
        versionsPerMinute.hashCode +
        projectedBytes.hashCode;

  factory SystemInfoHistory.fromJson(Map<String, dynamic> json) => _$SystemInfoHistoryFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoHistoryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

