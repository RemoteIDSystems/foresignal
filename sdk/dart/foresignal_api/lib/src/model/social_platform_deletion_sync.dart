//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_platform_deletion_sync.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPlatformDeletionSync {
  /// Returns a new [SocialPlatformDeletionSync] instance.
  SocialPlatformDeletionSync({

    required  this.enabled,

     this.lastRunAt,

     this.tombstonedLast30d,
  });

  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'lastRunAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastRunAt;



          // minimum: 0
  @JsonKey(
    
    name: r'tombstonedLast30d',
    required: false,
    includeIfNull: false,
  )


  final int? tombstonedLast30d;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPlatformDeletionSync &&
      other.enabled == enabled &&
      other.lastRunAt == lastRunAt &&
      other.tombstonedLast30d == tombstonedLast30d;

    @override
    int get hashCode =>
        enabled.hashCode +
        (lastRunAt == null ? 0 : lastRunAt.hashCode) +
        tombstonedLast30d.hashCode;

  factory SocialPlatformDeletionSync.fromJson(Map<String, dynamic> json) => _$SocialPlatformDeletionSyncFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPlatformDeletionSyncToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

