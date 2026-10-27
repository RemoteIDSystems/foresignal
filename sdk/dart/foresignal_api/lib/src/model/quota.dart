//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'quota.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Quota {
  /// Returns a new [Quota] instance.
  Quota({

     this.apiRateLimitPerMinute,

     this.maxStorageBytes,

     this.maxAlerts,

     this.maxChannels,

     this.maxSavedViews,
  });

      /// Requests a minute across the user's sessions and own API tokens; over it is 429 RATE_LIMITED
          // minimum: 0
  @JsonKey(
    
    name: r'apiRateLimitPerMinute',
    required: false,
    includeIfNull: false,
  )


  final int? apiRateLimitPerMinute;



      /// Bytes of uploaded attachments plus the JSON of owned workspaces
          // minimum: 0
  @JsonKey(
    
    name: r'maxStorageBytes',
    required: false,
    includeIfNull: false,
  )


  final int? maxStorageBytes;



      /// Trigger rules the user owns
          // minimum: 0
  @JsonKey(
    
    name: r'maxAlerts',
    required: false,
    includeIfNull: false,
  )


  final int? maxAlerts;



      /// Notification channels the user owns
          // minimum: 0
  @JsonKey(
    
    name: r'maxChannels',
    required: false,
    includeIfNull: false,
  )


  final int? maxChannels;



      /// Saved views the user owns
          // minimum: 0
  @JsonKey(
    
    name: r'maxSavedViews',
    required: false,
    includeIfNull: false,
  )


  final int? maxSavedViews;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Quota &&
      other.apiRateLimitPerMinute == apiRateLimitPerMinute &&
      other.maxStorageBytes == maxStorageBytes &&
      other.maxAlerts == maxAlerts &&
      other.maxChannels == maxChannels &&
      other.maxSavedViews == maxSavedViews;

    @override
    int get hashCode =>
        (apiRateLimitPerMinute == null ? 0 : apiRateLimitPerMinute.hashCode) +
        (maxStorageBytes == null ? 0 : maxStorageBytes.hashCode) +
        (maxAlerts == null ? 0 : maxAlerts.hashCode) +
        (maxChannels == null ? 0 : maxChannels.hashCode) +
        (maxSavedViews == null ? 0 : maxSavedViews.hashCode);

  factory Quota.fromJson(Map<String, dynamic> json) => _$QuotaFromJson(json);

  Map<String, dynamic> toJson() => _$QuotaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

