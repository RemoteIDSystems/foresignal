// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'native_push_payload_apns_aps.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NativePushPayloadApnsApsCWProxy {
  NativePushPayloadApnsAps alert(NativePushPayloadApnsApsAlert alert);

  NativePushPayloadApnsAps sound(NativePushPayloadApnsApsSoundEnum? sound);

  NativePushPayloadApnsAps mutableContent(
    NativePushPayloadApnsApsMutableContentEnum? mutableContent,
  );

  NativePushPayloadApnsAps interruptionLevel(
    NativePushPayloadApnsApsInterruptionLevelEnum? interruptionLevel,
  );

  NativePushPayloadApnsAps threadId(String? threadId);

  NativePushPayloadApnsAps category(String? category);

  NativePushPayloadApnsAps badge(int? badge);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadApnsAps(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadApnsAps(...).copyWith(id: 12, name: "My name")
  /// ```
  NativePushPayloadApnsAps call({
    NativePushPayloadApnsApsAlert alert,
    NativePushPayloadApnsApsSoundEnum? sound,
    NativePushPayloadApnsApsMutableContentEnum? mutableContent,
    NativePushPayloadApnsApsInterruptionLevelEnum? interruptionLevel,
    String? threadId,
    String? category,
    int? badge,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNativePushPayloadApnsAps.copyWith(...)` or call `instanceOfNativePushPayloadApnsAps.copyWith.fieldName(value)` for a single field.
class _$NativePushPayloadApnsApsCWProxyImpl
    implements _$NativePushPayloadApnsApsCWProxy {
  const _$NativePushPayloadApnsApsCWProxyImpl(this._value);

  final NativePushPayloadApnsAps _value;

  @override
  NativePushPayloadApnsAps alert(NativePushPayloadApnsApsAlert alert) =>
      call(alert: alert);

  @override
  NativePushPayloadApnsAps sound(NativePushPayloadApnsApsSoundEnum? sound) =>
      call(sound: sound);

  @override
  NativePushPayloadApnsAps mutableContent(
    NativePushPayloadApnsApsMutableContentEnum? mutableContent,
  ) => call(mutableContent: mutableContent);

  @override
  NativePushPayloadApnsAps interruptionLevel(
    NativePushPayloadApnsApsInterruptionLevelEnum? interruptionLevel,
  ) => call(interruptionLevel: interruptionLevel);

  @override
  NativePushPayloadApnsAps threadId(String? threadId) =>
      call(threadId: threadId);

  @override
  NativePushPayloadApnsAps category(String? category) =>
      call(category: category);

  @override
  NativePushPayloadApnsAps badge(int? badge) => call(badge: badge);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadApnsAps(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadApnsAps(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NativePushPayloadApnsAps call({
    Object? alert = const $CopyWithPlaceholder(),
    Object? sound = const $CopyWithPlaceholder(),
    Object? mutableContent = const $CopyWithPlaceholder(),
    Object? interruptionLevel = const $CopyWithPlaceholder(),
    Object? threadId = const $CopyWithPlaceholder(),
    Object? category = const $CopyWithPlaceholder(),
    Object? badge = const $CopyWithPlaceholder(),
  }) {
    return NativePushPayloadApnsAps(
      alert: alert == const $CopyWithPlaceholder() || alert == null
          ? _value.alert
          // ignore: cast_nullable_to_non_nullable
          : alert as NativePushPayloadApnsApsAlert,
      sound: sound == const $CopyWithPlaceholder()
          ? _value.sound
          // ignore: cast_nullable_to_non_nullable
          : sound as NativePushPayloadApnsApsSoundEnum?,
      mutableContent: mutableContent == const $CopyWithPlaceholder()
          ? _value.mutableContent
          // ignore: cast_nullable_to_non_nullable
          : mutableContent as NativePushPayloadApnsApsMutableContentEnum?,
      interruptionLevel: interruptionLevel == const $CopyWithPlaceholder()
          ? _value.interruptionLevel
          // ignore: cast_nullable_to_non_nullable
          : interruptionLevel as NativePushPayloadApnsApsInterruptionLevelEnum?,
      threadId: threadId == const $CopyWithPlaceholder()
          ? _value.threadId
          // ignore: cast_nullable_to_non_nullable
          : threadId as String?,
      category: category == const $CopyWithPlaceholder()
          ? _value.category
          // ignore: cast_nullable_to_non_nullable
          : category as String?,
      badge: badge == const $CopyWithPlaceholder()
          ? _value.badge
          // ignore: cast_nullable_to_non_nullable
          : badge as int?,
    );
  }
}

extension $NativePushPayloadApnsApsCopyWith on NativePushPayloadApnsAps {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNativePushPayloadApnsAps.copyWith(...)` or `instanceOfNativePushPayloadApnsAps.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NativePushPayloadApnsApsCWProxy get copyWith =>
      _$NativePushPayloadApnsApsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NativePushPayloadApnsAps _$NativePushPayloadApnsApsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate(
  'NativePushPayloadApnsAps',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['alert']);
    final val = NativePushPayloadApnsAps(
      alert: $checkedConvert(
        'alert',
        (v) =>
            NativePushPayloadApnsApsAlert.fromJson(v as Map<String, dynamic>),
      ),
      sound: $checkedConvert(
        'sound',
        (v) =>
            $enumDecodeNullable(_$NativePushPayloadApnsApsSoundEnumEnumMap, v),
      ),
      mutableContent: $checkedConvert(
        'mutable-content',
        (v) => $enumDecodeNullable(
          _$NativePushPayloadApnsApsMutableContentEnumEnumMap,
          v,
        ),
      ),
      interruptionLevel: $checkedConvert(
        'interruption-level',
        (v) => $enumDecodeNullable(
          _$NativePushPayloadApnsApsInterruptionLevelEnumEnumMap,
          v,
        ),
      ),
      threadId: $checkedConvert('thread-id', (v) => v as String?),
      category: $checkedConvert('category', (v) => v as String?),
      badge: $checkedConvert('badge', (v) => (v as num?)?.toInt()),
    );
    return val;
  },
  fieldKeyMap: const {
    'mutableContent': 'mutable-content',
    'interruptionLevel': 'interruption-level',
    'threadId': 'thread-id',
  },
);

Map<String, dynamic> _$NativePushPayloadApnsApsToJson(
  NativePushPayloadApnsAps instance,
) => <String, dynamic>{
  'alert': instance.alert.toJson(),
  'sound': ?_$NativePushPayloadApnsApsSoundEnumEnumMap[instance.sound],
  'mutable-content':
      ?_$NativePushPayloadApnsApsMutableContentEnumEnumMap[instance
          .mutableContent],
  'interruption-level':
      ?_$NativePushPayloadApnsApsInterruptionLevelEnumEnumMap[instance
          .interruptionLevel],
  'thread-id': ?instance.threadId,
  'category': ?instance.category,
  'badge': ?instance.badge,
};

const _$NativePushPayloadApnsApsSoundEnumEnumMap = {
  NativePushPayloadApnsApsSoundEnum.default_: 'default',
};

const _$NativePushPayloadApnsApsMutableContentEnumEnumMap = {
  NativePushPayloadApnsApsMutableContentEnum.number1: 1,
};

const _$NativePushPayloadApnsApsInterruptionLevelEnumEnumMap = {
  NativePushPayloadApnsApsInterruptionLevelEnum.passive: 'passive',
  NativePushPayloadApnsApsInterruptionLevelEnum.active: 'active',
  NativePushPayloadApnsApsInterruptionLevelEnum.timeSensitive: 'time-sensitive',
};
