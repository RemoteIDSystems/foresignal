// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_platform_deletion_sync.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPlatformDeletionSyncCWProxy {
  SocialPlatformDeletionSync enabled(bool enabled);

  SocialPlatformDeletionSync lastRunAt(DateTime? lastRunAt);

  SocialPlatformDeletionSync tombstonedLast30d(int? tombstonedLast30d);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformDeletionSync(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformDeletionSync(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPlatformDeletionSync call({
    bool enabled,
    DateTime? lastRunAt,
    int? tombstonedLast30d,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPlatformDeletionSync.copyWith(...)` or call `instanceOfSocialPlatformDeletionSync.copyWith.fieldName(value)` for a single field.
class _$SocialPlatformDeletionSyncCWProxyImpl
    implements _$SocialPlatformDeletionSyncCWProxy {
  const _$SocialPlatformDeletionSyncCWProxyImpl(this._value);

  final SocialPlatformDeletionSync _value;

  @override
  SocialPlatformDeletionSync enabled(bool enabled) => call(enabled: enabled);

  @override
  SocialPlatformDeletionSync lastRunAt(DateTime? lastRunAt) =>
      call(lastRunAt: lastRunAt);

  @override
  SocialPlatformDeletionSync tombstonedLast30d(int? tombstonedLast30d) =>
      call(tombstonedLast30d: tombstonedLast30d);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformDeletionSync(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformDeletionSync(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPlatformDeletionSync call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? lastRunAt = const $CopyWithPlaceholder(),
    Object? tombstonedLast30d = const $CopyWithPlaceholder(),
  }) {
    return SocialPlatformDeletionSync(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      lastRunAt: lastRunAt == const $CopyWithPlaceholder()
          ? _value.lastRunAt
          // ignore: cast_nullable_to_non_nullable
          : lastRunAt as DateTime?,
      tombstonedLast30d: tombstonedLast30d == const $CopyWithPlaceholder()
          ? _value.tombstonedLast30d
          // ignore: cast_nullable_to_non_nullable
          : tombstonedLast30d as int?,
    );
  }
}

extension $SocialPlatformDeletionSyncCopyWith on SocialPlatformDeletionSync {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPlatformDeletionSync.copyWith(...)` or `instanceOfSocialPlatformDeletionSync.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPlatformDeletionSyncCWProxy get copyWith =>
      _$SocialPlatformDeletionSyncCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPlatformDeletionSync _$SocialPlatformDeletionSyncFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialPlatformDeletionSync', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['enabled']);
  final val = SocialPlatformDeletionSync(
    enabled: $checkedConvert('enabled', (v) => v as bool),
    lastRunAt: $checkedConvert(
      'lastRunAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    tombstonedLast30d: $checkedConvert(
      'tombstonedLast30d',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialPlatformDeletionSyncToJson(
  SocialPlatformDeletionSync instance,
) => <String, dynamic>{
  'enabled': instance.enabled,
  'lastRunAt': ?instance.lastRunAt?.toIso8601String(),
  'tombstonedLast30d': ?instance.tombstonedLast30d,
};
