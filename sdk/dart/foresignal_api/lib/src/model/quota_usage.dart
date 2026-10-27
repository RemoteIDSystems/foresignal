//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/quota.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'quota_usage.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class QuotaUsage {
  /// Returns a new [QuotaUsage] instance.
  QuotaUsage({

    required  this.quota,

    required  this.usage,
  });

  @JsonKey(
    
    name: r'quota',
    required: true,
    includeIfNull: false,
  )


  final Quota quota;



      /// Current usage: `alerts`, `channels`, `savedViews`, `storageBytes` and `apiRequestsThisMinute`
  @JsonKey(
    
    name: r'usage',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> usage;





    @override
    bool operator ==(Object other) => identical(this, other) || other is QuotaUsage &&
      other.quota == quota &&
      other.usage == usage;

    @override
    int get hashCode =>
        quota.hashCode +
        usage.hashCode;

  factory QuotaUsage.fromJson(Map<String, dynamic> json) => _$QuotaUsageFromJson(json);

  Map<String, dynamic> toJson() => _$QuotaUsageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

