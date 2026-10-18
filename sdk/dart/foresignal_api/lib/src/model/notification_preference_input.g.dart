// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_preference_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationPreferenceInputCWProxy {
  NotificationPreferenceInput id(String? id);

  NotificationPreferenceInput eventTypes(
    List<NotificationPreferenceInputEventTypesEnum> eventTypes,
  );

  NotificationPreferenceInput channel(
    NotificationPreferenceInputChannel channel,
  );

  NotificationPreferenceInput quietHours(QuietHours? quietHours);

  NotificationPreferenceInput enabled(bool? enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationPreferenceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationPreferenceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationPreferenceInput call({
    String? id,
    List<NotificationPreferenceInputEventTypesEnum> eventTypes,
    NotificationPreferenceInputChannel channel,
    QuietHours? quietHours,
    bool? enabled,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationPreferenceInput.copyWith(...)` or call `instanceOfNotificationPreferenceInput.copyWith.fieldName(value)` for a single field.
class _$NotificationPreferenceInputCWProxyImpl
    implements _$NotificationPreferenceInputCWProxy {
  const _$NotificationPreferenceInputCWProxyImpl(this._value);

  final NotificationPreferenceInput _value;

  @override
  NotificationPreferenceInput id(String? id) => call(id: id);

  @override
  NotificationPreferenceInput eventTypes(
    List<NotificationPreferenceInputEventTypesEnum> eventTypes,
  ) => call(eventTypes: eventTypes);

  @override
  NotificationPreferenceInput channel(
    NotificationPreferenceInputChannel channel,
  ) => call(channel: channel);

  @override
  NotificationPreferenceInput quietHours(QuietHours? quietHours) =>
      call(quietHours: quietHours);

  @override
  NotificationPreferenceInput enabled(bool? enabled) => call(enabled: enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationPreferenceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationPreferenceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationPreferenceInput call({
    Object? id = const $CopyWithPlaceholder(),
    Object? eventTypes = const $CopyWithPlaceholder(),
    Object? channel = const $CopyWithPlaceholder(),
    Object? quietHours = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
  }) {
    return NotificationPreferenceInput(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      eventTypes:
          eventTypes == const $CopyWithPlaceholder() || eventTypes == null
          ? _value.eventTypes
          // ignore: cast_nullable_to_non_nullable
          : eventTypes as List<NotificationPreferenceInputEventTypesEnum>,
      channel: channel == const $CopyWithPlaceholder() || channel == null
          ? _value.channel
          // ignore: cast_nullable_to_non_nullable
          : channel as NotificationPreferenceInputChannel,
      quietHours: quietHours == const $CopyWithPlaceholder()
          ? _value.quietHours
          // ignore: cast_nullable_to_non_nullable
          : quietHours as QuietHours?,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
    );
  }
}

extension $NotificationPreferenceInputCopyWith on NotificationPreferenceInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationPreferenceInput.copyWith(...)` or `instanceOfNotificationPreferenceInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationPreferenceInputCWProxy get copyWith =>
      _$NotificationPreferenceInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationPreferenceInput _$NotificationPreferenceInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationPreferenceInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['eventTypes', 'channel']);
  final val = NotificationPreferenceInput(
    id: $checkedConvert('id', (v) => v as String?),
    eventTypes: $checkedConvert(
      'eventTypes',
      (v) => (v as List<dynamic>)
          .map(
            (e) => $enumDecode(
              _$NotificationPreferenceInputEventTypesEnumEnumMap,
              e,
            ),
          )
          .toList(),
    ),
    channel: $checkedConvert(
      'channel',
      (v) => NotificationPreferenceInputChannel.fromJson(
        v as Map<String, dynamic>,
      ),
    ),
    quietHours: $checkedConvert(
      'quietHours',
      (v) => v == null ? null : QuietHours.fromJson(v as Map<String, dynamic>),
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
  );
  return val;
});

Map<String, dynamic> _$NotificationPreferenceInputToJson(
  NotificationPreferenceInput instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'eventTypes': instance.eventTypes
      .map((e) => _$NotificationPreferenceInputEventTypesEnumEnumMap[e]!)
      .toList(),
  'channel': instance.channel.toJson(),
  'quietHours': ?instance.quietHours?.toJson(),
  'enabled': ?instance.enabled,
};

const _$NotificationPreferenceInputEventTypesEnumEnumMap = {
  NotificationPreferenceInputEventTypesEnum.mention: 'mention',
  NotificationPreferenceInputEventTypesEnum.share: 'share',
  NotificationPreferenceInputEventTypesEnum.report: 'report',
  NotificationPreferenceInputEventTypesEnum.chronicle: 'chronicle',
  NotificationPreferenceInputEventTypesEnum.assessment: 'assessment',
  NotificationPreferenceInputEventTypesEnum.commentReply: 'comment-reply',
};
