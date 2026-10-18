// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_preference.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationPreferenceCWProxy {
  NotificationPreference id(String id);

  NotificationPreference principalId(String? principalId);

  NotificationPreference eventTypes(
    List<NotificationPreferenceEventTypesEnum> eventTypes,
  );

  NotificationPreference channel(NotificationPreferenceChannel channel);

  NotificationPreference quietHours(QuietHours? quietHours);

  NotificationPreference enabled(bool enabled);

  NotificationPreference createdAt(DateTime? createdAt);

  NotificationPreference updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationPreference(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationPreference(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationPreference call({
    String id,
    String? principalId,
    List<NotificationPreferenceEventTypesEnum> eventTypes,
    NotificationPreferenceChannel channel,
    QuietHours? quietHours,
    bool enabled,
    DateTime? createdAt,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationPreference.copyWith(...)` or call `instanceOfNotificationPreference.copyWith.fieldName(value)` for a single field.
class _$NotificationPreferenceCWProxyImpl
    implements _$NotificationPreferenceCWProxy {
  const _$NotificationPreferenceCWProxyImpl(this._value);

  final NotificationPreference _value;

  @override
  NotificationPreference id(String id) => call(id: id);

  @override
  NotificationPreference principalId(String? principalId) =>
      call(principalId: principalId);

  @override
  NotificationPreference eventTypes(
    List<NotificationPreferenceEventTypesEnum> eventTypes,
  ) => call(eventTypes: eventTypes);

  @override
  NotificationPreference channel(NotificationPreferenceChannel channel) =>
      call(channel: channel);

  @override
  NotificationPreference quietHours(QuietHours? quietHours) =>
      call(quietHours: quietHours);

  @override
  NotificationPreference enabled(bool enabled) => call(enabled: enabled);

  @override
  NotificationPreference createdAt(DateTime? createdAt) =>
      call(createdAt: createdAt);

  @override
  NotificationPreference updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationPreference(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationPreference(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationPreference call({
    Object? id = const $CopyWithPlaceholder(),
    Object? principalId = const $CopyWithPlaceholder(),
    Object? eventTypes = const $CopyWithPlaceholder(),
    Object? channel = const $CopyWithPlaceholder(),
    Object? quietHours = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return NotificationPreference(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      principalId: principalId == const $CopyWithPlaceholder()
          ? _value.principalId
          // ignore: cast_nullable_to_non_nullable
          : principalId as String?,
      eventTypes:
          eventTypes == const $CopyWithPlaceholder() || eventTypes == null
          ? _value.eventTypes
          // ignore: cast_nullable_to_non_nullable
          : eventTypes as List<NotificationPreferenceEventTypesEnum>,
      channel: channel == const $CopyWithPlaceholder() || channel == null
          ? _value.channel
          // ignore: cast_nullable_to_non_nullable
          : channel as NotificationPreferenceChannel,
      quietHours: quietHours == const $CopyWithPlaceholder()
          ? _value.quietHours
          // ignore: cast_nullable_to_non_nullable
          : quietHours as QuietHours?,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      createdAt: createdAt == const $CopyWithPlaceholder()
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $NotificationPreferenceCopyWith on NotificationPreference {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationPreference.copyWith(...)` or `instanceOfNotificationPreference.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationPreferenceCWProxy get copyWith =>
      _$NotificationPreferenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationPreference _$NotificationPreferenceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationPreference', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'eventTypes', 'channel', 'enabled'],
  );
  final val = NotificationPreference(
    id: $checkedConvert('id', (v) => v as String),
    principalId: $checkedConvert('principalId', (v) => v as String?),
    eventTypes: $checkedConvert(
      'eventTypes',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                $enumDecode(_$NotificationPreferenceEventTypesEnumEnumMap, e),
          )
          .toList(),
    ),
    channel: $checkedConvert(
      'channel',
      (v) => NotificationPreferenceChannel.fromJson(v as Map<String, dynamic>),
    ),
    quietHours: $checkedConvert(
      'quietHours',
      (v) => v == null ? null : QuietHours.fromJson(v as Map<String, dynamic>),
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool),
    createdAt: $checkedConvert(
      'createdAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$NotificationPreferenceToJson(
  NotificationPreference instance,
) => <String, dynamic>{
  'id': instance.id,
  'principalId': ?instance.principalId,
  'eventTypes': instance.eventTypes
      .map((e) => _$NotificationPreferenceEventTypesEnumEnumMap[e]!)
      .toList(),
  'channel': instance.channel.toJson(),
  'quietHours': ?instance.quietHours?.toJson(),
  'enabled': instance.enabled,
  'createdAt': ?instance.createdAt?.toIso8601String(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};

const _$NotificationPreferenceEventTypesEnumEnumMap = {
  NotificationPreferenceEventTypesEnum.mention: 'mention',
  NotificationPreferenceEventTypesEnum.share: 'share',
  NotificationPreferenceEventTypesEnum.report: 'report',
  NotificationPreferenceEventTypesEnum.chronicle: 'chronicle',
  NotificationPreferenceEventTypesEnum.assessment: 'assessment',
  NotificationPreferenceEventTypesEnum.commentReply: 'comment-reply',
};
